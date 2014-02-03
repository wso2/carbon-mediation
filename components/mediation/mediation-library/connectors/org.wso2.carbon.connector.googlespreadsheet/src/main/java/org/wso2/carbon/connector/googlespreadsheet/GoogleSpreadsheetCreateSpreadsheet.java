package org.wso2.carbon.connector.googlespreadsheet;

import java.io.IOException;
import java.net.URL;

import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.PlainTextConstruct;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.ServiceException;
import com.google.gdata.data.docs.*;

public class GoogleSpreadsheetCreateSpreadsheet extends AbstractConnector {

    private static final int DEFAULT_ROW_COUNT = 100;
    private static final int DEFAULT_COLUMN_COUNT = 20;
    private static final int DEFAULT_WORKSHEET_COUNT = 3;
    public static final String SPREADSHEET_NAME = "spreadsheetName";
    public static final String WORKSHEET_COUNT = "worksheetCount";
    private int rowCount = DEFAULT_ROW_COUNT;
    private int columnCount = DEFAULT_COLUMN_COUNT;
    private int worksheetCountInt = DEFAULT_WORKSHEET_COUNT;
    private static Log log = LogFactory
            .getLog(GoogleSpreadsheetCreateSpreadsheet.class);

    public void connect(MessageContext messageContext) throws ConnectException {

        try {

            String spreadsheetName = GoogleSpreadsheetUtils
                    .lookupFunctionParam(messageContext, SPREADSHEET_NAME);
            String worksheetCount = GoogleSpreadsheetUtils
                    .lookupFunctionParam(messageContext, WORKSHEET_COUNT);

            if (spreadsheetName == null
                    || "".equals(spreadsheetName.trim())) {
                log.error("Please make sure you have given a name for the new spreadsheet");
                ConnectException connectException = new ConnectException("Please make sure you have given a name for the new spreadsheet");
                GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, connectException);
                return;
            }

            try {
                if (worksheetCount != null) {
                    worksheetCountInt = Integer.parseInt(worksheetCount);
                }

            } catch (NumberFormatException ex) {
                log.error("Please enter a valid number for worksheetCount");
                GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, ex);
                return;
            }

            SpreadsheetService ssService = new GoogleSpreadsheetClientLoader(
                    messageContext).loadSpreadsheetService();


            // Create an empty spreadsheet
            DocumentListEntry createdEntry = createNewDocument(spreadsheetName, messageContext);

            if (createdEntry != null) {
                if (log.isDebugEnabled()) {
                    log.info("Spreadsheet now online @ :" + createdEntry.getHtmlLink().getHref());
                }


                GoogleSpreadsheet gss = new GoogleSpreadsheet(ssService);

                com.google.gdata.data.spreadsheet.SpreadsheetEntry ssEntry = gss
                        .getSpreadSheetsByTitle(spreadsheetName);

                GoogleSpreadsheetWorksheet gssWorksheet = new GoogleSpreadsheetWorksheet(
                        ssService, ssEntry.getWorksheetFeedUrl());
                for (int i = 2; i <= worksheetCountInt; i++) {
                    gssWorksheet.createWorksheet("Sheet" + i, rowCount, columnCount);
                }

                if(messageContext.getEnvelope().getBody().getFirstElement() != null) {
                    messageContext.getEnvelope().getBody().getFirstElement().detach();
                }

                OMFactory factory = OMAbstractFactory.getOMFactory();
                OMNamespace ns = factory.createOMNamespace("http://org.wso2.esbconnectors.googlespreadsheet", "ns");
                OMElement searchResult = factory.createOMElement("createSpreadsheetResult", ns);
                OMElement result = factory.createOMElement("result", ns);
                searchResult.addChild(result);
                result.setText("true");
                messageContext.getEnvelope().getBody().addChild(searchResult);
            }


        } catch (IOException te) {
            log.error("Failed to show status: " + te.getMessage(), te);
            GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, te);
        } catch (ServiceException te) {
            log.error("Failed to show status: " + te.getMessage(), te);
            GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, te);
        }
    }

    public DocumentListEntry createNewDocument(String title, MessageContext messageContext)
            throws IOException, ServiceException {
        DocumentListEntry newEntry = new SpreadsheetEntry();
        DocsService client = new DocsService("GoogleSpreadsheet-v3");
        client.setProtocolVersion(DocsService.Versions.V3);

        if (messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_USERNAME) != null && messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_PASSWORD) != null) {
            client.setUserCredentials(messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_USERNAME).toString(), messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_PASSWORD).toString());

        } else if (messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_KEY) != null && messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_SECRET) != null && messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN) != null && messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN_SECRET) != null) {
            OAuthParameters oAuthParameters = new OAuthParameters();
            oAuthParameters.setOAuthConsumerKey(messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_KEY).toString());
            oAuthParameters.setOAuthConsumerSecret(messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_CONSUMER_SECRET).toString());
            oAuthParameters.setOAuthToken(messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN).toString());
            oAuthParameters.setOAuthTokenSecret(messageContext.getProperty(GoogleSpreadsheetConstants.GOOGLE_SPREADSHEET_USER_ACCESS_TOKEN_SECRET).toString());
            OAuthSigner oAuthSigner = new OAuthHmacSha1Signer();
            try {
                client.setOAuthCredentials(oAuthParameters, oAuthSigner);
            } catch (OAuthException te) {
                log.error("Failed to show status: " + te.getMessage(), te);
                GoogleSpreadsheetUtils.storeErrorResponseStatus(messageContext, te);
                return null;
            }

        }
        client.useSsl();
        newEntry.setTitle(new PlainTextConstruct(title));
        return client.insert(new URL("http://docs.google.com/feeds/default/private/full"), newEntry);
    }


}

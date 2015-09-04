/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.FeedEP;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.filter.ListParseFilter;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.ParserOptions;
import org.apache.abdera.util.Constants;
import org.apache.abdera.util.filter.WhiteListParseFilter;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;

/**
 * ConsumeFeed uses to feeds from given Backend
 */

public class ConsumeFeed extends InboundRequestProcessorImpl {

    private static final Log log = LogFactory.getLog(ConsumeFeed.class.getName());
    private long scanInterval;
    private long lastRanTime;
    private String host;
    private String feedType;
    private Feed feed = null;
    private OMElement item = null;
    Date date;
    private java.util.Date lastUpdated;
    private Date newUpdated;
    RssInject rssInject;
    Document<Feed> doc;
    InputStream input;
    DateFormat format;
    Parser parser;
    ParserOptions opts;
    ListParseFilter filter;
    Entry entry = null;
    Factory factory;
    RegistryHandler registryHandler;
    String pathName;
    String dateFormat;

    public ConsumeFeed(RssInject rssInject, long scanInterval, String host, String feedType,
                       RegistryHandler registryHandler, String name, String dateFormat) {
        this.host = host;
        this.feedType = feedType;
        this.scanInterval = scanInterval;
        this.rssInject = rssInject;
        this.registryHandler = registryHandler;
        this.pathName = name;
        this.dateFormat = dateFormat;
    }

    //check time interval
    public void execute() {
        try {
            long currentTime = (new Date()).getTime();
            if (((lastRanTime + scanInterval) <= currentTime)) {
                lastRanTime = currentTime;
                log.debug("lastRanTime " + lastRanTime);
                consume();
            } else if (log.isDebugEnabled()) {
                log.debug("Skip cycle since concurrent rate is higher than the scan interval : Feed Inbound EP ");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    //consume feeds
    public void consume() throws ClassNotFoundException, IOException {
        format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        parser = Abdera.getNewParser();
        try {
            input = new URL(host).openStream();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        //set filter
        opts = parser.getDefaultParserOptions();
        filter = new WhiteListParseFilter();
        try {
            if (feedType.equalsIgnoreCase("RSS")) {
                filter.add(new QName("rss"));
                filter.add(new QName("channel"));
                filter.add(new QName("item"));
                filter.add(new QName("title"));
                filter.add(new QName("guid"));
                filter.add(new QName("description"));
                filter.add(new QName("pubDate"));
                filter.add(new QName("link"));
            } else if (feedType.equalsIgnoreCase("Atom")) {
                filter.add(Constants.FEED);
                filter.add(Constants.ENTRY);
                filter.add(Constants.TITLE);
                filter.add(Constants.ID);
                filter.add(Constants.CONTENT);
                filter.add(Constants.UPDATED);
                filter.add(Constants.LINK);
                filter.add(Constants.AUTHOR);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        opts.setParseFilter(filter);

        try {
            doc = parser.parse(input, "", opts);
            if (doc.getRoot() == null) {
                log.error("Please check host address or feed type");
                return;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
        //convert RSS feeds as Atom
        try {
            if (feedType.equalsIgnoreCase("RSS")) {
                factory = Abdera.getNewFactory();
                feed = factory.newFeed();

                item = (OMElement) doc.getRoot();


                Iterator values1 = item.getFirstElement().getChildrenWithName(new QName("item"));
                while (values1.hasNext()) {
                    entry = feed.insertEntry();
                    OMElement omElement = (OMElement) values1.next();

                    Iterator values2 = omElement.getChildrenWithName(new QName("title"));
                    OMElement Title = (OMElement) values2.next();
                    entry.setTitle(Title.getText());

                    Iterator values3 = omElement.getChildrenWithName(new QName("pubDate"));
                    OMElement Updated = (OMElement) values3.next();
                    try {
                        date = format.parse(Updated.getText());
                        entry.setUpdated(date);
                    } catch (ParseException e) {
                        if (dateFormat != null) {
                            format =
                                    new SimpleDateFormat(dateFormat, Locale.ENGLISH);
                            date = format.parse(Updated.getText());
                            entry.setUpdated(date);
                        } else {
                            log.error(e.getMessage(),e);
                            return;
                        }
                    }

                    Iterator values4 = omElement.getChildrenWithName(new QName("description"));
                    OMElement Content = (OMElement) values4.next();
                    entry.setContent(Content.getText());

                    Iterator values5 = omElement.getChildrenWithName(new QName("guid"));
                    OMElement guid1 = (OMElement) values5.next();
                    entry.setId(guid1.getText());

                    Iterator values6 = omElement.getChildrenWithName(new QName("link"));
                    OMElement link = (OMElement) values6.next();
                    entry.setBaseUri(link.getText());

                }
            } else if (feedType.equalsIgnoreCase("Atom")) {
                feed = doc.getRoot();
            }
            try {
                log.debug(lastUpdated + " : " + feed.getEntries().get(0).getUpdated());
                format = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
                newUpdated=feed.getEntries().get(0).getUpdated();
                if (registryHandler.readFromRegistry(pathName) != null) {
                    lastUpdated =
                            format.parse(registryHandler.readFromRegistry(pathName)
                                    .toString());
                } else {
                    registryHandler.writeToRegistry(pathName, newUpdated);
                }
                if (lastUpdated == null) {
                    rssInject.invoke(feed);
                    registryHandler.writeToRegistry(pathName, newUpdated);
                    log.debug("LastUpdated Date was Null");
                } else if (newUpdated.after(lastUpdated)) {
                    rssInject.invoke(feed);
                    registryHandler.writeToRegistry(pathName, newUpdated);
                    log.debug("New Entry was Added");
                } else {
                    log.debug("there Is No New Feed");
                    return;
                }

            } catch (Exception e) {
                log.error("Error in lastUpdate time checking " + e.getMessage());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        input.close();
    }

    public void init() {
    }
}

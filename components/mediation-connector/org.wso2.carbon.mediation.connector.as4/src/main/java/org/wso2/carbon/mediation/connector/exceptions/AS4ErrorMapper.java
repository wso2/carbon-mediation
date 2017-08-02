/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediation.connector.exceptions;

import org.wso2.carbon.mediation.connector.AS4Constants;
import org.wso2.carbon.mediation.connector.message.beans.Description;
import org.wso2.carbon.mediation.connector.message.beans.Error;

/**
 * AS4 error mapper maps error codes with relevant severity and category.
 */
public class AS4ErrorMapper {

    private static final String ENG = "eng";

    /**
     * Helper method to set error details and description. When given the errorCode, error and errorDescription,
     * This function will set correct severity and categories, and also will create Description object and set it to
     * error.
     *
     * @param errorCode {@link ErrorCode} object
     * @param error {@link Error} object
     * @param errorDescription Error description
     */
    public static void setErrorDetailsAndDesc(ErrorCode errorCode, Error error, String errorDescription) {

        Description description = new Description();
        description.setLang(ENG);
        description.setValue(errorDescription);
        error.setDescription(description);
        error.setErrorCode(errorCode.getErrorCodeString());
        error.setOrigin(AS4Constants.EBMS);

        switch (errorCode) {
            case EBMS0001:
                error.setCategory(Category.Content.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0002:
                error.setCategory(Category.Content.name());
                error.setSeverity(Severity.Warning.name());
                break;
            case EBMS0003:
                error.setCategory(Category.Content.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0004:
                error.setCategory(Category.Content.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0005:
                error.setCategory(Category.Communication.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0007:
                error.setCategory(Category.Unpackaging.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0008:
                error.setCategory(Category.Unpackaging.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0009:
                error.setCategory(Category.Unpackaging.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0010:
                error.setCategory(Category.Processing.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0011:
                error.setCategory(Category.Content.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0301:
                error.setCategory(Category.Communication.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0302:
                error.setCategory(Category.Communication.name());
                error.setSeverity(Severity.Failure.name());
                break;
            case EBMS0303:
                error.setCategory(Category.Communication.name());
                error.setSeverity(Severity.Failure.name());
                break;
        }
    }

    /**
     * Applicable Severities
     */
    enum Severity {
        Failure,
        Warning
    }

    /**
     * Applicable Categories
     */
    enum Category {
        Content,
        Communication,
        Unpackaging,
        Processing
    }

    /**
     * Error codes
     */
    public enum ErrorCode {
        //ValueNotRecognised
        EBMS0001("EBMS:0001"),
        //FeatureNotSupported
        EBMS0002("EBMS:0002"),
        //ValueInconsistent
        EBMS0003("EBMS:0003"),
        //Other
        EBMS0004("EBMS:0004"),
        //ConnectionFailure
        EBMS0005("EBMS:0005"),
        //MimeInconsistency
        EBMS0007("EBMS:0007"),
        //FeatureNotSupported
        EBMS0008("EBMS:0008"),
        //InvalidHeader
        EBMS0009("EBMS:0009"),
        //ProcessingModeMismatch
        EBMS0010("EBMS:0010"),
        //ExternalPayloadError
        EBMS0011("EBMS:0011"),
        //MissingReceipt
        EBMS0301("EBMS:0301"),
        //InvalidReceipt
        EBMS0302("EBMS:0302"),
        //DecompressionFailure
        EBMS0303("EBMS:0303");

        private String errorCode;

        ErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorCodeString() {
            return this.errorCode;
        }
    }
}

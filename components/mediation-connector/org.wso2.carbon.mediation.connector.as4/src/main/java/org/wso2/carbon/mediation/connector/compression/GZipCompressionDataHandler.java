/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediation.connector.compression;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This is the data handler which can compress and uncompress the data provided to it.
 */
public class GZipCompressionDataHandler extends DataHandler {

    private String contentType;

    // default contentType
    public static String GZIP_COMPRESSION = "application/gzip";

    /**
     * Constructor which takes a datasource.
     *
     * @param ds {@link DataSource} object
     */
    public GZipCompressionDataHandler(DataSource ds) {
        super(ds);
        this.contentType = GZIP_COMPRESSION;
    }

    /**
     * Constructor which takes data source and content type.
     *
     * @param ds {@link DataSource} object
     * @param contentType content type
     */
    public GZipCompressionDataHandler(DataSource ds, String contentType) {
        super(ds);
        this.contentType = contentType;
    }

    /**
     * Overridden method to get input stream, this method will differentiate returning input stream depending on the
     * content type of the data handler.
     *
     * @return inputStream
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {

        if (GZIP_COMPRESSION.equals(contentType)) {
            return new GZipCompressInputStream(super.getInputStream());
        } else {
            return new GZIPInputStream(super.getInputStream());
        }
    }

    /**
     * Overridden method to write compressed data or uncompressed data depending on the content type.
     *
     * @param out {@link OutputStream} object
     * @throws IOException
     */
    public void writeTo(OutputStream out) throws IOException {

        if (GZIP_COMPRESSION.equals(contentType)) {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out);
            super.writeTo(gzipOutputStream);
            gzipOutputStream.finish();
        } else {
            GZIPInputStream gzipInputStream = new GZIPInputStream(super.getInputStream());
            byte[] buffer = new byte[256];
            for (int length = 0; (length = gzipInputStream.read(buffer)) != -1; ) {
                out.write(buffer, 0, length);
            }
        }
    }

    /**
     * Overridden method to return content type.
     *
     * @return contentType
     */
    public String getContentType() {
        return this.contentType;
    }

}

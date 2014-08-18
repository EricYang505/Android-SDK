/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.accela.mobile.http.mime;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 *  Description:
 * AccelaMultipartFormEntity generates multiple part form entity for request body.
 * 
 *  Notes:
 * 
 *  Revision History
 * 
 * 	@since 4.0
 * 
 * </pre>
 */

public class AccelaMultipartFormEntity implements HttpEntity {
	public static final String MULTIPART_File_KEY = "uploadedFile";
	public static final String MULTIPART_SEPARATOR_LINE = "---------------------------7de1a0c22082";
    private final AbstractMultipartForm multipart;
    private final long contentLength;

    AccelaMultipartFormEntity(
            final AbstractMultipartForm multipart,
            final String contentType,
            final long contentLength) {
        super();
        this.multipart = multipart;
        this.contentLength = contentLength;
    }

    AbstractMultipartForm getMultipart() {
        return this.multipart;
    }

    @Override
    public boolean isRepeatable() {
    	return false;
    }

    @Override
    public boolean isChunked() {
    	return false;
    }

    @Override
    public boolean isStreaming() {
    	return false;
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public Header getContentType() {
    	return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + MULTIPART_SEPARATOR_LINE);
    }

    @Override
    public Header getContentEncoding() {
        return null;
    }

    @Override
    public void consumeContent()
        throws IOException, UnsupportedOperationException{
        if (isStreaming()) {
            throw new UnsupportedOperationException(
                    "Streaming entity does not implement #consumeContent()");
        }
    }

    @Override
    public InputStream getContent() throws IOException {
        throw new UnsupportedOperationException(
                    "Multipart form entity does not implement #getContent()");
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        this.multipart.writeTo(outstream);
    }

}

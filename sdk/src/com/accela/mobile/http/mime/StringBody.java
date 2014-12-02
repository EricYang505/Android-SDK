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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/*
 *  Description:
 *  Text body part backed by a byte array.
 *  
 *  Notes:
 * 
 *  Revision History
 * 
 * 	@since 4.0
 * 
 * </pre>
 */

public class StringBody extends AbstractContentBody {

    private final byte[] content;
    public static final Charset ASCII = Charset.forName("US-ASCII");

    public StringBody(final String text, final ContentType contentType) {
        super(contentType);
        ContentType.notNull(text, "Text");
        final Charset charset = contentType.getCharset();
        this.content = text.getBytes(charset != null ? charset : ASCII);
    }

    public Reader getReader() {
        final Charset charset = getContentType().getCharset();
        return new InputStreamReader(
                new ByteArrayInputStream(this.content),
                charset != null ? charset : ASCII);
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        ContentType.notNull(out, "Output stream");
        final InputStream in = new ByteArrayInputStream(this.content);
        final byte[] tmp = new byte[4096];
        int l;
        while ((l = in.read(tmp)) != -1) {
            out.write(tmp, 0, l);
        }
        out.flush();
    }

    @Override
    public String getTransferEncoding() {
        return MIME.ENC_8BIT;
    }

    @Override
    public long getContentLength() {
        return this.content.length;
    }

    @Override
    public String getFilename() {
        return null;
    }

}

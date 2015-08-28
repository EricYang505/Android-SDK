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

package com.accela.mobile.http.volley.Legacy.mime;

import com.accela.mobile.http.volley.Legacy.HttpEntity;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/*
 *  Description:
 * AccelaMultipartEntityBuilder generates the MIME multipart encoded content bodies.
 * 
 *  Notes:
 * 
 *  Revision History
 * 
 * 	@since 4.0
 * 
 * </pre>
 */
public class AccelaMultipartEntityBuilder {

    /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();

    private final static String DEFAULT_SUBTYPE = "form-data";

    private String subType = DEFAULT_SUBTYPE;
    private HttpMultipartMode mode = HttpMultipartMode.STRICT;
    private String boundary = null;
    private Charset charset = null;
    private List<FormBodyPart> bodyParts = null;
    private int mLenth;

    public static AccelaMultipartEntityBuilder create() {
        return new AccelaMultipartEntityBuilder();
    }

    AccelaMultipartEntityBuilder() {
        super();
    }

    public AccelaMultipartEntityBuilder setMode(final HttpMultipartMode mode) {
        this.mode = mode;
        return this;
    }

    public AccelaMultipartEntityBuilder setLaxMode() {
        this.mode = HttpMultipartMode.BROWSER_COMPATIBLE;
        return this;
    }

    public AccelaMultipartEntityBuilder setStrictMode() {
        this.mode = HttpMultipartMode.STRICT;
        return this;
    }

    public AccelaMultipartEntityBuilder setBoundary(final String boundary) {
        this.boundary = boundary;
        return this;
    }

    public AccelaMultipartEntityBuilder setMimeSubtype(final String subType) {
        ContentType.notBlank(subType, "MIME subtype");
        this.subType = subType;
        return this;
    }

    public AccelaMultipartEntityBuilder setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public AccelaMultipartEntityBuilder addPart(final FormBodyPart bodyPart) {
        if (bodyPart == null) {
            return this;
        }
        if (this.bodyParts == null) {
            this.bodyParts = new ArrayList<FormBodyPart>();
        }
        this.bodyParts.add(bodyPart);
        return this;
    }

    public AccelaMultipartEntityBuilder addPart(final String name, final ContentBody contentBody) {
        ContentType.notNull(name, "Name");
        ContentType.notNull(contentBody, "Content body");
        return addPart(new FormBodyPart(name, contentBody));
    }

    public AccelaMultipartEntityBuilder addTextBody(
            final String name, final String text, final ContentType contentType) {
    	this.mLenth += text.getBytes().length;
        return addPart(name, new StringBody(text, contentType));
    }

    public AccelaMultipartEntityBuilder addTextBody(
            final String name, final String text) {
    	this.mLenth += text.getBytes().length;
        return addTextBody(name, text, ContentType.DEFAULT_TEXT);
    }

    public AccelaMultipartEntityBuilder addBinaryBody(
            final String name, final byte[] b, final ContentType contentType, final String filename) {
    	this.mLenth += b.length;
        return addPart(name, new ByteArrayBody(b, contentType, filename));
    }

    public AccelaMultipartEntityBuilder addBinaryBody(
            final String name, final byte[] b) {
    	this.mLenth += b.length;
        return addBinaryBody(name, b, ContentType.DEFAULT_BINARY, null);
    }

    public AccelaMultipartEntityBuilder addBinaryBody(
            final String name, final File file, final ContentType contentType, final String filename) {
    	this.mLenth += file.length();
        return addPart(name, new FileBody(file, contentType, filename));
    }

    public AccelaMultipartEntityBuilder addBinaryBody(
            final String name, final File file) {
    	this.mLenth += file.length();
        return addBinaryBody(name, file, ContentType.DEFAULT_BINARY, file != null ? file.getName() : null);
    }

    public AccelaMultipartEntityBuilder addBinaryBody(
            final String name, final InputStream stream, final ContentType contentType,
            final String filename) {
        return addPart(name, new InputStreamBody(stream, contentType, filename));
    }

    public AccelaMultipartEntityBuilder addBinaryBody(final String name, final InputStream stream) {
        return addBinaryBody(name, stream, ContentType.DEFAULT_BINARY, null);
    }

    private String generateContentType(
            final String boundary,
            final String subType,
            final Charset charset) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("multipart/").append(subType).append("; boundary=");
        buffer.append(boundary);
        if (charset != null) {
            buffer.append("; charset=");
            buffer.append(charset.name());
        }
        return buffer.toString();
    }

    private String generateBoundary() {
        final StringBuilder buffer = new StringBuilder();
        final Random rand = new Random();
        final int count = rand.nextInt(11) + 30; // a random size from 30 to 40
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    AccelaMultipartFormEntity buildEntity() {
        final String st = subType != null ? subType : DEFAULT_SUBTYPE;
        final Charset cs = charset;
        final String b = boundary != null ? boundary : generateBoundary();
        final List<FormBodyPart> bps = bodyParts != null ? new ArrayList<FormBodyPart>(bodyParts) :
                Collections.<FormBodyPart>emptyList();
        final HttpMultipartMode m = mode != null ? mode : HttpMultipartMode.STRICT;
        AbstractMultipartForm form = null;
        switch (m) {
            case BROWSER_COMPATIBLE:
                form = new HttpBrowserCompatibleMultipart(st, cs, b, bps);
                break;
            default:
                form = new HttpBrowserCompatibleMultipart(st, cs, b, bps);

        }
        return new AccelaMultipartFormEntity(form, generateContentType(b, st, cs), form.getTotalLength());
    }

    public HttpEntity build() {
        return buildEntity();
    }

}

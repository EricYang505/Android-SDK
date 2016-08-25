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

package com.accela.mobile.http.volley.Legacy;

import java.io.IOException;
import java.io.InputStream;


/**
 * Static helpers for dealing with {@link HttpEntity}s.
 *
 * @since 4.0
 */
public final class EntityUtils {

    private EntityUtils() {
    }

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists,
     * is closed. The process is done, <i>quietly</i> , without throwing any IOException.
     *
     * @param entity the entity to consume.
     *
     *
     * @since 4.2
     */
    public static void consumeQuietly(final HttpEntity entity) {
        try {
          consume(entity);
        } catch (final IOException ignore) {
        }
    }

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists,
     * is closed.
     *
     * @param entity the entity to consume.
     * @throws java.io.IOException if an error occurs reading the input stream
     *
     * @since 4.1
     */
    public static void consume(final HttpEntity entity) throws IOException {
        if (entity == null) {
            return;
        }
        if (entity.isStreaming()) {
            final InputStream instream = entity.getContent();
            if (instream != null) {
                instream.close();
            }
        }
    }

    /**
     * Updates an entity in a response by first consuming an existing entity, then setting the new one.
     *
     * @param response the response with an entity to update; must not be null.
     * @param entity the entity to set in the response.
     * @throws java.io.IOException if an error occurs while reading the input stream on the existing
     * entity.
     * @throws IllegalArgumentException if response is null.
     *
     * @since 4.3
     */
    public static void updateEntity(
            final HttpResponse response, final HttpEntity entity) throws IOException {
        Args.notNull(response, "Response");
        consume(response.getEntity());
        response.setEntity(entity);
    }

    /**
     * Read the contents of an entity and return it as a byte array.
     *
     * @param entity the entity to read from=
     * @return byte array containing the entity content. May be null if
     *   {@link HttpEntity#getContent()} is null.
     * @throws java.io.IOException if an error occurs reading the input stream
     * @throws IllegalArgumentException if entity is null or if content length &gt; Integer.MAX_VALUE
     */
    public static byte[] toByteArray(final HttpEntity entity) throws IOException {
        Args.notNull(entity, "Entity");
        final InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }
        try {
            Args.check(entity.getContentLength() <= Integer.MAX_VALUE,
                    "HTTP entity too large to be buffered in memory");
            int i = (int)entity.getContentLength();
            if (i < 0) {
                i = 4096;
            }
            final ByteArrayBuffer buffer = new ByteArrayBuffer(i);
            final byte[] tmp = new byte[4096];
            int l;
            while((l = instream.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
            return buffer.toByteArray();
        } finally {
            instream.close();
        }
    }

    /**
     * Obtains character set of the entity, if known.
     *
     * @param entity must not be null
     * @return the character set, or null if not found
     * @throws ParseException if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null
     *
     * @deprecated (4.1.3) use ContentType#getOrDefault(HttpEntity)
     */
    @Deprecated
    public static String getContentCharSet(final HttpEntity entity) throws ParseException {
        Args.notNull(entity, "Entity");
        String charset = null;
        if (entity.getContentType() != null) {
            final HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                final NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }

    /**
     * Obtains MIME type of the entity, if known.
     *
     * @param entity must not be null
     * @return the character set, or null if not found
     * @throws ParseException if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null
     *
     * @since 4.1
     *
     * @deprecated (4.1.3) use ContentType#getOrDefault(HttpEntity)
     */
    @Deprecated
    public static String getContentMimeType(final HttpEntity entity) throws ParseException {
        Args.notNull(entity, "Entity");
        String mimeType = null;
        if (entity.getContentType() != null) {
            final HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                mimeType = values[0].getName();
            }
        }
        return mimeType;
    }

}

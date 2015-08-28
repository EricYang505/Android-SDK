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

import com.accela.mobile.http.volley.Legacy.BasicHeaderValueParser;
import com.accela.mobile.http.volley.Legacy.CharArrayBuffer;
import com.accela.mobile.http.volley.Legacy.Header;
import com.accela.mobile.http.volley.Legacy.HeaderElement;
import com.accela.mobile.http.volley.Legacy.HttpEntity;
import com.accela.mobile.http.volley.Legacy.NameValuePair;
import com.accela.mobile.http.volley.Legacy.ParseException;
import com.accela.mobile.http.volley.Legacy.ParserCursor;


import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;



/*
 *  Description:
 * Content type information consisting of a MIME type and an optional charset.
 * This class makes no attempts to verify validity of the MIME type.
 * The input parameters of the {@link #create(String, String)} method, however, may not
 * contain characters <">, <;>, <,> reserved by the HTTP specification.
 * 
 *  Notes:
 * 
 *  Revision History
 * 
 * 	@since 4.0
 * 
 * </pre>
 */

@Immutable
public final class ContentType implements Serializable {
    public final static String SEPARATORS = " ;,:@()<>\\\"/[]?={}\t";
    public final static String UNSAFE_CHARS = "\"\\";

    private static final long serialVersionUID = -7768694718232371896L;
    
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    // constants
    public static final ContentType APPLICATION_ATOM_XML = create(
            "application/atom+xml", ISO_8859_1);
    public static final ContentType APPLICATION_FORM_URLENCODED = create(
            "application/x-www-form-urlencoded", ISO_8859_1);
    public static final ContentType APPLICATION_JSON = create(
            "application/json", UTF_8);
    public static final ContentType APPLICATION_OCTET_STREAM = create(
            "application/octet-stream", (Charset) null);
    public static final ContentType APPLICATION_SVG_XML = create(
            "application/svg+xml", ISO_8859_1);
    public static final ContentType APPLICATION_XHTML_XML = create(
            "application/xhtml+xml", ISO_8859_1);
    public static final ContentType APPLICATION_XML = create(
            "application/xml", ISO_8859_1);
    public static final ContentType MULTIPART_FORM_DATA = create(
            "multipart/form-data", ISO_8859_1);
    public static final ContentType TEXT_HTML = create(
            "text/html", ISO_8859_1);
    public static final ContentType TEXT_PLAIN = create(
            "text/plain", ISO_8859_1);
    public static final ContentType TEXT_XML = create(
            "text/xml", ISO_8859_1);
    public static final ContentType WILDCARD = create(
            "*/*", (Charset) null);

    // defaults
    public static final ContentType DEFAULT_TEXT = TEXT_PLAIN;
    public static final ContentType DEFAULT_BINARY = APPLICATION_OCTET_STREAM;

    private final String mimeType;
    private final Charset charset;
    private final NameValuePair[] params;

    ContentType(
            final String mimeType,
            final Charset charset) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = null;
    }

    ContentType(
            final String mimeType,
            final NameValuePair[] params) throws UnsupportedCharsetException {
        this.mimeType = mimeType;
        this.params = params;
        final String s = getParameter("charset");
        this.charset = !isBlank(s) ? Charset.forName(s) : null;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Charset getCharset() {
        return this.charset;
    }
    
	private static void check(final boolean expression, final String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

	private <T extends CharSequence> T notEmpty(final T argument,
			final String name) {
		if (argument == null) {
			throw new IllegalArgumentException(name + " may not be null");
		}
		if (isEmpty(argument)) {
			throw new IllegalArgumentException(name + " may not be empty");
		}
		return argument;
	}
	
	public static <T> T notNull(final T argument, final String name) {
		if (argument == null) {
			throw new IllegalArgumentException(name + " may not be null");
		}
		return argument;
	}
	
	public static <T extends CharSequence> T notBlank(final T argument, final String name) {
		if (argument == null) {
			throw new IllegalArgumentException(name + " may not be null");
		}
		if (isBlank(argument)) {
			throw new IllegalArgumentException(name + " may not be blank");
		}
		return argument;
	}
	
	private boolean isEmpty(final CharSequence s) {
		if (s == null) {
			return true;
		}
		return s.length() == 0;
	}

	private static boolean isBlank(final CharSequence s) {
		if (s == null) {
			return true;
		}
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isWhitespace(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}
		
    public String getParameter(final String name) {
        this.notEmpty(name, "Parameter name");
        if (this.params == null) {
            return null;
        }
        for (final NameValuePair param: this.params) {
            if (param.getName().equalsIgnoreCase(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    protected int estimateNameValuePairLen(final NameValuePair nvp) {
        if (nvp == null)
            return 0;

        int result = nvp.getName().length(); // name
        final String value = nvp.getValue();
        if (value != null) {
            // assume quotes, but no escaped characters
            result += 3 + value.length(); // ="value"
        }
        return result;
    }

    protected int estimateParametersLen(final NameValuePair[] nvps) {
        if ((nvps == null) || (nvps.length < 1))
            return 0;

        int result = (nvps.length - 1) * 2; // "; " between the parameters
        for (int i = 0; i < nvps.length; i++) {
            result += estimateNameValuePairLen(nvps[i]);
        }

        return result;
    }

    protected boolean isSeparator(char ch) {
                return SEPARATORS.indexOf(ch) >= 0;
            }
    protected boolean isUnsafe(char ch) {
                return UNSAFE_CHARS.indexOf(ch) >= 0;
            }

    protected void doFormatValue(final CharArrayBuffer buffer,
                                 final String value,
                                 boolean quote) {

        if (!quote) {
            for (int i = 0; (i < value.length()) && !quote; i++) {
                quote = isSeparator(value.charAt(i));
            }
        }

        if (quote) {
            buffer.append('"');
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (isUnsafe(ch)) {
                buffer.append('\\');
            }
            buffer.append(ch);
        }
        if (quote) {
            buffer.append('"');
        }
    }

    public CharArrayBuffer formatNameValuePair(CharArrayBuffer buffer,
                                                                                                      final NameValuePair nvp,
                                                                                                      final boolean quote) {
                if (nvp == null) {
                        throw new IllegalArgumentException
                            ("NameValuePair must not be null.");
                    }

                int len = estimateNameValuePairLen(nvp);
                if (buffer == null) {
                        buffer = new CharArrayBuffer(len);
                    } else {
                        buffer.ensureCapacity(len);
                    }

                buffer.append(nvp.getName());
                final String value = nvp.getValue();
                if (value != null) {
                        buffer.append('=');
                        doFormatValue(buffer, value, quote);
                    }

                return buffer;
            }

    private CharArrayBuffer formatParameters(CharArrayBuffer buffer,
                                             NameValuePair[] nvps,
                                             boolean quote) {
        if (nvps == null) {
            throw new IllegalArgumentException
                    ("Parameters must not be null.");
        }

        int len = estimateParametersLen(nvps);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }

        for (int i = 0; i < nvps.length; i++) {
            if (i > 0) {
                buffer.append("; ");
            }
            formatNameValuePair(buffer, nvps[i], quote);
        }

        return buffer;
    }

    /**
     * Generates textual representation of this content type which can be used as the value
     * of a <code>Content-Type</code> header.
     */
    @Override
    public String toString() {
        final CharArrayBuffer buf = new CharArrayBuffer(64);
        buf.append(this.mimeType);
        if (this.params != null) {
            buf.append("; ");
            formatParameters(buf, this.params, false);
        } else if (this.charset != null) {
            buf.append("; charset=");
            buf.append(this.charset.name());
        }
        return buf.toString();
    }

    private static boolean valid(final String s) {
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch == '"' || ch == ',' || ch == ';') {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new instance of {@link com.accela.mobile.http.volley.Legacy.mime.ContentType}.
     *
     * @param mimeType MIME type. It may not be <code>null</code> or empty. It may not contain
     *        characters <">, <;>, <,> reserved by the HTTP specification.
     * @param charset charset.
     * @return content type
     */
    public static ContentType create(final String mimeType, final Charset charset) {
        final String type = notBlank(mimeType, "MIME type").toLowerCase(Locale.US);
        check(valid(type), "MIME type may not contain reserved characters");
        return new ContentType(type, charset);
    }

    /**
     * Creates a new instance of {@link com.accela.mobile.http.volley.Legacy.mime.ContentType} without a charset.
     *
     * @param mimeType MIME type. It may not be <code>null</code> or empty. It may not contain
     *        characters <">, <;>, <,> reserved by the HTTP specification.
     * @return content type
     */
    public static ContentType create(final String mimeType) {
        return new ContentType(mimeType, (Charset) null);
    }

    /**
     * Creates a new instance of {@link com.accela.mobile.http.volley.Legacy.mime.ContentType}.
     *
     * @param mimeType MIME type. It may not be <code>null</code> or empty. It may not contain
     *        characters <">, <;>, <,> reserved by the HTTP specification.
     * @param charset charset. It may not contain characters <">, <;>, <,> reserved by the HTTP
     *        specification. This parameter is optional.
     * @return content type
     * @throws java.nio.charset.UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     */
    public static ContentType create(
            final String mimeType, final String charset) throws UnsupportedCharsetException {
        return create(mimeType, !isBlank(charset) ? Charset.forName(charset) : null);
    }

    private static ContentType create(final HeaderElement helem) {
        final String mimeType = helem.getName();
        final NameValuePair[] params = helem.getParameters();
        return new ContentType(mimeType, params != null && params.length > 0 ? params : null);
    }

    /**
     * Parses textual representation of <code>Content-Type</code> value.
     *
     * @param s text
     * @return content type
     * @throws org.apache.http.ParseException if the given text does not represent a valid
     * <code>Content-Type</code> value.
     * @throws java.nio.charset.UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     */
    public static ContentType parse(
            final String s) throws ParseException, UnsupportedCharsetException {
        notNull(s, "Content type");
        final CharArrayBuffer buf = new CharArrayBuffer(s.length());
        buf.append(s);
        final ParserCursor cursor = new ParserCursor(0, s.length());
        final HeaderElement[] elements = BasicHeaderValueParser.DEFAULT.parseElements(buf, cursor);
        if (elements.length > 0) {
            return create(elements[0]);
        } else {
            throw new ParseException("Invalid content type: " + s);
        }
    }

    /**
     * Extracts <code>Content-Type</code> value from {@link org.apache.http.HttpEntity} exactly as
     * specified by the <code>Content-Type</code> header of the entity. Returns <code>null</code>
     * if not specified.
     *
     * @param entity HTTP entity
     * @return content type
     * @throws org.apache.http.ParseException if the given text does not represent a valid
     * <code>Content-Type</code> value.
     * @throws java.nio.charset.UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     */
    public static ContentType get(
            final HttpEntity entity) throws ParseException, UnsupportedCharsetException {
        if (entity == null) {
            return null;
        }
        final Header header = entity.getContentType();
        if (header != null) {
            final HeaderElement[] elements = header.getElements();
            if (elements.length > 0) {
                return create(elements[0]);
            }
        }
        return null;
    }

    /**
     * Extracts <code>Content-Type</code> value from {@link org.apache.http.HttpEntity} or returns the default value
     * {@link #DEFAULT_TEXT} if not explicitly specified.
     *
     * @param entity HTTP entity
     * @return content type
     * @throws org.apache.http.ParseException if the given text does not represent a valid
     * <code>Content-Type</code> value.
     * @throws java.nio.charset.UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     */
    public static ContentType getOrDefault(
            final HttpEntity entity) throws ParseException, UnsupportedCharsetException {
        final ContentType contentType = get(entity);
        return contentType != null ? contentType : DEFAULT_TEXT;
    }

    /**
     * Creates a new instance with this MIME type and the given Charset.
     *
     * @param charset charset
     * @return a new instance with this MIME type and the given Charset.
     * @since 4.3
     */
    public ContentType withCharset(final Charset charset) {
        return create(this.getMimeType(), charset);
    }

    /**
     * Creates a new instance with this MIME type and the given Charset name.
     *
     * @param charset name
     * @return a new instance with this MIME type and the given Charset name.
     * @throws java.nio.charset.UnsupportedCharsetException Thrown when the named charset is not available in
     * this instance of the Java virtual machine
     * @since 4.3
     */
    public ContentType withCharset(final String charset) {
        return create(this.getMimeType(), charset);
    }

}

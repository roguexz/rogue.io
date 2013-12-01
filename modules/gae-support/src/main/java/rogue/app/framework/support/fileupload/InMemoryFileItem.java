/*
 * Copyright 2013, Rogue.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rogue.app.framework.support.fileupload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemHeadersSupport;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.util.Streams;

import java.io.*;
import java.util.Map;

/**
 * An in-memory file item implementation. Use with caution.
 */
public class InMemoryFileItem implements FileItem, FileItemHeadersSupport
{
    /**
     * Default content charset to be used when no explicit charset
     * parameter is provided by the sender. Media subtypes of the
     * "text" type are defined to have a default charset value of
     * "ISO-8859-1" when received via HTTP.
     */
    private static final String DEFAULT_CHARSET = "ISO-8859-1";

    private String fieldName;
    private String contentType;
    private boolean isFormField;
    private String fileName;
    private int sizeThreshold;


    private byte[] cachedContent;
    private FileItemHeaders headers;
    private transient ByteArrayOutputStream outputStream;

    // ----------------------------------------------------------- Constructors


    /**
     * Constructs a new <code>DiskFileItem</code> instance.
     *
     * @param fieldName     The name of the form field.
     * @param contentType   The content type passed by the browser or
     *                      <code>null</code> if not specified.
     * @param isFormField   Whether or not this item is a plain form field, as
     *                      opposed to a file upload.
     * @param fileName      The original filename in the user's filesystem, or
     *                      <code>null</code> if not specified.
     * @param sizeThreshold The threshold, in bytes, below which items will be
     *                      retained in memory and above which they will be ignored
     */
    public InMemoryFileItem(String fieldName,
                            String contentType, boolean isFormField, String fileName,
                            int sizeThreshold)
    {
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.isFormField = isFormField;
        this.fileName = fileName;
        this.sizeThreshold = sizeThreshold;
    }

    @Override
    public String getContentType()
    {
        return contentType;
    }


    /**
     * Returns the content charset passed by the agent or <code>null</code> if
     * not defined.
     *
     * @return The content charset passed by the agent or <code>null</code> if
     *         not defined.
     */
    public String getCharSet()
    {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map params = parser.parse(getContentType(), ';');
        return (String) params.get("charset");
    }


    @Override
    public String getName()
    {
        return Streams.checkFileName(fileName);
    }

    @Override
    public boolean isInMemory()
    {
        return true;
    }

    @Override
    public long getSize()
    {
        initCachedContent();
        return cachedContent != null ? cachedContent.length : 0;
    }

    @Override
    public byte[] get()
    {
        initCachedContent();
        return cachedContent;
    }

    @Override
    public String getString(final String charset)
            throws UnsupportedEncodingException
    {
        return new String(get(), charset);
    }

    @Override
    public String getString()
    {
        byte[] rawData = get();
        String charset = getCharSet();
        if (charset == null)
        {
            charset = DEFAULT_CHARSET;
        }
        try
        {
            return new String(rawData, charset);
        }
        catch (UnsupportedEncodingException e)
        {
            return new String(rawData);
        }
    }

    @Override
    public void write(File file) throws Exception
    {
        throw new UnsupportedOperationException("Writing to a file is not supported.");
    }

    @Override
    public void delete()
    {
        cachedContent = null;
        outputStream = null;
    }

    @Override
    public String getFieldName()
    {
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    @Override
    public boolean isFormField()
    {
        return isFormField;
    }

    @Override
    public void setFormField(boolean state)
    {
        isFormField = state;
    }

    @Override
    public OutputStream getOutputStream()
            throws IOException
    {
        if (outputStream == null)
        {
            outputStream = new ByteArrayOutputStream(sizeThreshold);
        }
        return outputStream;
    }

    @Override
    public InputStream getInputStream()
            throws IOException
    {
        initCachedContent();
        return cachedContent != null ? new ByteArrayInputStream(cachedContent) : null;
    }

    @Override
    public FileItemHeaders getHeaders()
    {
        return headers;
    }

    @Override
    public void setHeaders(FileItemHeaders pHeaders)
    {
        headers = pHeaders;
    }

    @Override
    public String toString()
    {
        return "name=" + this.getName()
                + ", size="
                + this.getSize()
                + "bytes, "
                + "isFormField=" + isFormField()
                + ", FieldName="
                + this.getFieldName();
    }

    private void initCachedContent()
    {
        if (cachedContent == null && outputStream != null)
        {
            cachedContent = outputStream.toByteArray();
        }
    }

}

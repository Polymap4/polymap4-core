/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.service.fs.spi;

import java.util.Map;

import java.io.IOException;
import java.io.OutputStream;

import com.bradmcevoy.http.GetableResource;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @author Javadoc is partially taken from {@link GetableResource}
 */
public interface IContentFile
        extends IContentNode {

    /**
     * Send the resource's content using the given output stream.
     * <p/>
     * The Range argument is not-null for partial content requests. In this case
     * implementations should (but are not required) to only send the data range
     * requested.
     * <p/>
     * The contentType argument is that which was resolved by negotiation in the
     * getContentType method. For example HTTP allows a given resource to have
     * multiple representations on the same URL. For example, a data series could be
     * retrieved as a chart as SVG, PNG, JPEG, or as text as CSV or XML. When the
     * user agent requests the resource is specified what content types it can
     * accept. These are matched against those that can be provided by the server and
     * a preferred representation is selected. That contentType is set in the
     * response header and is provided here so that the resource implementation can
     * render itself appropriately.
     * 
     * @param out - the output stream to send the content to
     * @param range - null for normal GET's, not null for partial GET's. May be
     *        ignored
     * @param params - request parameters
     * @param contentType - the contentType selected by negotiation
     * @throws java.io.IOException - if there is an exception writing content to the
     *         output stream. This indicates that the client has disconnected (as
     *         frequently occurs with http transfers). DO NOT throw an IOException if
     *         there was an internal error generating the response (eg if reading
     *         from a database)
     * @throws com.bradmcevoy.http.exceptions.NotAuthorizedException
     */
    public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
    throws IOException, BadRequestException;


    /**
     * Given a comma separated listed of preferred content types acceptable for a
     * client, return one content type which is the best. Returns the most preferred
     * MIME type. E.g. text/html, image/jpeg, etc
     * <p/>
     * See - http://www.iana.org/assignments/media-types/ for a list of content types
     * See - http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html for details about
     * the accept header
     * <p/>
     * If you can't handle accepts interpretation, just return a single content type,
     * e.g. text/html
     */
    String getContentType( String accepts );

    /** The length of the content in this resource. If unknown return NULL
     */
    Long getContentLength();

}

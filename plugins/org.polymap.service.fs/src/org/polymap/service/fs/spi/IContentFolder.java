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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContentFolder
        extends IContentNode {

    /**
     * Generates the HTML content used to display folders in a web browser.
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
     * @param out
     * @param contentType
     */
    public void sendDescription( OutputStream out, Range range, Map<String,String> params, String contentType )
    throws IOException, BadRequestException;

}

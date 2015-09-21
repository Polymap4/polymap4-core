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

import io.milton.http.FileItem;

import java.util.Map;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * <p/>
 * Also allows upload via browser for folders via {@link #processForm(Map, Map)}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IContentWriteable {

    void replaceContent( InputStream in, Long length )
            throws IOException, BadRequestException, NotAuthorizedException;

    String processForm( Map<String,String> params, Map<String,FileItem> files )
            throws IOException, BadRequestException, NotAuthorizedException;

}

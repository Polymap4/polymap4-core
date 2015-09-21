/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.webdav;

import io.milton.http.Auth;

import org.polymap.service.fs.spi.IContentNode;

/**
 * This interface allows access to the underlying node of a {@link WebDavResource}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ContentNodeResource {

    public IContentNode getNode();

    /**
     * Returns the real maxAgeSeconds value. This is a workaround for bug(?) in
     * Milton's HTTP 1.1 GetHandler#checkIfModifiedSince().
     * 
     * @see BalkonCacheControl
     */
    public Long getRealMaxAgeSeconds( Auth auth );

}

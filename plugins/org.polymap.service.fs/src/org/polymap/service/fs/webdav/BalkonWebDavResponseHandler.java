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

import io.milton.http.http11.CacheControlHelper;
import io.milton.http.http11.DefaultHttp11ResponseHandler;
import io.milton.http.http11.Http11ResponseHandler;
import io.milton.http.webdav.DefaultWebDavResponseHandler;
import io.milton.http.webdav.PropFindXmlGenerator;
import io.milton.http.webdav.ResourceTypeHelper;

/**
 * 
 * @see BalkonCacheControl
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BalkonWebDavResponseHandler
        extends DefaultWebDavResponseHandler {

    public BalkonWebDavResponseHandler( Http11ResponseHandler wrapped, ResourceTypeHelper resourceTypeHelper,
            PropFindXmlGenerator propFindXmlGenerator ) {
        super( wrapped, resourceTypeHelper, propFindXmlGenerator );
    }

    public void setCacheControl( CacheControlHelper cacheControl ) {
        ((DefaultHttp11ResponseHandler)wrapped).setCacheControlHelper( cacheControl );
    }
    
}

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

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.http11.CacheControlHelper;
import com.bradmcevoy.http.http11.DefaultHttp11ResponseHandler;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;


/**
 * 
 * @see BalkonCacheControl
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BalkonWebDavResponseHandler
        extends DefaultWebDavResponseHandler {

    public BalkonWebDavResponseHandler( AuthenticationService authService ) {
        super( authService );
    }

    public void setCacheControl( CacheControlHelper cacheControl ) {
        ((DefaultHttp11ResponseHandler)wrapped).setCacheControlHelper( cacheControl );
    }
    
}

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
import io.milton.http.Response;
import io.milton.http.http11.CacheControlHelper;
import io.milton.resource.GetableResource;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates Cache-Control reponse headers.
 * <p/>
 * Implements a workaround for a bug(?) in Milton's HTTP 1.1 GetHandler. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BalkonCacheControl
        implements CacheControlHelper {

    private static final Log log = LogFactory.getLog( BalkonCacheControl.class );

    private boolean             usePrivateCache = false;

    
    public BalkonCacheControl( boolean usePrivateCache ) {
        this.usePrivateCache = usePrivateCache;
    }

    public void setCacheControl( final GetableResource resource, final Response response, Auth auth ) {
        // XXX workaround for bug(?) in Milton's HTTP1.1 GetHandler
        Long delta = ((ContentNodeResource)resource).getRealMaxAgeSeconds( auth );
        // Long delta = resource.getMaxAgeSeconds( auth );

        log.debug( "setCacheControl: " + delta + " - " + resource.getClass() );

        if (delta != null) {
            if (usePrivateCache && auth != null) {
                response.setCacheControlPrivateMaxAgeHeader( delta );
                // response.setCacheControlMaxAgeHeader(delta);
            }
            else {
                response.setCacheControlMaxAgeHeader( delta );
            }
            // Disable, might be interfering with IE.. ?
            // Date expiresAt = calcExpiresAt( new Date(), delta.longValue() );
            // if( log.isTraceEnabled() ) {
            // log.trace( "set expires: " + expiresAt );
            // }
            // response.setExpiresHeader( expiresAt );
        }
        else {
            response.setCacheControlNoCacheHeader();
        }
    }

    public static Date calcExpiresAt( Date modifiedDate, long deltaSeconds ) {
        long deltaMs = deltaSeconds * 1000;
        long expiresAt = System.currentTimeMillis() + deltaMs;
        return new Date( expiresAt );
    }
    
}

/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;

/**
 * Helper for {@link ServiceHandler}s.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ServiceUriBuilder {

    private static Log log = LogFactory.getLog( ServiceUriBuilder.class );
    
    private static final char       EQUAL = '=';
    private static final char       AMPERSAND = '&';
    private static final String     QUESTION_MARK = "?";

    public static ServiceUriBuilder forServiceId( String id ) {
        return new ServiceUriBuilder( id );
    }
    
    
    // instance *******************************************

    private StringBuilder       buf = new StringBuilder( 256 );
    
    private boolean             hasParams;
    
    public ServiceUriBuilder( String id ) {
        String baseUri = RWT.getServiceManager().getServiceHandlerUrl( id );
        hasParams = baseUri.contains( QUESTION_MARK );
        buf.append( RWT.getServiceManager().getServiceHandlerUrl( id ) );
    }

    public boolean hasParams() {
        return hasParams;
    }
    
    public ServiceUriBuilder appendParam( String name, String value ) {
        buf.append( hasParams ? AMPERSAND : QUESTION_MARK ).append( name ).append( EQUAL ).append( value );
        hasParams = true;
        return this;
    }

    public String encodedAbsoluteUri() {
        throw new RuntimeException( "not yet implemented." );
    }
    
    public String encodedRelativeUri() {
        String uri = buf.toString();
        // RAP 2.3: absolute URI without hostname:port
        if (uri.startsWith( "/" )) {
            String relative = QUESTION_MARK + StringUtils.substringAfter( uri, QUESTION_MARK );
            return RWT.getResponse().encodeURL( relative );
        }
        // convert to relative URL
        else {
            // FIXME code from RAP... ???
            int firstSlash = uri.indexOf( "/" , uri.indexOf( "//" ) + 2 ); // first slash after double slash of "http://"
            //url.delete( 0, firstSlash + 1 ); // Result is sth like "/rap?custom_service_handler..."
            return RWT.getResponse().encodeURL( uri.substring( firstSlash, uri.length() ) );
        }        
    }
    
    @Override
    public String toString() {
        return encodedAbsoluteUri();
    }
    
}

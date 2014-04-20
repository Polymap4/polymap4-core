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
package org.polymap.core.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This callback handler is used for service logins without dialog. As there is a
 * mapping between callback handler and JAAS config the service JAAS config is always
 * needed, even if is is almost always the same as the dialog login config.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ServicesCallbackHandler
        implements CallbackHandler {

    private static Log log = LogFactory.getLog( ServicesCallbackHandler.class );

    private static final ThreadLocal<String[]>  challenge = new ThreadLocal();
    

    public static void challenge( String user, String passwd ) {
        challenge.set( new String[] { user, passwd } );
    }

    
    // instance *******************************************
    
    public ServicesCallbackHandler() {
    }


    public void handle( Callback[] callbacks )
    throws IOException, UnsupportedCallbackException {
        
        String[] userPasswd = challenge.get();
        challenge.set( null );
        
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                ((NameCallback)callback).setName( userPasswd[0] );
            }
            else if (callback instanceof PasswordCallback) {
                ((PasswordCallback)callback).setPassword( userPasswd[1].toCharArray() );
            }
        }
    }

}

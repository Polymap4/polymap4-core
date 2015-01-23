/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.security;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.sun.security.auth.UserPrincipal;

/**
 * Extension of {@link com.sun.security.auth.module.LdapLoginModule} that supports
 * and provides the {@link UserPrincipal}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LdapLoginModule
        extends com.sun.security.auth.module.LdapLoginModule
        implements LoginModule {

    private static Log log = LogFactory.getLog( LdapLoginModule.class );
    
    private Subject             subject;

    private AuthorizationModule authModule;

    
    @Override
    public void initialize( @SuppressWarnings("hiding") Subject subject, 
            CallbackHandler callbackHandler, Map<String,?> sharedState, Map<String,?> options ) {
        super.initialize( this.subject = subject, callbackHandler, sharedState, options );

        authModule = AuthorizationModuleExtension.forOptions( options )
                .initialize( this, subject, callbackHandler, sharedState, options );
    }

    
    @Override
    public boolean commit() throws LoginException {
        if (super.commit()) {
            for (UserPrincipal principal : subject.getPrincipals( UserPrincipal.class )) {
                log.info( "principal: " + principal );
                
                org.polymap.core.security.UserPrincipal user = new org.polymap.core.security.UserPrincipal( principal.getName() ) {
                    public String getPassword() {
                        // XXX Auto-generated method stub
                        throw new RuntimeException( "not yet implemented." );
                    }
                };
                subject.getPrincipals().add( user );                

                subject.getPrivateCredentials().add( this );
                subject.getPrivateCredentials().add( authModule );
            }
            return true;
        }
        return false;
    }
    
}

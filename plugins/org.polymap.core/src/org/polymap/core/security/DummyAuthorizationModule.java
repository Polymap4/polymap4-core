/* 
 * polymap.org
 * Copyright (C) 2013-2014, Falko Bräutigam. All rights reserved.
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.polymap.core.security.DummyLoginModule.DummyUserPrincipal;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DummyAuthorizationModule
        implements AuthorizationModule {

    private static Log log = LogFactory.getLog( DummyAuthorizationModule.class );
    
    private DummyLoginModule    delegateLoginModule;


    @Override
    public void init( LoginModule loginModule ) {
        delegateLoginModule = loginModule instanceof DummyLoginModule ? (DummyLoginModule)loginModule : null;
    }


    @Override
    public void initialize( Subject subject, CallbackHandler callbackHandler,
            Map<String,?> sharedState, Map<String,?> options ) {

        if (delegateLoginModule == null) {
            delegateLoginModule = new DummyLoginModule();
            delegateLoginModule.initialize( subject, callbackHandler, sharedState, options );
        }
    }


    @Override
    public Set<Principal> rolesOf( Subject subject ) {
        Set<DummyUserPrincipal> dummyPrincipals = subject.getPrincipals( DummyUserPrincipal.class );
        assert dummyPrincipals.size() <= 1;
        
        // DummyUserPrincipal found
        if (!dummyPrincipals.isEmpty()) {
            return getOnlyElement( dummyPrincipals ).getRoles();
        }
        //
        else {
            Set<UserPrincipal> users = subject.getPrincipals( UserPrincipal.class );
            assert users.size() == 1 : "Too many/less UserPrincipals in subject: " + users;
            DummyUserPrincipal dummyUser = delegateLoginModule.userForName( getOnlyElement( users ).getName() );
            return dummyUser != null ? dummyUser.getRoles() : Collections.EMPTY_SET;
        }
    }

}

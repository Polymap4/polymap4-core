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

import java.util.Set;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.security.DummyLoginModule.DummyUserPrincipal;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DummyAuthorizationModule
        implements AuthorizationModule {

    private static Log log = LogFactory.getLog( DummyAuthorizationModule.class );


    @Override
    public void init( LoginModule loginModule ) {
    }


    @Override
    public Set<Principal> rolesOf( Subject subject ) {
        Set<DummyUserPrincipal> principals = subject.getPrincipals( DummyUserPrincipal.class );
        assert principals.size() == 1;
        DummyUserPrincipal user = principals.iterator().next();
        return user.getRoles();
    }

}

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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;

/**
 * A no-op authorazation module. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NullAuthorizationModule
        implements AuthorizationModule {

    @Override
    public void init( LoginModule loginModule ) {
    }

    
    @Override
    public void initialize( Subject subject, CallbackHandler callbackHandler,
            Map<String,?> sharedState, Map<String,?> options ) {
    }


    @Override
    public Set<Principal> rolesOf( Subject subject ) {
        return Collections.EMPTY_SET;
    }
    
}

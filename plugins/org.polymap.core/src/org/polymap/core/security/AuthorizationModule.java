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

import java.util.Map;
import java.util.Set;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;

/**
 * Describes the interface implemented by authorization technology providers.
 * AuthorizationModules are plugged in under applications to provide a particular
 * type of user - role mapping. The POLYMAP security framework allows to combine and
 * use a {@link LoginModule} together with different AuthorizationModules. Both
 * modules share the same <a
 * href="http://polymap.org/polymap3/wiki/UserGuide/Authentication">jaas_config</a>.
 * 
 * @see LoginModule
 * @see <a href="http://polymap.org/polymap3/wiki/UserGuide/Authentication">jaas_config</a>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface AuthorizationModule {

    public void init( LoginModule loginModule );
    
    
    /**
     * Initialize this Module with parameters of the {@link LoginModule}. If this
     * <code>AuthorizationModule</code> does not understand any of the data stored in
     * <code>sharedState</code> or <code>options</code> parameters, they can be
     * ignored.
     * 
     * @see LoginModule#initialize(Subject, CallbackHandler, Map, Map)
     * @param callbackHandler a <code>CallbackHandler</code> for communicating with
     *        the end user (prompting for usernames and passwords, for example).
     * @param sharedState state shared with other configured LoginModules.
     * @param options options specified in the login <code>Configuration</code> for
     *        this particular <code>LoginModule</code>.
     */
    void initialize( Subject subject, CallbackHandler callbackHandler,
                    Map<String,?> sharedState, Map<String,?> options );

    public Set<Principal> rolesOf( Subject subject );
    
}

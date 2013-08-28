/* 
 * polymap.org
 * Copyright (C) 2013, Falko Br�utigam. All rights reserved.
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

import javax.security.auth.spi.LoginModule;

import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preovide a preference page for the UI. 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface PreferencesLoginModule
        extends LoginModule {

    public IWorkbenchPreferencePage createPreferencePage();
    
}

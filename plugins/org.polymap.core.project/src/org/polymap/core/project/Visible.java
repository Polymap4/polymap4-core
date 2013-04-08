/* 
 * polymap.org
 * Copyright 2013, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.project;

import org.polymap.core.model.TransientProperty;

/**
 * This general interface allows to give entities a visible state.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface Visible {

    public static final String      PROP_VISIBLE = "visible";
    
    public boolean isVisible();
    
    @TransientProperty(PROP_VISIBLE)
    public void setVisible( boolean visible );

}

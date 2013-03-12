/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. ALl rights reserved.
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
package org.polymap.core.project.model;

/**
 * Provides interface and mixin to give entities a label. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
public interface Visible
        extends org.polymap.core.project.Visible {

    /**
     * The mixin.
     */
    public abstract static class Mixin
            implements Visible {

        private boolean                 visible = false;
        
        public boolean isVisible() {
            return visible;
        }

        public void setVisible( boolean visible ) {
            if (this.visible != visible) {
                boolean old = this.visible;
                this.visible = visible;
            }
        }
    }
    
}

/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.model.event;

import java.beans.PropertyChangeEvent;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public interface PropertyEventFilter {
    
    /**
     * 
     */
    public static PropertyEventFilter ALL = new PropertyEventFilter() {
        public boolean accept( PropertyChangeEvent ev ) {
            return true;
        }
    };
    
    /**
     * Checks if <b>all</b> the given filters accept an event. 
     */
    public class And
            implements PropertyEventFilter {
        
        PropertyEventFilter[]   children;
        
        public And( PropertyEventFilter... children ) {
            assert children != null;
            this.children = children;
        }

        public boolean accept( PropertyChangeEvent ev ) {
            for (PropertyEventFilter filter : children) {
                if (filter.accept( ev ) == false) {
                    return false;
                }
            }
            return true;
        }
        
    }

    
    // interface ******************************************
    
    boolean accept( PropertyChangeEvent ev );

}

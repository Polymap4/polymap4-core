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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Filter events that are fired by entities which class is assignable to the given
 * classes.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class SourceClassPropertyEventFilter
        implements PropertyEventFilter {

    private static Log log = LogFactory.getLog( SourceClassPropertyEventFilter.class );

    private Class[]         allowed;
    
    
    public SourceClassPropertyEventFilter( Class... allowed ) {
        this.allowed = allowed;
    }

    public boolean accept( PropertyChangeEvent ev ) {
        Class<? extends Object> sourceClass = ev.getSource().getClass();
        for (Class cl : allowed) {
            if (!cl.isAssignableFrom( sourceClass )) {
                return false; 
            }
        }
        return true;
    }
    
}

/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.runtime;

import java.util.ArrayList;
import java.util.List;

import org.polymap.core.model2.Entity;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConcurrentEntityModificationException
        extends ModelRuntimeException {

    private List<Entity>            modified;

    
    public ConcurrentEntityModificationException( String msg, List<Entity> modified ) {
        super( msg );
        this.modified = new ArrayList( modified );
    }

    
    public ConcurrentEntityModificationException( String msg, List<Entity> modified, Throwable cause ) {
        super( msg, cause );
        this.modified = new ArrayList( modified );
    }

    
    public List<Entity> getModified() {
        return modified;
    }
    
}

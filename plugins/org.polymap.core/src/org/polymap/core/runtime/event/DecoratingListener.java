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
package org.polymap.core.runtime.event;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class DecoratingListener
        implements EventListener {

    protected EventListener     delegate;
    
    
    public DecoratingListener( EventListener delegate ) {
        assert delegate != null;
        this.delegate = delegate;
    }


    @Override
    public int hashCode() {
        return delegate.hashCode();
    }


    @Override
    public boolean equals( Object obj ) {
        return delegate.equals( obj );
    }
    
}

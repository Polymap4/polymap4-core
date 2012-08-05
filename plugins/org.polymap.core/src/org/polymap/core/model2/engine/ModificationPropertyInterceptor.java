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
package org.polymap.core.model2.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.EntityRuntimeContext.EntityStatus;

/**
 * Check for modifications and raise status of the entity accordingly. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ModificationPropertyInterceptor
        implements Property {

    private static Log log = LogFactory.getLog( ModificationPropertyInterceptor.class );

    private Property                delegate;
    
    private EntityRuntimeContext    context;
    
    
    public ModificationPropertyInterceptor( Property delegate, EntityRuntimeContext context ) {
        assert delegate != null && context != null;
        this.delegate = delegate;
        this.context = context;
    }

    public Object get() {
        return delegate.get();
    }

    public void set( Object value ) {
        delegate.set( value );
        context.raiseStatus( EntityStatus.MODIFIED );
    }

    public PropertyInfo getInfo() {
        return delegate.getInfo();
    }
    
}

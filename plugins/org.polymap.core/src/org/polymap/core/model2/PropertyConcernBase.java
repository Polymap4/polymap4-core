/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2;

import org.polymap.core.model2.runtime.EntityRuntimeContext;
import org.polymap.core.model2.runtime.PropertyInfo;

/**
 * Abstract base class for all property concerns.
 * <p/>
 * Implementations should be thread save. Instances might be instantiated on-demand
 * and so cannot hold an internal state.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class PropertyConcernBase<T>
        implements PropertyBase<T> {

    protected EntityRuntimeContext      context;
    
    /**
     * The delegate of this concern. Cast this to {@link Property} or {@link CollectionProperty}.
     */
    protected PropertyBase              delegate;

    
    @Override
    public PropertyInfo getInfo() {
        return delegate.getInfo();
    }    
    
}

/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IAdaptable;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class DefaultOperationContext
        implements IFeatureOperationContext {

    private static Log log = LogFactory.getLog( DefaultOperationContext.class );

    private List<IAdaptable>        adapters = new ArrayList();
    

    public Object getAdapter( Class adapter ) {
        for (IAdaptable elm : adapters) {
            Object result = elm.getAdapter( adapter );
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    
    public <T> T adapt( Class<T> adapter ) {
        return adapter.cast( getAdapter( adapter ) );
    }

    
    public void addAdapter( IAdaptable adapter ) {
        adapters.add( adapter );
    }
    
}

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

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;

/**
 * 
 * <p/>
 * Implementation should init feature source and collections lazily, so that blocking
 * access of backend stores is not necessarily needed to just init the operations.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFeatureOperationContext
        extends IAdaptable {
    
    public FeatureCollection features()
    throws Exception;
    
    public FeatureSource featureSource()
    throws Exception;
    

    /**
     * Returns an object which is an instance of the given class associated with this
     * object. An operation context at least adapts to:
     * <ul>
     * <li>{@link FeatureOperationExtension}</li>
     * <li>{@link IUndoableOperation} - the container of this operation</li>
     * </ul>
     * 
     * @param adapter The adapter class to look up.
     * @return An object castable to the given class, or <code>null</code> if this
     *         object does not have an adapter for the given class.
     */
    public Object getAdapter( Class adapter );

    /**
     * This just calls {@link #getAdapter(Class)} and does to necessary cast.
     */
    public <T> T adapt( Class<T> adapter );
    
}

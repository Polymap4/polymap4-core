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

import java.util.Iterator;

import org.opengis.feature.Feature;

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
    
    public Iterator<Feature> features() 
    throws Exception;
    
    public int featuresSize() 
    throws Exception;

}

/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.rap.openlayers.layer.Layer;

/**
 * Provides a List of {@link Layer} objects to a {@link MapViewer}. This input
 * element might be a single layer object, a List or an array of layers.
 * 
 * @see OlLayerProvider
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OlContentProvider<CL>
        implements IStructuredContentProvider {

    private Collection<CL>          layers;
    
    
    public OlContentProvider( CL... layers ) {
        this.layers = Arrays.asList( layers );
    }

    public OlContentProvider( Collection<CL> layers ) {
        this.layers = layers;
    }

    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        if (newInput.getClass().isArray()) {
            layers = Arrays.asList( (CL[])newInput );
        }
        else if (newInput instanceof Collection) {
            this.layers = (Collection<CL>)newInput;
        }
        else {
            layers = Collections.singletonList( (CL)newInput );
        }
    }

    @Override
    public Object[] getElements( Object inputElement ) {
        return layers.toArray();
    }

    @Override
    public void dispose() {
    }
    
}

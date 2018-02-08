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

import org.eclipse.jface.viewers.LabelProvider;

import org.polymap.rap.openlayers.layer.Layer;

/**
 * An {@link ILayerProvider} converts input elements of {@link MapViewer} to
 * {@link Layer} instances. This interface acts like a {@link LabelProvider} for
 * viewer.
 * 
 * @param <CL>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ILayerProvider<CL> {

    /**
     * Creates a {@link Layer} for the given element.
     *
     * @param elm The input element of the {@link MapViewer}.
     * @return Newly created {@link Layer} instance.
     */
    public Layer getLayer( CL elm );
        
    /**
     * Returns the render priority for the given layer. Higher priorities are
     * rendered above other layers.
     */
    public int getPriority( CL elm );
    
}

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

import org.polymap.rap.openlayers.layer.Layer;

/**
 * The label provider to use together with {@link OlContentProvider}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OlLayerProvider
        implements ILayerProvider<Layer> {

    private volatile int        priorityCount = 0;
    
    @Override
    public Layer getLayer( Layer elm ) {
        return elm;
    }

    @Override
    public int getPriority( Layer elm ) {
        return priorityCount++;
    }
    
}

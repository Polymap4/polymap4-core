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
package org.polymap.core.data.feature.buffer;

import java.util.EventObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureStoreEvent
        extends EventObject {

    private String          layerId;
    
    private long            newVersion;

    
    public FeatureStoreEvent( Object source, String layerId, long newVersion ) {
        super( source );
        this.layerId = layerId;
        this.newVersion = newVersion;
    }

    public String getLayerId() {
        return layerId;
    }
    
    public long getNewVersion() {
        return newVersion;
    }

}

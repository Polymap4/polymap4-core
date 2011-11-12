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
package org.polymap.core.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model.event.IModelStoreListener;
import org.polymap.core.model.event.ModelStoreEvent;
import org.polymap.core.project.ILayer;

/**
 * Receives {@link ModelStoreEvent}s with source instanceof {@link ILayer}
 * only.
 * 
 * @see FeatureChangeTracker
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FeatureStoreListener
        implements IModelStoreListener {

    private static Log log = LogFactory.getLog( FeatureStoreListener.class );

    public abstract void featureChange( FeatureStoreEvent ev );


    public boolean isValid() {
        return true;
    }

    public void modelChanged( ModelStoreEvent ev ) {
        if (ev.getSource() instanceof ILayer) {
            featureChange( new FeatureStoreEvent( ev ) );
        }
    }
    
}

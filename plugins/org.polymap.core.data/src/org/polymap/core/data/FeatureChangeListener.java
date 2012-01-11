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

import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.IModelChangeListener;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FeatureChangeListener
        implements IModelChangeListener {

    private static Log log = LogFactory.getLog( FeatureChangeListener.class );

    
    public abstract void featureChange( FeatureChangeEvent ev );

    
    public final void modelChanged( ModelChangeEvent ev ) {
        if (ev instanceof FeatureChangeEvent) {
            featureChange( (FeatureChangeEvent)ev );
        }
        else {
            log.warn( "skipping event: " + ev );
        }
    }
    
}

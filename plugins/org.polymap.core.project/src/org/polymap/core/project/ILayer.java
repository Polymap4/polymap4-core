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
package org.polymap.core.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.model2.Concerns;
import org.polymap.model2.Defaults;
import org.polymap.model2.Mixins;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.event.PropertyChangeSupport;

/**
 *  
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns({
    PropertyChangeSupport.class
    // ACLCheckConcern.class
})
@Mixins({
    //ACL.class
})
public class ILayer
        extends ProjectNode {

    private static Log log = LogFactory.getLog( ILayer.class );
    
    /**
     *
     */
    public Property<String>         resourceIdentifier;
    
    @Defaults
    public Property<Integer>        orderKey;
    
}

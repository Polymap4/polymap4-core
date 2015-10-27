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

import java.util.EventObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.model2.Association;
import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Defaults;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.runtime.Lifecycle;

/**
 * Provides a mixin for {@link ILayer} and {@link IMap} defining therm as part of a
 * hierarchy of maps.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ProjectNode
        extends Entity
        implements Lifecycle {

    private static Log log = LogFactory.getLog( ProjectNode.class );

    @Queryable
    public Property<String>             label;

    @Defaults
    @Queryable
    public CollectionProperty<String>   keywords;

    @Defaults
    public Property<Boolean>            visible;

    public Association<IMap>            parentMap;

    
    @Override
    public void onLifecycleChange( State state ) {
        if (state == State.AFTER_COMMIT) {
            log.info( "Lifecycle: " + this );
            EventManager.instance().publish( new ProjectNodeCommittedEvent( this ) );
        }
    }


    /**
     * 
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    public static class ProjectNodeCommittedEvent
            extends EventObject {

        public ProjectNodeCommittedEvent( ProjectNode source ) {
            super( source );
        }

        @Override
        public ProjectNode getSource() {
            return (ProjectNode)super.getSource();
        }

        public <T extends ProjectNode> T getEntity() {
            return (T)super.getSource();
        }
        
    }
}

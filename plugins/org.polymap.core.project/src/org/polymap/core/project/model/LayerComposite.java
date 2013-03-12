/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rigths reserved.
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
package org.polymap.core.project.model;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;

import org.polymap.core.project.ILayer;
import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.qi4j.security.ACL;
import org.polymap.core.qi4j.security.ACLCheckConcern;
import org.polymap.core.qi4j.security.ACLFilterConcern;

/**
 * The composite providing the implementation of the {@link ILayer} interface.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
@Concerns( {
        ACLCheckConcern.class, 
        ACLFilterConcern.class, 
        PropertyChangeSupport.Concern.class
})
@Mixins( {
        LayerState.Mixin.class, 
        Labeled.Mixin.class,
        Visible.Mixin.class,
        ACL.Mixin.class, 
        ParentMap.Mixin.class,
        PipelineHolder.Mixin.class,
        PropertyChangeSupport.Mixin.class,
        ModelChangeSupport.Mixin.class,
        QiEntity.Mixin.class
} )
public interface LayerComposite
        extends QiEntity, ILayer, LayerState, Labeled, Visible, ACL, ParentMap, PipelineHolder,
                PropertyChangeSupport, ModelChangeSupport, EntityComposite {

}

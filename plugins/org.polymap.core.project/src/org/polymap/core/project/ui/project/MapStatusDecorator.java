/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapStatusDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator {

    private static final Log log = LogFactory.getLog( MapStatusDecorator.class );

    private static final ImageDescriptor    empty = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, "icons/obj16/map_empty_obj.gif" );

    private Map<String,IMap>            decorated = new HashMap();
    

    public void decorate( Object elm, IDecoration decoration ) {
        if (elm instanceof IMap) {
            IMap map = (IMap)elm;
            
            if (map.getLayers().isEmpty()) {
                DecorationContext context = (DecorationContext)decoration.getDecorationContext();
                context.putProperty( IDecoration.ENABLE_REPLACE, Boolean.TRUE );
                decoration.addOverlay( empty, IDecoration.REPLACE );
            }

            // register listener
            if (decorated.put( map.id(), map ) == null) {
                map.addPropertyChangeListener( this, new EventFilter<PropertyChangeEvent>() {
                    public boolean apply( PropertyChangeEvent ev ) {
                        return ev.getPropertyName().equals( IMap.PROP_LAYERS );
                    }
                });
            }
        }
    }


    public void dispose() {
        super.dispose();
        for (IMap map : decorated.values()) {
            try {
                map.removePropertyChangeListener( this );
            }
            catch (NoSuchEntityException e) {
            }
        }
        decorated.clear();
    }

    
    @EventHandler(display=true,delay=3000)
    public void propertyChange( List<PropertyChangeEvent> ev ) {
        fireLabelProviderChanged( new LabelProviderChangedEvent( MapStatusDecorator.this ) );
    }

}

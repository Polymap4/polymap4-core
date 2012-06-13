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
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapStatusDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator, PropertyChangeListener {

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
                map.addPropertyChangeListener( this );
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


    public void propertyChange( PropertyChangeEvent ev ) {
        if (ev.getSource() instanceof IMap
                && ev.getPropertyName().equals( IMap.PROP_LAYERS )) {

            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        fireLabelProviderChanged( new LabelProviderChangedEvent( MapStatusDecorator.this ) );
                    }
                    catch (Exception e) {
                        log.warn( e.getLocalizedMessage() );
                    }
                }
            };
            if (Display.getCurrent() != null) {
                runnable.run();
            }
            else {
                Polymap.getSessionDisplay().asyncExec( runnable );
            }
        }
    }

}

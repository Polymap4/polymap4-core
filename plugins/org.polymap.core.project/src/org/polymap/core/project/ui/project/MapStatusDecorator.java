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

import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import com.google.common.collect.MapMaker;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.Visible;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

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
    private static final ImageDescriptor    visible = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, "icons/obj16/map_obj.gif" );
    private static final ImageDescriptor    visibleOvr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, "icons/ovr16/visible_ovr.png" );
    
    private Map<String,IMap>                decorated;

//    private IWorkbenchPage                  page;
//
//    private PartListenerAdapter             partListener;
//    
//    private Map<String,IMap>                activeMaps = new HashMap();
    
    
    public MapStatusDecorator() {
        decorated = new MapMaker().weakValues().initialCapacity( 128 ).makeMap();
        
        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent ev ) {
                try {
                    String prop = ev.getPropertyName();
                    return ev.getSource() instanceof IMap
                            && decorated.containsKey( ((IMap)ev.getSource()).id() )
                            && (prop.equals( IMap.PROP_LAYERS ) || prop.equals( Visible.PROP_VISIBLE ));
                }
                catch (NoSuchEntityException e) {
                    return false;
                }
            }
        });

//        Polymap.getSessionDisplay().asyncExec( new Runnable() {
//            public void run() {
//                page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//                // init activeMaps
//                for (IEditorReference editor : page.getEditorReferences()) {
//                    try {
//                        IMap map = (IMap)editor.getEditorInput().getAdapter( IMap.class );
//                        if (map != null) {
//                            activeMaps.put( map.id(), map );
//                        }
//                    }
//                    catch (PartInitException e) {
//                        throw new RuntimeException( e );
//                    }
//                }
//                // install listener
//                assert partListener == null;
//                page.addPartListener( partListener = new PartListenerAdapter() {
//                    @Override
//                    public void partClosed( IWorkbenchPart part ) {
//                        if (part instanceof IEditorPart) {
//                            IMap map = (IMap)((IEditorPart)part).getEditorInput().getAdapter( IMap.class );
//                            if (map != null) {
//                                activeMaps.remove( map.id() );
//                                fireLabelProviderChanged( new LabelProviderChangedEvent( MapStatusDecorator.this ) );
//                            }
//                        }
//                    }
//                    @Override
//                    public void partOpened( IWorkbenchPart part ) {
//                        if (part instanceof IEditorPart) {
//                            IMap map = (IMap)((IEditorPart)part).getEditorInput().getAdapter( IMap.class );
//                            if (map != null) {
//                                activeMaps.put( map.id(), map );
//                                fireLabelProviderChanged( new LabelProviderChangedEvent( MapStatusDecorator.this ) );
//                            }
//                        }
//                    }
//                });
//            }
//        });
    }

    
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( this );
        decorated.clear();
//        if (page != null) {
//            page.removePartListener( partListener );
//            page = null;
//        }
    }


    @Override
    protected void fireLabelProviderChanged( LabelProviderChangedEvent ev ) {
        if (!PlatformUI.getWorkbench().isClosing()) {
            super.fireLabelProviderChanged( ev );
        }
    }


    @EventHandler(display=true,delay=1000)
    protected void propertyChange( List<PropertyChangeEvent> ev ) {
        fireLabelProviderChanged( new LabelProviderChangedEvent( MapStatusDecorator.this ) );
    }

    
    public void decorate( Object elm, IDecoration decoration ) {
        if (elm instanceof IMap) {
            IMap map = (IMap)elm;
            // visible
            if (map.isVisible() /*activeMaps.containsKey( map.id() )*/) {
                DecorationContext context = (DecorationContext)decoration.getDecorationContext();
                context.putProperty( IDecoration.ENABLE_REPLACE, Boolean.TRUE );
                decoration.addOverlay( visible, IDecoration.REPLACE );
                decoration.addOverlay( visibleOvr, IDecoration.TOP_LEFT );
            }
            // empty
            if (map.getLayers().isEmpty()) {
                DecorationContext context = (DecorationContext)decoration.getDecorationContext();
                context.putProperty( IDecoration.ENABLE_REPLACE, Boolean.TRUE );
                decoration.addOverlay( empty, IDecoration.REPLACE );
            }
            decorated.put( map.id(), map );
        }
    }

}

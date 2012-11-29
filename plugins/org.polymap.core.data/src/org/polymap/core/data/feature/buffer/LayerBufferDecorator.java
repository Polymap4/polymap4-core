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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.data.FeatureChangeListener;
import org.polymap.core.data.FeatureChangeTracker;
import org.polymap.core.data.FeatureStoreEvent;
import org.polymap.core.data.FeatureStoreListener;
import org.polymap.core.model.event.ModelChangeTracker;
import org.polymap.core.model.event.ModelHandle;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * Decorates {@link ILayer} according their buffer state.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerBufferDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator {

    private static Log log = LogFactory.getLog( LayerBufferDecorator.class );

    public static final Point       DEFAULT_SIZE = new Point( 17, 16 );

    public static final int         TOP_LEFT = 0;
    public static final int         TOP_RIGHT = 1;
    public static final int         BOTTOM_LEFT = 2;
    public static final int         BOTTOM_RIGHT = 3;

    private static final String     OUTGOING = "icons/ovr16/outgo_synch3.gif";    
    private static final String     INCOMING = "icons/ovr16/incom_synch.gif";    
    private static final String     CONFLICT = "icons/ovr16/conf_synch.gif";    

    /** The ids of the decorated layers. */
    private Set<String>             decorated = new HashSet();

    private FeatureChangeListener   changeListener;
    
    private FeatureStoreListener    storeListener;
    
    private Display                 display;
    
    
    public LayerBufferDecorator() {
        this.display = Polymap.getSessionDisplay();
        
        // FeatureChangeListener
        EventManager.instance().subscribe( this, new EventFilter<FeatureChangeEvent>() {
            public boolean apply( FeatureChangeEvent ev ) {
                return decorated.contains( ev.getSource().id() );
            }
        });
        
        // FeatureStoreListener
        storeListener = new FeatureStoreListener() {
            public void featureChange( FeatureStoreEvent ev ) {
                LayerBufferDecorator.this.featureChange( ev );
            }
        };
        FeatureChangeTracker.instance().addFeatureListener( storeListener );
    }

    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        FeatureChangeTracker.instance().removeFeatureListener( storeListener );
        decorated.clear();
    }

    
    @EventHandler(delay=3000,display=true)
    protected void featureChanges( List<FeatureChangeEvent> events ) {
        fireLabelProviderChanged( new LabelProviderChangedEvent( LayerBufferDecorator.this ) );
    }

    
    public void decorate( Object elm, IDecoration decoration ) {
        if (elm instanceof ILayer) {
            ILayer layer = (ILayer)elm;

            try {
                layer.id();
            }
            catch (NoSuchEntityException e) {
                // handled by EntityModificationDecorator
                return;
            }

            LayerFeatureBufferManager layerBuffer = LayerFeatureBufferManager.forLayer( layer, true );
            if (layerBuffer == null) {
                return;
            }
            
            try {
                boolean outgoing = !layerBuffer.getBuffer().isEmpty();
                
                ModelHandle layerHandle = FeatureChangeTracker.layerHandle( layer );
                boolean incoming = ModelChangeTracker.instance().isConflicting( 
                        layerHandle, layerBuffer.getLayerTimestamp() );
                
                if (outgoing && incoming) {
                    decoration.addPrefix( "# " );                    
                }
                else if (incoming) {
                    decoration.addPrefix( "< " );                    
                }
                else if (outgoing) {
                    ImageDescriptor ovr = DataPlugin.imageDescriptorFromPlugin( DataPlugin.PLUGIN_ID, OUTGOING );
                    decoration.addOverlay( ovr, BOTTOM_RIGHT );
                    decoration.addPrefix( "> " );
                }
            }
            catch (Exception e) {
                log.warn( "", e );
                // XXX add question mark overlay
            }

            decorated.add( layer.id() );
        }
    }


    public void featureChange( FeatureStoreEvent ev ) {
        display.asyncExec( new Runnable() {
            public void run() {
                fireLabelProviderChanged( new LabelProviderChangedEvent( LayerBufferDecorator.this ) );
            }
        });
    }

}

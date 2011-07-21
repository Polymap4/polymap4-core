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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

/**
 * Decorates {@link ILayer} according their buffer state.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerBufferDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator, IFeatureChangeListener {

    private static Log log = LogFactory.getLog( LayerBufferDecorator.class );

    public static final Point       DEFAULT_SIZE = new Point( 17, 16 );

    public static final int         TOP_LEFT = 0;
    public static final int         TOP_RIGHT = 1;
    public static final int         BOTTOM_LEFT = 2;
    public static final int         BOTTOM_RIGHT = 3;

    private static final String     outgoing = "icons/ovr16/outgo_synch3.gif";    
    private static final String     incoming = "icons/ovr16/incom_synch.gif";    
    private static final String     conflict = "icons/ovr16/conf_synch.gif";    

    private Map<String,LayerFeatureBufferManager>   decorated = new HashMap();

    
    public void dispose() {
        for (LayerFeatureBufferManager layerBuffer : decorated.values()) {
            layerBuffer.removeFeatureChangeListener( this );
        }
        decorated.clear();

    }


    public void decorate( Object elm, IDecoration decoration ) {
        if (elm instanceof ILayer) {
            ILayer layer = (ILayer)elm;

            LayerFeatureBufferManager layerBuffer = LayerFeatureBufferManager.forLayer( layer, true );
            if (layerBuffer == null) {
                return;
            }
            
            try {
                // outgoing
                if (!layerBuffer.getBuffer().isEmpty()) {
                    ImageDescriptor ovr = DataPlugin.imageDescriptorFromPlugin( DataPlugin.PLUGIN_ID, outgoing );
                    decoration.addOverlay( ovr, BOTTOM_RIGHT );
                    decoration.addPrefix( "> " );
                }
                // XXX add incoming and conflict
            }
            catch (Exception e) {
                log.warn( "", e );
                // XXX add question mark overlay
            }

            // register listener
            if (decorated.put( layer.id(), layerBuffer ) == null) {
                layerBuffer.addFeatureChangeListener( this );
            }
        }
    }


//    public Image decorateImage( Image image, Object elm ) {
//        Image result = null;
//        
//        if (elm instanceof ILayer) {
//            ILayer layer = (ILayer)elm;
//
//            LayerFeatureBufferManager layerBuffer = LayerFeatureBufferManager.forLayer( layer, false );
//            if (layerBuffer == null) {
//                return image;
//            }
//            
//            try {
//                String name = "layer_buffer";
//                DecoratedImageDescriptor decoratedImageDescriptor = null;
//                
//                // outgoing
//                if (!layerBuffer.getBuffer().isEmpty()) {
//                    decoratedImageDescriptor = new DecoratedImageDescriptor( image );
//                    decoratedImageDescriptor.addDecoration( outgoing, BOTTOM_RIGHT );
//                    name += "_outgoing";
//                }
//                // XXX add incoming and conflict
//        
//                if (decoratedImageDescriptor != null) {
//                    result = DataPlugin.getDefault().imageForDescriptor( decoratedImageDescriptor, name );
//                }
//            }
//            catch (Exception e) {
//                log.warn( "", e );
//                // XXX add question mark overlay
//            }
//
//            // register listener
//            if (decorated.put( layer.id(), layerBuffer ) == null) {
//                layerBuffer.getBuffer().addFeatureChangeListener( this );
//            }
//        }
//        return result;
//    }
//
//
//    public String decorateText( String text, Object element ) {
//        return text;
//    }


    public void featureChange( final FeatureChangeEvent ev ) {
        Runnable runnable = new Runnable() {
            public void run() {
                fireLabelProviderChanged( new LabelProviderChangedEvent( LayerBufferDecorator.this ) );
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

/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.project.ui.layer;

import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import com.google.common.base.Supplier;
import com.google.common.collect.MapMaker;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerStatus;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.Visible;
import org.polymap.core.runtime.DisplayLazyInit;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class LayerStatusDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator {

    private static Log log = LogFactory.getLog( LayerStatusDecorator.class );
    
    public static final Point       DEFAULT_SIZE = new Point( 17, 16 );

    public static final int         TOP_LEFT = 0;
    public static final int         TOP_RIGHT = 1;
    public static final int         BOTTOM_LEFT = 2;
    public static final int         BOTTOM_RIGHT = 3;

    private static final String     visible = "icons/obj16/layer_visible_obj.gif";
    //private static final String     visible = "icons/elcl16/eye.png";
    private static final String     visible_ovr = "icons/ovr16/visible_ovr.png";    
    private static final String     selectable = "icons/ovr16/selectable_ovr_small.png";
    private static final String     editable = "icons/ovr16/write_ovr.gif";
    private static final String     waiting = "icons/ovr16/clock0_ovr.gif";
    private static final String     baseImage = "icons/obj16/layer_obj.gif";
    
    private static final Lazy<Color> MISSING_COLOR = new DisplayLazyInit( new Supplier<Color>() {
        public Color get() { return Graphics.getColor( 255, 0, 0 ); }
    });
    private static final Lazy<Color> INACTIVE_COLOR = new DisplayLazyInit( new Supplier<Color>() {
        public Color get() { return Graphics.getColor( 0x60, 0x60, 0x60 ); }
    }); 

    // instance *******************************************
    
    public Lazy<Font>               bold = new DisplayLazyInit( new Supplier<Font>() {
        public Font get() { return JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ); } 
    });
    
    public Lazy<Font>               italic = new DisplayLazyInit( new Supplier<Font>() {
        public Font get() { return JFaceResources.getFontRegistry().getItalic( JFaceResources.DEFAULT_FONT ); } 
    });
    
    private Map<String,ILayer>      decorated;


    public LayerStatusDecorator() {
        decorated = new MapMaker().weakValues().initialCapacity( 128 ).makeMap();

        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent ev ) {
                try {
                    return ev.getSource() instanceof ILayer 
                            && decorated.containsKey( ((ILayer)ev.getSource()).id() )
                            && (ev.getPropertyName().equals( Visible.PROP_VISIBLE )
                            || ev.getPropertyName().equals( ILayer.PROP_LAYERSTATUS ) );
                }
                catch (NoSuchEntityException e) {
                    return false;
                }
            }
        });
    }

    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        decorated.clear();
        super.dispose();
    }


    @EventHandler(delay=1000,display=true)
    public void propertyChange( List<PropertyChangeEvent> ev ) {
        if (!PlatformUI.getWorkbench().isClosing()) {
            fireLabelProviderChanged( new LabelProviderChangedEvent( LayerStatusDecorator.this ) );
        }
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
            
            // visible
            if (layer.isVisible()) {
                ImageDescriptor image = ProjectPlugin.getDefault().imageDescriptor( visible );
                decoration.setFont( bold.get() );

                DecorationContext context = (DecorationContext)decoration.getDecorationContext();
                context.putProperty( IDecoration.ENABLE_REPLACE, Boolean.TRUE );
                decoration.addOverlay( image, IDecoration.REPLACE );

                ImageDescriptor ovr = ProjectPlugin.getDefault().imageDescriptor( visible_ovr );
                decoration.addOverlay( ovr, TOP_LEFT );
            }

            // inactive == not visible
            if (!layer.isVisible()) {
                decoration.setForegroundColor( INACTIVE_COLOR.get() );
            }

            LayerStatus layerStatus = layer.getLayerStatus();
            if (layerStatus == LayerStatus.STATUS_MISSING) {
//                decoration.setForegroundColor( MISSING_COLOR );    
                decoration.addSuffix( Messages.get( "LayerStatusDecorator_missing") );    
            }
            else if (layerStatus == LayerStatus.STATUS_WAITING) {
                ImageDescriptor ovr = ProjectPlugin.getDefault().imageDescriptor( waiting );
//                decoration.setFont( italic );
                decoration.addOverlay( ovr, TOP_RIGHT );
                decoration.addSuffix( Messages.get( "LayerStatusDecorator_checking") );
            }
            else if (layerStatus == LayerStatus.STATUS_OK) {
                //
            }
            
            // register listener
            decorated.put( layer.id(), layer );
        }
    }


    public Image decorateImage( Image image, Object elm ) {
        Image result = null;
        
//        if (elm instanceof ILayer) {
//            ILayer layer = (ILayer)elm;
//            DecoratedImageDescriptor decoratedImageDescriptor = new DecoratedImageDescriptor( image );
//            String name = "layer";
//            // editable
//            if (layer.isEditable()) {
//                decoratedImageDescriptor.addDecoration( editable, TOP_RIGHT );
//                name += "_editable";
//            }
//            // visible
//            else if (layer.isVisible()) {
//                decoratedImageDescriptor.addDecoration( visible, TOP_RIGHT );
//                name += "_visible";
//            }
//            // selectable
//            if (layer.isSelectable()) {
//                decoratedImageDescriptor.addDecoration( selectable, TOP_LEFT );
//                name += "_selectable";
//            }
//            if (!name.equals( "layer" )) {
//                result = ProjectPlugin.getDefault().imageForDescriptor( 
//                        decoratedImageDescriptor, name );
//            }
//
//            // register listener
//            if (decorated.put( layer.id(), layer ) == null) {
//                layer.addPropertyChangeListener( this );
//            }
//        }
        return result;
    }


    public String decorateText( String text, Object elm ) {
        if (elm instanceof ILayer) {
            ILayer layer = (ILayer)elm;
            
//            if (layer.isVisible()) {
//                return "* " + text;
//            }
        }
        return text;
    }


//    /*
//     * 
//     */
//    static class DecoratedImageDescriptor
//            extends CompositeImageDescriptor {
//
//        private Image                   baseImage;
//        
//        private List<String>            overlays = new ArrayList();
//        
//        private List<Integer>           quadrants = new ArrayList();
//        
//        
//        public DecoratedImageDescriptor( String baseImageName ) {
//            this.baseImage = ProjectPlugin.getDefault().imageForName( baseImageName );
//        }
//        
//        public DecoratedImageDescriptor( Image image ) {
//            this.baseImage = image;
//        }
//
//        public void addDecoration( String overlayImageName, int quadrant ) {
//            overlays.add( overlayImageName );
//            quadrants.add( quadrant );
//        }
//
//        protected void drawCompositeImage( int width, int height ) {
//            drawImage( baseImage.getImageData(), 0, 0 );
//            
//            for (int i=0; i<overlays.size(); i++) {
//                Image ovrImage = ProjectPlugin.getDefault().imageForName( overlays.get( i ) );
//                switch (quadrants.get( i )) {
//                    case TOP_RIGHT: 
//                        drawImage( ovrImage.getImageData(), 9, 0 ); 
//                        break;
//                    case TOP_LEFT: 
//                        drawImage( ovrImage.getImageData(), 0, 0 ); 
//                        break;
//                    case BOTTOM_LEFT: 
//                        drawImage( ovrImage.getImageData(), 0, 8 ); 
//                        break;
//                    case BOTTOM_RIGHT: 
//                        drawImage( ovrImage.getImageData(), 9, 8 ); 
//                        break;
//                    default:
//                        throw new IllegalArgumentException( "Unknown quadrant: " + quadrants.get( i ) );
//                }
//            }
//        }
//
//        protected Point getSize() {
//            return DEFAULT_SIZE;
//        }
//
//    }

}

/* 
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
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

import java.util.HashMap;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerStatus;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @since 3.1
 */
public class LayerStatusDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator, PropertyChangeListener {

    private static Log log = LogFactory.getLog( LayerStatusDecorator.class );
    
    public static final Point       DEFAULT_SIZE = new Point( 17, 16 );

    public static final int         TOP_LEFT = 0;
    public static final int         TOP_RIGHT = 1;
    public static final int         BOTTOM_LEFT = 2;
    public static final int         BOTTOM_RIGHT = 3;

    private static final String     visible = "icons/ovr16/visible_ovr2.gif";    
    private static final String     selectable = "icons/ovr16/selectable_ovr_small.png";
    private static final String     editable = "icons/ovr16/write_ovr.gif";
    private static final String     waiting = "icons/ovr16/clock0_ovr.gif";
    private static final String     baseImage = "icons/obj16/layer_obj.gif";
    
//    private final Color      MISSING_COLOR = Graphics.getColor( 255, 0, 0 );
//    private final Color      INACTIVE_COLOR = Graphics.getColor( 0x80, 0x80, 0x80 );
//    
//    public final Font        bold = JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT );; 
//    public final Font        italic = JFaceResources.getFontRegistry().getItalic( JFaceResources.DEFAULT_FONT );

    private Map<String,ILayer>      decorated = new HashMap();


    public LayerStatusDecorator() {
//        final Display display = Polymap.getSessionDisplay();
//        display.syncExec( new Runnable() {
//            public void run() {
//                MISSING_COLOR = Graphics.getColor( 255, 0, 0 );
//                INACTIVE_COLOR = Graphics.getColor( 0x80, 0x80, 0x80 );
////                Font systemFont = display.getSystemFont();
////                FontData fd = systemFont.getFontData()[0];
////                bold = Graphics.getFont( fd.getName(), fd.getHeight(), SWT.BOLD );
////                italic = Graphics.getFont( fd.getName(), fd.getHeight(), SWT.ITALIC );
////                //font = new Font( systemFont.getDevice(), fd.getName(), fd.getHeight(), SWT.BOLD );
//            }
//        });
    }

    
    public void dispose() {
        super.dispose();
        for (ILayer layer : decorated.values()) {
            try {
                layer.removePropertyChangeListener( this );
            }
            catch (NoSuchEntityException e) {
            }
        }
        decorated.clear();
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
            
            // editable
            if (layer.isEditable()) {
                ImageDescriptor ovr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, editable );
                decoration.addOverlay( ovr, TOP_RIGHT );
            }
            // visible
            else if (layer.isVisible()) {
                ImageDescriptor ovr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, visible );
//                decoration.setFont( bold );
                decoration.addOverlay( ovr, TOP_RIGHT );
            }
            // selectable
            if (layer.isSelectable()) {
                ImageDescriptor ovr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, selectable );
                decoration.addOverlay( ovr, TOP_LEFT );
            }

            // inactive == not visible
            if (!layer.isVisible()) {
//                decoration.setForegroundColor( INACTIVE_COLOR );
            }

            LayerStatus layerStatus = layer.getLayerStatus();
            if (layerStatus == LayerStatus.STATUS_MISSING) {
//                decoration.setForegroundColor( MISSING_COLOR );    
                decoration.addSuffix( Messages.get( "LayerStatusDecorator_missing") );    
            }
            else if (layerStatus == LayerStatus.STATUS_OK) {
                //
            }
            else if (layerStatus == LayerStatus.STATUS_WAITING) {
                ImageDescriptor ovr = ProjectPlugin.imageDescriptorFromPlugin( ProjectPlugin.PLUGIN_ID, waiting );
//                decoration.setFont( italic );
                decoration.addOverlay( ovr, TOP_RIGHT );
                decoration.addSuffix( Messages.get( "LayerStatusDecorator_checking") );
            }
            
            // register listener
            if (decorated.put( layer.id(), layer ) == null) {
                layer.addPropertyChangeListener( this );
            }
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


//    public void decorate( Object elm, IDecoration decoration ) {
//        if (elm instanceof ILayer) {
//            ILayer layer = (ILayer)elm;
//            // visible
//            if (layer.isVisible()) {
//                decoration.addOverlay( visible, IDecoration.BOTTOM_RIGHT );
//                decoration.addPrefix( "*" );
//            }
//            // register listener
//            if (decorated.put( layer.id(), layer ) == null) {
//                layer.addPropertyChangeListener( this );
//            }
//        }
//    }


    public void propertyChange( PropertyChangeEvent ev ) {
        log.debug( "propertyChange(): " + ev.getSource() + " : " + ev.getPropertyName() );
        if (ev.getSource() instanceof ILayer
                && (ev.getPropertyName().equals( ILayer.PROP_VISIBLE )
                || ev.getPropertyName().equals( ILayer.PROP_SELECTABLE )
                || ev.getPropertyName().equals( ILayer.PROP_EDITABLE )
                || ev.getPropertyName().equals( ILayer.PROP_LAYERSTATUS ))) {

            Runnable runnable = new Runnable() {
                public void run() {
                    fireLabelProviderChanged( new LabelProviderChangedEvent( LayerStatusDecorator.this ) );
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

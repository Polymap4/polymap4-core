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
package org.polymap.rhei.navigator.layer;

import java.util.HashMap;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.polymap.core.project.ILayer;
import org.polymap.rhei.RheiPlugin;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerStatusDecorator
        extends BaseLabelProvider
        implements ILabelDecorator, PropertyChangeListener {

    private static Log log = LogFactory.getLog( LayerStatusDecorator.class );
    
    public static final Point       DEFAULT_SIZE = new Point( 16, 16 );

    private static final ImageDescriptor visible = ImageDescriptor.createFromURL( 
            RheiPlugin.getDefault().getBundle().getResource( "icons/ovr16/eye_ovr.png" ) );
    
    private Map<String,ILayer>      decorated = new HashMap();

    private Font                    bold;

    
    public LayerStatusDecorator() {
//        final Display display = RWTLifeCycle.getSessionDisplay();
//        display.syncExec( new Runnable() {
//            public void run() {
//                Font systemFont = display.getSystemFont();
//                FontData fd = systemFont.getFontData()[0];
//                bold = Graphics.getFont( fd.getName(), fd.getHeight(), SWT.BOLD );
//                //font = new Font( systemFont.getDevice(), fd.getName(), fd.getHeight(), SWT.BOLD );
//            }
//        });
    }

    
    public void dispose() {
        log.info( "dispose(): ..." );
        for (ILayer layer : decorated.values()) {
            layer.removePropertyChangeListener( this );
        }
        decorated.clear();
    }


    public Image decorateImage( Image image, Object elm ) {
        Image result = image;
        if (elm instanceof ILayer) {
            ILayer layer = (ILayer)elm;
            // visible
            if (layer.isVisible()) {
                Image baseImage = RheiPlugin.getDefault().imageForName( "icons/obj16/layer_obj.gif" );
                ImageDescriptor imageDescr = new DecoratedImageDescriptor( baseImage.getImageData() );
                result = RheiPlugin.getDefault().imageForDescriptor( imageDescr, "layer_visible" );
            }
            // register listener
            if (decorated.put( layer.id(), layer ) == null) {
                layer.addPropertyChangeListener( this );
            }
        }
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
        log.info( "propertyChange(): " + ev.getSource() + " : " + ev.getPropertyName() );
        if (ev.getSource() instanceof ILayer
                && ev.getPropertyName().equals( ILayer.PROP_VISIBLE )) {

            fireLabelProviderChanged( new LabelProviderChangedEvent( this ) );
        }
    }


    /*
     * 
     */
    class DecoratedImageDescriptor
            extends CompositeImageDescriptor {

        private ImageData       baseImage;
        
        public DecoratedImageDescriptor( ImageData baseImage ) {
            this.baseImage = baseImage;
        }

        protected void drawCompositeImage( int width, int height ) {
            drawImage( baseImage, 0, 0 );

            Image ovrImage = RheiPlugin.getDefault().imageForName( "icons/ovr16/eye_ovr.png" );
            drawImage( ovrImage.getImageData(), 8, 0 );
        }

        protected Point getSize() {
            return DEFAULT_SIZE;
        }

    }

}

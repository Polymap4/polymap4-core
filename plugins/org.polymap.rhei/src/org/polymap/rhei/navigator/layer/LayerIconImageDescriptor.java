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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.resource.CompositeImageDescriptor;

import org.polymap.core.project.ILayer;

import org.polymap.rhei.RheiPlugin;

/**
 * The label icon used for {@link ILayer} elements in viewers.
 * <p>
 * This could be used to extend the icon depending on layer state. Unfortunatelly
 * returning an image bigger than 16x16 has no effect in the tree control of the
 * navigator. So this just returnes <code>icons/obj16/layer_obj.gif</code> currently.
 * Decorators are used to display layer status.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class LayerIconImageDescriptor
        extends CompositeImageDescriptor {

    private static Log log = LogFactory.getLog( LayerIconImageDescriptor.class );

    public static final Point DEFAULT_SIZE = new Point( 16, 16 );


    public LayerIconImageDescriptor() {
    }

    protected void drawCompositeImage( int width, int height ) {
        Image baseImage = RheiPlugin.getDefault().imageForName( "icons/obj16/layer_obj.gif" );
        drawImage( baseImage.getImageData(), 0, 0 );

        Image ovrImage = RheiPlugin.getDefault().imageForName( "icons/ovr16/eye_ovr.png" );
        drawImage( ovrImage.getImageData(), 0, 0 );
    }

    protected Point getSize() {
        return DEFAULT_SIZE;
    }
    
}

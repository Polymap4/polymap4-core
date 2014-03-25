/*
 * polymap.org
 * Copyright (C) 2009-2014, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.openlayers.rap.widget.layers;

import org.eclipse.swt.graphics.Image;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.Size;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImageLayer extends Layer {

    public ImageLayer( String name, Image image, Bounds bounds ) {
        throw new RuntimeException( "XXX ImageLayer not yet ported to RAP 1.5" );
        //_ImageLayer( name, ResourceFactory.getImagePath( image ), bounds, new Size( image.getBounds() ) );
    }


    public ImageLayer( String name, String url, Bounds bounds, Size size ) {
        _ImageLayer( name, url, bounds, size );
    }


    public void _ImageLayer( String layer_name, String url, Bounds bounds, Size size ) {
        super.setName( layer_name );
        super.create( "new OpenLayers.Layer.Image( '" + getName() + "','" + url + "', " + bounds.getJSObjRef() + ","
                + size.getJSObjRef() + " );" );
    }

}

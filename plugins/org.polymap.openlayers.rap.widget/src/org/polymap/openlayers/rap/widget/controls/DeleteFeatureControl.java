/*
 * polymap.org
 * Copyright 2009-2012, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
package org.polymap.openlayers.rap.widget.controls;

import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.IOUtils;

import org.polymap.openlayers.rap.widget.OpenlayersPlugin;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class DeleteFeatureControl 
        extends Control {

    public static final String          EVENT_FEATURE_DELETED = "featuredeleted";
    
    
	public DeleteFeatureControl(VectorLayer layer) {    
        super.create( "new OpenLayers.Control();" );
        try {
            URL res = OpenlayersPlugin.getDefault().getBundle().getResource( "ol_js_addins/DeleteFeatureControl.js" );
            String js = IOUtils.toString( res.openStream() );
            
            js = StringUtils.replace( js, "$0", getJSObjRef() );
            js = StringUtils.replace( js, "$1", layer.getJSObjRef() );
            js = StringUtils.replace( js, "$2", EVENT_FEATURE_DELETED );
            
            addObjModCode( js );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }

	}

//    public void addMode( int mode ) {
//        super.addObjModCode( "obj.mode |=  OpenLayers.Control.ModifyFeature." + mode2name( mode ) );
//    }
//
//    public void rmMode( int mode ) {
//        super.addObjModCode( "obj.mode &=  ~OpenLayers.Control.ModifyFeature." + mode2name( mode ) );
//    }

}

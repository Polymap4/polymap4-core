/* 
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data;

import java.net.URL;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.geohub.FeatureCollectionFactory;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.project.ILayer;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.osgi.framework.BundleContext;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @since 3.0
 */
public class DataPlugin
        extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.polymap.core.data";

    /**
     * Central filter factory that can and should be used by all
     * code code depending on the data plugin.
     */
    public static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
    
    private static DataPlugin  plugin;


    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
        
        LayerFeatureSelectionManager.setFeatureCollectionFactory(
                new FeatureCollectionFactory() {
                    public FeatureCollection newFeatureCollection( Object layer, Filter filter ) {
                        try {
                            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( (ILayer)layer, false );
                            return fs.getFeatures( filter );
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                });
    }


    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop( context );
    }


    public static DataPlugin getDefault() {
        return plugin;
    }


    public Image imageForDescriptor( ImageDescriptor imageDescriptor, String key ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( key );
        if (image == null || image.isDisposed()) {
            images.put( key, imageDescriptor );
            image = images.get( key );
        }
        return image;
    }

    
    public Image imageForName( String resName ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( resName );
        if (image == null || image.isDisposed()) {
            URL res = getBundle().getResource( resName );
            assert res != null : "Image resource not found: " + resName;
            images.put( resName, ImageDescriptor.createFromURL( res ) );
            image = images.get( resName );
        }
        return image;
    }

}

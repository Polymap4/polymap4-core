/* 
 * polymap.org
 * Copyright (C) 2009-2013, Polymap GmbH. All rights reserved.
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

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.geohub.FeatureCollectionFactory;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.project.ILayer;
import org.polymap.core.ui.ImageRegistryHelper;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.osgi.framework.BundleContext;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
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


    public static DataPlugin getDefault() {
        return plugin;
    }

    // instance *******************************************
    
    private ImageRegistryHelper         images = new ImageRegistryHelper( this );
    

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


    public Image imageForDescriptor( ImageDescriptor descriptor, String key ) {
        return images.image( descriptor, key );
    }

    
    public Image imageForName( String resName ) {
        return images.image( resName );
    }

    
    public ImageDescriptor imageDescriptor( String path ) {
        return images.imageDescriptor( path );
    }

}

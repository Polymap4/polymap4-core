/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.geohub.FeatureCollectionFactory;
import org.polymap.core.geohub.LayerFeatureSelectionManager;
import org.polymap.core.project.ILayer;

import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;
import org.osgi.framework.BundleContext;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DataPlugin
        extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.polymap.core.data";

    // The shared instance
    private static DataPlugin  plugin;


    /**
     * The constructor
     */
    public DataPlugin() {
    }


    public void start( BundleContext context )
            throws Exception {
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


    public void stop( BundleContext context )
            throws Exception {
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

}

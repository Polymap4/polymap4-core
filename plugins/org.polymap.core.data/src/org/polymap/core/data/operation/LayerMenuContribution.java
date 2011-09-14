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
package org.polymap.core.data.operation;

import java.util.Iterator;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerMenuContribution
        extends BaseMenuContribution {

    private static Log log = LogFactory.getLog( LayerMenuContribution.class );


    public IFeatureOperationContext newContext() {
        IStructuredSelection sel = currentSelection();
        if (sel != null && sel.getFirstElement() instanceof ILayer) {
            
            try {
                final ILayer layer = (ILayer)sel.getFirstElement();                
                final Query query = Query.ALL;
                
                // The context
                return new IFeatureOperationContext() {
                    
                    private int                 size = -1;
                    
                    private FeatureSource       fs;
                    
                    private FeatureCollection   fc;
                    
                    /**
                     * Lazily init.
                     */
                    private void checkInit() 
                    throws Exception {
                        if (fs == null) {
                            fs = PipelineFeatureSource.forLayer( layer, false );
                        }
                        if (fc == null) {
                            fc = fs.getFeatures( query );
                        }
                        
                    }
                    public int featuresSize() 
                    throws Exception {
                        checkInit();
                        if (size == -1) {
                            size = fs.getCount( query );
                        }
                        return size;
                    }

                    public Iterator<Feature> features() 
                    throws Exception {
                        checkInit();
                        return fc.iterator();
                    }

                    public Object getAdapter( Class adapter ) {
                        throw new RuntimeException( "not yet implemented." );
                    }
                };
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        return null;
    }
    
}

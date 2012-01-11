/*
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.operations.feature;

import java.util.HashSet;
import java.util.Set;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.Identifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.data.operation.DefaultFeatureOperation;
import org.polymap.core.data.operation.FeatureOperationExtension;
import org.polymap.core.data.operation.IFeatureOperation;
import org.polymap.core.data.operations.NewFeatureOperation;
import org.polymap.core.data.util.ProgressListenerAdaptor;

/**
 * Removed features.
 * 
 * @see NewFeatureOperation
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class RemoveFeaturesOperation
        extends DefaultFeatureOperation
        implements IFeatureOperation {

    private static Log log = LogFactory.getLog( RemoveFeaturesOperation.class );

    public static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );

    
    public Status execute( IProgressMonitor monitor )
    throws Exception {
        monitor.beginTask( context.adapt( FeatureOperationExtension.class ).getLabel(), 10 );
        
        FeatureSource fs = context.featureSource();
        if (!(fs instanceof FeatureStore)) {
            throw new Exception( "Source is not a feature store. Features cannot be removed." );
        }

        IProgressMonitor fidsMonitor = new SubProgressMonitor( monitor, 5, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );
        final Set<Identifier> fids = new HashSet(); 
        context.features().accepts( new FeatureVisitor() {
            public void visit( Feature feature ) {
                fids.add( feature.getIdentifier() );
            }
        }, new ProgressListenerAdaptor( fidsMonitor ) );
        
        Filter filter = ff.id( fids );
        ((FeatureStore)fs).removeFeatures( filter );
        monitor.done();
        
        return Status.OK;
    }

}

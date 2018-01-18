/* 
 * polymap.org
 * Copyright (C) 2009-2018, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data;

import java.util.Collections;
import java.util.List;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import org.polymap.core.data.pipeline.Pipeline;

/**
 * The <code>DataStore</code> of a {@link PipelineFeatureSource}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PipelineDataStore
        extends ContentDataStore
        implements DataStore {

    private Pipeline        pipeline;
    
    
    public PipelineDataStore( Pipeline pipeline ) {
        this.pipeline = pipeline;
    }

    public PipelineFeatureSource getFeatureSource() throws IOException {
        return (PipelineFeatureSource)getFeatureSource( getTypeNames()[0] );
    }
    
    @Override
    protected List<Name> createTypeNames() throws IOException {
        Name name = new NameImpl( pipeline.dataSourceDescription().resourceName.get() );
        return Collections.singletonList( name );
    }

    @Override
    protected PipelineFeatureSource createFeatureSource( ContentEntry entry ) throws IOException {
        return new PipelineFeatureSource( entry, pipeline );
    }

}

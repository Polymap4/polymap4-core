/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.feature;

import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.Produces;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class DefaultFeaturesProcessor
        implements FeaturesProducer {

    @Override
    @Produces( TransactionRequest.class )
    public void setTransactionRequest( TransactionRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Override
    @Produces( ModifyFeaturesRequest.class )
    public void modifyFeaturesRequest( ModifyFeaturesRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Override
    @Produces( RemoveFeaturesRequest.class )
    public void removeFeaturesRequest( RemoveFeaturesRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Override
    @Produces( AddFeaturesRequest.class )
    public void addFeaturesRequest( AddFeaturesRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Override
    @Produces( GetFeaturesRequest.class )
    public void getFeatureTypeRequest( GetFeatureTypeRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Override
    @Produces( GetFeaturesSizeRequest.class )
    public void getFeatureSizeRequest( GetFeaturesSizeRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Override
    @Produces( GetBoundsRequest.class )
    public void getFeatureBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }

    @Override
    @Produces( GetFeaturesRequest.class )
    public void getFeatureRequest( GetFeaturesRequest request, ProcessorContext context ) throws Exception {
        context.sendRequest( request );
    }
    
}

/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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

import org.geotools.data.FeatureStore;

import org.polymap.core.data.pipeline.EndOfProcessing;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.Produces;

/**
 * Defines the transactional WFS ({@link FeatureStore}) pipeline usecase.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface FeaturesProducer
        extends PipelineProcessor {

    @Produces(TransactionResponse.class)
    public void setTransactionRequest( TransactionRequest request, ProcessorContext context ) throws Exception;
    
    @Produces(ModifyFeaturesResponse.class)
    public void modifyFeaturesRequest( ModifyFeaturesRequest request, ProcessorContext context ) throws Exception;
    
    @Produces(ModifyFeaturesResponse.class)
    public void removeFeaturesRequest( RemoveFeaturesRequest request, ProcessorContext context ) throws Exception;
    
    @Produces(ModifyFeaturesResponse.class)
    public void addFeaturesRequest( AddFeaturesRequest request, ProcessorContext context ) throws Exception;
    
    @Produces(GetFeatureTypeResponse.class)
    public void getFeatureTypeRequest( GetFeatureTypeRequest request, ProcessorContext context ) throws Exception;
    
    @Produces(GetFeaturesSizeResponse.class)
    public void getFeatureSizeRequest( GetFeaturesSizeRequest request, ProcessorContext context ) throws Exception;
    
    @Produces(GetBoundsResponse.class)
    public void getFeatureBoundsRequest( GetBoundsRequest request, ProcessorContext context ) throws Exception;
    
    @Produces({GetFeaturesResponse.class, EndOfProcessing.class})
    public void getFeatureRequest( GetFeaturesRequest request, ProcessorContext context ) throws Exception;
    
}

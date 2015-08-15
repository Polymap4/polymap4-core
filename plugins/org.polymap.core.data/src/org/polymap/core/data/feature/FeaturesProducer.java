/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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

import org.polymap.core.data.pipeline.EndOfProcessing;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.Produces;

/**
 * Defines the WFS non-transactionel pipeline usecase.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface FeaturesProducer
        extends PipelineProcessor {

//  new Class[] {},
//  new Class[] {ModifyFeaturesRequest.class, RemoveFeaturesRequest.class, AddFeaturesRequest.class, GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
//  new Class[] {ModifyFeaturesResponse.class, GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class},
//  new Class[] {} );

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
    
    @Produces({GetFeaturesResponse.class, EndOfProcessing.class})
    public void getFeatureRequest( GetFeaturesRequest request, ProcessorContext context ) throws Exception;
    
}

/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.serialize.sld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.PointStyle;

/**
 * Serialize {@link PointStyle}.
 *
 * @author Falko Bräutigam
 */
public class StrokeSerializer {

    private static Log log = LogFactory.getLog( StrokeSerializer.class );


    // ich hab noch keine ahnung, wie der StrokeSerializer rausfactoriert werden muss
    
    
//    @Override
//    protected PointSymbolizerDescriptor createDescriptor() {
//        return new PointSymbolizerDescriptor();
//    }
//
//
//    @Override
//    public void doSerialize( PointStyle style ) {
//        setValue( style.strokeWidth.get(), (PointSymbolizerDescriptor sd, Double value) -> sd.strokeWidth.set( value ) );
//        setValue( style.strokeOpacity.get(), (PointSymbolizerDescriptor sd, Double value) -> sd.strokeOpacity.set( value ) );
//        setValue( style.strokeColor.get(), (PointSymbolizerDescriptor sd, Color value) -> sd.strokeColor.set( value ) );
//        setValue( style.fillColor.get(), (PointSymbolizerDescriptor sd, Color value) -> sd.fillColor.set( value ) );
//        setValue( style.fillOpacity.get(), (PointSymbolizerDescriptor sd, Double value) -> sd.fillOpacity.set( value ) );
//    }
//    
//    protected Stroke buildStroke( Config<StrokeDescriptor> stroke ) {
//        // ich bin mir nicht sicher ob mir so eine schreibweise wirklich gefällt
//        return stroke.map( s -> 
//                sf.createStroke( ff.literal( s.color.get() ), ff.literal( s.width.get() ), ff.literal( s.opacity.get() ) ) )
//                .orElse( sf.getDefaultStroke() );
//    }

}

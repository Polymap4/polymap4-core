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
package org.polymap.core.data.process;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Label;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Range;
import oms3.annotations.UI;

/**
 * 
 *
 * @author Falko Bräutigam
 */
@Label("Area of interest")
@Name("Area of interest")
public class OmsAreaOfInterestGridCoverage
        extends JGTModel {

    private static final Log log = LogFactory.getLog( OmsAreaOfInterestGridCoverage.class );
    
    @In
    @Description("The source raster data")
    public GridCoverage2D       inCoverage;
    
    @In
    @Description("The source raster data")
    @UI(JGTConstants.EASTING_UI_HINT)
    public ReferencedEnvelope   aoi;
    
    @In
    @Description("The source raster data")
    @Range(min=1, max=100)
    public int                  resolutionFactor = 1;
    
    @Out
    public GridCoverage2D       outCoverage;
    
    @Execute
    public void process() {
        throw new RuntimeException( "Andrea will do!" );    
    }
    
}

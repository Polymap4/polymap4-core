/*
 * polymap.org Copyright (C) 2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.data.process;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.processing.Operations;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.libs.modules.JGTProcessingRegion;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Label;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Range;


/**
 * 
 *
 * @author Falko Bräutigam
 */
@Description("Module that extracts a gridcoverage for a given area of interest, resampling it if necessary.")
@Label("Area of interest")
@Name("Area of interest")
public class OmsAreaOfInterestGridCoverage extends JGTModel {

    private static final Log log = LogFactory.getLog(OmsAreaOfInterestGridCoverage.class);

    @In
    @Description("The source raster data")
    public GridCoverage2DReader inCoverageReader;

    @In
    @Description("The source raster data")
    public ReferencedEnvelope aoi;

    @In
    @Description("The source raster data")
    @Range(min = 1, max = 100)
    public int resolutionFactor = 1;

    @Out
    public GridCoverage2D outCoverage;

    @Execute
    public void process() throws Exception {

        CoordinateReferenceSystem crs = inCoverageReader.getCoordinateReferenceSystem();
        
        aoi = aoi.transform( crs, true );
        
        GeneralEnvelope originalEnvelope = inCoverageReader.getOriginalEnvelope();
        GridEnvelope2D overviewGridEnvelope = (GridEnvelope2D) inCoverageReader.getOriginalGridRange();

        double[] llCorner = originalEnvelope.getLowerCorner().getCoordinate();
        double[] urCorner = originalEnvelope.getUpperCorner().getCoordinate();

        JGTProcessingRegion originalRegion = new JGTProcessingRegion(llCorner[0], urCorner[0], llCorner[1], urCorner[1],
                overviewGridEnvelope.height, overviewGridEnvelope.width);

        RegionMap originalRegionMap = CoverageUtilities.makeRegionParamsMap(urCorner[1], llCorner[1], llCorner[0], urCorner[0],
                originalRegion.getWEResolution(), originalRegion.getNSResolution(), overviewGridEnvelope.width,
                overviewGridEnvelope.height);

        RegionMap subRegion = originalRegionMap.toSubRegion(aoi.getMaxY(), aoi.getMinY(), aoi.getMinX(), aoi.getMaxX());

        double xres = subRegion.getXres();
        double yres = subRegion.getYres();

        if (resolutionFactor > 1) {
            xres = xres * resolutionFactor;
            yres = yres * resolutionFactor;
        }

        outCoverage = CoverageUtilities.getGridCoverage((AbstractGridCoverage2DReader) inCoverageReader, subRegion.getNorth(),
                subRegion.getSouth(), subRegion.getEast(), subRegion.getWest(), xres, yres,
                crs);

        ReferencedEnvelope outenvelope = new ReferencedEnvelope(subRegion.getWest(), subRegion.getEast(), subRegion.getSouth(),
                subRegion.getNorth(), crs);
        outCoverage = (GridCoverage2D) Operations.DEFAULT.crop(outCoverage, outenvelope);

    }

}

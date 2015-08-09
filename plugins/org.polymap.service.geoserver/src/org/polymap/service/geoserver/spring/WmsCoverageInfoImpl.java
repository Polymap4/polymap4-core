///* 
// * polymap.org
// * Copyright 2010, Polymap GmbH, and individual contributors as indicated
// * by the @authors tag.
// *
// * This is free software; you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as
// * published by the Free Software Foundation; either version 2.1 of
// * the License, or (at your option) any later version.
// *
// * This software is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this software; if not, write to the Free
// * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
// * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
// *
// * $Id$
// */
//
//package org.polymap.service.geoserver.spring;
//
//import java.io.IOException;
//
//import org.opengis.coverage.grid.Format;
//import org.opengis.coverage.grid.GridCoverageReader;
//import org.opengis.parameter.GeneralParameterValue;
//import org.opengis.util.ProgressListener;
//
//import org.geotools.coverage.grid.GridCoverage2D;
//import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
//import org.geotools.factory.Hints;
//
//import org.geoserver.catalog.Catalog;
//import org.geoserver.catalog.CoverageInfo;
//import org.geoserver.catalog.impl.CoverageInfoImpl;
//
//
///**
// * 
// *
// * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
// * @version POLYMAP3 ($Revision$)
// * @since 3.0
// */
//@SuppressWarnings("deprecation")
//public class WmsCoverageInfoImpl
//        extends CoverageInfoImpl
//        implements CoverageInfo {
//
//    
//    public WmsCoverageInfoImpl( Catalog catalog ) {
//        super( catalog );
//    }
//
//    public GridCoverageReader getGridCoverageReader( ProgressListener listener, Hints hints )
//            throws IOException {
//        return new WmsGridCoverageReader();
//    }
//
////    public <T> T getAdapter( Class<T> adapterClass, Map<?, ?> hints ) {
////        // XXX Auto-generated method stub
////        throw new RuntimeException( "not yet implemented." );
////    }
//
//
//    /**
//     * 
//     */
//    class WmsGridCoverageReader
//            extends AbstractGridCoverage2DReader {
//
//        public GridCoverage2D read( GeneralParameterValue[] _parameters )
//                throws IllegalArgumentException, IOException {
//            // XXX Auto-generated method stub
//            throw new RuntimeException( "not yet implemented." );
//        }
//
//        public Format getFormat() {
//            // XXX Auto-generated method stub
//            throw new RuntimeException( "not yet implemented." );
//        }
//        
//    }
//    
//}

///* 
// * polymap.org
// * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
//package org.polymap.service.geoserver.spring;
//
//import java.util.Map;
//
//import java.io.IOException;
//import java.io.Serializable;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import org.geotools.data.AbstractDataStoreFactory;
//import org.geotools.data.DataStore;
//import org.geotools.data.DataStoreFactorySpi;
//
//import org.polymap.core.data.PipelineFeatureSource;
//import org.polymap.core.data.pipeline.PipelineIncubationException;
//import org.polymap.core.project.ILayer;
//
///**
// * This DataStore factory bridges the gab between GeoServer/GeoTools data access
// * and POLYMAP {@link PipelineFeatureSource}s. It allows to add FeatureTypes to
// * catalog of GeoServer, which then are bound to POLYMAP's data.
// * <p>
// * This factory is registered by {@link PipelineDataStoreFactoryInitializer}.
// * 
// * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
// * @version POLYMAP3 ($Revision$)
// * @since 3.0
// */
//public class PipelineDataStoreFactory
//        extends AbstractDataStoreFactory
//        implements DataStoreFactorySpi {
//
//    private static final Log log = LogFactory.getLog( PipelineDataStoreFactory.class );
//
//    public static final Param PARAM_LAYER = new Param(
//            "PipelineDataStoreFactory:ILayer", ILayer.class,
//            "The ILayer to provide the data from." );
//    
//    
//    public DataStore createDataStore( Map<String, Serializable> params )
//            throws IOException {
//        try {
//            PipelineFeatureSource result = PipelineFeatureSource.forLayer( 
//                    (ILayer)params.get( PARAM_LAYER.key ), false );
//            return result.getDataStore();
//        }
//        catch (PipelineIncubationException e) {
//            throw new IOException( e );
//        }
//    } 
//
//    public DataStore createNewDataStore( Map<String, Serializable> params )
//            throws IOException {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }
//
//    public String getDescription() {
//        return "The PipelineDataStore provides the data of an IMap. This connection provides access to the Features published through the layers of the IMap.";
//    }
//
//    public Param[] getParametersInfo() {
//        log.debug( "getParameterInfo(): ..." );
//
//        return new Param[] { PARAM_LAYER };
//    }
//
//}

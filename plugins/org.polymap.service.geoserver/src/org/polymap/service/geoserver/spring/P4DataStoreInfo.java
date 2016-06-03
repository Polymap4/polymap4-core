/* 
 * polymap.org
 * Copyright (C) 2010-2016, Polymap GmbH. All rights reserved.
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
package org.polymap.service.geoserver.spring;

import static org.polymap.service.geoserver.spring.Utils.simpleName;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.Serializable;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Polygon;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;

import org.polymap.service.geoserver.GeoServerServlet;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class P4DataStoreInfo
        extends DataStoreInfoImpl
        implements DataStoreInfo {

    private static final Log log = LogFactory.getLog( P4DataStoreInfo.class );

    private ILayer                      layer;
    
    private Lazy<FeatureSource>         fs = new PlainLazyInit( () -> createFeatureSource() );
    
    
    protected P4DataStoreInfo( Catalog catalog, ILayer layer ) {
        super( catalog );
        assert layer != null;
        this.layer = layer;

        setId( (String)layer.id() );
        setName( layer.label.get() );
        setDescription( "DataStore of ILayer: " + layer.label.get() );
        setType( "PipelineDataStore" );
        Map<String,Serializable> params = new HashMap<String,Serializable>();
        // FIXME params.put( PipelineDataStoreFactory.PARAM_LAYER.key, layer );
        setConnectionParameters( params );
        setEnabled( true );
        log.debug( "    loaded: " + this );
    }

    
    public ILayer getLayer() {
        return layer;
    }


    @Override
    public DataAccess<? extends FeatureType, ? extends Feature> getDataStore( ProgressListener listener )
            throws IOException {
        return fs.get().getDataStore();
    }


    /**
     * {@link PipelineFeatureSource} if this is a {@link Feature} layer, or
     * {@link ContentFeatureSource} and {@link MemoryDataStore} if layer is WMS.
     */
    public FeatureSource getFeatureSource() {
        return fs.get();
    }
    
    
    protected FeatureSource createFeatureSource() {
        try {
            // feature resource
            GeoServerServlet server = GeoServerServlet.instance.get();
            Pipeline pipeline = server.getOrCreatePipeline( layer, FeaturesProducer.class );
            PipelineFeatureSource result = new PipelineFeatureSource( pipeline );
            if (result == null || result.getPipeline().length() == 0) {
                throw new PipelineIncubationException( "WMS layer? : " + layer.label.get() );
            }

//            // set name/namespace for target schema
//            Name name = new NameImpl( NAMESPACE, simpleName( layer.getLabel() ) );
//            fs.getPipeline().addFirst( new FeatureRenameProcessor( name ) );
            
            return result;
        }
        // 
        catch (PipelineIncubationException e) {
            // WMS
            // XXX howto skip layer in case of WFS!?
            return wrapCoverageLayer();
        }
        // no geores found or something
        catch (Exception e) {
            log.error( "Error while creating catalog: " + e.getLocalizedMessage() );
            throw new RuntimeException( e );
        }
    }

    
    /**
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
     * (temporary).
     *
     * @param reader the grid coverage reader.
     * @return a feature with the grid coverage envelope as the geometry and the grid
     *         coverage itself in the "grid" attribute.
     */
    protected ContentFeatureSource wrapCoverageLayer() {
        // createSurroundingPolygon();

        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName( simpleName( layer.label.get() ) );
        ftb.setNamespaceURI( Utils.NAMESPACE );
        // required to have schema.getGeometryDescriptor() not 
        // return null in org.geotools.renderer.lite.StreamingRenderer.processStylers()
        // polygonProperty requires CoordinateReferenceSystem
        ftb.setCRS( Geometries.WGS84.get() );
        ftb.add( "geom", Polygon.class );
        ftb.add( "params", GeneralParameterValue[].class );
        final SimpleFeatureType schema = ftb.buildFeatureType();

        // create the feature
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( schema );
        fb.set( "geom", null );
        SimpleFeature feature = fb.buildFeature( null );
        MemoryDataStore ds = new MemoryDataStore( new SimpleFeature[] {feature} );
        try {
            return ds.getFeatureSource( ftb.getName() );
        }
        catch (IOException e) {
            throw new RuntimeException( "Does never happen: ", e );
        }
    }

    
//  private void createSurroundingPolygon() {
    // final PrecisionModel pm = new PrecisionModel();
    // final GeometryFactory gf = new GeometryFactory(pm, 0);
    // final Rectangle2D rect = gridCoverageReader.getOriginalEnvelope()
    // .toRectangle2D();
    // final CoordinateReferenceSystem sourceCrs = CRS
    // .getHorizontalCRS(gridCoverageReader.getCrs());
    // if(sourceCrs==null)
    // throw new UnsupportedOperationException(
    // Errors.format(
    // ErrorKeys.CANT_SEPARATE_CRS_$1,gridCoverageReader.getCrs()));
    //
    //
    // final Coordinate[] coord = new Coordinate[5];
    // coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
    // coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
    // coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
    // coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
    // coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
    //
    // // }
    // final LinearRing ring = gf.createLinearRing(coord);
    // final Polygon bounds = new Polygon(ring, null, gf);  
//}

}

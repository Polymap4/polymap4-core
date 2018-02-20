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

import static org.polymap.core.data.util.Geometries.WGS84;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.impl.AttributeTypeInfoImpl;
import org.geoserver.catalog.impl.FeatureTypeInfoImpl;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureTypes;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;

import org.polymap.service.geoserver.GeoServerUtils;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class P4FeatureTypeInfo
        extends FeatureTypeInfoImpl
        implements FeatureTypeInfo {

    private static final Log log = LogFactory.getLog( P4FeatureTypeInfo.class );

    protected P4DataStoreInfo           dsInfo;
    
    private FeatureSource               fs;

    private SimpleFeatureType           schema;
    

    protected P4FeatureTypeInfo( Catalog catalog, P4DataStoreInfo dsInfo ) {
        super( catalog, dsInfo.getLayer().id() );
        this.dsInfo = dsInfo;
        setStore( dsInfo );
//        setEnabled( true );
//        setAdvertised( true );

        ILayer layer = dsInfo.getLayer();

        fs = dsInfo.getFeatureSource();
        schema = (SimpleFeatureType)fs.getSchema();
        
        setNamespace( GeoServerUtils.defaultNsInfo.get() );
        setName( GeoServerUtils.simpleName( layer.label.get() ) );
        setNativeName( fs.getName().getLocalPart() );  // GeoServerUtils.simpleName( layer.label.get() ) );
        setTitle( layer.label.get() );
        // description and stuff is set in P4LayerInfo

        // bbox
        try {
            if (schema.getGeometryDescriptor() != null) {
                CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
                ReferencedEnvelope bbox = fs.getBounds();
                setSRS( CRS.toSRS( crs ) );
                setNativeCRS( crs );
                setNativeBoundingBox( bbox );
                setProjectionPolicy( ProjectionPolicy.NONE );
                ReferencedEnvelope latlong = bbox.transform( WGS84.get(), true );
                setLatLonBoundingBox( latlong );
            }
        }
        catch (Exception e) {
            log.warn( "", e );
            setLatLonBoundingBox( new ReferencedEnvelope( WGS84.get() ) );
        }
        
        List<AttributeTypeInfo> attributeInfos = new ArrayList();
        for (AttributeDescriptor attribute : schema.getAttributeDescriptors()) {
            AttributeTypeInfoImpl attributeInfo = new AttributeTypeInfoImpl();
            attributeInfos.add( attributeInfo );
            
            // XXX hashCode() of AttributeTypeInfoImpl and FeatureTypeInfoImpl
            // are referencing each other, yes! With this set calling hashCode()
            // results in an StackOverflow
            //attributeInfo.setFeatureType( P4FeatureTypeInfo.this );
            
            attributeInfo.setName( attribute.getName().getLocalPart() );
            attributeInfo.setMinOccurs( attribute.getMinOccurs());
            attributeInfo.setMaxOccurs( attribute.getMaxOccurs());
            attributeInfo.setNillable( attribute.isNillable());
            attributeInfo.setBinding( attribute.getType().getBinding());
            int length = FeatureTypes.getFieldLength( attribute );
            if (length > 0) {
                attributeInfo.setLength( length );
            }
            attributeInfo.setId( "id-" + attribute.hashCode() );
        }
        setAttributes( attributeInfos );
        log.info( "FeatureType: " + this );
    }

    
    @Override
    public List<AttributeTypeInfo> attributes() throws IOException {
        return getAttributes();
    }


    protected DataStore ds( ProgressListener monitor ) throws IOException {
        return (DataStore)dsInfo.getDataStore( monitor );
    }
    
    
    public P4DataStoreInfo getDsInfo() {
        return dsInfo;
    }


    @Override
    public FeatureSource<? extends FeatureType, ? extends Feature> getFeatureSource(
            ProgressListener monitor, Hints hints )
            throws IOException {
        return ds( monitor ).getFeatureSource( getNativeName() );
    }

    
    @Override
    public FeatureType getFeatureType() throws IOException {
        return ds( new NullProgressListener() ).getSchema( getNativeName() );
    }
    
}

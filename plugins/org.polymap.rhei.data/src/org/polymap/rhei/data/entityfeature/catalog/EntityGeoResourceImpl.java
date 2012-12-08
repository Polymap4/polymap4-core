/*
 * polymap.org Copyright 2010, Polymap GmbH, and individual contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * $Id: $
 */
package org.polymap.rhei.data.entityfeature.catalog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ITransientResolve;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.data.entityfeature.EntityProvider;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public class EntityGeoResourceImpl
        extends IGeoResource
        implements ITransientResolve {

    private static final Log log = LogFactory.getLog( EntityGeoResourceImpl.class );

    private EntityProvider      provider;

    private volatile Status     status;

    private volatile Throwable  message;


    public EntityGeoResourceImpl( EntityServiceImpl service, EntityProvider provider ) {
        this.service = service;
        this.provider = provider;
    }


    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor )
    throws IOException {
        if (adaptee == null) {
            return null;
        }
        if (adaptee.isAssignableFrom( ITransientResolve.class )) {
            return adaptee.cast( this );
        }
        if (adaptee.isAssignableFrom( IService.class )) {
            return adaptee.cast( service );
        }
        if (adaptee.isAssignableFrom( IGeoResource.class )) {
            return adaptee.cast( this );
        }
        if (adaptee.isAssignableFrom( IGeoResourceInfo.class )) {
            return adaptee.cast( createInfo( monitor ) );
        }
//        if (adaptee.isAssignableFrom( FeatureStore.class )) {
//            return adaptee.cast( service.getDS().getFeatureSource( type ) );
//        }
//        if (adaptee.isAssignableFrom( FeatureSource.class )) {
//            return adaptee.cast( service.getDS().getFeatureSource( type ) );
//        }
        if (adaptee.isAssignableFrom( SimpleFeatureType.class )) {
            throw new RuntimeException( "not implemented, see source for more information." );
//            return adaptee.cast( provider.getSchema() );
        }
        if (adaptee.isAssignableFrom( EntityProvider.class )) {
            return adaptee.cast( provider );
        }

        return super.resolve( adaptee, monitor );
    }


    public <T> boolean canResolve( Class<T> adaptee ) {
        if (adaptee == null) {
            return false;
        }
        return adaptee.isAssignableFrom( IGeoResourceInfo.class )
//                || adaptee.isAssignableFrom( FeatureStore.class )
//                || adaptee.isAssignableFrom( FeatureSource.class )
                || adaptee.isAssignableFrom( IService.class )
                || adaptee.isAssignableFrom( ITransientResolve.class )
                || adaptee.isAssignableFrom( SimpleFeatureType.class )
                || adaptee.isAssignableFrom( EntityProvider.class )
                || super.canResolve( adaptee );
    }


    public Status getStatus() {
        if (status == null) {
            return service.getStatus();
        }
        return status;
    }


    public Throwable getMessage() {
        if (message == null) {
            return service.getMessage();
        }
        return message;
    }


    public URL getIdentifier() {
        try {
            return new URL( service.getIdentifier().toString() + "#" + provider.getEntityType().getName() ); //$NON-NLS-1$
        }
        catch (MalformedURLException e) {
            return service.getIdentifier();
        }
    }


    protected IGeoResourceInfo createInfo( IProgressMonitor monitor )
            throws IOException {
        return new EntityResourceInfo();
    }


    /**
     * 
     */
    class EntityResourceInfo
            extends IGeoResourceInfo {

        EntityResourceInfo()
        throws IOException {
//            try {
//                schema = (SimpleFeatureType)provider.getSchema();
//            }
//            catch (Exception e) {
//                log.info( e.getMessage(), e );
//                status = Status.BROKEN;
//                message = new Exception( "Error obtaining the feature type: " + provider.getClass().getName() ).initCause( e ); //$NON-NLS-1$
//                bounds = new ReferencedEnvelope( new Envelope(), getCRS() );
//            }
            
//            keywords = new String[] { type, ft.getName().getNamespaceURI() };
        }


        public CoordinateReferenceSystem getCRS() {
            return provider.getCoordinateReferenceSystem( provider.getDefaultGeometry() );
        }


        public String getName() {
            return provider.getEntityName().getLocalPart();
//            return schema.getName().getLocalPart();
        }


        public URI getSchema() {
            try {
                return new URI( provider.getEntityName().getNamespaceURI() );
            }
            catch (URISyntaxException e) {
                return null;
            }
        }


        public String getTitle() {
            return provider.getEntityName().getLocalPart();
        }


        public ReferencedEnvelope getBounds() {
            return provider.getBounds();
                
//            Envelope bounds;
//            try {
//                bounds = source.getBounds();
//                if (bounds == null) {
//                    return new ReferencedEnvelope( new Envelope(), DefaultGeographicCRS.WGS84 );
//                }
//                if (bounds instanceof ReferencedEnvelope) {
//                    return (ReferencedEnvelope)bounds;
//                }
//                return new ReferencedEnvelope( bounds, getCRS() );
//            }
//            catch (IOException e) {
//                return new ReferencedEnvelope( new Envelope(), DefaultGeographicCRS.WGS84 );
//            }
        }

    }
}
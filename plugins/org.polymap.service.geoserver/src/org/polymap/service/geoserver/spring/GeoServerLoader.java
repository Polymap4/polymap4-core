/* 
 * polymap.org
 * Copyright (C) 2009-2015-2018, Falko Bräutigam. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.service.geoserver.spring;

import static org.polymap.service.geoserver.GeoServerUtils.simpleName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.Keyword;
import org.geoserver.catalog.PublishedType;
import org.geoserver.catalog.Wrapper;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInitializer;
import org.geoserver.config.GeoServerReinitializer;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.impl.GeoServerInfoImpl;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.wfs.GMLInfo;
import org.geoserver.wfs.GMLInfo.SrsNameStyle;
import org.geoserver.wfs.GMLInfoImpl;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.WFSInfo.ServiceLevel;
import org.geoserver.wfs.WFSInfoImpl;
import org.geoserver.wms.WMSInfoImpl;
import org.geotools.util.Version;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

import org.polymap.service.geoserver.GeoServerPlugin;
import org.polymap.service.geoserver.GeoServerServlet;
import org.polymap.service.geoserver.GeoServerUtils;

/**
 * Initializes GeoServer configuration and catalog on startup.
 * <p>
 * This class is registered in a spring context and post processes the singleton
 * beans {@link Catalog} and {@link GeoServer}, populating them with data from an
 * {@link IMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings( "deprecation" )
public class GeoServerLoader
        implements BeanPostProcessor, DisposableBean, ApplicationContextAware {

    private static final Log log = LogFactory.getLog( GeoServerLoader.class );

    private GeoServerResourceLoader resourceLoader;

    private GeoServer               geoserver;


    public GeoServerLoader( GeoServerResourceLoader resourceLoader ) {
        this.resourceLoader = resourceLoader;
    }


    public void destroy() throws Exception {
        if (geoserver != null) {
            geoserver.dispose();
            geoserver = null;
        }
    }


    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        System.out.println( "Test" );
        // there are dependencies in GeoServer that crash without it
        // GeoServerDataDirectory from
        // org.vfny.geoserver.global.GeoserverDataDirectory (GeoTools)
        // org.geoserver.config.GeoServerDataDirectory (GeoServer)
        // GeoServerDataDirectory.init(
        // (WebApplicationContext)applicationContext );
    }


    @Override
    public Object postProcessAfterInitialization( Object bean, String beanName ) throws BeansException {
        return bean;
    }


    @Override
    public final Object postProcessBeforeInitialization( Object bean, String beanName ) throws BeansException {
        // find our GeoServerWms / IMap
        GeoServerServlet service = GeoServerServlet.instance.get();
        assert service != null : "No GeoServerServlet registered for this thread!";

        try {
            // Catalog
            if (bean instanceof Catalog) {
                if (bean instanceof Wrapper && ((Wrapper)bean).isWrapperFor( Catalog.class )) {
                    return bean;
                }
                loadCatalog( (Catalog)bean, service );
            }
            // GeoServer
            else if (bean instanceof GeoServer) {
                geoserver = (GeoServer)bean;
                loadGeoServer( service );
                loadInitializers( geoserver );
            }
        }
        catch (BeansException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        return bean;
    }


    protected <R> R set( Supplier<R> supplier, Consumer<R> setter ) {
        R result = supplier.get();
        setter.accept( result );
        return result;
    }
    
    
    protected void loadCatalog( Catalog catalog, GeoServerServlet service ) throws Exception {
        log.info( "Loading catalog..." );

        IMap map = service.map;

        // workspace
        WorkspaceInfoImpl wsInfo = new WorkspaceInfoImpl();
        wsInfo.setId( simpleName( map.id() ) + "-ws" );
        wsInfo.setDefault( true );
        wsInfo.setName( "the-one-and-only" ); //simpleName( GeoServerPlugin.instance().baseName.orElse( map.label.get() ) ) );
        //catalog.add( MapInfo );
        log.info( "Workspace: " + wsInfo );
        
        catalog.add( GeoServerUtils.defaultNsInfo.get() );
        log.info( "Namespace: " + GeoServerUtils.defaultNsInfo.get() );

        for (ILayer layer : map.layers) {
            try {
                // Features
                P4DataStoreInfo dsInfo = P4DataStoreInfo.canHandle( catalog, layer );
                if (dsInfo != null) {
                    dsInfo.setWorkspace( wsInfo );

                    if (dsInfo.getFeatureSource().getSchema().getGeometryDescriptor()!= null) {
                        P4FeatureTypeInfo ftInfo = new P4FeatureTypeInfo( catalog, dsInfo );
                        catalog.add( ftInfo );
                    
                        P4LayerInfo layerInfo = new P4LayerInfo( catalog, layer, ftInfo, PublishedType.VECTOR );
                        layerInfo.createFeatureStyleInfo( service.createSLD( layer ), resourceLoader );
                        catalog.add( layerInfo );
                    }
                    continue;
                }
                // WMS
                P4ImageStoreInfo imInfo = P4ImageStoreInfo.canHandle( catalog, layer );
                if (imInfo != null) {
                    imInfo.setWorkspace( wsInfo );
                    P4ImageResourceInfo resInfo = new P4ImageResourceInfo( catalog, imInfo );
                    catalog.add( resInfo );

                    P4LayerInfo layerInfo = new P4LayerInfo( catalog, layer, resInfo, PublishedType.WMS );
                    layerInfo.createFeatureStyleInfo( service.createSLD( layer ), resourceLoader );
                    catalog.add( layerInfo );
                    continue;
                }
                // XXX GridCoverage is not handled yet
            }
            catch (Exception e) {
                // don't break entire GeoServer if upstream WMS/WFS or else fails
                log.warn( "Error loading layer: " + layer, e );
            }
        }
        catalog.add( wsInfo );
    }


    protected void loadGeoServer( GeoServerServlet service ) {
        IMap map = service.map;

        log.info( "Loading GeoServer..." );
        GeoServerInfoImpl gsInfo = new GeoServerInfoImpl( geoserver );
        gsInfo.setTitle( "GeoServer powered by mapzone.io" );
        gsInfo.setId( simpleName( map.id() ) + "-gs" );
        // XXX alias is added by ArenaConfig when running in mapzone (see comment there)
        String proxyBaseUrl = GeoServerPlugin.instance().baseUrl.map( s -> 
                !s.contains( service.alias ) ? s+service.alias : s ).get();
        gsInfo.setProxyBaseUrl( proxyBaseUrl );
        log.info( "Proxy base URL: " + gsInfo.getProxyBaseUrl() );

        gsInfo.setVerbose( true );
        gsInfo.setVerboseExceptions( true );
        geoserver.setGlobal( gsInfo );
        log.info( "GeoServer: " + gsInfo );

        createWMSInfo( map );
        createWFSInfo( map );
    }

    
    protected void loadInitializers(GeoServer geoServer) throws Exception {
        List<GeoServerInitializer> initializers = GeoServerExtensions.extensions( GeoServerInitializer.class );
        for (GeoServerInitializer initer : initializers) {
            try {
                initer.initialize( geoServer );
            }
            catch( Throwable t ) {
                log.warn( "Failed to run initializer " + initer, t );
            }
        }
    }
    
    protected void reloadInitializers(GeoServer geoServer) throws Exception {
        List<GeoServerReinitializer> initializers = GeoServerExtensions.extensions( GeoServerReinitializer.class );
        for (GeoServerReinitializer initer : initializers) {
            try {
                initer.reinitialize( geoServer );
            }
            catch( Throwable t ) {
                log.warn( "Failed to run initializer " + initer, t );
            }
        }
    }

    
    protected void createWMSInfo( IMap map ) {
        WMSInfoImpl wms = new WMSInfoImpl();
        wms.setGeoServer( geoserver );
        wms.setId( simpleName( map.id() ) + "-wms" );
        wms.setKeywords( Lists.newArrayList( new Keyword( "-Test-" ) ) );
        wms.setOutputStrategy( "SPEED" );
        addMaintainer( wms, map );

        // XXX make this configurable; deliver all known EPSG codes for now :)
        // FIXME configure allowed EPSG codes
        List<String> srs = new ArrayList();
        srs.add( "EPSG:25832" );
        srs.add( "EPSG:25833" );
        srs.add( "EPSG:31468" );
        srs.add( "EPSG:4326" );
        srs.add( "EPSG:3857" );
        wms.setSRS( srs );
        
//        List<Version> versions = new ArrayList<Version>();
//        versions.add( new Version( "1.1.1" ) );
//        versions.add( new Version( "1.3" ) );
//        wms.setVersions( versions );
        geoserver.add( wms );
        log.info( "WMS: " + wms );
    }

    
    protected void createWFSInfo( IMap map ) {
        WFSInfoImpl wfs = new WFSInfoImpl();
        wfs.setGeoServer( geoserver );
        // XXX make this configurable (where to get authentication from when TRANSACTIONAL?)
        wfs.setServiceLevel( ServiceLevel.BASIC );
        wfs.setId( simpleName( map.id() ) + "-wfs" );
        wfs.setOutputStrategy( "SPEED" );
        
        addMaintainer( wfs, map );
        
        //gml2
        GMLInfo gml = new GMLInfoImpl();
        gml.setOverrideGMLAttributes( true );
        Boolean srsXmlStyle = false; //(Boolean) properties.get( "srsXmlStyle" );
        if( srsXmlStyle ) {
            gml.setSrsNameStyle( SrsNameStyle.XML );    
        }
        else {
            gml.setSrsNameStyle( SrsNameStyle.NORMAL );
        }
        wfs.getGML().put( WFSInfo.Version.V_10 , gml );
        
        //gml3
        gml = new GMLInfoImpl();
        gml.setSrsNameStyle( SrsNameStyle.URN );
        gml.setOverrideGMLAttributes( false );
        wfs.getGML().put( WFSInfo.Version.V_11 , gml );

        //gml32
        gml = new GMLInfoImpl();
        gml.setSrsNameStyle( SrsNameStyle.URN2 );
        gml.setOverrideGMLAttributes( false );
        wfs.getGML().put( WFSInfo.Version.V_20 , gml );

        wfs.getVersions().add( new Version( "1.0.0" ) );
        wfs.getVersions().add( new Version( "1.1.0" ) );
        wfs.getVersions().add( new Version( "2.0.0" ) );

        geoserver.add( wfs );        
        log.info( "WFS: '" + wfs.getTitle() + "'" );
    }


    protected void addMaintainer( ServiceInfo service, IMap map ) {
        service.setTitle( simpleName( GeoServerPlugin.instance().baseName.orElse( map.label.get() ) ) );
        //service.setName( simpleName( map.label.get() ) );
        
        service.setMaintainer( "-maintainer-" );
        service.setAccessConstraints( "-none-" );
        service.setFees( "-none-" );
        service.setAbstract( map.description.opt().orElse( "" ) );
        service.setOutputStrategy( "SPEED" );
    }

}

/* 
 * polymap.org
 * Copyright (C) 2009-2015, Falko Bräutigam. All rights reserved.
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

import static org.polymap.service.geoserver.spring.Utils.simpleName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.PublishedType;
import org.geoserver.catalog.Wrapper;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;
import org.geoserver.config.GeoServer;
import org.geoserver.config.impl.GeoServerInfoImpl;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.wms.WMSInfoImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.service.geoserver.GeoServerPlugin;
import org.polymap.service.geoserver.GeoServerServlet;

/**
 * Initializes GeoServer configuration and catalog on startup.
 * <p>
 * This class is registered in a spring context and post processes the singleton
 * beans {@link Catalog} and {@link GeoServer}, populating them with data from an
 * {@link IMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
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
                Catalog catalog = (Catalog)bean;
                loadCatalog( catalog, service );
            }
            // GeoServer
            else if (bean instanceof GeoServer) {
                geoserver = (GeoServer)bean;
                loadGeoServer( service );
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
        wsInfo.setId( (String)map.id() );
        wsInfo.setName( simpleName( map.label.get() ) );
        log.info( "    loaded: " + wsInfo );
        
        catalog.add( Utils.defaultNsInfo.get() );
        log.info( "    loaded: " + Utils.defaultNsInfo.get() );

        for (ILayer layer : map.layers) {
            P4DataStoreInfo dsInfo = new P4DataStoreInfo( catalog, layer );
            dsInfo.setWorkspace( wsInfo );

            // Feature layers and upstream WMS are represented same way;
            // all rendering is done by PipelineMapResponse
            P4FeatureTypeInfo ftInfo = new P4FeatureTypeInfo( catalog, dsInfo );
            catalog.add( ftInfo );
            P4LayerInfo layerInfo = new P4LayerInfo( catalog, layer, ftInfo, PublishedType.VECTOR );
            layerInfo.createFeatureStyleInfo( service.createSLD( layer ), resourceLoader );
            catalog.add( layerInfo );
        }
    }


    protected void loadGeoServer( GeoServerServlet service ) {
        IMap map = service.map;

        log.info( "Loading GeoServer..." );
        GeoServerInfoImpl gsInfo = new GeoServerInfoImpl( geoserver );
        gsInfo.setTitle( "mapzone.io powered by GeoServer :)" );
        gsInfo.setId( "geoserver-polymap4" );
        gsInfo.setProxyBaseUrl( GeoServerPlugin.instance().baseUrl.get() + service.alias + "/" );
        log.info( "    proxy base URL: " + gsInfo.getProxyBaseUrl() );
        // XXX indent XML output, make configurable
        gsInfo.setVerbose( true );
        gsInfo.setVerboseExceptions( true );
        //gsInfo.setGlobalServices( true );
        geoserver.setGlobal( gsInfo );
        log.info( "    loaded: " + gsInfo );

        createWMSInfo( map );
       // createAndAddWFSInfo( map, geoserver );
    }

    
    protected void createWMSInfo( IMap map ) {
        WMSInfoImpl wms = new WMSInfoImpl();
        wms.setGeoServer( geoserver );
        wms.setId( simpleName( map.label.get() ) + "-wms" );
        wms.setMaintainer( "" );
        wms.setTitle( map.label.get() );
//        wms.setAbstract( "POLYMAP4 (polymap.org) powered by GeoServer (geoserver.org)." );
        wms.setName( simpleName( map.label.get() ) );
        wms.setOutputStrategy( "SPEED" );

        // XXX make this configurable; deliver all known EPSG codes for now :)
        // FIXME configure allowed EPSG codes
        List<String> srs = new ArrayList();
        srs.add( "EPSG:31468" );
        srs.add( "EPSG:4326" );
        srs.add( "EPSG:3857" );
        wms.setSRS( srs );
        
//        List<Version> versions = new ArrayList<Version>();
//        versions.add( new Version( "1.1.1" ) );
//        versions.add( new Version( "1.3" ) );
//        wms.setVersions( versions );
        geoserver.add( wms );
        log.info( "    loaded: " + wms );
    }


//    @SuppressWarnings("unchecked")
//    private void createAndAddWFSInfo( IMap map, GeoServer geoserver ) {
//        // WFS
//        WFSInfoImpl wfs = new WFSInfoImpl();
//        wfs.setGeoServer( geoserver );
//        // XXX make this configurable (where to get authentication from when
//        // TRANSACTIONAL?)
//        wfs.setServiceLevel( ServiceLevel.BASIC );
//        wfs.setId( simpleName( map.label.get() ) + "-wfs" );
//        wfs.setMaintainer( "" );
//        wfs.setTitle( simpleName( map.label.get() ) );
//        wfs.setName( simpleName( map.label.get() ) + "-wfs" );
//        // XXX
//        wfs.setOutputStrategy( "SPEED" );
//        // wfs.set( srs );
//        // List<Version> versions = new ArrayList();
//        // versions.add( new Version( "1.1.1" ) );
//        // versions.add( new Version( "1.3" ) );
//        // wfs.setVersions( versions );
//        wfs.getVersions().add( new Version( "1.0.0" ) );
//        wfs.getVersions().add( new Version( "1.1.0" ) );
//        
//        createGMLInfo2( wfs );
//        createGMLInfo3( wfs );
//
//        geoserver.add( wfs );
//        
//        log.info( "    loaded WFS: '" + wfs.getTitle() + "'" );
//    }
//
//
//    private void createGMLInfo3( WFSInfoImpl wfs ) {
//        GMLInfo gml = new GMLInfoImpl();
//        gml.setSrsNameStyle( SrsNameStyle.URN );
//        wfs.getGML().put( WFSInfo.Version.V_11, gml );
//    }
//
//
//    private void createGMLInfo2( WFSInfoImpl wfs ) {
//        GMLInfo gml = new GMLInfoImpl();
//        Boolean srsXmlStyle = true;
//        if (srsXmlStyle) {
//            gml.setSrsNameStyle( SrsNameStyle.XML );
//        }
//        else {
//            gml.setSrsNameStyle( SrsNameStyle.NORMAL );
//        }
//        wfs.getGML().put( WFSInfo.Version.V_10, gml );
//    }

}

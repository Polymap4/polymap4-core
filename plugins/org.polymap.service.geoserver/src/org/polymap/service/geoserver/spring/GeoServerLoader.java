/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.service.geoserver.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.IOException;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;

import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.Wrapper;
import org.geoserver.catalog.LayerInfo.Type;
import org.geoserver.catalog.impl.AttributeTypeInfoImpl;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.impl.NamespaceInfoImpl;
import org.geoserver.catalog.impl.StyleInfoImpl;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;
import org.geoserver.config.GeoServer;
import org.geoserver.config.impl.GeoServerInfoImpl;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.GMLInfo;
import org.geoserver.wfs.GMLInfoImpl;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.WFSInfoImpl;
import org.geoserver.wfs.GMLInfo.SrsNameStyle;
import org.geoserver.wfs.WFSInfo.ServiceLevel;
import org.geoserver.wms.WMSInfoImpl;
import org.geotools.data.DataStore;
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.Version;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.vfny.geoserver.global.GeoserverDataDirectory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Polygon;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.pipeline.DefaultPipelineIncubator;
import org.polymap.core.data.pipeline.IPipelineIncubator;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.runtime.Stringer;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.runtime.cache.CacheManager;
import org.polymap.core.style.IStyle;

import org.polymap.service.ServicesPlugin;
import org.polymap.service.geoserver.GeoServerWms;

/**
 * Initializes GeoServer configuration and catalog on startup.
 * <p>
 * This class is registered in a spring context and post processes the 
 * singleton beans {@link Catalog} and {@link GeoServer}, populating them 
 * with data from an {@link IMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class GeoServerLoader
        implements BeanPostProcessor, DisposableBean, ApplicationContextAware {

    private static final Log log = LogFactory.getLog( GeoServerLoader.class );

    /** The namespace of all features delivered via GeoServer. */
    public static final String  NAMESPACE = "http://www.polymap.org/";
    
    
    private GeoServerResourceLoader resourceLoader;

    private GeoServer               geoserver;
    
    private Map<String,ILayer>      layers = new HashMap();
    
    private IPipelineIncubator      pipelineIncubator;
    
    private Cache<String,Pipeline>  pipelines;

    
    public GeoServerLoader( GeoServerResourceLoader resourceLoader ) {
        this.resourceLoader = resourceLoader;
        this.pipelineIncubator = new DefaultPipelineIncubator();
        this.pipelines = CacheManager.instance().newCache( CacheConfig.DEFAULT.initSize( 32 ) );
    }
    

    public void destroy()
    throws Exception {
        if (geoserver != null) {
            geoserver.dispose();
            geoserver = null;
        }
        if (pipelines != null) {
            pipelines.dispose();
            pipelines = null;
        }
    }

    
    public ILayer getLayer( String name ) {
        return layers.get( name );
    }

    /**
     * Creates a new processing {@link Pipeline} for the given {@link ILayer}
     * and usecase.
     * <p>
     * XXX The result needs to be cached
     * 
     * @throws IOException 
     * @throws PipelineIncubationException 
     */
    public Pipeline getOrCreatePipeline( final ILayer layer, final LayerUseCase usecase ) 
    throws IOException, PipelineIncubationException {
        
        return pipelines.get( layer.id(), new CacheLoader<String,Pipeline,IOException>() {
        
            public Pipeline load( String key ) throws IOException {
                try {
                    IService service = findService( layer );
                    return pipelineIncubator.newPipeline( 
                            usecase, layer.getMap(), layer, service );
                }
                catch (PipelineIncubationException e) {
                    // should never happen
                    throw new RuntimeException( e );
                }
            }
            
            public int size() throws IOException {
                return Cache.ELEMENT_SIZE_UNKNOW;
            }
        });
    }


    /**
     * Find the corresponding {@link ILayer} for the given {@link MapLayer} of
     * the MapContext.
     */
    protected ILayer findLayer( MapLayer mapLayer ) {
        log.debug( "findLayer(): mapLayer= " + mapLayer );
        
        ILayer layer = getLayer( mapLayer.getTitle() );
        
//        FeatureSource<? extends FeatureType, ? extends Feature> fs = mapLayer.getFeatureSource();
//        PipelineDataStore pds = (PipelineDataStore)fs.getDataStore();
//        ILayer layer = pds.getFeatureSource().getPipeline().getLayers().iterator().next();
        
        return layer;
    }

    
    protected IService findService( ILayer layer ) 
    throws IOException {
        IGeoResource res = layer.getGeoResource();
        if (res == null) {
            throw new ServiceException( "Unable to find geo resource of layer: " + layer );
        }
        // XXX give a reasonable monitor; check state 
        IService service = res.service( null );
        log.debug( "service: " + service );
        return service;
    }
    
    
    public void setApplicationContext( ApplicationContext applicationContext )
    throws BeansException {
        // there are dependencies in GeoServer that crash without it
        GeoserverDataDirectory.init( (WebApplicationContext)applicationContext );
    }


    public final Object postProcessAfterInitialization( Object bean, String beanName )
    throws BeansException {
        return bean;
    }


    public final Object postProcessBeforeInitialization( Object bean, String beanName )
    throws BeansException {
        // Catalog
        if (bean instanceof Catalog) {
            if (bean instanceof Wrapper && ((Wrapper)bean).isWrapperFor( Catalog.class )) {
                return bean;
            }
            try {
                // find our GeoServerWms / IMap
                IMap map = GeoServerWms.servers.get().getMap();
                
                Catalog catalog = (Catalog)bean;
                loadCatalog( catalog, map );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        // GeoServer
        if (bean instanceof GeoServer) {
            geoserver = (GeoServer)bean;
            try {
                // find our GeoServerWms / IMap
                GeoServerWms service = GeoServerWms.servers.get();
                loadGeoServer( service );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return bean;
    }


    protected void loadCatalog( Catalog catalog, IMap map ) 
    throws PipelineIncubationException, IOException {
        log.debug( "Loading catalog..." );
        
        WorkspaceInfoImpl wsInfo = new WorkspaceInfoImpl();
        wsInfo.setId( map.id() );
        wsInfo.setName( map.getLabel() + "ws" );
        catalog.add( wsInfo );
        log.debug( "    loaded Workspace: '" + wsInfo.getName() +"'");
        
        NamespaceInfoImpl defaultNsInfo = new NamespaceInfoImpl();
        defaultNsInfo.setId( map.id() );
        defaultNsInfo.setPrefix( "polymap" );
        defaultNsInfo.setURI( NAMESPACE );
//        defaultNsInfo.setPrefix( "gml" );
//        defaultNsInfo.setURI( "http://www.opengis.net/gml" );
        catalog.add( defaultNsInfo );
        log.debug( "    loaded Namespace: '" + defaultNsInfo.getName() +"'");
        
        for (ILayer layer : map.getLayers()) {
            SimpleFeatureType schema = null;
            DataStore ds = null;

            // try feature/vector resource
            try {
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
                if (fs == null || fs.getPipeline().length() == 0) {
                    throw new PipelineIncubationException( "WMS layer? : " + layer.getLabel() );
                }

                // set name/namespace for target schema
                Name name = new NameImpl( NAMESPACE, simpleName( layer.getLabel() ) );
                fs.getPipeline().addFirst( new FeatureRenameProcessor( name ) );
                
                schema = fs.getSchema();
                ds = fs.getDataStore();
            }
            // 
            catch (PipelineIncubationException e) {
                // XXX howto skip layer in case of WFS!?
                String typeName = simpleName( layer.getLabel() );
                ds = wrapCoverageLayer( layer, typeName );
                schema = ds.getSchema( typeName );
            }
            // no geores found or something
            catch (Exception e) {
                log.error( "Error while creating catalog: " + e.getLocalizedMessage() );
                log.debug( "", e );
                break;
            }
                
            // DataStore
            MyDataStoreInfoImpl dsInfo = new MyDataStoreInfoImpl( catalog, ds );
            dsInfo.setId( layer.id() );
            dsInfo.setName( layer.getLabel() );
            dsInfo.setDescription( "DataStore of ILayer: " + layer.getLabel() );
            dsInfo.setWorkspace( wsInfo );
            dsInfo.setType( "PipelineDataStore" );
            Map params = new HashMap();
            params.put( PipelineDataStoreFactory.PARAM_LAYER.key, layer );
            dsInfo.setConnectionParameters( params );
            dsInfo.setEnabled( true );
            catalog.add( dsInfo );
            log.debug( "    loaded DataStore: '" + dsInfo.getName() +"'");

            // FeatureType
            MyFeatureTypeInfoImpl ftInfo = new MyFeatureTypeInfoImpl( catalog, layer.id(), ds );
            ftInfo.setName( schema.getTypeName() );
            ftInfo.setTitle( layer.getLabel() );
            ftInfo.setKeywords( new ArrayList( layer.getKeywords() ) );
            ftInfo.setDescription( "FeatureType of ILayer: " + layer.getLabel() );
            ftInfo.setStore( dsInfo );
            ftInfo.setNamespace( defaultNsInfo );
            ftInfo.setNativeCRS( layer.getCRS() );
            //ftInfo.setNativeBoundingBox( map.getMaxExtent() );
            ftInfo.setNativeName( schema.getTypeName() );
            ftInfo.setProjectionPolicy( ProjectionPolicy.NONE );
            // XXX this the "default" SRS; WFS needs this to work; shouldn't this be the the "native"
            // SRS of the data?
            ftInfo.setSRS( layer.getCRSCode() );
            ReferencedEnvelope bbox = map.getMaxExtent();
            try {
                Envelope latlon = CRS.transform( bbox, DefaultGeographicCRS.WGS84 );
                double[] lu = latlon.getLowerCorner().getCoordinate();
                double[] ro = latlon.getUpperCorner().getCoordinate();
                ftInfo.setLatLonBoundingBox( new ReferencedEnvelope( 
                        lu[0], ro[0], lu[1], ro[1], CRS.decode( "EPSG:4326" ) ) );
            }
            catch (Exception e) {
                log.warn( e );
                ftInfo.setLatLonBoundingBox( new ReferencedEnvelope( DefaultGeographicCRS.WGS84 ) );
            }
            ftInfo.setEnabled( true );

            List<AttributeTypeInfo> attributeInfos = new ArrayList();
            for (AttributeDescriptor attribute : schema.getAttributeDescriptors()) {
                AttributeTypeInfoImpl attributeInfo = new AttributeTypeInfoImpl();
                attributeInfo.setFeatureType( ftInfo );
                attributeInfo.setAttribute( attribute );
                attributeInfo.setId( attribute.toString() );
            }
            ftInfo.setAttributes( attributeInfos );
            catalog.add( ftInfo );
            log.debug( "    loaded FeatureType: '" + ftInfo.getName() +"'");

            // Layer
            LayerInfoImpl layerInfo = new LayerInfoImpl();
            layerInfo.setResource( ftInfo );
            layerInfo.setId( layer.id() );
            layerInfo.setName( schema.getTypeName() );
            layers.put( layerInfo.getName(), layer );
            layerInfo.setEnabled( true );
            layerInfo.setType( Type.VECTOR );
            Set styles = new HashSet();

            StyleInfoImpl style = new StyleInfoImpl( catalog );
            IStyle layerStyle = layer.getStyle();
            String styleName = layerStyle.getTitle() != null 
                    ? layerStyle.getTitle() : layer.getLabel() + "-style";
            style.setId( simpleName( styleName ) );
            style.setName( simpleName( styleName ) );

            File sldFile = GeoserverDataDirectory.findStyleFile( styleName + ".sld", true );
            if (!sldFile.getParentFile().exists()) {
                sldFile.getParentFile().mkdirs();
            }
            FileUtils.writeStringToFile( sldFile, layerStyle.createSLD( new NullProgressMonitor() ), "UTF-8" );

            style.setFilename( sldFile.getName() );
            catalog.add( style );
            styles.add( style );
            layerInfo.setStyles( styles );
            layerInfo.setDefaultStyle( style );
            catalog.add( layerInfo );
            log.debug( "    loaded Layer: '" + layerInfo.getName() +"'");
                
//            }
//            catch (Exception e) {
//                log.info( "No feature pipeline, creating CoverageStore..." );
//                // XXX build CoverageStore if FeatureStore failed
//                
//                // CoverageStore
//                CoverageStoreInfoImpl csInfo = new CoverageStoreInfoImpl( catalog );
//                csInfo.setId( map.id() + "-coverage" );
//                csInfo.setName( map.getLabel() + "-coverage" );
//                csInfo.setDescription( "CoverageStore of IMap: " + map.getLabel() );
//                csInfo.setWorkspace( wsInfo );
//                csInfo.setType( "polymap" );
//                csInfo.setEnabled( true );
//                //csInfo.setURL( )
//                Map params = new HashMap();
//                params.put( PipelineDataStoreFactory.PARAM_LAYER.key, layer );
//                csInfo.setConnectionParameters( params );
//                csInfo.setCatalog( catalog );
//                catalog.add( csInfo );
//                log.debug( "    loaded CoverageStore: '" + csInfo.getName() +"'");
//            
//                // Coverage
//                WmsCoverageInfoImpl cvInfo = new WmsCoverageInfoImpl( catalog );
//                cvInfo.setStore( csInfo );
//                cvInfo.setName( layer.getLabel() );
//                cvInfo.setTitle( layer.getLabel() );
//                cvInfo.setKeywords( new ArrayList( layer.getKeywords() ) );
//                cvInfo.setDescription( "Coverage of ILayer: " + layer.getLabel() );
//                cvInfo.setNamespace( nsInfo );
//                cvInfo.setNativeCRS( layer.getCRS() );
//                cvInfo.setNativeName( layer.getLabel() );
//                cvInfo.setProjectionPolicy( ProjectionPolicy.NONE );
//                // FIXME ...
//                cvInfo.setSRS( "EPSG:31468" );
//                //ftInfo.setNativeBoundingBox( map.getMaxExtent() );
//                // FIXME check CRS if it's lat/lon
//                cvInfo.setLatLonBoundingBox( map.getMaxExtent() );
//                cvInfo.setEnabled( true );
//                catalog.add( cvInfo );
//                log.debug( "    loaded Coverage: '" + cvInfo.getName() +"'");
//
//                // Layer
//                LayerInfoImpl layerInfo = new LayerInfoImpl();
//                layerInfo.setResource( cvInfo );
//                layerInfo.setId( layer.id() );
//                layerInfo.setName( layer.getLabel() );
//                layers.put( layerInfo.getName(), layer );
//                layerInfo.setEnabled( true );
//                layerInfo.setType( Type.RASTER );
//                catalog.add( layerInfo );
//                log.debug( "    loaded Layer: '" + layerInfo.getName() +"'");
//
//                // styles for Coverage!?
//                Set styles = new HashSet();
//                StyleInfoImpl style = new StyleInfoImpl( catalog );
//                style.setId( "default-style" );
//                style.setName( "default" );
//                style.setFilename( "default_line.sld" );
//                catalog.add( style );
//                styles.add( style );
//                layerInfo.setStyles( styles );
//                layerInfo.setDefaultStyle( style );
//            }
        }
        
    }


    protected void loadGeoServer( GeoServerWms service ) {
        IMap map = service.getMap();

        String proxyUrl = ServicesPlugin.getDefault().getBaseUrl();
        log.debug( "    proxy URL: " + proxyUrl );
        
        // GeoServer
        log.debug( "Loading GeoServer..." );
        GeoServerInfoImpl gsInfo = new GeoServerInfoImpl( geoserver );
        gsInfo.setTitle( "POLYMAP3 powered by GeoServer :)" );
        gsInfo.setId( "geoserver-polymap3" );
        gsInfo.setProxyBaseUrl( proxyUrl + service.getPathSpec() + "/" );
        // XXX indent XML output, make configurable
        gsInfo.setVerbose( true );
        gsInfo.setVerboseExceptions( true );
//        gsInfo.setGlobalServices( true );
        geoserver.setGlobal( gsInfo );
        log.debug( "    loaded GeoServer: '" + gsInfo.getTitle() +"'");

//        // FIXME configure allowed EPSG codes
//        List<String> srs = new ArrayList();
//        srs.add( "EPSG:31468" );
//        srs.add( "EPSG:4326" );

        // WMS
        WMSInfoImpl wms = new WMSInfoImpl();
        wms.setGeoServer( geoserver );
        wms.setId( simpleName( map.getLabel() ) + "-wms" );
        wms.setMaintainer( "" );
        wms.setTitle( simpleName( map.getLabel() ) );
        wms.setAbstract( "POLYMAP3 (polymap.org) powered by GeoServer (geoserver.org)." );
        wms.setName( simpleName( map.getLabel() ) );
        // XXX
        //wms.setOnlineResource( "http://localhost:10080/services/Atlas" );
        //wms.setSchemaBaseURL( )
        wms.setOutputStrategy( "SPEED" );
        
        // XXX make this configurable; deliver all known EPSG codes for now :)
//        wms.setSRS( srs );
        List<Version> versions = new ArrayList();
        versions.add( new Version( "1.1.1" ) );
        versions.add( new Version( "1.3" ) );
        wms.setVersions( versions );
        geoserver.add( wms );
        log.debug( "    loaded WMS: '" + wms.getTitle() +"'");
        
        // WFS
        WFSInfoImpl wfs = new WFSInfoImpl();
        wfs.setGeoServer( geoserver );
        // XXX make this configurable (where to get authentication from when TRANSACTIONAL?)
        wfs.setServiceLevel( ServiceLevel.BASIC );
        wfs.setId( simpleName( map.getLabel() ) + "-wfs" );
        wfs.setMaintainer( "" );
        wfs.setTitle( simpleName( map.getLabel() ) );
        wfs.setName( simpleName( map.getLabel() ) + "-wfs" );
        // XXX
        //wfs.setOnlineResource( "http://localhost:10080/services/Atlas" );
        wfs.setOutputStrategy( "SPEED" );
//        wfs.set( srs );
//        List<Version> versions = new ArrayList();
//        versions.add( new Version( "1.1.1" ) );
//        versions.add( new Version( "1.3" ) );
//        wfs.setVersions( versions );
        
        // GML2
        GMLInfo gml = new GMLInfoImpl();
        Boolean srsXmlStyle = true;
        if (srsXmlStyle) {
            gml.setSrsNameStyle( SrsNameStyle.XML );    
        }
        else {
            gml.setSrsNameStyle( SrsNameStyle.NORMAL );
        }
        wfs.getGML().put( WFSInfo.Version.V_10 , gml );
        
        //GML3
        gml = new GMLInfoImpl();
        gml.setSrsNameStyle(SrsNameStyle.URN);
        wfs.getGML().put( WFSInfo.Version.V_11 , gml );
        wfs.getVersions().add( new Version( "1.0.0" ) );
        wfs.getVersions().add( new Version( "1.1.0" ) );
        
        geoserver.add( wfs );
        log.debug( "    loaded WFS: '" + wfs.getTitle() +"'");
    }

    
    /**
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
     * (temporary).
     *
     * @param  reader the grid coverage reader.
     * @return a feature with the grid coverage envelope as the geometry and the
     *         grid coverage itself in the "grid" attribute.
     */
    protected CollectionDataStore wrapCoverageLayer(
            ILayer layer, String typeName ) { 

//        // create surrounding polygon
//        final PrecisionModel pm = new PrecisionModel();
//        final GeometryFactory gf = new GeometryFactory(pm, 0);
//        final Rectangle2D rect = gridCoverageReader.getOriginalEnvelope()
//                .toRectangle2D();
//        final CoordinateReferenceSystem sourceCrs = CRS
//            .getHorizontalCRS(gridCoverageReader.getCrs());
//        if(sourceCrs==null)
//            throw new UnsupportedOperationException(
//                    Errors.format(
//                            ErrorKeys.CANT_SEPARATE_CRS_$1,gridCoverageReader.getCrs()));
//
//
//        final Coordinate[] coord = new Coordinate[5];
//        coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
//        coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
//        coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
//        coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
//        coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
//
//        // }
//        final LinearRing ring = gf.createLinearRing(coord);
//        final Polygon bounds = new Polygon(ring, null, gf);

        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName( typeName );
        ftb.setNamespaceURI( NAMESPACE );
        ftb.add( "geom", Polygon.class, layer.getCRS() );
        ftb.add( "layer", ILayer.class );
        ftb.add( "params", GeneralParameterValue[].class );
        SimpleFeatureType schema = ftb.buildFeatureType();
        
        // create the feature
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( schema );
        fb.add( null );
        fb.add( layer );
        SimpleFeature feature = fb.buildFeature( null );

        final FeatureCollection<SimpleFeatureType, SimpleFeature> collection = 
                FeatureCollections.newCollection();
        collection.add(feature);

        return new CollectionDataStore( collection );
    }

    
    protected String simpleName( String s ) {
        return Stringer.on( s ).replaceUmlauts().toURIPath( "_" ).toString();
    }
    
}

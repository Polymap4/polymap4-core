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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.Keyword;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.PublishedType;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.Wrapper;
import org.geoserver.catalog.impl.AttributeTypeInfoImpl;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.impl.NamespaceInfoImpl;
import org.geoserver.catalog.impl.StyleInfoImpl;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.impl.GeoServerInfoImpl;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.wfs.GMLInfo;
import org.geoserver.wfs.GMLInfo.SrsNameStyle;
import org.geoserver.wfs.GMLInfoImpl;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.WFSInfo.ServiceLevel;
import org.geoserver.wfs.WFSInfoImpl;
import org.geoserver.wms.WMSInfoImpl;
import org.geotools.data.DataStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.Version;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Stringer;
import org.polymap.service.geoserver.GeoServerPlugin;
import org.polymap.service.geoserver.GeoServerServlet;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.vividsolutions.jts.geom.Polygon;

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

    private static final Log        log       = LogFactory.getLog( GeoServerLoader.class );

    /** The namespace of all features delivered via GeoServer. */
    public static final String      NAMESPACE = "http://www.polymap.org/";

    private GeoServerResourceLoader resourceLoader;

    private GeoServer               geoserver;

    private Map<String,ILayer>      layers    = new HashMap<String,ILayer>();


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


    public final Object postProcessAfterInitialization( Object bean, String beanName ) throws BeansException {
        return bean;
    }


    public final Object postProcessBeforeInitialization( Object bean, String beanName ) throws BeansException {
        if (bean instanceof Catalog) {
            postProcessCatalogBean( bean );
        }
        else if (bean instanceof GeoServer) {
            postProcessGeoServerBean( bean );
        }
        return bean;
    }


    private void postProcessGeoServerBean( Object bean ) {
        geoserver = (GeoServer)bean;
        try {
            // find our GeoServerWms / IMap
            GeoServerServlet service = GeoServerServlet.instance.get();
            assert service != null : "No GeoServerServlet registered for this thread!";
            loadGeoServer( service, geoserver );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    private void postProcessCatalogBean( Object bean ) {
        if (bean instanceof Wrapper && ((Wrapper)bean).isWrapperFor( Catalog.class )) {
            return;
        }
        try {
            // find our GeoServerWms / IMap
            GeoServerServlet service = GeoServerServlet.instance.get();
            assert service != null : "No GeoServerServlet registered for this thread!";

            Catalog catalog = (Catalog)bean;
            loadCatalog( catalog, service );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    protected void loadCatalog( Catalog catalog, GeoServerServlet service ) throws Exception {
        log.info( "Loading catalog..." );

        IMap map = service.getMap();

        CatalogBuilder catalogBuilder = new org.geoserver.catalog.CatalogBuilder( catalog );
        WorkspaceInfoImpl wsInfo = createAndAddWorkspace( map, catalogBuilder );
        NamespaceInfoImpl defaultNsInfo = createAndAddNamespace( catalog, map );

        for (ILayer layer : map.layers) {
            Triple<? extends StoreInfo,? extends DataStore,? extends SimpleFeatureType> dsInfoDsSchema = createAndAddDataStore(
                    layer, catalogBuilder, catalog, wsInfo );
            StoreInfo dsInfo = dsInfoDsSchema.getLeft();
            DataStore ds = dsInfoDsSchema.getMiddle();
            SimpleFeatureType schema = dsInfoDsSchema.getRight();
            MyFeatureTypeInfoImpl ftInfo = createAndAddFeatureType( catalog, catalogBuilder, defaultNsInfo, layer,
                    dsInfo, ds, schema );
            StyleInfoImpl style = createAndAddStyle( catalog, layer );
            createAndAddLayer( catalog, catalogBuilder, layer, schema, ftInfo, style );
        }
    }


    private void createAndAddLayer( Catalog catalog, CatalogBuilder catalogBuilder, ILayer layer,
            SimpleFeatureType schema, MyFeatureTypeInfoImpl ftInfo, StyleInfoImpl style ) throws IOException {
        LayerInfo layerInfo = catalogBuilder.buildLayer( ftInfo );

        layerInfo.setResource( ftInfo );
        ((LayerInfoImpl)layerInfo).setId( String.valueOf( layer.id() ) );
        layerInfo.setName( schema.getTypeName() );
        layers.put( layerInfo.getName(), layer );
        layerInfo.setEnabled( true );
        layerInfo.setType( PublishedType.VECTOR );

        layerInfo.getStyles().add( style );
        layerInfo.setDefaultStyle( style );
        catalog.add( layerInfo );
        log.debug( "    loaded Layer: '" + layerInfo.getName() + "'" );
    }


    private StyleInfoImpl createAndAddStyle( Catalog catalog, ILayer layer ) throws IOException {
        StyleInfoImpl style = new StyleInfoImpl( catalog );
        // IStyle layerStyle = layer.getStyle();
        String styleName = /*
                            * layerStyle.getTitle() != null ? layerStyle.getTitle() :
                            */layer.label.get() + "-style";
        style.setId( simpleName( styleName ) );
        style.setName( simpleName( styleName ) );

        File sldFile = new GeoServerDataDirectory( this.resourceLoader ).config( style ).file();
        if (!sldFile.getParentFile().exists()) {
            sldFile.getParentFile().mkdirs();
        }
        FileUtils.writeStringToFile( sldFile, createLayerStyleSLD( layer.label.get() ), "UTF-8" );

        style.setFilename( sldFile.getName() );
        catalog.add( style );
        return style;
    }


    private String createLayerStyleSLD( String mapLabel ) {
        // copied from
        // http://docs.geoserver.org/stable/en/user/styling/sld-introduction.html
        StringBuilder sb = new StringBuilder();
        sb.append( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );
        sb.append( "<StyledLayerDescriptor version=\"1.0.0\"" );
        sb.append( "    xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\"" );
        sb.append( "    xmlns=\"http://www.opengis.net/sld\"" );
        sb.append( "    xmlns:ogc=\"http://www.opengis.net/ogc\"" );
        sb.append( "    xmlns:xlink=\"http://www.w3.org/1999/xlink\"" );
        sb.append( "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" );
        sb.append( "  <NamedLayer>" );
        sb.append( "    <Name>" + mapLabel + "</Name>" );
        sb.append( "    <UserStyle>" );
        sb.append( "      <Title>GeoServer SLD Cook Book: Simple point</Title>" );
        sb.append( "      <FeatureTypeStyle>" );
        sb.append( "        <Rule>" );
        sb.append( "          <PointSymbolizer>" );
        sb.append( "            <Graphic>" );
        sb.append( "              <Mark>" );
        sb.append( "                <WellKnownName>circle</WellKnownName>" );
        sb.append( "                <Fill>" );
        sb.append( "                  <CssParameter name=\"fill\">#FF0000</CssParameter>" );
        sb.append( "                </Fill>" );
        sb.append( "              </Mark>" );
        sb.append( "              <Size>6</Size>" );
        sb.append( "            </Graphic>" );
        sb.append( "          </PointSymbolizer>" );
        sb.append( "        </Rule>" );
        sb.append( "      </FeatureTypeStyle>" );
        sb.append( "    </UserStyle>" );
        sb.append( "  </NamedLayer>" );
        sb.append( "</StyledLayerDescriptor>" );
        return sb.toString();
    }


    private MyFeatureTypeInfoImpl createAndAddFeatureType( Catalog catalog, CatalogBuilder catalogBuilder,
            NamespaceInfoImpl defaultNsInfo, ILayer layer, StoreInfo dsInfo, DataStore ds, SimpleFeatureType schema )
            throws Exception {
        // FeatureType
        MyFeatureTypeInfoImpl ftInfo = new MyFeatureTypeInfoImpl( catalog, (String)layer.id(), ds );
        ftInfo.setName( schema.getTypeName() );
        ftInfo.setTitle( layer.label.get() );
        for(String keyword : layer.keywords) {
            ftInfo.getKeywords().add( new Keyword(keyword) );
        }
        ftInfo.setDescription( "FeatureType of ILayer: " + layer.label.get() );
        ftInfo.setStore( dsInfo );
        ftInfo.setNamespace( defaultNsInfo );
        // FIXME ftInfo.setNativeCRS( layer.getCRS() );
        // ftInfo.setNativeBoundingBox( map.getMaxExtent() );
        ftInfo.setNativeName( schema.getTypeName() );
        ftInfo.setProjectionPolicy( ProjectionPolicy.NONE );
        // XXX this the "default" SRS; WFS needs this to work; shouldn't this be the
        // "native"
        // SRS of the data?
        // FIXME ftInfo.setSRS( layer.getCRSCode() );
//        setBoundingBox(ftInfo);
        ftInfo.setEnabled( true );

        List<AttributeTypeInfo> attributeInfos = new ArrayList();
        for (AttributeDescriptor attribute : schema.getAttributeDescriptors()) {
            AttributeTypeInfoImpl attributeInfo = new AttributeTypeInfoImpl();
            attributeInfo.setFeatureType( ftInfo );
            attributeInfo.setAttribute( attribute );
            attributeInfo.setId( attribute.toString() );
        }
        ftInfo.setAttributes( attributeInfos );
        // set missing default values
        catalogBuilder.initFeatureType( ftInfo );
        catalog.add( ftInfo );
        log.debug( "    loaded FeatureType: '" + ftInfo.getName() + "'" );
        return ftInfo;
    }


//    private void setBoundingBox( FeatureTypeInfo ftInfo ) {
//        ReferencedEnvelope bbox = /* map.getMaxExtent() */new ReferencedEnvelope();
//        try {
//            GeneralEnvelope latlon = CRS.transform( bbox, DefaultGeographicCRS.WGS84 );
//            double[] lu = latlon.getLowerCorner().getCoordinate();
//            double[] ro = latlon.getUpperCorner().getCoordinate();
//            ftInfo.setLatLonBoundingBox( new ReferencedEnvelope( lu[0], ro[0], lu[1], ro[1], CRS.decode( "EPSG:4326" ) ) );
//        }
//        catch (Exception e) {
//            log.warn( e );
//            ftInfo.setLatLonBoundingBox( new ReferencedEnvelope( DefaultGeographicCRS.WGS84 ) );
//        }
//    }


    /**
     * @return
     * @throws IOException
     */
    private Triple<? extends StoreInfo,? extends DataStore,? extends SimpleFeatureType> createAndAddDataStore(
            ILayer layer, CatalogBuilder catalogBuilder, Catalog catalog, WorkspaceInfo wsInfo ) throws IOException {

        // FIXME just coverage/WMS supported so far
        String typeName = simpleName( layer.label.get() );
        DataStore ds = wrapCoverageLayer( layer, typeName );
        SimpleFeatureType schema = ds.getSchema( typeName );

        // DataStore
        StoreInfo dsInfo = createAndAddDataStore( layer, catalogBuilder, catalog, wsInfo, ds );

        return Triple.of( dsInfo, ds, schema );
    }
    
    
    /**
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
     * (temporary).
     *
     * @param reader the grid coverage reader.
     * @return a feature with the grid coverage envelope as the geometry and the grid
     *         coverage itself in the "grid" attribute.
     */
    protected org.geotools.data.DataStore wrapCoverageLayer( ILayer layer, String typeName ) {

        // createSurroundingPolygon();

        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName( typeName );
        ftb.setNamespaceURI( NAMESPACE );
        // required to have schema.getGeometryDescriptor() not 
        // return null in org.geotools.renderer.lite.StreamingRenderer.processStylers(
        // Graphics2D, Layer, AffineTransform, CoordinateReferenceSystem, Envelope, 
        // Rectangle, String)
        ftb.add( "polygonProperty", Polygon.class );
        // polygonProperty requires CoordinateReferenceSystem
        ftb.setCRS( DefaultGeographicCRS.WGS84 );
        ftb.add( "params", GeneralParameterValue[].class );
        final SimpleFeatureType simpleFeatureType = ftb.buildFeatureType();

        // create the feature
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( simpleFeatureType );
        fb.add( null );
        final SimpleFeature feature = fb.buildFeature( null );

        return new MyContentDataStore(simpleFeatureType, feature);
    }
    
//    private void createSurroundingPolygon() {
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
//    }

    private StoreInfo createAndAddDataStore( ILayer layer, CatalogBuilder catalogBuilder, Catalog catalog,
            WorkspaceInfo wsInfo, DataStore ds ) {
        MyDataStoreInfoImpl dsInfo = new MyDataStoreInfoImpl( catalog, ds );
        dsInfo.setId( (String)layer.id() );
        dsInfo.setName( layer.label.get() );
        dsInfo.setDescription( "DataStore of ILayer: " + layer.label.get() );
        dsInfo.setWorkspace( wsInfo );
        dsInfo.setType( "PipelineDataStore" );
        Map<String,Serializable> params = new HashMap<String,Serializable>();
        // FIXME params.put( PipelineDataStoreFactory.PARAM_LAYER.key, layer );
        dsInfo.setConnectionParameters( params );
        dsInfo.setEnabled( true );

        catalogBuilder.setStore( dsInfo );
        log.debug( "    loaded DataStore: '" + dsInfo.getName() + "'" );
        return dsInfo;
    }


    // try feature/vector resource
    // private void createAndAddDataStore(ILayer layer) throws IOException {
    // SimpleFeatureType schema;
    // DataStore ds;
    // try {
    // PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
    // if (fs == null || fs.getPipeline().length() == 0) {
    // throw new PipelineIncubationException( "WMS layer? : " + layer.label.get() );
    // }
    //
    // // set name/namespace for target schema
    // Name name = new NameImpl( NAMESPACE, simpleName( layer.label.get() ) );
    // // fs.getPipeline().addFirst( new FeatureRenameProcessor( name ) );
    //
    // schema = fs.getSchema();
    // ds = fs.getDataStore();
    // }
    // //
    // catch (PipelineIncubationException e) {
    // // XXX howto skip layer in case of WFS!?
    // String typeName = simpleName( layer.label.get() );
    // ds = wrapCoverageLayer( layer, typeName );
    // schema = ds.getSchema( typeName );
    // }
    // // no geores found or something
    // catch (Exception e) {
    // log.error( "Error while creating catalog: " + e.getLocalizedMessage() );
    // log.debug( "", e );
    // }
    // return Triple.of( dsInfo, ds, schema );
    // }

    private NamespaceInfoImpl createAndAddNamespace( Catalog catalog, IMap map ) {
        NamespaceInfoImpl defaultNsInfo = new NamespaceInfoImpl();
        defaultNsInfo.setId( (String)map.id() );
        defaultNsInfo.setPrefix( "polymap" );
        defaultNsInfo.setURI( NAMESPACE );
        // defaultNsInfo.setPrefix( "gml" );
        // defaultNsInfo.setURI( "http://www.opengis.net/gml" );
        catalog.add( defaultNsInfo );

        log.debug( "    loaded Namespace: '" + defaultNsInfo.getName() + "'" );
        return defaultNsInfo;
    }


    private WorkspaceInfoImpl createAndAddWorkspace( IMap map, CatalogBuilder catalogBuilder ) {
        WorkspaceInfoImpl wsInfo = new WorkspaceInfoImpl();
        wsInfo.setId( (String)map.id() );
        wsInfo.setName( map.label.get() + "ws" );
        catalogBuilder.setWorkspace( wsInfo );
        log.info( "    loaded Workspace: '" + wsInfo.getName() + "'" );
        return wsInfo;
    }


    protected void loadGeoServer( GeoServerServlet service, GeoServer geoserver ) {
        IMap map = service.getMap();

        internalLoadGeoServer( service, geoserver );

        // // FIXME configure allowed EPSG codes
        // List<String> srs = new ArrayList();
        // srs.add( "EPSG:31468" );
        // srs.add( "EPSG:4326" );

        createAndAddWMSInfo( map, geoserver );
        createAndAddWFSInfo( map, geoserver );
    }


    @SuppressWarnings("unchecked")
    private void createAndAddWFSInfo( IMap map, GeoServer geoserver ) {
        // WFS
        WFSInfoImpl wfs = new WFSInfoImpl();
        wfs.setGeoServer( geoserver );
        // XXX make this configurable (where to get authentication from when
        // TRANSACTIONAL?)
        wfs.setServiceLevel( ServiceLevel.BASIC );
        wfs.setId( simpleName( map.label.get() ) + "-wfs" );
        wfs.setMaintainer( "" );
        wfs.setTitle( simpleName( map.label.get() ) );
        wfs.setName( simpleName( map.label.get() ) + "-wfs" );
        // XXX
        wfs.setOutputStrategy( "SPEED" );
        // wfs.set( srs );
        // List<Version> versions = new ArrayList();
        // versions.add( new Version( "1.1.1" ) );
        // versions.add( new Version( "1.3" ) );
        // wfs.setVersions( versions );
        wfs.getVersions().add( new Version( "1.0.0" ) );
        wfs.getVersions().add( new Version( "1.1.0" ) );
        
        createGMLInfo2( wfs );
        createGMLInfo3( wfs );

        geoserver.add( wfs );
        
        log.info( "    loaded WFS: '" + wfs.getTitle() + "'" );
    }


    private void createGMLInfo3( WFSInfoImpl wfs ) {
        GMLInfo gml = new GMLInfoImpl();
        gml.setSrsNameStyle( SrsNameStyle.URN );
        wfs.getGML().put( WFSInfo.Version.V_11, gml );
    }


    private void createGMLInfo2( WFSInfoImpl wfs ) {
        GMLInfo gml = new GMLInfoImpl();
        Boolean srsXmlStyle = true;
        if (srsXmlStyle) {
            gml.setSrsNameStyle( SrsNameStyle.XML );
        }
        else {
            gml.setSrsNameStyle( SrsNameStyle.NORMAL );
        }
        wfs.getGML().put( WFSInfo.Version.V_10, gml );
    }


    private void createAndAddWMSInfo( IMap map, GeoServer geoserver ) {
        // WMS
        WMSInfoImpl wms = new WMSInfoImpl();
        wms.setGeoServer( geoserver );
        wms.setId( simpleName( map.label.get() ) + "-wms" );
        wms.setMaintainer( "" );
        wms.setTitle( simpleName( map.label.get() ) );
        wms.setAbstract( "POLYMAP4 (polymap.org) powered by GeoServer (geoserver.org)." );
        wms.setName( simpleName( map.label.get() ) );
        // XXX
        // wms.setOnlineResource( "http://localhost:10080/services/Atlas" );
        // wms.setSchemaBaseURL( )
        wms.setOutputStrategy( "SPEED" );

        // XXX make this configurable; deliver all known EPSG codes for now :)
        // wms.setSRS( srs );
        List<Version> versions = new ArrayList<Version>();
        versions.add( new Version( "1.1.1" ) );
        versions.add( new Version( "1.3" ) );
        wms.setVersions( versions );
        geoserver.add( wms );
        log.info( "    loaded WMS: '" + wms.getTitle() + "'" );
    }


    private void internalLoadGeoServer( GeoServerServlet service, GeoServer geoserver ) {
        String proxyUrl = GeoServerPlugin.instance().getBaseUrl();
        log.info( "    proxy URL: " + proxyUrl );

        // GeoServer
        log.info( "Loading GeoServer..." );
        GeoServerInfoImpl gsInfo = new GeoServerInfoImpl( geoserver );
        gsInfo.setTitle( "POLYMAP3 powered by GeoServer :)" );
        gsInfo.setId( "geoserver-polymap3" );
        gsInfo.setProxyBaseUrl( proxyUrl + service.getPathSpec() + "/" );
        // XXX indent XML output, make configurable
        gsInfo.setVerbose( true );
        gsInfo.setVerboseExceptions( true );
        // gsInfo.setGlobalServices( true );
        geoserver.setGlobal( gsInfo );
        log.info( "    loaded GeoServer: '" + gsInfo.getTitle() + "'" );
    }

    protected String simpleName( String s ) {
        // FIXME make replacement configurable
        return Stringer.of( s ).replaceUmlauts().toURIPath( "" ).toString();
    }
}

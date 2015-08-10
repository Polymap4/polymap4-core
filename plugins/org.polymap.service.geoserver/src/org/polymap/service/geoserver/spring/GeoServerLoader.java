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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.Wrapper;
import org.geoserver.catalog.impl.NamespaceInfoImpl;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;
import org.geoserver.config.GeoServer;
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
import org.geotools.data.collection.CollectionDataStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.util.Version;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.parameter.GeneralParameterValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Stringer;

import org.polymap.service.geoserver.GeoServerPlugin;
import org.polymap.service.geoserver.GeoServerServlet;

//import org.vfny.geoserver.global.GeoserverDataDirectory;

/**
 * Initializes GeoServer configuration and catalog on startup.
 * <p>
 * This class is registered in a spring context and post processes the singleton
 * beans {@link Catalog} and {@link GeoServer}, populating them with data from
 * an {@link IMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GeoServerLoader 
        implements BeanPostProcessor, DisposableBean, ApplicationContextAware {

	private static final Log log = LogFactory.getLog(GeoServerLoader.class);

	/** The namespace of all features delivered via GeoServer. */
	public static final String NAMESPACE = "http://www.polymap.org/";

	private GeoServerResourceLoader    resourceLoader;

	private GeoServer                  geoserver;

	// private Map<String,ILayer> layers = new HashMap();
	//


	public GeoServerLoader( GeoServerResourceLoader resourceLoader ) {
		this.resourceLoader = resourceLoader;
	}

	public void destroy() throws Exception {
		if (geoserver != null) {
			geoserver.dispose();
			geoserver = null;
		}
	}


	public void setApplicationContext( ApplicationContext applicationContext )
			throws BeansException {
		System.out.println("Test");
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
        // Catalog
        if (bean instanceof Catalog) {
            if (bean instanceof Wrapper && ((Wrapper)bean).isWrapperFor( Catalog.class )) {
                return bean;
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
        // GeoServer
        if (bean instanceof GeoServer) {
            geoserver = (GeoServer)bean;
            try {
                // find our GeoServerWms / IMap
                GeoServerServlet service = GeoServerServlet.instance.get();
                assert service != null : "No GeoServerServlet registered for this thread!";
                loadGeoServer( service );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
        return bean;
    }


    protected void loadCatalog( Catalog catalog, GeoServerServlet service ) throws Exception {
        log.info( "Loading catalog..." );

        IMap map = service.getMap();
        
        WorkspaceInfoImpl wsInfo = new WorkspaceInfoImpl();
        wsInfo.setId( (String)map.id() );
        wsInfo.setName( map.label.get() + "ws" );
        catalog.add( wsInfo );
        log.info( "    loaded Workspace: '" + wsInfo.getName() +"'");

        NamespaceInfoImpl defaultNsInfo = new NamespaceInfoImpl();
        defaultNsInfo.setId( (String)map.id() );
        defaultNsInfo.setPrefix( "polymap" );
        defaultNsInfo.setURI( NAMESPACE );
        // defaultNsInfo.setPrefix( "gml" );
        // defaultNsInfo.setURI( "http://www.opengis.net/gml" );
        catalog.add( defaultNsInfo );
        log.debug( "    loaded Namespace: '" + defaultNsInfo.getName() +"'");

        for (ILayer layer : map.layers) {
            SimpleFeatureType schema = null;
            DataStore ds = null;

            // try feature/vector resource
//            try {
//                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
//                if (fs == null || fs.getPipeline().length() == 0) {
//                    throw new PipelineIncubationException( "WMS layer? : " + layer.getLabel() );
//                }
//
//                // set name/namespace for target schema
//                Name name = new NameImpl( NAMESPACE, simpleName( layer.getLabel() ) );
//                fs.getPipeline().addFirst( new FeatureRenameProcessor( name ) );
//
//                schema = fs.getSchema();
//                ds = fs.getDataStore();
//            }
//            //
//            catch (PipelineIncubationException e) {
//                // XXX howto skip layer in case of WFS!?
//                String typeName = simpleName( layer.getLabel() );
//                ds = wrapCoverageLayer( layer, typeName );
//                schema = ds.getSchema( typeName );
//            }
//            // no geores found or something
//            catch (Exception e) {
//                log.error( "Error while creating catalog: " + e.getLocalizedMessage() );
//                log.debug( "", e );
//                break;
//            }
                
            // FIXME just coverage/WMS supported so far
            String typeName = simpleName( layer.label.get() );
            ds = wrapCoverageLayer( layer, typeName );
            schema = ds.getSchema( typeName );
            
            // DataStore
            MyDataStoreInfoImpl dsInfo = new MyDataStoreInfoImpl( catalog, ds );
            dsInfo.setId( (String)layer.id() );
            dsInfo.setName( layer.label.get() );
            dsInfo.setDescription( "DataStore of ILayer: " + layer.label.get() );
            dsInfo.setWorkspace( wsInfo );
            dsInfo.setType( "PipelineDataStore" );
            Map params = new HashMap();
// FIXME           params.put( PipelineDataStoreFactory.PARAM_LAYER.key, layer );
            dsInfo.setConnectionParameters( params );
            dsInfo.setEnabled( true );
            catalog.add( dsInfo );
            log.debug( "    loaded DataStore: '" + dsInfo.getName() +"'");

            // FeatureType
            MyFeatureTypeInfoImpl ftInfo = new MyFeatureTypeInfoImpl( catalog, (String)layer.id(), ds );
            ftInfo.setName( schema.getTypeName() );
            ftInfo.setTitle( layer.label.get() );
            ftInfo.setKeywords( new ArrayList( layer.keywords ) );
            ftInfo.setDescription( "FeatureType of ILayer: " + layer.label.get() );
            ftInfo.setStore( dsInfo );
            ftInfo.setNamespace( defaultNsInfo );
// FIXME            ftInfo.setNativeCRS( layer.getCRS() );
            //ftInfo.setNativeBoundingBox( map.getMaxExtent() );
            ftInfo.setNativeName( schema.getTypeName() );
            ftInfo.setProjectionPolicy( ProjectionPolicy.NONE );
            // XXX this the "default" SRS; WFS needs this to work; shouldn't this be the "native"
            // SRS of the data?
// FIXME            ftInfo.setSRS( layer.getCRSCode() );
//            ReferencedEnvelope bbox = map.getMaxExtent();
//            try {
//                Envelope latlon = CRS.transform( bbox, DefaultGeographicCRS.WGS84 );
//                double[] lu = latlon.getLowerCorner().getCoordinate();
//                double[] ro = latlon.getUpperCorner().getCoordinate();
//                ftInfo.setLatLonBoundingBox( new ReferencedEnvelope(
//                        lu[0], ro[0], lu[1], ro[1], CRS.decode( "EPSG:4326" ) ) );
//            }
//            catch (Exception e) {
//                log.warn( e );
//                ftInfo.setLatLonBoundingBox( new ReferencedEnvelope( DefaultGeographicCRS.WGS84 ) );
//            }
            ftInfo.setEnabled( true );

//            List<AttributeTypeInfo> attributeInfos = new ArrayList();
//            for (AttributeDescriptor attribute : schema.getAttributeDescriptors()) {
//                AttributeTypeInfoImpl attributeInfo = new AttributeTypeInfoImpl();
//                attributeInfo.setFeatureType( ftInfo );
//                attributeInfo.setAttribute( attribute );
//                attributeInfo.setId( attribute.toString() );
//            }
//            ftInfo.setAttributes( attributeInfos );
//            catalog.add( ftInfo );
//            log.debug( "    loaded FeatureType: '" + ftInfo.getName() +"'");
//
//            // Layer
//            LayerInfoImpl layerInfo = new LayerInfoImpl();
//            layerInfo.setResource( ftInfo );
//            layerInfo.setId( layer.id() );
//            layerInfo.setName( schema.getTypeName() );
//            layers.put( layerInfo.getName(), layer );
//            layerInfo.setEnabled( true );
//            layerInfo.setType( Type.VECTOR );
//            Set styles = new HashSet();
//
//            StyleInfoImpl style = new StyleInfoImpl( catalog );
//            IStyle layerStyle = layer.getStyle();
//            String styleName = layerStyle.getTitle() != null
//                    ? layerStyle.getTitle() : layer.getLabel() + "-style";
//                    style.setId( simpleName( styleName ) );
//                    style.setName( simpleName( styleName ) );
//
//                    File sldFile = GeoserverDataDirectory.findStyleFile( styleName + ".sld",
//                            true );
//                    if (!sldFile.getParentFile().exists()) {
//                        sldFile.getParentFile().mkdirs();
//                    }
//                    FileUtils.writeStringToFile( sldFile, layerStyle.createSLD( new
//                            NullProgressMonitor() ), "UTF-8" );
//
//                    style.setFilename( sldFile.getName() );
//                    catalog.add( style );
//                    styles.add( style );
//                    layerInfo.setStyles( styles );
//                    layerInfo.setDefaultStyle( style );
//                    catalog.add( layerInfo );
//                    log.debug( "    loaded Layer: '" + layerInfo.getName() +"'");
//            }
        }
    }

    
    protected void loadGeoServer( GeoServerServlet service ) {
        IMap map = service.getMap();

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
        log.info( "    loaded GeoServer: '" + gsInfo.getTitle() +"'");

        // // FIXME configure allowed EPSG codes
        // List<String> srs = new ArrayList();
        // srs.add( "EPSG:31468" );
        // srs.add( "EPSG:4326" );

        // WMS
        WMSInfoImpl wms = new WMSInfoImpl();
        wms.setGeoServer( geoserver );
        wms.setId( simpleName( map.label.get() ) + "-wms" );
        wms.setMaintainer( "" );
        wms.setTitle( simpleName( map.label.get() ) );
        wms.setAbstract( "POLYMAP4 (polymap.org) powered by GeoServer (geoserver.org)." );
        wms.setName( simpleName( map.label.get() ) );
        // XXX
        //wms.setOnlineResource( "http://localhost:10080/services/Atlas" );
        //wms.setSchemaBaseURL( )
        wms.setOutputStrategy( "SPEED" );

        // XXX make this configurable; deliver all known EPSG codes for now :)
        // wms.setSRS( srs );
        List<Version> versions = new ArrayList();
        versions.add( new Version( "1.1.1" ) );
        versions.add( new Version( "1.3" ) );
        wms.setVersions( versions );
        geoserver.add( wms );
        log.info( "    loaded WMS: '" + wms.getTitle() +"'");

        // WFS
        WFSInfoImpl wfs = new WFSInfoImpl();
        wfs.setGeoServer( geoserver );
        // XXX make this configurable (where to get authentication from when TRANSACTIONAL?)
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
        gml.setSrsNameStyle( SrsNameStyle.URN );
        wfs.getGML().put( WFSInfo.Version.V_11 , gml );
        wfs.getVersions().add( new Version( "1.0.0" ) );
        wfs.getVersions().add( new Version( "1.1.0" ) );

        geoserver.add( wfs );
        log.info( "    loaded WFS: '" + wfs.getTitle() +"'");
    }

    
    /**
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
     * (temporary).
     *
     * @param reader the grid coverage reader.
     * @return a feature with the grid coverage envelope as the geometry and the grid
     *         coverage itself in the "grid" attribute.
     */
    protected CollectionDataStore wrapCoverageLayer( ILayer layer, String typeName ) {

        // // create surrounding polygon
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

        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName( typeName );
        ftb.setNamespaceURI( NAMESPACE );
// FIXME        ftb.add( "geom", Polygon.class, layer.getCRS() );
        ftb.add( "layer", ILayer.class );
        ftb.add( "params", GeneralParameterValue[].class );
        SimpleFeatureType schema = ftb.buildFeatureType();

        // create the feature
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder( schema );
        fb.add( null );
        fb.add( layer );
        SimpleFeature feature = fb.buildFeature( null );

        DefaultFeatureCollection coll = new DefaultFeatureCollection( null, null );
        coll.add( feature );

        return new CollectionDataStore( coll );
    }


//    private void loadCatalog( Catalog catalog ) {
//        log.debug( "Loading catalog..." );
//        WorkspaceInfo wsInfo = createDummyWorkspace( catalog );
//        NamespaceInfoImpl defaultNsInfo = createNamespace( catalog );
//        createDummyLayer( catalog, wsInfo, defaultNsInfo );
//    }


//    private NamespaceInfoImpl createNamespace( Catalog catalog ) {
//        NamespaceInfoImpl defaultNsInfo = new NamespaceInfoImpl();
//        defaultNsInfo.setId( getMapId() );
//        defaultNsInfo.setPrefix( "polymap" );
//        defaultNsInfo.setURI( NAMESPACE );
//        // defaultNsInfo.setPrefix( "gml" );
//        // defaultNsInfo.setURI( "http://www.opengis.net/gml" );
//        catalog.add( defaultNsInfo );
//        log.debug( "    loaded Namespace: '" + defaultNsInfo.getName() + "'" );
//        return defaultNsInfo;
//    }


//    private WorkspaceInfo createDummyWorkspace( Catalog catalog ) {
//        if (catalog.getWorkspaces().size() == 0) {
//            WorkspaceInfoImpl wsInfo = new WorkspaceInfoImpl();
//            wsInfo.setId( getMapId() );
//            wsInfo.setName( getMapLabel() + "ws" );
//            catalog.add( wsInfo );
//            log.debug( "    loaded Workspace: '" + wsInfo.getName() + "'" );
//            return wsInfo;
//        }
//        else {
//            return catalog.getWorkspaces().get( 0 );
//        }
//    }


//    private void createDummyLayer( Catalog catalog, WorkspaceInfo wsInfo, NamespaceInfoImpl defaultNsInfo ) {
//        SimpleFeatureType schema = null;
//        DataStore ds = null;
//
//        String typeName = simpleName( getLayerLabel() );
//        ds = wrapCoverageLayer( typeName );
//        try {
//            schema = ds.getSchema( typeName );
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // DataStore
//        DataStoreInfo dsInfo = createDummyDataStore( catalog, wsInfo, ds );
//
//        // FeatureType
//        MyFeatureTypeInfoImpl ftInfo = createDummyFeatureType( catalog, defaultNsInfo, schema, ds, dsInfo );
//
//        // Layer
//        createDummyLayer( catalog, schema, ftInfo );
//    }
//

//	protected org.geotools.data.DataStore wrapCoverageLayer(String typeName) {
//		SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
//		ftb.setName(typeName);
//		ftb.setNamespaceURI(NAMESPACE);
//		ftb.add("geom", Polygon.class, getLayerCRS());
//		ftb.add("params", GeneralParameterValue[].class);
//		final SimpleFeatureType simpleFeatureType = ftb.buildFeatureType();
//
//		// create the feature
//		SimpleFeatureBuilder fb = new SimpleFeatureBuilder(simpleFeatureType);
//		fb.add(null);
//		final SimpleFeature feature = fb.buildFeature(null);
//
//		return new ContentDataStore() {
//
//			@Override
//			protected List<Name> createTypeNames() throws IOException {
//				return Lists.newArrayList(simpleFeatureType.getName());
//			}
//
//			@Override
//			protected ContentFeatureSource createFeatureSource(
//					ContentEntry entry) throws IOException {
//				return new ContentFeatureSource(entry, null) {
//
//					@Override
//					protected SimpleFeatureType buildFeatureType()
//							throws IOException {
//						entries.put(simpleFeatureType.getName(), entry);
//						return simpleFeatureType;
//					}
//
//					@Override
//					protected ReferencedEnvelope getBoundsInternal(Query arg0)
//							throws IOException {
//						return new ReferencedEnvelope();
//					}
//
//					@Override
//					protected int getCountInternal(Query arg0)
//							throws IOException {
//						return 1;
//					}
//
//					@Override
//					protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
//							Query arg0) throws IOException {
//						return new FeatureReader<SimpleFeatureType, SimpleFeature>() {
//							private boolean first = true;
//
//							@Override
//							public void close() throws IOException {
//							}
//
//							@Override
//							public SimpleFeatureType getFeatureType() {
//								return simpleFeatureType;
//							}
//
//							@Override
//							public boolean hasNext() throws IOException {
//								return first;
//							}
//
//							@Override
//							public SimpleFeature next() throws IOException,
//									IllegalArgumentException,
//									NoSuchElementException {
//								first = false;
//								return feature;
//							}
//						};
//					}
//
//				};
//			}
//		};
//	}

//
//    private void createDummyLayer( Catalog catalog, SimpleFeatureType schema, MyFeatureTypeInfoImpl ftInfo ) {
//        LayerInfoImpl layerInfo = new LayerInfoImpl();
//        layerInfo.setResource( ftInfo );
//        layerInfo.setId( getLayerId() );
//        layerInfo.setName( schema.getTypeName() );
//        layerInfo.setEnabled( true );
//        layerInfo.setType( PublishedType.VECTOR );
//        Set styles = new HashSet();
//
//        StyleInfoImpl style = new StyleInfoImpl( catalog );
//        String styleName = getLayerStyleTitle() != null ? getLayerStyleTitle() : getLayerLabel() + "-style";
//        style.setId( simpleName( styleName ) );
//        style.setName( simpleName( styleName ) );
//
//        Resource sldResource = new GeoServerDataDirectory( this.resourceLoader ).config( style );
//        File sldFile = sldResource.file();
//        if (!sldFile.getParentFile().exists()) {
//            sldFile.getParentFile().mkdirs();
//        }
//        try {
//            FileUtils.writeStringToFile( sldFile, createLayerStyleSLD( new NullProgressMonitor() ), "UTF-8" );
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        style.setFilename( sldFile.getName() );
//        catalog.add( style );
//        styles.add( style );
//        layerInfo.setStyles( styles );
//        layerInfo.setDefaultStyle( style );
//        catalog.add( layerInfo );
//        log.debug( "    loaded Layer: '" + layerInfo.getName() + "'" );
//    }
//
//
//    private String createLayerStyleSLD( NullProgressMonitor nullProgressMonitor ) {
//        // copied from
//        // http://docs.geoserver.org/stable/en/user/styling/sld-introduction.html
//        StringBuilder sb = new StringBuilder();
//        sb.append( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );
//        sb.append( "<StyledLayerDescriptor version=\"1.0.0\"" );
//        sb.append( "    xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\"" );
//        sb.append( "    xmlns=\"http://www.opengis.net/sld\"" );
//        sb.append( "    xmlns:ogc=\"http://www.opengis.net/ogc\"" );
//        sb.append( "    xmlns:xlink=\"http://www.w3.org/1999/xlink\"" );
//        sb.append( "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" );
//        sb.append( "  <NamedLayer>" );
//        sb.append( "    <Name>" + getMapLabel() + "</Name>" );
//        sb.append( "    <UserStyle>" );
//        sb.append( "      <Title>GeoServer SLD Cook Book: Simple point</Title>" );
//        sb.append( "      <FeatureTypeStyle>" );
//        sb.append( "        <Rule>" );
//        sb.append( "          <PointSymbolizer>" );
//        sb.append( "            <Graphic>" );
//        sb.append( "              <Mark>" );
//        sb.append( "                <WellKnownName>circle</WellKnownName>" );
//        sb.append( "                <Fill>" );
//        sb.append( "                  <CssParameter name=\"fill\">#FF0000</CssParameter>" );
//        sb.append( "                </Fill>" );
//        sb.append( "              </Mark>" );
//        sb.append( "              <Size>6</Size>" );
//        sb.append( "            </Graphic>" );
//        sb.append( "          </PointSymbolizer>" );
//        sb.append( "        </Rule>" );
//        sb.append( "      </FeatureTypeStyle>" );
//        sb.append( "    </UserStyle>" );
//        sb.append( "  </NamedLayer>" );
//        sb.append( "</StyledLayerDescriptor>" );
//        return sb.toString();
//    }
//
//
//    private String getLayerStyleTitle() {
//        return "MyDummyLayerStyleTitle";
//    }
//
//
//    private MyFeatureTypeInfoImpl createDummyFeatureType( Catalog catalog, NamespaceInfoImpl defaultNsInfo,
//            SimpleFeatureType schema, DataStore ds, DataStoreInfo dsInfo ) {
//        MyFeatureTypeInfoImpl ftInfo = new MyFeatureTypeInfoImpl( catalog, getLayerId(), ds );
//        ftInfo.setName( schema.getTypeName() );
//        ftInfo.setTitle( getLayerLabel() );
//        ftInfo.setKeywords( new ArrayList<KeywordInfo>( getLayerKeywords() ) );
//        ftInfo.setDescription( "FeatureType of ILayer: " + getLayerLabel() );
//        ftInfo.setStore( dsInfo );
//        ftInfo.setNamespace( defaultNsInfo );
//        ftInfo.setNativeCRS( getLayerCRS() );
//        // ftInfo.setNativeBoundingBox( map.getMaxExtent() );
//        ftInfo.setNativeName( schema.getTypeName() );
//        ftInfo.setProjectionPolicy( ProjectionPolicy.NONE );
//        // XXX this the "default" SRS; WFS needs this to work; shouldn't
//        // this be the the "native"
//        // SRS of the data?
//        ftInfo.setSRS( getLayerCRSCode() );
//        ReferencedEnvelope bbox = getDummyMaxExtent();
//        try {
//            org.opengis.geometry.Envelope latlon = CRS.transform( bbox, DefaultGeographicCRS.WGS84 );
//            double[] lu = latlon.getLowerCorner().getCoordinate();
//            double[] ro = latlon.getUpperCorner().getCoordinate();
//            ftInfo.setLatLonBoundingBox( new ReferencedEnvelope( lu[0], ro[0], lu[1], ro[1], CRS.decode( "EPSG:4326" ) ) );
//        }
//        catch (Exception e) {
//            log.warn( e );
//            ftInfo.setLatLonBoundingBox( new ReferencedEnvelope( DefaultGeographicCRS.WGS84 ) );
//        }
//        ftInfo.setEnabled( true );
//
//        List<AttributeTypeInfo> attributeInfos = new ArrayList();
//        for (AttributeDescriptor attribute : schema.getAttributeDescriptors()) {
//            AttributeTypeInfoImpl attributeInfo = new AttributeTypeInfoImpl();
//            attributeInfo.setFeatureType( ftInfo );
//            attributeInfo.setAttribute( attribute );
//            attributeInfo.setId( attribute.toString() );
//        }
//        ftInfo.setAttributes( attributeInfos );
//        catalog.add( ftInfo );
//        log.debug( "    loaded FeatureType: '" + ftInfo.getName() + "'" );
//        return ftInfo;
//    }
//
//
//    private ReferencedEnvelope getDummyMaxExtent() {
//        return new ReferencedEnvelope();
//    }
//
//
//    private String getLayerCRSCode() {
//        return "EPSG:4326";
//    }
//
//
//    private CoordinateReferenceSystem getLayerCRS() {
//        return null;
//    }
//
//
//    private Collection<? extends KeywordInfo> getLayerKeywords() {
//        return Collections.EMPTY_LIST;
//    }
//
//
//    private String getLayerLabel() {
//        return "My Dummy Layer";
//    }
//
//
//    private String getLayerId() {
//        return "MyDummyLayer";
//    }
//

//    private DataStoreInfo createDummyDataStore( Catalog catalog, WorkspaceInfo wsInfo, DataStore ds ) {
//        DataStoreInfo existingDataStore = catalog.getDataStoreByName( getLayerLabel() );
//        if (existingDataStore == null) {
//            MyDataStoreInfoImpl dsInfo = new MyDataStoreInfoImpl( catalog, ds );
//            dsInfo.setId( getLayerId() );
//            dsInfo.setName( getLayerLabel() );
//            dsInfo.setDescription( "DataStore of ILayer: " + getLayerLabel() );
//            dsInfo.setWorkspace( wsInfo );
//            dsInfo.setType( "PipelineDataStore" );
//            Map params = new HashMap();
//            dsInfo.setConnectionParameters( params );
//            dsInfo.setEnabled( true );
//            catalog.add( dsInfo );
//            log.debug( "    loaded DataStore: '" + dsInfo.getName() + "'" );
//            return dsInfo;
//        }
//        else {
//            return existingDataStore;
//        }
//    }

//
//    private String getMapLabel() {
//        return "My Dummy Map";
//    }
//
//
//    private String getMapId() {
//        return "MyDummyMap";
//    }
//

//    protected void loadGeoServer( GeoServerServlet service ) {
//        String proxyUrl = getBaseUrl();
//        log.debug( "    proxy URL: " + proxyUrl );
//
//        // GeoServer
//        log.debug( "Loading GeoServer..." );
//        GeoServerInfoImpl gsInfo = new GeoServerInfoImpl( geoserver );
//        gsInfo.setTitle( "POLYMAP3 powered by GeoServer :)" );
//        gsInfo.setId( "geoserver-polymap3" );
//        gsInfo.setProxyBaseUrl( proxyUrl + service.getPathSpec() + "/" );
//        // XXX indent XML output, make configurable
//        gsInfo.setVerbose( true );
//        gsInfo.setVerboseExceptions( true );
//        // gsInfo.setGlobalServices( true );
//        geoserver.setGlobal( gsInfo );
//        log.debug( "    loaded GeoServer: '" + gsInfo.getTitle() + "'" );
//
//        // // FIXME configure allowed EPSG codes
//        // List<String> srs = new ArrayList();
//        // srs.add( "EPSG:31468" );
//        // srs.add( "EPSG:4326" );
//
//        // WMS
//        WMSInfoImpl wms = new WMSInfoImpl();
//        wms.setGeoServer( geoserver );
//        wms.setId( simpleName( getMapLabel() ) + "-wms" );
//        wms.setMaintainer( "" );
//        wms.setTitle( simpleName( getMapLabel() ) );
//        wms.setAbstract( "POLYMAP3 (polymap.org) powered by GeoServer (geoserver.org)." );
//        wms.setName( simpleName( getMapLabel() ) );
//        // XXX
//        // wms.setOnlineResource( "http://localhost:10080/services/Atlas" );
//        // wms.setSchemaBaseURL( )
//        wms.setOutputStrategy( "SPEED" );
//
//        // XXX make this configurable; deliver all known EPSG codes for now :)
//        // wms.setSRS( srs );
//        List<Version> versions = new ArrayList();
//        versions.add( new Version( "1.1.1" ) );
//        versions.add( new Version( "1.3" ) );
//        wms.setVersions( versions );
//        geoserver.add( wms );
//        log.debug( "    loaded WMS: '" + wms.getTitle() + "'" );
//
//        // WFS
//        WFSInfoImpl wfs = new WFSInfoImpl();
//        wfs.setGeoServer( geoserver );
//        // XXX make this configurable (where to get authentication from when
//        // TRANSACTIONAL?)
//        wfs.setServiceLevel( ServiceLevel.BASIC );
//        wfs.setId( simpleName( getMapLabel() ) + "-wfs" );
//        wfs.setMaintainer( "" );
//        wfs.setTitle( simpleName( getMapLabel() ) );
//        wfs.setName( simpleName( getMapLabel() ) + "-wfs" );
//        // XXX
//        // wfs.setOnlineResource( "http://localhost:10080/services/Atlas" );
//        wfs.setOutputStrategy( "SPEED" );
//        // wfs.set( srs );
//        // List<Version> versions = new ArrayList();
//        // versions.add( new Version( "1.1.1" ) );
//        // versions.add( new Version( "1.3" ) );
//        // wfs.setVersions( versions );
//
//        // GML2
//        GMLInfo gml = new GMLInfoImpl();
//        Boolean srsXmlStyle = true;
//        if (srsXmlStyle) {
//            gml.setSrsNameStyle( SrsNameStyle.XML );
//        }
//        else {
//            gml.setSrsNameStyle( SrsNameStyle.NORMAL );
//        }
//        wfs.getGML().put( WFSInfo.Version.V_10, gml );
//
//        // GML3
//        gml = new GMLInfoImpl();
//        gml.setSrsNameStyle( SrsNameStyle.URN );
//        wfs.getGML().put( WFSInfo.Version.V_11, gml );
//        wfs.getVersions().add( new Version( "1.0.0" ) );
//        wfs.getVersions().add( new Version( "1.1.0" ) );
//
//        geoserver.add( wfs );
//        log.debug( "    loaded WFS: '" + wfs.getTitle() + "'" );
//    }
//
//
//    private String getBaseUrl() {
//        return "/";
//    }
//

	protected String simpleName(String s) {
		// FIXME make replacement configurable
		return Stringer.of(s).replaceUmlauts().toURIPath("").toString();
	}
}

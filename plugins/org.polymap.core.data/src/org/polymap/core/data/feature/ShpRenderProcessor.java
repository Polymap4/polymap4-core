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

package org.polymap.core.data.feature;


/**
 * Example code to render a shapefile. Currently it is not used because we
 * handle all features via {@link FeatureRenderProcessor2}. Once we use it again
 * it has to moved to the catalog.shp package.
 * 
 * @deprecated Kept here  to save the code.
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> <li>19.10.2009:
 *         created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ShpRenderProcessor {
/*        extends FeatureRenderProcessor
        implements PipelineProcessor {

    private static final Log log = LogFactory.getLog( ShpRenderProcessor.class );

    
    public ShpRenderProcessor( DataStore dataStore, Collection<ILayer> layers ) {
        super( dataStore, layers );
        this.dataStore = dataStore;
        this.layers = layers;
    }


    public boolean handles( Class requestClass ) {
        return EncodedImage.GetDataRequest.class.isAssignableFrom( requestClass );
    }

    
    public void processRequest( ProcessorRequest r, ProcessorContext context )
            throws Exception {
        AwtImage.GetDataRequest request = (AwtImage.GetDataRequest)r;
        log.debug( "    DataStore:" + dataStore );
        
        // render
        long start = System.currentTimeMillis();
        Image image = render( request.getImageSize(), request.getBBox() );
        log.debug( "   ...done: (" + (System.currentTimeMillis()-start) + "ms)." );

        // response
        context.sendResponse( new AwtImage.GetDataResponse( image ) );
        context.sendResponse( ProcessorResponse.EOP );
    }

    
    public void processResponse( ProcessorResponse reponse, ProcessorContext context )
            throws Exception {
        throw new IllegalStateException( "This is a terminal processor." );
    }


    private Image render( Dimension imageSize, ReferencedEnvelope bbox ) {
        // mapContext
        MapContext mapContext = new DefaultMapContext( bbox.getCoordinateReferenceSystem() );
        CoordinateReferenceSystem dataCRS = null;
        //mapContext.setAreaOfInterest( bbox, bbox.getCoordinateReferenceSystem() );
        for (ILayer layer : layers) {
            try {
                log.debug( "        BBOX: " + bbox + ", CRS:" + bbox.getCoordinateReferenceSystem().getName() );
                //FeatureSource fs = (FeatureSource)layer.findGeoResource( FeatureSource.class );
                log.debug( "        GeoResource: " + layer.getGeoResource() );
                IGeoResource geores = layer.getGeoResource();
                FeatureSource fs = geores.resolve( FeatureSource.class, null );
                log.debug( "            FeatureSource: " + fs.getName() );
//                log.debug( "            Features: " + fs.getFeatures().size() );
//                for (Iterator it=fs.getFeatures( Query.ALL ).iterator(); it.hasNext(); ) {
//                    Object feature = it.next();
//                    System.out.println( "              feature: " + feature );
//                }

                IndexedShapefileDataStore ds = (IndexedShapefileDataStore)dataStore;
                String typeName = ds.getTypeNames()[0];
                log.debug( "Type name: " + typeName );
                FeatureSource featureSource = ds.getFeatureSource();
                FeatureType schema = featureSource.getSchema();
                log.debug( "            FeatureType name: " + schema.getName() );
                log.debug( "            FeatureType geom: " + schema.getGeometryDescriptor().getType() );
                //log.debug( "            Shapefile attrs: " + schema. );
                
                dataCRS = layer.getCRSCode() != null ? layer.getCRS() : null;
                
                mapContext.addLayer( fs, layer.getStyle() );
            }
            catch (IOException e) {
                e.printStackTrace();
                // FIXME set layer status and statusMessage
            }
        }
        
        // render
        Graphics2D g = null;
        try {
            // result image
            BufferedImage result = new BufferedImage(
                    imageSize.width, imageSize.height, BufferedImage.TYPE_4BYTE_ABGR);
            g = result.createGraphics();

            // rendering hints
            RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY );
            hints.add( new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON ) );
            g.setRenderingHints( hints );
            
            ShapefileRenderer renderer = new ShapefileRenderer();
            renderer.setContext( mapContext );

            // render params
            Map rendererParams = new HashMap();
            rendererParams.put( "optimizedDataLoadingEnabled", Boolean.TRUE );
            if (dataCRS != null) {
                rendererParams.put( ShapefileRenderer.FORCE_CRS_KEY, dataCRS );
            }
            renderer.setRendererHints( rendererParams );

            Rectangle paintArea = new Rectangle( (int)imageSize.getWidth(), (int)imageSize.getHeight() );
            renderer.paint( g, paintArea, bbox );
            return result;
        }
        finally {
            if (g != null) { g.dispose(); }
        }
    }*/
    
}

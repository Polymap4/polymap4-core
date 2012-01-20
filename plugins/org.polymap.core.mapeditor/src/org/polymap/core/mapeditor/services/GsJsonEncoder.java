package org.polymap.core.mapeditor.services;

import java.util.List;
import java.util.Set;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.geoserver.wfs.response.GeoJSONBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * GeoJSON server based on {@link GeoJSONBuilder} from GeoServer. The encoder strips
 * all non-geometry properties from the features. It just encodes geoetries.
 * Coordinates are encoded with all decimals. This makes output big and slow for big
 * geometries compared to {@link GtJsonEncoder}.
 */
class GsJsonEncoder 
        extends JsonEncoder {

    private static final Log log = LogFactory.getLog( GsJsonEncoder.class );

    
    /**
     * <p>
     * Initially found at {@link org.geoserver.wfs.response.GeoJSONOutputFormat}
     * 
     * @param fc
     * @param out
     * @throws IOException
     * @throws ServiceException
     */
    public void encode( CountingOutputStream out, String encoding )
    throws IOException {

        // XXX investigate setting proper charsets in this
        // it's part of the constructor, just need to hook it up.
        Writer outWriter = new BufferedWriter( new OutputStreamWriter( out, encoding ) );

        GeoJSONBuilder jsonWriter = new GeoJSONBuilder( outWriter );

        // Generate bounds for every feature?
        boolean featureBounding = false;  //wfs.isFeatureBounding();
        boolean hasGeom = false;

        try {
            jsonWriter.object().key( "type" ).value( "FeatureCollection" );
            jsonWriter.key( "features" );
            jsonWriter.array();

            //            MathTransform transform = !mapCRS.equals( featureCRS )
            //                    ? CRS.findMathTransform( featureCRS, mapCRS, true )
            //                    : null;

            SimpleFeatureType fType;
            List<AttributeDescriptor> types;

            int featureCount = 0;
            for (Feature f : features) {
                // check byte limit
                if (out.getCount() > maxBytes) {
                    log.warn( "    Byte limit reached. Features encoded: " + featureCount );
                    final int encoded = featureCount;
                    display.asyncExec( new Runnable() {
                        public void run() {
                            MessageDialog.openInformation(
                                    PolymapWorkbench.getShellToParentOn(),
                                    Messages.get( "GsJsonEncoder_toManyFeatures_title" ),
                                    Messages.get( "GsJsonEncoder_toManyFeatures_msg", features.size(), encoded ) );
                        }
                    } );
                    break;
                }
                      
                SimpleFeature feature = (SimpleFeature)f;
                jsonWriter.object();
                jsonWriter.key( "type" ).value( "Feature" );
                jsonWriter.key( "id" ).value( feature.getID() );

                fType = feature.getFeatureType();
                types = fType.getAttributeDescriptors();

                GeometryDescriptor defaultGeomType = fType.getGeometryDescriptor();

                //                    if (crs == null && defaultGeomType != null) {
                //                        crs = fType.getGeometryDescriptor().getCoordinateReferenceSystem();
                //                        try {
                //                            transform = CRS.findMathTransform( crs, mapCRS, true );
                //                        }
                //                        catch (FactoryException e) {
                //                            log.warn( "", e );
                //                        }
                //                    }
                jsonWriter.key( "geometry" );
                Geometry aGeom = (Geometry)feature.getDefaultGeometry();

                if (aGeom == null) {
                    // In case the default geometry is not set, we will
                    // just use the first geometry we find
                    for (int j = 0; j < types.size() && aGeom == null; j++) {
                        Object value = feature.getAttribute( j );
                        if (value != null && value instanceof Geometry) {
                            aGeom = (Geometry)value;
                        }
                    }
                }
                // Write the geometry, whether it is a null or not
                if (aGeom != null) {
                    try {
                        // XXX find transform outside the loop
                        CoordinateReferenceSystem featureCRS = fType.getGeometryDescriptor().getCoordinateReferenceSystem();
                        MathTransform transform = featureCRS != null
                                ? CRS.findMathTransform( featureCRS, mapCRS, true )
                                : null;
                        Geometry aGeom2 = transform != null 
                                ? JTS.transform( aGeom, transform )
                                : aGeom;
                        jsonWriter.writeGeom( aGeom2 );
                    }
                    catch (Exception e) {
                        log.warn( "", e );
                        jsonWriter.writeGeom( aGeom );
                    }
                    hasGeom = true;
                }
                else {
                    jsonWriter.value( null );
                }
                if (defaultGeomType != null) {
                    jsonWriter.key( "geometry_name" ).value( defaultGeomType.getLocalName() );
                }

                jsonWriter.key( "properties" );
                jsonWriter.object();

                jsonWriter.endObject(); // end the properties
                jsonWriter.endObject(); // end the feature
                featureCount++;
            }

            jsonWriter.endArray(); // end features

            // Coordinate Referense System, currently only if the namespace is
            // EPSG
            if (mapCRS != null) {
                Set<ReferenceIdentifier> ids = mapCRS.getIdentifiers();
                // WKT defined crs might not have identifiers at all
                if (ids != null && ids.size() > 0) {
                    NamedIdentifier namedIdent = (NamedIdentifier)ids.iterator().next();
                    String csStr = namedIdent.getCodeSpace().toUpperCase();

                    if (csStr.equals( "EPSG" )) {
                        jsonWriter.key( "crs" );
                        jsonWriter.object();
                        jsonWriter.key( "type" ).value( csStr );
                        jsonWriter.key( "properties" );
                        jsonWriter.object();
                        jsonWriter.key( "code" );
                        jsonWriter.value( namedIdent.getCode() );
                        jsonWriter.endObject(); // end properties
                        jsonWriter.endObject(); // end crs
                    }
                }
            }

//            // Bounding box for featurecollection
//            if (hasGeom) {
//                ReferencedEnvelope e = null;
//                //                 for (int i = 0; i < resultsList.size(); i++) {
//                //                     FeatureCollection collection = (FeatureCollection)resultsList.get( i );
//                if (e == null) {
//                    e = fc.getBounds();
//                }
//                else {
//                    e.expandToInclude( fc.getBounds() );
//                }
//
//                //                 }
//
//                if (e != null) {
//                    log.warn( "featureBounding is commented out." );
//                    //jsonWriter.writeBoundingBox( e );
//                }
//            }

            jsonWriter.endObject(); // end featurecollection
            outWriter.flush();
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e );
        }

    }
    
}
/**
 * 
 */
package org.polymap.catalog.dxf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResourceInfo;
import org.eclipse.core.runtime.IStatus;
import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

class DxfGeoResourceInfo
        extends IGeoResourceInfo {

    private final DxfGeoResourceImpl dxfResource;

    private SimpleFeatureType        featureType;

    private ResourceInfo             header;


    /**
     * Connect to Shapefile and grab header information.
     * 
     * @param shpGeoResourceImpl
     * @throws IOException
     */
    DxfGeoResourceInfo( DxfGeoResourceImpl shpGeoResourceImpl )
            throws IOException {
        dxfResource = shpGeoResourceImpl;
        featureType = dxfResource.parent.getDS( null ) .getSchema();
        try {
            FeatureSource<SimpleFeatureType, SimpleFeature> source = dxfResource.parent
                    .getDS( null ).getFeatureSource();
            header = source.getInfo();
            bounds = header.getBounds();

            Envelope tmpBounds = source.getBounds();
            if (tmpBounds instanceof ReferencedEnvelope) {
                bounds = (ReferencedEnvelope)tmpBounds;
            }
            if (tmpBounds != null) {
                bounds = new ReferencedEnvelope( tmpBounds, getCRS() );
            }

            if (bounds == null) {
                bounds = new ReferencedEnvelope( new Envelope(), getCRS() );
                FeatureIterator<SimpleFeature> iter = source.getFeatures().features();
                try {
                    while (iter.hasNext()) {
                        SimpleFeature element = iter.next();
                        if (bounds.isNull())
                            bounds.init( element.getBounds() );
                        else
                            bounds.include( element.getBounds() );
                    }
                }
                finally {
                    iter.close();
                }
            }
        }
        catch (Exception e) {
            CatalogPlugin.getDefault().getLog().log(
                    new org.eclipse.core.runtime.Status( IStatus.WARNING,
                            "net.refractions.udig.catalog", 0, "Messages.ShpGeoResourceImpl_error_layer_bounds", e ) ); //$NON-NLS-1$
            bounds = new ReferencedEnvelope( new Envelope(), getCRS() );
        }

        keywords = new String[] { ".shp", "Shapefile", //$NON-NLS-1$ //$NON-NLS-2$
                featureType.getName().getLocalPart(), featureType.getName().getNamespaceURI() };

        title = featureType.getName().getLocalPart();
        title = title.replace( '_', ' ' );
        title = title.trim();
    }


    public CoordinateReferenceSystem getCRS() {
        return featureType.getCoordinateReferenceSystem();
    }


    public String getName() {
        return featureType.getName().getLocalPart();
    }


    public URI getSchema() {
        try {
            return new URI( featureType.getName().getNamespaceURI() );
        }
        catch (URISyntaxException e) {
            return null;
        }
    }


    public String getTitle() {
        return title;
    }


    /**
     * Description of shapefile contents.
     * 
     * @return description of Shapefile Contents
     */
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }
}
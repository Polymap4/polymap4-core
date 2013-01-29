package org.polymap.core.project.ui.properties;

import java.util.HashSet;

import net.refractions.udig.catalog.IGeoResource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import com.google.common.collect.Sets;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.jface.viewers.ICellEditorValidator;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerStatus;
import org.polymap.core.project.Messages;
import org.polymap.core.project.operations.SetPropertyOperation;

/**
 * The property source of an {@link ILayer}. Allows the user to directly
 * change some properties.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class LayerPropertySource
        implements IPropertySource2 {

    private static Log log = LogFactory.getLog( LayerPropertySource.class );
    
    private ILayer                  layer;

    private IGeoResource            geores;
    
    
    public LayerPropertySource( ILayer layer ) {
        this.layer = layer;
        this.geores = layer.getGeoResource();
    }


    protected String i18n( String key, Object...args ) {
        return Messages.get( "LayerProperty_" + key, args );
    }
    
    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        
        // opacity descriptor
        TextPropertyDescriptor opacityDescr = new TextPropertyDescriptor( ILayer.PROP_OPACITY, i18n( "label_opacity" ) );
        opacityDescr.setValidator( new ICellEditorValidator() {
            public String isValid( Object value ) {
                try {
                    int i = Integer.parseInt( (String)value );
                    if (i < 0 || i > 100) {
                        return i18n( "invalidOpacity", i );
                    } else {
                        return null;
                    }
                } 
                catch (NumberFormatException e) {
                    return i18n( "invalidOpacity", value );
                }
            }
        });
        // all other descriptors
        IPropertyDescriptor[] result = new IPropertyDescriptor[] {
                new TextPropertyDescriptor( ILayer.PROP_LABEL, i18n( "label_name" ) ),
                new TextPropertyDescriptor( ILayer.PROP_KEYWORDS, i18n( "label_keywords" ) ),
                new CrsPropertyDescriptor( ILayer.PROP_CRSCODE, i18n( "label_crs" ) ),
                new PropertyDescriptor( "maxExtent", i18n( "label_maxExtent" ) ),
                new PropertyDescriptor( "datacrs", i18n( "label_dataCrs" ) ),
                new PropertyDescriptor( ILayer.PROP_GEORESID, i18n( "label_geores" ) ),
                new TextPropertyDescriptor( ILayer.PROP_ORDERKEY, i18n( "label_zPriority" ) ),
                opacityDescr
        };
        return result;
    }

    
    public Object getPropertyValue( Object id ) {
        //layer.setLayerStatus( LayerStatus.STATUS_OK );
        try {
            if (id.equals( ILayer.PROP_LABEL )) {
                return layer.getLabel();
            }
            else if (id.equals( ILayer.PROP_KEYWORDS )) {
                String value = StringUtils.join( layer.getKeywords(), ", " );
                return value != null ? value : "";
            }
            else if (id.equals( "type" )) {
                return geores != null
                        ? StringUtils.substringAfterLast( geores.getClass().getName(), "." )
                        : i18n( "noGeoRes" );
            }
            else if (id.equals( ILayer.PROP_CRSCODE )) {
                return layer.getCRS();
            }
            else if (id.equals( "maxExtent" )) {
                if (geores != null) {
                    ReferencedEnvelope bounds = geores.getInfo( new NullProgressMonitor() ).getBounds();
                    if (bounds != null) {
                        return new EnvelopPropertySource( bounds );
                    }
                }
                return i18n( "unknownValue" );
            }
            else if (id.equals( "datacrs" )) {
                if (geores != null) {
                    CoordinateReferenceSystem crs = geores.getInfo( new NullProgressMonitor() ).getCRS();
                    if (crs != null) {
                        String srs = CRS.toSRS( crs );
                        if (srs != null) {
                            return srs;
                        }
                    }
                }
                return i18n( "unknownValue" );
            }
            else if (id.equals( ILayer.PROP_GEORESID )) {
                return geores != null ? geores.getIdentifier() : i18n( "noGeoRes" );
            }
            else if (id.equals( ILayer.PROP_ORDERKEY )) {
                Integer result = layer.getOrderKey();
                return result != null ? result.toString() : "0";
            }
            else if (id.equals( ILayer.PROP_OPACITY )) {
                Integer result = layer.getOpacity();
                return result != null ? result.toString() : "100";
            }
            else {
                return i18n( "unknownValue" );
            }
        }
        catch (Exception e) {
            log.error( "Error while getting property: " + id, e );
            LayerStatus error = new LayerStatus( LayerStatus.ERROR, LayerStatus.UNSPECIFIED, i18n( "valueError", id ), e );
            if (layer.getLayerStatus().isOK()) {
                layer.setLayerStatus( error );
            } else {
                layer.getLayerStatus().add( error );
            }
            return "Fehler: " + e.getMessage();
        }
    }

    
    public void setPropertyValue( Object id, Object value ) {
        try {
            SetPropertyOperation op = new SetPropertyOperation();
            
            if (id.equals( ILayer.PROP_LABEL )) {
                op.init( ILayer.class, layer, ILayer.PROP_LABEL, value );
                OperationSupport.instance().execute( op, false, false );
            }
            else if (id.equals( ILayer.PROP_KEYWORDS )) {
                String[] array = StringUtils.split( (String)value, ", " );
                HashSet<String> keywords = Sets.newHashSet( array );
                log.info( "Setting keywords: " + keywords );
                op.init( ILayer.class, layer, ILayer.PROP_KEYWORDS, keywords );
                OperationSupport.instance().execute( op, false, false );
            }
            else if (id.equals( ILayer.PROP_ORDERKEY )) {
                op.init( ILayer.class, layer, ILayer.PROP_ORDERKEY, new Integer( value.toString() ) );
                OperationSupport.instance().execute( op, false, false );
            }
            else if (id.equals( ILayer.PROP_OPACITY )) {
                op.init( ILayer.class, layer, ILayer.PROP_OPACITY, new Integer( value.toString() ) );
                OperationSupport.instance().execute( op, false, false );
            }
            else if (id.equals( ILayer.PROP_CRSCODE )) {
                CoordinateReferenceSystem crs = (CoordinateReferenceSystem)value;
                op.init( ILayer.class, layer, ILayer.PROP_CRSCODE, crs );
                OperationSupport.instance().execute( op, false, false );
            }
            else {
                log.error( "Property is read-only: " + id );
            }
        }
        catch (Exception e) {
            log.error( "Error while changing property: " + id, e );
        }
    }
    
    public boolean isPropertyResettable( Object id ) {
        return false;
    }

    public boolean isPropertySet( Object id ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public Object getEditableValue() {
        return null;
    }

    public void resetPropertyValue( Object id ) {
        throw new RuntimeException( "not yet implemented." );
    }

}
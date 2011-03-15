package org.polymap.core.project.ui;

import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.qi4j.operations.SetPropertyOperation;

/**
 * The property source for an {@link ILayer}. Allows the user to directly
 * change some properties.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class LayerPropertySource
        implements IPropertySource2 {

    private static Log log = LogFactory.getLog( LayerPropertySource.class );
    
    private ILayer                  layer;

    
    /**
     * 
     */
    public LayerPropertySource( ILayer layer ) {
        this.layer = layer;
    }

    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        log.debug( "..." );
        
        // opacity descriptor
        TextPropertyDescriptor opacityDescr = new TextPropertyDescriptor( ILayer.PROP_OPACITY, "Deckkraft" );
        opacityDescr.setValidator( new ICellEditorValidator() {
            public String isValid( Object value ) {
                try {
                    int i = Integer.parseInt( (String)value );
                    if (i < 0 || i > 100) {
                        return "Werte müssen zwischen 0 und 100 liegen.";
                    } else {
                        return null;
                    }
                } 
                catch (NumberFormatException e) {
                    return "Kein Zahlenwert: " + value;
                }
            }
        });
        // all other descriptors
        IPropertyDescriptor[] result = new IPropertyDescriptor[] {
                new TextPropertyDescriptor( ILayer.PROP_LABEL, "Name" ),
                new TextPropertyDescriptor( ILayer.PROP_KEYWORDS, "Schlagworte" ),
                //new PropertyDescriptor( "type", "Typ" ),
                new TextPropertyDescriptor( ILayer.PROP_CRSCODE, "Koordinatenreferenssystem" ),
                new TextPropertyDescriptor( "datacrs", "Daten-CRS" ),
                new PropertyDescriptor( ILayer.PROP_GEORESID, "Geo-Ressource" ),
                new TextPropertyDescriptor( ILayer.PROP_ORDERKEY, "Z-Priorität" ),
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
                log.info( "Property value: " + value );
                return value != null ? value : "";
            }
            else if (id.equals( "type" )) {
                return StringUtils.substringAfterLast( layer.getGeoResource().getClass().getName(), "." );
            }
            else if (id.equals( ILayer.PROP_CRSCODE )) {
                return layer.getCRSCode();
            }
            else if (id.equals( "datacrs" )) {
                CoordinateReferenceSystem dataCRS = layer.getGeoResource().getInfo( new NullProgressMonitor() ).getCRS();
                return dataCRS != null ? dataCRS.getName().getCode() : "[unspecified]";
            }
            else if (id.equals( ILayer.PROP_GEORESID )) {
                return layer.getGeoResource().getIdentifier();
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
                return "[unbekannt]";
            }
        }
        catch (Exception e) {
            log.error( "Error while getting property: " + id, e );
            LayerStatus error = new LayerStatus( LayerStatus.ERROR, LayerStatus.UNSPECIFIED, "Error while getting property value: " + id, e );
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
            SetPropertyOperation op = ProjectRepository.instance().newOperation( SetPropertyOperation.class );
            
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
                CoordinateReferenceSystem crs = CRS.decode( (String)value );
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
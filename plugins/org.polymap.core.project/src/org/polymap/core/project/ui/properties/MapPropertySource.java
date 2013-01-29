package org.polymap.core.project.ui.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Labeled;
import org.polymap.core.project.MapStatus;
import org.polymap.core.project.Messages;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.operations.SetPropertyOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * The property source of {@link IMap}. Allows the user to directly
 * change some properties.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class MapPropertySource
        implements IPropertySource, IPropertySource2 {

    private static Log log = LogFactory.getLog( MapPropertySource.class );
    
    private IMap                map;

    
    public MapPropertySource( IMap map ) {
        this.map = map;
    }

    
    protected String i18n( String key, Object...args ) {
        return Messages.get( "MapProperty_" + key, args );
    }
        

    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] result = new IPropertyDescriptor[] {
                new TextPropertyDescriptor( IMap.PROP_LABEL, i18n( "label_name" ) ),
                new CrsPropertyDescriptor( IMap.PROP_CRSCODE, i18n( "label_crs" ) ),
                new PropertyDescriptor( IMap.PROP_MAXEXTENT, i18n( "label_maxExtent" ) ),
        };
        return result;
    }

    
    public Object getPropertyValue( Object id ) {
        //map.setMapStatus( MapStatus.STATUS_OK );
        try {
            if (id.equals( IMap.PROP_LABEL )) {
                return map.getLabel();
            }
            else if (id.equals( IMap.PROP_CRSCODE )) {
                return map.getCRS();
            }
            else if (id.equals( IMap.PROP_MAXEXTENT )) {
                return new EnvelopPropertySource( map.getMaxExtent() ).setEditable( true );
            }
            else {
                return i18n( "unknownValue" );
            }
        }
        catch (Exception e) {
            log.error( "Error while getting property: " + id, e );
            MapStatus error = new MapStatus( MapStatus.ERROR, MapStatus.UNSPECIFIED, i18n( "valueError", id ), e );
            if (map.getMapStatus().isOK()) {
                map.setMapStatus( error );
            } else {
                map.getMapStatus().add( error );
            }
            return "Fehler: " + e.getMessage();
        }
    }

    
    public void setPropertyValue( Object id, Object value ) {
        try {
            SetPropertyOperation op = new SetPropertyOperation();

            if (id.equals( IMap.PROP_LABEL )) {
                op.init( Labeled.class, map, IMap.PROP_LABEL, value );
                OperationSupport.instance().execute( op, false, false );
            }
            else if (id.equals( IMap.PROP_CRSCODE )) {
                String srs = CRS.toSRS( (CoordinateReferenceSystem)value );
                if (srs != null) {
                    op.init( IMap.class, map, IMap.PROP_CRSCODE, srs );
                    OperationSupport.instance().execute( op, false, false );
                }
            }
            else {
                log.error( "Property is read-only: " + id );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this, "Error while changing property: " + id, e );
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
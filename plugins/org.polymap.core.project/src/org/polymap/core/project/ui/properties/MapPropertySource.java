package org.polymap.core.project.ui.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.referencing.CRS;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.Labeled;
import org.polymap.core.project.MapStatus;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.operations.SetPropertyOperation;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * The property source of {@link IMap}. Allows the user to directly
 * change some properties.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MapPropertySource
        implements IPropertySource2 {

    private static Log log = LogFactory.getLog( MapPropertySource.class );
    
    private IMap                map;

    
    public MapPropertySource( IMap map ) {
        this.map = map;
    }

    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        log.debug( "..." );
        
        IPropertyDescriptor[] result = new IPropertyDescriptor[] {
                new TextPropertyDescriptor( IMap.PROP_LABEL, "Name" ),
                new TextPropertyDescriptor( IMap.PROP_CRSCODE, "CRS" ),
                new PropertyDescriptor( IMap.PROP_MAXEXTENT, "Begrenzung" ),
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
                return map.getCRSCode();
            }
            else if (id.equals( IMap.PROP_MAXEXTENT )) {
                return map.getMaxExtent();
            }
            else {
                return "[unknown]";
            }
        }
        catch (Exception e) {
            log.error( "Error while getting property: " + id, e );
            MapStatus error = new MapStatus( MapStatus.ERROR, MapStatus.UNSPECIFIED, "Error while getting property value: " + id, e );
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
            SetPropertyOperation op = ProjectRepository.instance().newOperation( SetPropertyOperation.class );

            if (id.equals( IMap.PROP_LABEL )) {
                op.init( Labeled.class, map, IMap.PROP_LABEL, value );
                OperationSupport.instance().execute( op, false, false );
            }
            else if (id.equals( IMap.PROP_CRSCODE )) {
                CRS.decode( (String)value );
                op.init( IMap.class, map, IMap.PROP_CRSCODE, value );
                OperationSupport.instance().execute( op, false, false );
            }
            else {
                log.error( "Property is read-only: " + id );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( ProjectPlugin.PLUGIN_ID, this
                    , "Error while changing property: " + id, e );
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
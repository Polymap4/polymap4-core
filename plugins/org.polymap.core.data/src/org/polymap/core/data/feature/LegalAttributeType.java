package org.polymap.core.data.feature;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.polymap.core.data.Messages;

/**
 * Maps between the Name of a type in the combo box cell editor and the
 * class of the type.
 * 
 * @author jones
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class LegalAttributeType {

    /**
     * Get the map of legal attribute types.
     * <p>
     * This is not cached since every call may produce different translations
     * depending on the locale of the session.
     */
    public static List<LegalAttributeType> types() {
        List<LegalAttributeType> types = new ArrayList<LegalAttributeType>();
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_stringType" ), String.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_booleanType" ), Boolean.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_dateType" ), Date.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_integerType" ), Integer.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_longType" ), Long.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_floatType" ), Float.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_doubleType" ), Double.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_pointType" ), Point.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_lineStringType" ), LineString.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_polygonType" ), Polygon.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_geometryType" ), Geometry.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_multiPointType" ), MultiPoint.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_multiLineStringType" ), MultiLineString.class ) );
        types.add( new LegalAttributeType( Messages.get( "LegalAttributeType_multiPolygonType" ), MultiPolygon.class ) );

        return types;
        
//        Map<Class, LegalAttributeType> result = new HashMap();
//        for (LegalAttributeType type : types) {
//            result.put( type.type, type );
//        }
//        return result;
    }

    
    // instance *******************************************

    private final String name;

    private final Class  type;

    
    public LegalAttributeType( String name, Class type ) {
        this.name = name;
        this.type = type;
    }
    
    public String getName(){
        return name;
    }
    
    public Class getType(){
        return type;
    }
    
}
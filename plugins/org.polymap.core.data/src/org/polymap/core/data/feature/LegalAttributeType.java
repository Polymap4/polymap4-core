package org.polymap.core.data.feature;

import java.util.Arrays;
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
 * @since 3.0
 */
public class LegalAttributeType {

    public static final int     DEFAULT_STRING_LENGTH = 255;
    
    /**
     * Get the map of legal attribute types.
     * <p/>
     * This is not cached since every call may produce different translations
     * depending on the locale of the session.
     */
    public static List<LegalAttributeType> types() {
        return Arrays.asList( new LegalAttributeType[] {             
                new LegalAttributeType( Messages.get( "LegalAttributeType_stringType" ), String.class, DEFAULT_STRING_LENGTH ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_bigStringType" ), String.class, 4096 ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_booleanType" ), Boolean.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_dateType" ), Date.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_integerType" ), Integer.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_longType" ), Long.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_floatType" ), Float.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_doubleType" ), Double.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_pointType" ), Point.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_lineStringType" ), LineString.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_polygonType" ), Polygon.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_geometryType" ), Geometry.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_multiPointType" ), MultiPoint.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_multiLineStringType" ), MultiLineString.class ),
                new LegalAttributeType( Messages.get( "LegalAttributeType_multiPolygonType" ), MultiPolygon.class )        
        });
    }

    
    // instance *******************************************

    private final String    name;

    private final Class     type;
    
    private final Integer   length;

    
    public LegalAttributeType( String name, Class type ) {
        this.name = name;
        this.type = type;
        this.length = null;
    }
    
    public LegalAttributeType( String name, Class type, Integer length ) {
        this.name = name;
        this.type = type;
        this.length = length;
    }
    
    public String getName(){
        return name;
    }
    
    public Class getType(){
        return type;
    }

    public Integer getLength() {
        return length;
    }
    
}
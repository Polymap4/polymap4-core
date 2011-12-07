/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
 */
package org.polymap.core.runtime.recordstore.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.geotools.geojson.geom.GeometryJSON;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.QueryExpression.Equal;

/**
 * Encode/Decode {@link Geometry} values using {@link NumericField} build-in support of
 * Lucene.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class GeometryValueCoder
        implements LuceneValueCoder {

    static final String                 FIELD_MAXX = "_maxx_";
    static final String                 FIELD_MAXY = "_maxy_";
    static final String                 FIELD_MINX = "_minx_";
    static final String                 FIELD_MINY = "_miny_";
    

    private NumericValueCoder           numeric = new NumericValueCoder();
    
    private GeometryJSON                jsonCoder = new GeometryJSON( 8 );
    
    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof Geometry) {
            Geometry geom = (Geometry)value;

            // store geom -> JSON
            StringWriter out;
            try {
                out = new StringWriter( 4*1024 );
                jsonCoder.write( geom, out );
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }

            Field field = (Field)doc.getFieldable( key );
            if (field != null) {
                field.setValue( out.toString() );
            }
            else {
                doc.add( new Field( key, out.toString(), Store.YES, Index.NO ) );
            }

            // store bbox
            Envelope envelop = geom.getEnvelopeInternal();
            numeric.encode( doc, key+FIELD_MAXX, envelop.getMaxX(), true );
            numeric.encode( doc, key+FIELD_MAXY, envelop.getMaxY(), true ); 
            numeric.encode( doc, key+FIELD_MINX, envelop.getMinX(), true ); 
            numeric.encode( doc, key+FIELD_MINY, envelop.getMinY(), true );
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        if (doc.getFieldable( key+FIELD_MAXX ) != null) {
            try {
                Field field = (Field)doc.getFieldable( key );
                return jsonCoder.read( new StringReader( field.stringValue() ) );
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }
        }
        else {
            return null;
        }
    }


    public Query searchQuery( QueryExpression exp ) {
        // EQUALS
        if (exp instanceof QueryExpression.Equal) {
            Equal equalExp = (QueryExpression.Equal)exp;
            
            if (equalExp.value instanceof Coordinate) {
                String key = equalExp.key;
                Coordinate coord = (Coordinate)equalExp.value;
                
                BooleanQuery result = new BooleanQuery();
                
                QueryExpression.Equal xQuery = new Equal( key + "_x", coord.x );
                result.add( numeric.searchQuery( xQuery ), BooleanClause.Occur.MUST );
                
                QueryExpression.Equal yQuery = new Equal( key + "_y", coord.y );
                result.add( numeric.searchQuery( xQuery ), BooleanClause.Occur.MUST );
                
                return result;
            }
        }
        // MATCHES
        else if (exp instanceof QueryExpression.Match) {
//            Match matchExp = (Match)exp;
//
//            if (matchExp.value instanceof Number) {
//                throw new UnsupportedOperationException( "MATCHES not supported for Number values." );
//            }
        }
        return null;
    }
    
}

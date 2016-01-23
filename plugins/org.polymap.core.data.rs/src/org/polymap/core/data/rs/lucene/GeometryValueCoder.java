/* 
 * polymap.org
 * Copyright (C) 2011-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.rs.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

import org.polymap.recordstore.QueryExpression;
import org.polymap.recordstore.QueryExpression.BBox;
import org.polymap.recordstore.QueryExpression.Greater;
import org.polymap.recordstore.QueryExpression.Less;
import org.polymap.recordstore.lucene.LuceneValueCoder;
import org.polymap.recordstore.lucene.NumericValueCoder;

/**
 * Encode/Decode {@link Geometry} values using {@link NumericField} build-in support
 * of Lucene.
 * <p/>
 * <b>Note:</b> The results generated from {@link #searchQuery(QueryExpression)}
 * should be post-processed to make sure that the geometry <b>actually</b> intersects
 * the bbox. The search just checks that the bounds of the geometry intersect the bbox!
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class GeometryValueCoder
        implements LuceneValueCoder {

    public static final String              FIELD_MAXX = "_maxx_";
    public static final String              FIELD_MAXY = "_maxy_";
    public static final String              FIELD_MINX = "_minx_";
    public static final String              FIELD_MINY = "_miny_";
    
    /** Re-used readers per thread. */
    static final ThreadLocal<WKBReader> wkbReaders = new ThreadLocal<WKBReader>() {
        protected WKBReader initialValue() {
            return new WKBReader();
        }
    };
    /** Re-used readers per thread. */
    static final ThreadLocal<WKTReader> wktReaders = new ThreadLocal<WKTReader>() {
        protected WKTReader initialValue() {
            return new WKTReader();
        }
    };
    
    /** The coder used to handle bbox min/max values for queries. */
    private NumericValueCoder           numeric = new NumericValueCoder();
    
    
    protected byte[] encode( Geometry geom ) {
        return new WKBWriter().write( geom );    
    }

    
    public Object decode( Document doc, String key ) {
        if (doc.getFieldable( key+FIELD_MAXX ) != null) {
            Field field = (Field)doc.getFieldable( key );
            try {
                return wkbReaders.get().read( field.getBinaryValue() );
            }
            catch (Exception e) {
                try {
                    return wktReaders.get().read( field.stringValue() );
                }
                catch (Exception ee) {
                    throw new RuntimeException( ee );
                }
            }
        }
        else {
            return null;
        }
    }


    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof Geometry) {
            Geometry geom = (Geometry)value;

            // store geom -> WKT, JSON, ...
            byte[] out = encode( geom );

            Field field = (Field)doc.getFieldable( key );
            if (field != null) {
                field.setValue( out );
            }
            else {
                doc.add( new Field( key, out ) );
            }

            // store bbox
            Envelope envelop = geom.getEnvelopeInternal();
            numeric.encode( doc, key+FIELD_MAXX, envelop.getMaxX(), true, true );
            numeric.encode( doc, key+FIELD_MAXY, envelop.getMaxY(), true, true ); 
            numeric.encode( doc, key+FIELD_MINX, envelop.getMinX(), true, true ); 
            numeric.encode( doc, key+FIELD_MINY, envelop.getMinY(), true, true );
            return true;
        }
        else {
            return false;
        }
    }
    

    public Query searchQuery( QueryExpression exp ) {
        // BBOX
        if (exp instanceof QueryExpression.BBox) {
            //        return !(other.minx > maxx ||
            //                other.maxx < minx ||
            //                other.miny > maxy ||
            //                other.maxy < miny);
            //        -> !maxx < other.minx && !mixx > other.maxx
            //        -> maxx > other.minx && minx < other.maxx

            BBox bbox = (QueryExpression.BBox)exp;
            
            BooleanQuery result = new BooleanQuery();

            // maxx > bbox.getMinX
            result.add( numeric.searchQuery(
                    new Greater( bbox.key+FIELD_MAXX, bbox.minX ) ), BooleanClause.Occur.MUST );
            // minx < bbox.getMaxX
            result.add( numeric.searchQuery(
                    new Less( bbox.key+FIELD_MINX, bbox.maxX ) ), BooleanClause.Occur.MUST );
            // maxy > bbox.getMinY
            result.add( numeric.searchQuery(
                    new Greater( bbox.key+FIELD_MAXY, bbox.minY ) ), BooleanClause.Occur.MUST );
            // miny < bbox.getMaxY
            result.add( numeric.searchQuery(
                    new Less( bbox.key+FIELD_MINY, bbox.maxY ) ), BooleanClause.Occur.MUST );
            return result;
        }
//        // EQUALS
//        else if (exp instanceof QueryExpression.Equal) {
//            Equal equal = (QueryExpression.Equal)exp;
//            
//            if (equal.value instanceof Geometry) {
//                String encoded = encode( (Geometry)equal.value );
//                return new TermQuery( new Term( equal.key, encoded ) );
//            }
//        }
        return null;
    }
    
}

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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.QueryExpression.BBox;
import org.polymap.core.runtime.recordstore.QueryExpression.Equal;
import org.polymap.core.runtime.recordstore.QueryExpression.Greater;
import org.polymap.core.runtime.recordstore.QueryExpression.Less;

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
    
    /** The coder used to handle bbox min/max values for queries. */
    private NumericValueCoder           numeric = new NumericValueCoder();
    
    //private GeometryJSON                jsonCoder = new GeometryJSON( 8 );
    
    
    protected String encode( Geometry geom ) {
        return new WKTWriter().write( geom );    
    }
    
    protected Geometry decode( String encoded ) {
        try {
            return new WKTReader().read( encoded );
        }
        catch (ParseException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof Geometry) {
            Geometry geom = (Geometry)value;

            // store geom -> WKT, JSON, ...
            String out = encode( geom );

            Field field = (Field)doc.getFieldable( key );
            if (field != null) {
                field.setValue( out );
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
            Field field = (Field)doc.getFieldable( key );
            return decode( field.stringValue() );
        }
        else {
            return null;
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
        // EQUALS
        else if (exp instanceof QueryExpression.Equal) {
            Equal equal = (QueryExpression.Equal)exp;
            
            if (equal.value instanceof Geometry) {
                String encoded = encode( (Geometry)equal.value );
                return new TermQuery( new Term( equal.key, encoded ) );
            }
        }
        return null;
    }
    
}

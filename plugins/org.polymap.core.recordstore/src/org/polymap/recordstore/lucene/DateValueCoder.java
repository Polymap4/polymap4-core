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
package org.polymap.recordstore.lucene;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

import org.polymap.recordstore.QueryExpression;
import org.polymap.recordstore.QueryExpression.Comparison;

/**
 * En/Decode {@link Date} values using {@link NumericField} build-in support of
 * Lucene. Uses less memory and should be faster than storing numbers as String (
 * {@link NumberValueCoder}).
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class DateValueCoder
        implements LuceneValueCoder {

    private static Log log = LogFactory.getLog( DateValueCoder.class );
    
    public static final String      SUFFIX = "_date";

    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof Date) {
            NumericField field = (NumericField)doc.getFieldable( key+SUFFIX );
            if (field == null) {
                field = new NumericField( key+SUFFIX, Store.YES, indexed );
                doc.add( field );
            }
            field.setLongValue( ((Date)value).getTime() );
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        Fieldable field = doc.getFieldable( key+SUFFIX );
        if (field instanceof NumericField) {
            return new Date( ((NumericField)field).getNumericValue().longValue() );
        }
        else {
            return null;
        }
    }


    public Query searchQuery( QueryExpression exp ) {
        if (exp instanceof Comparison) {
            String key = ((Comparison)exp).key;
            Object value = ((Comparison)exp).value;
            
            if (value instanceof Date) {
                long date = ((Date)value).getTime();                
                
                if (exp instanceof QueryExpression.Equal) {
                    return NumericRangeQuery.newLongRange( key+SUFFIX, date, date, true, true );
                }                
                else if (exp instanceof QueryExpression.Greater) {
                    return NumericRangeQuery.newLongRange( key+SUFFIX, date, null, false, false );
                }                
                else if (exp instanceof QueryExpression.GreaterOrEqual) {
                    return NumericRangeQuery.newLongRange( key+SUFFIX, date, null, true, false );
                }                
                else if (exp instanceof QueryExpression.Less) {
                    return NumericRangeQuery.newLongRange( key+SUFFIX, null, date, false, false );
                }                
                else if (exp instanceof QueryExpression.LessOrEqual) {
                    return NumericRangeQuery.newLongRange( key+SUFFIX, null, date, false, true );
                }                
            }
        }
        return null;
    }
    
}

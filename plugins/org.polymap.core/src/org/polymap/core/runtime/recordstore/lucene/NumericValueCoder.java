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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.QueryExpression.Equal;
import org.polymap.core.runtime.recordstore.QueryExpression.Greater;
import org.polymap.core.runtime.recordstore.QueryExpression.Less;
import org.polymap.core.runtime.recordstore.QueryExpression.Match;


/**
 * En/Decode {@link Number} values using {@link NumericField} build-in support of
 * Lucene. Uses less memory and should be faster than storing numbers as String (
 * {@link NumberValueCoder}).
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class NumericValueCoder
        implements LuceneValueCoder {

    private static Log log = LogFactory.getLog( NumericValueCoder.class );

    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof Number) {
            NumericField field = (NumericField)doc.getFieldable( key );
            if (field == null) {
                field = new NumericField( key, Store.YES, indexed );
                doc.add( field );
            }
            if (value instanceof Integer) {
                field.setIntValue( (Integer)value );
            }
            else if (value instanceof Long) {
                field.setLongValue( (Long)value );
            }
            else if (value instanceof Float) {
                field.setFloatValue( (Float)value );
            }
            else if (value instanceof Double) {
                field.setDoubleValue( (Double)value );
            }
            else {
                throw new RuntimeException( "Unknown Number type: " + value.getClass() );
            }
            //log.debug( "encode(): " + field );
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        Fieldable field = doc.getFieldable( key );
        if (field instanceof NumericField) {
            return ((NumericField)field).getNumericValue();
        }
        else {
            return null;
        }
    }


    public Query searchQuery( QueryExpression exp ) {
        // EQUALS
        if (exp instanceof QueryExpression.Equal) {
            Equal equalExp = (QueryExpression.Equal)exp;
            
            if (equalExp.value instanceof Number) {
                String key = equalExp.key;
                Number value = (Number)equalExp.value;
                
                if (equalExp.value instanceof Integer) {
                    return NumericRangeQuery.newIntRange( key, value.intValue(), value.intValue(), true, true );
                }
                else if (equalExp.value instanceof Long) {
                    return NumericRangeQuery.newLongRange( key, value.longValue(), value.longValue(), true, true );
                }
                else if (equalExp.value instanceof Float) {
                    return NumericRangeQuery.newFloatRange( key, value.floatValue(), value.floatValue(), true, true );
                }
                else if (equalExp.value instanceof Double) {
                    return NumericRangeQuery.newDoubleRange( key, value.doubleValue(), value.doubleValue(), true, true );
                }
                else {
                    throw new RuntimeException( "Unknown Number type: " + value.getClass() );
                }
            }
        }
        // GREATER
        else if (exp instanceof QueryExpression.Greater) {
            Greater greaterExp = (QueryExpression.Greater)exp;
            
            if (greaterExp.value instanceof Number) {
                String key = greaterExp.key;
                Number value = (Number)greaterExp.value;
                
                if (greaterExp.value instanceof Integer) {
                    return NumericRangeQuery.newIntRange( key, value.intValue(), null, false, false );
                }
                else if (greaterExp.value instanceof Long) {
                    return NumericRangeQuery.newLongRange( key, value.longValue(), null, false, false );
                }
                else if (greaterExp.value instanceof Float) {
                    return NumericRangeQuery.newFloatRange( key, value.floatValue(), null, false, false );
                }
                else if (greaterExp.value instanceof Double) {
                    return NumericRangeQuery.newDoubleRange( key, value.doubleValue(), null, false, false );
                }
                else {
                    throw new RuntimeException( "Unknown Number type: " + value.getClass() );
                }
            }
        }
        // LESS
        else if (exp instanceof QueryExpression.Less) {
            Less lessExp = (QueryExpression.Less)exp;
            
            if (lessExp.value instanceof Number) {
                String key = lessExp.key;
                Number value = (Number)lessExp.value;
                
                if (lessExp.value instanceof Integer) {
                    return NumericRangeQuery.newIntRange( key, null, value.intValue(), false, false );
                }
                else if (lessExp.value instanceof Long) {
                    return NumericRangeQuery.newLongRange( key, null, value.longValue(), false, false );
                }
                else if (lessExp.value instanceof Float) {
                    return NumericRangeQuery.newFloatRange( key, null, value.floatValue(), false, false );
                }
                else if (lessExp.value instanceof Double) {
                    return NumericRangeQuery.newDoubleRange( key, null, value.doubleValue(), false, false );
                }
                else {
                    throw new RuntimeException( "Unknown Number type: " + value.getClass() );
                }
            }
        }
        // MATCHES
        else if (exp instanceof QueryExpression.Match) {
            Match matchExp = (Match)exp;

            if (matchExp.value instanceof Number) {
                throw new UnsupportedOperationException( "MATCHES not supported for Number values." );
            }
        }
        return null;
    }
    
}

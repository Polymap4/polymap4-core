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

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import org.polymap.recordstore.QueryExpression;
import org.polymap.recordstore.QueryExpression.Equal;
import org.polymap.recordstore.QueryExpression.Match;


/**
 * Interprets each and every field as a String {@link Field}. This *must* be
 * last consulted by {@link ValueCoders}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StringValueCoder
        implements LuceneValueCoder {

    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof String) {
            Field field = (Field)doc.getFieldable( key );
            if (field != null) {
                field.setValue( (String)value );
            }
            else {
                doc.add( new Field( key, (String)value, 
                        Store.YES, indexed ? Index.NOT_ANALYZED : Index.NO ) );
            }
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        return doc.get( key );
    }


    public Query searchQuery( QueryExpression exp ) {
        // EQUALS
        if (exp instanceof QueryExpression.Equal) {
            Equal equalExp = (QueryExpression.Equal)exp;
            
            if (equalExp.value == null) {
                throw new UnsupportedOperationException( "Null values are not supported for expression: Equal(String)" );
            }
            else if (equalExp.value instanceof String) {
                return new TermQuery( new Term( equalExp.key, (String)equalExp.value) );
            }
        }
        // MATCHES
        else if (exp instanceof QueryExpression.Match) {
            Match matchExp = (Match)exp;
            
            if (matchExp.value == null) {
                throw new UnsupportedOperationException( "Null values are not supported for expression: Match(String)" );
            }
            else if (matchExp.value instanceof String) {
                String value = (String)matchExp.value;
                
                // XXX properly substitute wildcard chars
                if (value.endsWith( "*" )
                        && StringUtils.countMatches( value, "*" ) == 1
                        && StringUtils.countMatches( value, "?" ) == 0) {
                    return new PrefixQuery( new Term( matchExp.key, value.substring( 0, value.length()-1 ) ) );
                }
                else {
                    return new WildcardQuery( new Term( matchExp.key, value ) );
                }
            }
        }
        return null;
    }
    
}

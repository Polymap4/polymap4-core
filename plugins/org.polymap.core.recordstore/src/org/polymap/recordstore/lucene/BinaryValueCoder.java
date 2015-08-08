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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;

import org.polymap.recordstore.QueryExpression;

/**
 * En/Decodes byte[] values.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public final class BinaryValueCoder
        implements LuceneValueCoder {

    private static Log log = LogFactory.getLog( BinaryValueCoder.class );

    
    public boolean encode( Document doc, String key, Object value, boolean indexed ) {
        if (value instanceof byte[]) {
            if (indexed) {
                log.warn( "Binary fields cannot be indexed." );
            }
            Field field = (Field)doc.getFieldable( key );
            if (field != null) {
                field.setValue( (byte[])value );
            }
            else {
                doc.add( new Field( key, (byte[])value ) );
            }
            return true;
        }
        else {
            return false;
        }
    }
    

    public Object decode( Document doc, String key ) {
        Fieldable field = doc.getFieldable( key );
        if (field instanceof Field && field.isBinary()) {
            return doc.getBinaryValue( key );
        }
        else {
            return null;
        }
    }


    public Query searchQuery( QueryExpression exp ) {
        return null;
    }
    
}

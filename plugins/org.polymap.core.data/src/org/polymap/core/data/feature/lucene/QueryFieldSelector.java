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
package org.polymap.core.data.feature.lucene;

import java.util.Arrays;

import org.geotools.data.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class QueryFieldSelector
        implements FieldSelector {

    private static final Log log = LogFactory.getLog( QueryFieldSelector.class );

    private Query           query;
    
    private String[]        propNames;
    
    
    public QueryFieldSelector( Query query ) {
        this.query = query;
        this.propNames = query.getPropertyNames();
        log.info( "FieldSelector: " + (propNames != null ? Arrays.asList( propNames ) : "ALL") );      
    }

    
    public FieldSelectorResult accept( String fieldName ) {
        FieldSelectorResult result = FieldSelectorResult.NO_LOAD; 
        if (propNames == null) {
            result = FieldSelectorResult.LOAD;
        }
        else if (StringUtils.indexOfAny( fieldName, propNames ) == 0) {
            result = FieldSelectorResult.LOAD;
        }
        return result;
    }
    
}

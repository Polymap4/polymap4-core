/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.style.serialize.sld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.FilterMappedNumbers;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;

/**
 * 
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class FilterMappedNumbersHandler
    extends StylePropertyValueHandler<FilterMappedNumbers,Number> {

    private static Log log = LogFactory.getLog( FilterMappedNumbersHandler.class );


    @Override
    public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, FilterMappedNumbers spv, SD sd,
            Setter<SD> setter ) {
        // split style descriptors
        List<SD> result = new ArrayList( spv.values.size() );
        
        Iterator<Filter> filters = spv.filters().iterator();
        Iterator<Number> values = spv.values.iterator();
        while (filters.hasNext()) {
            assert values.hasNext();
            
            SD clone = (SD)sd.clone();
            
            clone.filterAnd( filters.next() );
            setter.set( clone, SLDSerializer.ff.literal( values.next() ));
            result.add( clone );
        }
        
        return result;
    }

}

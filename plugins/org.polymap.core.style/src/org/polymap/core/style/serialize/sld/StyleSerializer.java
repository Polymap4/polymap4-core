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

import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.opengis.filter.Filter;
import org.xml.sax.SAXException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.ConstantFilter;
import org.polymap.core.style.model.PropertyMatchingNumberFilter;
import org.polymap.core.style.model.PropertyMatchingStringFilter;
import org.polymap.core.style.model.ScaleRangeFilter;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyValue;

/**
 * Serializes a particular {@link Style} into a flat list of
 * {@link SymbolizerDescriptor} instances. The resulting list represents the
 * cross-product of all complex filter and/or scale definitions specified via
 * {@link StylePropertyValue} types. Those different {@link StylePropertyValue} types
 * are handled by corresponding {@link StylePropertyValueHandler} types.
 *
 * @param <S> The input style type.
 * @param <SD> The output symbolizer descriptor.
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public abstract class StyleSerializer<S extends Style, SD extends SymbolizerDescriptor>
        extends StyleCompositeSerializer<S,SD> {

    private static final Log log = LogFactory.getLog( StyleSerializer.class );

    private Filter defaultFilter;

    private Pair<Integer, Integer> currentScale;


    protected abstract SD createStyleDescriptor();


    protected abstract void doSerializeStyle( S style );


    @Override
    protected final SD createDescriptor() {
        SD sd = createStyleDescriptor();
        if (defaultFilter != null) {
            sd.filterAnd( defaultFilter );
        }
        if (currentScale != null) {
            sd.scale.set( currentScale );
        }
        return sd;
    }


    @Override
    protected final void doSerialize( S style ) {
        StylePropertyValue<Filter> filterValue = style.visibleIf.get();
        if (filterValue != null) {
            if (filterValue instanceof ConstantFilter) {
                try {
                    defaultFilter = ((ConstantFilter)filterValue).filter();
                }
                catch (IOException | SAXException | ParserConfigurationException e) {
                    throw new RuntimeException( e );
                }
            }
            else if (filterValue instanceof PropertyMatchingNumberFilter) {
                PropertyMatchingNumberFilter prop = (PropertyMatchingNumberFilter)filterValue;
                defaultFilter = prop.relationalNumberOperator.get().asFilter( ff.property( prop.leftProperty.get() ),
                        ff.literal( prop.rightLiteral.get() ) );
            }
            else if (filterValue instanceof PropertyMatchingStringFilter) {
                PropertyMatchingStringFilter prop = (PropertyMatchingStringFilter)filterValue;
                defaultFilter = prop.relationalStringOperator.get().asFilter( ff.property( prop.leftProperty.get() ),
                        ff.literal( prop.rightLiteral.get() ) );
            }
            else if (filterValue instanceof ScaleRangeFilter) {
                ScaleRangeFilter prop = (ScaleRangeFilter)filterValue;
                currentScale = ImmutablePair.of( prop.minScale.get(), prop.maxScale.get() );
            }
            else {
                throw new RuntimeException( "unkown filter class " + filterValue.getClass() );
            }
        }
        doSerializeStyle( style );
    }
}

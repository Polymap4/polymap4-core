/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.query.grammar;

import org.apache.commons.io.FilenameUtils;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.engine.TemplateProperty;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyMatches<T>
        extends ComparisonPredicate<T> {

    protected char  singleWildcard = '?';

    protected char  multiWildcard = '*';
    
    public PropertyMatches( TemplateProperty<T> prop, T value, char singleWildcard, char multiWildcard ) {
        super( prop, value );
    }

    public PropertyMatches( TemplateProperty<T> prop, T value ) {
        super( prop, value );
    }

    @Override
    public boolean evaluate( Composite target ) {
        if (prop.getTraversed() != null) {
            throw new UnsupportedOperationException( "Composite properties is not yet supported." );
        }
        String propName = prop.getInfo().getName();
        Object propValue = target.info().getProperty( propName ).getValue( target );
        return propValue != null 
                ? FilenameUtils.wildcardMatch( propValue.toString(), value.toString() )
                : false;
    }
    
}

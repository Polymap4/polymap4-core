/* 
 * polymap.org
 * Copyright 2011, Falko Br�utigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.model.event;

import java.util.EventObject;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.EventFilter;

/**
 * Filter events that are fired by entities which classnames match the given
 * reges pattern.
 * 
 * @deprecated see SourceClassPropertyEventFilter
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 * @since 3.1
 */
public class RegexPropertyEventFilter
        implements EventFilter<EventObject> {

    private static Log log = LogFactory.getLog( RegexPropertyEventFilter.class );

    private Pattern         pattern;         
    
    
    public RegexPropertyEventFilter( String pattern ) {
        this.pattern = Pattern.compile( pattern );
    }

    public RegexPropertyEventFilter( String pattern, int flags ) {
        this.pattern = Pattern.compile( pattern, flags );
    }

    public boolean apply( EventObject ev ) {
        Object source = ev.getSource();
        return pattern.matcher( source.getClass().getName() ).matches();
    }
    
}

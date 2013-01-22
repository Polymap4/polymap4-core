/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyConcern;
import org.polymap.core.model2.runtime.PropertyInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LogConcern
        implements PropertyConcern {

    private static Log log = LogFactory.getLog( LogConcern.class );

    public Object doGet( Property delegate ) {
        log.info( "LOG: get property = " + delegate.getInfo().getName() );
        return delegate.get();
    }

    public void doSet( Property delegate, Object value ) {
        log.info( "LOG: set property = " + delegate.getInfo().getName() );
        delegate.set( value );
    }

    public PropertyInfo doGetInfo( Property delegate ) {
        return delegate.getInfo();
    }

}

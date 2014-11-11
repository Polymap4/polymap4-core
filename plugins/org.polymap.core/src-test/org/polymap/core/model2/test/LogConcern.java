/* 
 * polymap.org
 * Copyright (C) 2012-2014, Falko Bräutigam. All rights reserved.
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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.CollectionPropertyConcernAdapter;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.PropertyConcern;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LogConcern
        extends CollectionPropertyConcernAdapter
        implements PropertyConcern {

    private static Log log = LogFactory.getLog( LogConcern.class );

    protected void log( String method ) {
        PropertyInfo info = delegate.getInfo();
        Entity entity = context.getCompositePart( Entity.class );
        log.info( "LOG: property: " + entity.getClass().getSimpleName() + "." + info.getName() + "#" + method + "()" );        
    }
    
    @Override
    public Object get() {
        log( "get" );
        return ((Property)delegate).get();
    }

    @Override
    public void set( Object value ) {
        log( "set" );
        ((Property)delegate).set( value );
    }

    @Override
    public Object createValue( ValueInitializer initializer ) {
        log( "createValue" );
        return ((Property)delegate).createValue( initializer );
    }

    // collection *****************************************
    
    @Override
    public Object createElement( ValueInitializer initializer ) {
        log( "createElement" );
        return super.createElement( initializer );
    }

    @Override
    public int size() {
        log( "size" );
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        log( "isEmpty" );
        return super.isEmpty();
    }

    @Override
    public boolean contains( Object o ) {
        log( "contains" );
        return super.contains( o );
    }

    @Override
    public Iterator iterator() {
        log( "contains" );
        return super.iterator();
    }

    @Override
    public boolean add( Object o ) {
        log( "add" );
        return super.add( o );
    }

    @Override
    public Object[] toArray() {
        log( "toArray" );
        return super.toArray();
    }

    @Override
    public Object[] toArray( Object[] a ) {
        log( "toArray" );
        return super.toArray( a );
    }

    @Override
    public boolean remove( Object o ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean containsAll( Collection c ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean addAll( Collection c ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean removeAll( Collection c ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean retainAll( Collection c ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void clear() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}

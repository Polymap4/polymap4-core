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
package org.polymap.core.project.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.lang.reflect.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.association.ManyAssociation;

/**
 * In-memory implementation of an {@link ManyAssociation}; used to hold
 * temporar/transient associations ({@link ITempLayer}).
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ListManyAssociation<T>
        implements ManyAssociation<T> {

    private static Log log = LogFactory.getLog( ListManyAssociation.class );

    private List<T>         content = new ArrayList();
    
    
    public boolean add( T entity ) {
        return content.add( entity );
    }

    public boolean add( int i, T entity ) {
        content.add( i, entity );
        return true;
    }

    public boolean contains( T entity ) {
        return content.contains( entity );
    }

    public T get( int i ) {
        return content.get( i );
    }

    public boolean remove( T entity ) {
        return content.remove( entity );
    }

    public Iterator<T> iterator() {
        return content.iterator();
    }

    public int count() {
        return content.size();
    }

    public List toList() {
        return content;
    }

    public Set toSet() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isAggregated() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isImmutable() {
        throw new RuntimeException( "not yet implemented." );
    }

    public <E> E metaInfo( Class<E> infoType ) {
        throw new RuntimeException( "not yet implemented." );
    }

    public QualifiedName qualifiedName() {
        throw new RuntimeException( "not yet implemented." );
    }

    public Type type() {
        throw new RuntimeException( "not yet implemented." );
    }
    
}

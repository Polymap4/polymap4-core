/* 
 * polymap.org
 * Copyright 2013, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.project.ui.util;

import java.util.Iterator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Helps to handle {@link IStructuredSelection}. This adapter filters the given
 * {@link IStructuredSelection} for elements with the given type <code>T</code>.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class SelectionAdapter
        implements Iterable {

    private IStructuredSelection    delegate;
    
    public SelectionAdapter( ISelection delegate ) {
        this.delegate = delegate instanceof IStructuredSelection
                ? (IStructuredSelection)delegate
                : new StructuredSelection();
    }

    @Override
    public Iterator iterator() {
        return delegate.toList().iterator();
    }

    public <T> Iterator<T> iterator( Class<T> type ) {
        return Iterators.filter( iterator(), type );
    }

    public Object first() {
        Iterator it = iterator();
        return it.hasNext() ? it.next() : null;
    }

    public <T> T first( Class<T> type ) {
        Iterator<T> it = iterator( type );
        return it.hasNext() ? it.next() : null;
    }

    public int size() {
        return delegate.size();    
    }
    
    public <T> int size( Class<T> type ) {
        return Iterables.size( elementsOfType( type ) );    
    }

    public <T> Iterable<T> elementsOfType( Class<T> type ) {
        return Iterables.filter( this, type );
    }
}

/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Helps to handle {@link IStructuredSelection}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SelectionAdapter<T>
        implements Iterable<T> {

    private IStructuredSelection    delegate;
    
    public SelectionAdapter( ISelection delegate ) {
        this.delegate = delegate instanceof IStructuredSelection
                ? (IStructuredSelection)delegate
                : new StructuredSelection();
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform( delegate.iterator(), new Function<Object,T>() {
            public T apply( Object input ) {
                return (T)input;
            }
        });
    }
    
    public T first() {
        return (T)delegate.getFirstElement();
    }
    
}

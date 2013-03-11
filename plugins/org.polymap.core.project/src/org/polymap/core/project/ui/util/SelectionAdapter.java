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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Helps to handle {@link IStructuredSelection}. This adapter filters the given
 * {@link IStructuredSelection} for elements with the given type <code>T</code>.
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
        List<T> result = new ArrayList( delegate.size() );
        for (Object elm : delegate.toList()) {
            try {
                result.add( (T)elm );
            }
            catch (ClassCastException e) {
                // skip wrong type
            }
        }
        return result.iterator();
    }
    
    public T first() {
        Iterator<T> it = iterator();
        return it.hasNext() ? it.next() : null;
    }
    
}

/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.navigator.filter;

import org.apache.commons.lang.ArrayUtils;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.project.ILayer;

import org.polymap.rhei.filter.FilterFactory;

/**
 * 
 * @see FilterLabelProvider
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public class FilterContentProvider
        implements ITreeContentProvider {


    public Object[] getChildren( Object elm ) {
        // folder
        if (elm instanceof ILayer) {
            try {
                String id = ((ILayer)elm).id();
                return new Object[] { new FiltersFolderItem( (ILayer)elm ) };
            }
            catch (NoSuchEntityException e) {
                return ArrayUtils.EMPTY_OBJECT_ARRAY;
            }
        }
        // filters
        else if (elm instanceof FiltersFolderItem) {
            FiltersFolderItem folder = (FiltersFolderItem)elm;
            return FilterFactory.instance().filtersForLayer( folder.getLayer() ).toArray();
        }
        return null;
    }


    public Object getParent( Object elm ) {
        if (elm instanceof ILayer) {
            try {
                return ((ILayer)elm).getMap();
            }
            catch (NoSuchEntityException e) {
            }
        }
        return null;
    }


    public boolean hasChildren(Object element) {
        return getChildren(element) != null;
    }

    
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    
    public void dispose() {
    }

    
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }
    
}

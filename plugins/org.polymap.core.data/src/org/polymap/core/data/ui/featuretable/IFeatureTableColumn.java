/*
 * polymap.org Copyright 2011, Falko Bräutigam. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.data.ui.featuretable;

import java.util.Comparator;

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewerColumn;

interface IFeatureTableColumn {

    /**
     * Called by the {@link FeatureTableViewer} after this column was added to it.
     * 
     * @param viewer The viewer we are working for.
     */
    public void setViewer( FeatureTableViewer viewer );
    
    /**
     * The name of the property this column represents. The header of the
     * column may differ.
     */
    public String getName();

    public abstract EditingSupport getEditingSupport();

    public TableViewerColumn newViewerColumn();
    
    public Comparator<IFeatureTableElement> newComparator( int sortDir );

    /**
     *
     * @param dir Sort direction: {@link SWT#UP} or {@link SWT#DOWN}.
     */
    void sort( int dir );

}
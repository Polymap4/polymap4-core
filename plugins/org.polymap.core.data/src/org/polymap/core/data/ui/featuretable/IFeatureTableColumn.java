/*
 * polymap.org 
 * Copyright (C) 2011-2014, Falko Br�utigam. All rights reserved.
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

import org.eclipse.swt.SWT;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IFeatureTableColumn {

    /**
     * Called by the {@link FeatureTableViewer} after this column was added to it.
     * 
     * @param viewer The viewer we are working for.
     */
    public void setViewer( FeatureTableViewer viewer );

    public FeatureTableViewer getViewer();
    
    /**
     * The name of the property this column represents. The header of the
     * column may differ.
     */
    public String getName();

    public IFeatureTableColumn setLabelProvider( ColumnLabelProvider labelProvider );

    public ColumnLabelProvider getLabelProvider();

    /**
     * Creates the {@link TableViewerColumn} instance for this column.
     */
    public TableViewerColumn newViewerColumn();
    
    public Comparator<IFeatureTableElement> newComparator( int sortDir );

    /**
     *
     * @param dir Sort direction: {@link SWT#UP} or {@link SWT#DOWN}.
     */
    public IFeatureTableColumn sort( int dir );

}

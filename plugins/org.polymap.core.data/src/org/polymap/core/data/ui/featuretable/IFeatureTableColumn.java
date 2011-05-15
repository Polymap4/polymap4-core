/*
 * polymap.org Copyright 2011, Falko Bräutigam, and other contributors as indicated by the
 * @authors tag. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.data.ui.featuretable;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewerColumn;

interface IFeatureTableColumn {

    public void setViewer( FeatureTableViewer viewer );

    public abstract EditingSupport getEditingSupport();

    public abstract CellLabelProvider getLabelProvider();

    public TableViewerColumn newViewerColumn();

}
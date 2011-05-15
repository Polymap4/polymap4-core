/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.data.ui.featuretable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class FeatureTableLabelProvider
        extends BaseLabelProvider
        implements ITableLabelProvider {

    private static Log log = LogFactory.getLog( FeatureTableLabelProvider.class );

    private FeatureTableViewer      viewer;


    public FeatureTableLabelProvider( FeatureTableViewer viewer ) {
        super();
        this.viewer = viewer;
    }


    public void dispose() {
        super.dispose();
        this.viewer = null;
    }


    public String getColumnText( Object element, int columnIndex ) {
        if (element instanceof IFeatureTableElement) {

        }
    }


    public Image getColumnImage( Object element, int columnIndex ) {
        return null;
    }

}

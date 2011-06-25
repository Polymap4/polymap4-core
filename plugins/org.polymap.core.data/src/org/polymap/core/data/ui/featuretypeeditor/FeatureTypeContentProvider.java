/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.ui.featuretypeeditor;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * A Tree Content Provider that serves up attributeTypes from a SimpleFeatureType as a parent.
 * <p>
 * The code was originally found in {@link net.refractions.udig.ui.FeatureTypeEditor}.
 * 
 * @author jones
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class FeatureTypeContentProvider
        implements ITreeContentProvider {

    private TreeViewer viewer;


    public FeatureTypeContentProvider( TreeViewer viewer ) {
        this.viewer = viewer;
    }


    public void dispose() {
    }


    public void inputChanged( Viewer _viewer, Object _oldInput, Object _newInput ) {
    }


    public Object[] getChildren( Object parentElement ) {
        if (parentElement instanceof SimpleFeatureType) {
            SimpleFeatureType featureType = (SimpleFeatureType)parentElement;
            Object[] attributes = new Object[featureType.getAttributeCount()];
            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = featureType.getDescriptor( i );
            }
            return attributes;
        }
        return null;
    }


    public Object getParent( Object element ) {
        if (element instanceof AttributeDescriptor) {
            return viewer.getInput();
        }
        return null;
    }


    public boolean hasChildren( Object element ) {
        if (element instanceof SimpleFeatureType)
            return true;
        return false;
    }


    public Object[] getElements( Object inputElement ) {
        return getChildren( inputElement );
    }

}
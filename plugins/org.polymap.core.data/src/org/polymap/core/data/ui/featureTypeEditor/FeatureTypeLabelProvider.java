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
package org.polymap.core.data.ui.featureTypeEditor;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Label provider for labeling AttributeTypes.
 * <p>
 * The code was originally found in {@link net.refractions.udig.ui.FeatureTypeEditor}.
 * 
 * @author jones
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class FeatureTypeLabelProvider
        extends LabelProvider
        implements IBaseLabelProvider, ITableLabelProvider {

    private FeatureTypeEditor       fte;
    
    
    public FeatureTypeLabelProvider( FeatureTypeEditor fte ) {
        super();
        this.fte = fte;
    }


    public Image getColumnImage( Object element, int columnIndex ) {
        if (columnIndex > 2) {
            return fte.viewerColumns.get( columnIndex-3 )
                    .getImage( (AttributeDescriptor)element );
        }
        else {
            return null;
        }
    }


    public String getColumnText( Object element, int columnIndex ) {
        AttributeDescriptor attribute = (AttributeDescriptor)element;
        switch (columnIndex) {
            case 0: { // Attribute Name element
                return attribute.getLocalName();
            }
            case 1: { // Attribute Type element
                return attribute.getType().getBinding().getSimpleName();
            }
            case 2: { // CRS element
                if (attribute instanceof GeometryDescriptor) {
                    CoordinateReferenceSystem crs = ((GeometryDescriptor)attribute)
                            .getCoordinateReferenceSystem();
                    if (crs != null) {
                        return crs.getName().toString();
                    }
                    else {
                        return "Unspecified";
                    }
                }
                break;
            }
            default: {
                return fte.viewerColumns.get( columnIndex-3 ).getText( attribute );
            }
        }
        return null;
    }

}

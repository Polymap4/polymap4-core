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

import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.jface.viewers.ICellModifier;

/**
 * ...
 * <p>
 * The code was originally found in
 * {@link net.refractions.udig.ui.FeatureTypeEditor}.
 * 
 * @author jones
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
class AttributeCellModifier
        implements ICellModifier {

    /** fte */
    private final FeatureTypeEditor fte;

    private Object                  lastCRS;


    /**
     * 
     * @param fte
     */
    AttributeCellModifier( FeatureTypeEditor featureTypeEditor ) {
        this.fte = featureTypeEditor;
        this.lastCRS = this.fte.getDefaultCRS();
    }


    public boolean canModify( Object element, String property ) {
        switch (Integer.parseInt( property )) {
            case FeatureTypeEditor.NAME_COLUMN : {
                return true;
            }
            case FeatureTypeEditor.TYPE_COLUMN : {
                return true;
            }
            case FeatureTypeEditor.OTHER_COLUMN : {
                return element instanceof GeometryDescriptor;
            }
            default : {
                return fte.viewerColumns.get( Integer.parseInt( property ) - 3 )
                        .canModify( (AttributeDescriptor)element );
            }
        }
    }


    public Object getValue( Object element, String property ) {
        AttributeDescriptor attr = (AttributeDescriptor)element;
        switch (Integer.parseInt( property )) {
            case FeatureTypeEditor.NAME_COLUMN : {
                return attr.getName().toString();
            }
            case FeatureTypeEditor.TYPE_COLUMN : {
                for (int i = 0; i < this.fte.legalTypes.size(); i++) {
                    if (this.fte.legalTypes.get( i ).getType() == attr
                            .getType().getBinding())
                        return i;
                }
                return -1;
            }
            case FeatureTypeEditor.OTHER_COLUMN : {
                return ((GeometryDescriptor)element).getCoordinateReferenceSystem();
            }
            default : {
                return fte.viewerColumns.get( Integer.parseInt( property ) - 3 )
                        .getValue( (AttributeDescriptor)element );
            }
        }
    }


    public void modify( Object element, String property, Object value ) {
        if (element == null || property == null || value == null) {
            return;
        }

        AttributeDescriptor attr = (AttributeDescriptor)((TreeItem)element).getData();
        SimpleFeatureType ft = (SimpleFeatureType)this.fte.viewer.getInput();
        AttributeDescriptor newAttr = createNewAttributeType( attr, property, value );

        if (newAttr == null) {
            return;
        }
        int index = 0;
        for (; index < ft.getAttributeCount(); index++) {
            if (ft.getDescriptor( index ) == attr)
                break;
        }
        if (index == ft.getAttributeCount()) {
            return;
        }
        SimpleFeatureTypeBuilder builder = this.fte.builderFromFeatureType( ft );
        builder.remove( ft.getDescriptor( index ).getLocalName() );
        builder.add( index, newAttr );
        this.fte.featureType = builder.buildFeatureType();
        this.fte.viewer.setInput( this.fte.featureType );
    }


    private AttributeDescriptor createNewAttributeType( AttributeDescriptor attr,
            String property, Object value ) {
        AttributeTypeBuilder builder = new AttributeTypeBuilder();
        builder.init( attr );
        // builder.setName((String)property);

        switch (Integer.parseInt( property )) {
            case FeatureTypeEditor.NAME_COLUMN: {
                return builder.buildDescriptor( (String)value );
            }
            case FeatureTypeEditor.TYPE_COLUMN: {
                int choice = -1;
                if (value instanceof Integer) {
                    choice = (Integer)value;
                }
                else if (value instanceof String) {
                    choice = Integer.parseInt( (String)value );
                }
                if (choice == -1) {
                    return null;
                }
                else {
                    Class type = this.fte.legalTypes.get( choice ).getType();
                    builder.setBinding( type );
                    return builder.buildDescriptor( attr.getLocalName() );
                }
            }
            case FeatureTypeEditor.OTHER_COLUMN: {
                lastCRS = value;

                CoordinateReferenceSystem crs = (CoordinateReferenceSystem)value;
                if (this.fte.featureType.getGeometryDescriptor() == attr) {
                    this.fte.setDefaultCRS( crs );
                }

                builder.setCRS( crs );
                return builder.buildDescriptor( attr.getLocalName() );
            }
            default: {
                fte.viewerColumns.get( Integer.parseInt( property ) - 3 )
                        .modify( attr, value );
                return attr;
            }
        }
    }

}
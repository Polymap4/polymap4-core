/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data.ui.featuretypeeditor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;

import org.polymap.core.data.feature.typeeditor.AttributeMapping;
import org.polymap.core.data.feature.typeeditor.FeatureTypeMapping;
import org.polymap.core.data.util.FastApplyComboBoxCellEditor;

/**
 * Used in a {@link FeatureTypeEditor} to select the source attribute names
 * to be mapped to the attributes of the edited feature type.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ValueViewerColumn {

    private static Log log = LogFactory.getLog( ValueViewerColumn.class );

    private int                 prop = -1;
    
    private FeatureTypeEditor   fte;
    
    private FeatureTypeMapping  mappings;
    
    private SimpleFeatureType   sourceFeatureType;
    
    private String              defaultAttributeName;
    
    /** The items last created by {@link #newCellEditor(Tree)}. */
    private List<String>        items;
    
    
    public ValueViewerColumn( FeatureTypeMapping mappings, SimpleFeatureType sourceFeatureType ) {
        this.mappings = mappings;
        this.sourceFeatureType = sourceFeatureType;
    }

    public void init( int _prop, FeatureTypeEditor _fte ) {
        this.prop = _prop;
        this.fte = _fte;
    }
    
    public String getHeaderText() {
        return "Mapping";
    }

    public int getColumnProperty() {
        return prop;
    }

    public CellEditor newCellEditor( Tree tree ) {
        items = new ArrayList();
        for (AttributeDescriptor attr : sourceFeatureType.getAttributeDescriptors()) {
            if (!(attr instanceof GeometryType)) {
                items.add( attr.getLocalName() );
                if (defaultAttributeName == null) {
                    defaultAttributeName = attr.getLocalName();
                }
            }
        }
        ComboBoxCellEditor result = new FastApplyComboBoxCellEditor( tree, 
                items.toArray( new String[items.size()] ),
                SWT.READ_ONLY | SWT.FULL_SELECTION );
        return result;
        
//      TextCellEditor attributeNameEditor = new TextCellEditor( tree );
//      DialogCellEditor crsEditor = createCRSEditor( tree );
    }

    public Image getImage( AttributeDescriptor element ) {
        return null;
    }

    public String getText( AttributeDescriptor element ) {
        log.debug( "getText(" + element.getLocalName() + "): ..." );
        AttributeMapping mapping = mappings.get( element.getLocalName() );
        
        // new mapping
        if (mapping == null) {
            log.debug( "    no mapping found, creating new..." );
            mapping = new AttributeMapping( element.getLocalName(), String.class, null, "_undefinied_", null );
            mappings.put( mapping );
        }

        // check setting
        if (sourceFeatureType.getDescriptor( mapping.getSourceName() ) == null ) {
            String firstAttrName = sourceFeatureType.getDescriptor( 0 ).getLocalName();
            log.debug( "    adjust mapping to: " + firstAttrName );
            mapping.setSourceName( firstAttrName );
        }
        return mapping.getSourceName();
    }

    public boolean canModify( AttributeDescriptor element ) {
        log.debug( "canModify(" + element.getLocalName() + "): ..." );
        return true;
    }

    public Object getValue( AttributeDescriptor element ) {
        log.debug( "getValue(" + element.getLocalName() + "): ..." );
        return 0;
    }

    public void modify( AttributeDescriptor element, Object value ) {
        log.debug( "modify(" + element.getLocalName() + "): value= " + value );
        AttributeMapping mapping = mappings.get( element.getName().getLocalPart() );
        
        int itemIndex = Integer.parseInt( value.toString() );
        log.debug( "    attr: " + items.get( itemIndex ) );
        mapping.setSourceName( items.get( itemIndex ) );
    }

}

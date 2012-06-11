/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.core.project.ui.properties;

import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.polymap.core.runtime.Polymap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NumberPropertyDescriptor
        extends PropertyDescriptor {
    
    private static Log log = LogFactory.getLog( NumberPropertyDescriptor.class );
    
    private NumberFormat                format = NumberFormat.getInstance( Polymap.getSessionLocale() );
    
    private boolean                     editable;
    
    
    public NumberPropertyDescriptor( Object id, String displayName ) {
        super( id, displayName );

        setLabelProvider( new LabelProvider() {
            public String getText( Object elm ) {
                return format.format( elm );     
            }
        });
    }


    public NumberPropertyDescriptor setEditable( boolean editable ) {
        this.editable = editable;
        return this;
    }


    public CellEditor createPropertyEditor( Composite parent ) {
        if (!editable) {
            return null;
        }
        CellEditor editor = new TextCellEditor( parent ) {
            protected void doSetValue( Object value ) {
                if (value instanceof Number) {
                    super.doSetValue( format.format( value ) );
                }
                else {
                    super.doSetValue( "unknown type: " + value.getClass().getSimpleName() );
                }
            }

            protected Object doGetValue() {
                try {
                    return format.parse( (String)super.doGetValue() );
                }
                catch (ParseException e) {
                    throw new RuntimeException( "should never happen: ", e );
                }
            }
        };

        editor.setValidator( new ICellEditorValidator() {
            public String isValid( Object value ) {
                if (value instanceof String) {
                    try {
                        format.parse( (String)value );
                        return null;
                    }
                    catch (ParseException e) {
                        return e.getLocalizedMessage();
                    }
                }
                else if (value instanceof Number) {
                    return "";
                }
                return "";
            }
        });
        return editor;
    }

    
    public NumberPropertyDescriptor setFormat( NumberFormat format ) {
        this.format = format;
        return this;
    }

}

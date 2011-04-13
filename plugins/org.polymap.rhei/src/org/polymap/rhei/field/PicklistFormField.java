/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
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
package org.polymap.rhei.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.model.ConstantWithSynonyms;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class PicklistFormField
        implements IFormField {

    private static Log log = LogFactory.getLog( PicklistFormField.class );

    private IFormFieldSite          site;
    
    private Combo                   combo;
    
    private int                     comboStyle = SWT.None;
    
    private LabelProvider           labelProvider = new LabelProvider();

    /**
     * Maps display value into associated return code (when selected). The TreeMap
     * sorts tha keys alphabetically.
     */
    protected TreeMap<String,Object> values = new TreeMap();
    
    private Object                  loadedValue;
    
    private List<ModifyListener>    modifyListeners = new ArrayList();


    /**
     * 
     */
    public static class LabelProvider {
        public String getText( String label, Object value ) {
            return label;
        }
    }

    /**
     * A additional {@link LabelProvider} allows to transform the labels in the
     * dropdown of the combo of this picklist.
     */
    public void setLabelProvider( LabelProvider labelProvider ) {
        this.labelProvider = labelProvider;
    }

    
    public PicklistFormField() {
    }
    
    /**
     * The given strings are used as label in the combo and as value to
     * be stored in the property.
     *  
     * @param values
     */
    public PicklistFormField( Iterable<String> values ) {
        for (String value : values) {
            this.values.put( value, value );
        }
    }

    /**
     *  
     * @param values Maps labels into property values
     */
    public PicklistFormField( Map<String,Object> values ) {
        this.values.putAll( values );
    }

    public PicklistFormField( ConstantWithSynonyms.Type<? extends ConstantWithSynonyms,String> constants ) {
        for (ConstantWithSynonyms constant : constants) {
            this.values.put( (String)constant.label, constant.id );
        }
    }

    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }

    public void dispose() {
    }


    /**
     * Add a raw {@link ModifyListener} to the combo of this picklist. This
     * listener is called when the user types into the textfield of the combo.
     */
    public void addModifyListener( ModifyListener l ) {
        modifyListeners.add( l );
        if (combo != null) {
            combo.addModifyListener( l );
        }
    }


    /**
     * Sets the text of the combo editable.
     * <p>
     * This method can be called only while initializing before the widget has
     * been created.
     */
    public void setTextEditable( boolean editable ) {
        assert combo == null : "Method can be called only before the widget has been created.";
        comboStyle = !editable
            ? comboStyle | SWT.READ_ONLY
            : comboStyle & ~SWT.READ_ONLY;
    }
            
    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        combo = toolkit.createCombo( parent, Collections.EMPTY_SET );
        
        //
        for (ModifyListener l : modifyListeners) {
            combo.addModifyListener( l );
        }
        
        // add values
        for (Map.Entry<String,Object> entry : values.entrySet()) {
            combo.add( labelProvider.getText( entry.getKey(), entry.getValue() ) );
        }
        
        // modify listener
        combo.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                // assume text is a label: try to find a value for it 
                Object value = values.get( combo.getText() );
                log.debug( "modifyEvent(): combo= " + combo.getText() + ", value= " + value );
                site.fireEvent( PicklistFormField.this, IFormFieldListener.VALUE_CHANGE, value ); 
            }
        });
        // selection listener
        combo.addSelectionListener( new SelectionListener() {
            public void widgetSelected( SelectionEvent ev ) {
                log.debug( "widgetSelected(): selectionIndex= " + combo.getSelectionIndex() );
                
                int i = 0;
                for (String label : values.keySet()) {
                    if (i++ == combo.getSelectionIndex()) {
                        combo.setText( label );
                        break;
                    }
                }
                Object value = values.get( combo.getText() );
                site.fireEvent( PicklistFormField.this, IFormFieldListener.VALUE_CHANGE, value );
            }
            public void widgetDefaultSelected( SelectionEvent ev ) {
            }
        });

        // focus listener
        combo.addFocusListener( new FocusListener() {
            public void focusLost( FocusEvent event ) {
                site.fireEvent( this, IFormFieldListener.FOCUS_LOST, combo.getText() );
            }
            public void focusGained( FocusEvent event ) {
                site.fireEvent( this, IFormFieldListener.FOCUS_GAINED, combo.getText() );
            }
        });
        
        return combo;
    }

    public void setEnabled( boolean enabled ) {
        combo.setEnabled( enabled );
    }

    public void setValue( Object value ) {
        if (value != null) {
            // find label for given value
            for (Map.Entry<String,Object> entry : values.entrySet()) {
                if (value.equals( entry.getValue() )) {
                    combo.setText( entry.getKey() );
                }
            }
        }
    }

    public void load() throws Exception {
        assert combo != null : "Control is null, call createControl() first.";
        
        loadedValue = site.getFieldValue();
        setValue( loadedValue );
        
//        int i = 0;
//        for (Iterator it=values.values().iterator(); it.hasNext(); i++) {
//            if (it.next().equals( loadedValue )) {
//                combo.select( i );
//                return;
//            }
//        }
    }

    public void store() throws Exception {
        Object value = values.get( combo.getText() );
        site.setFieldValue( value );
    }

}

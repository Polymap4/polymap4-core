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

import org.apache.commons.lang.ArrayUtils;
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
 * <p/>
 * By default the {@link #setForceTextMatch(boolean)} is set to true, and
 * {@link #setTextEditable(boolean)} is set to false.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class PicklistFormField
        implements IFormField {

    private static Log log = LogFactory.getLog( PicklistFormField.class );

    public static final int         FORCE_MATCH = 1;
    
    public static final int         TEXT_EDITABLE = 2;
    
    private IFormFieldSite          site;
    
    private Combo                   combo;
    
    private boolean                 textEditable = false;

    private boolean                 forceTextMatch = true;
    
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

    
    public PicklistFormField( int... flags ) {
        if (flags.length > 0) {
            setTextEditable( ArrayUtils.contains( flags, TEXT_EDITABLE ) );
            setForceTextMatch( ArrayUtils.contains( flags, FORCE_MATCH ) );
        }
    }
    
    
    /**
     * The given strings are used as label in the combo and as value to
     * be stored in the property.
     *  
     * @param values
     */
    public PicklistFormField( Iterable<String> values, int... flags ) {
        this( flags );
        for (String value : values) {
            this.values.put( value, value );
        }
    }

    /**
     * The given strings are used as label in the combo and as value to
     * be stored in the property.
     *  
     * @param values
     */
    public PicklistFormField( String[] values, int... flags ) {
        this( flags );
        for (String value : values) {
            this.values.put( value, value );
        }
    }

    /**
     *  
     * @param values Maps labels into property values
     */
    public PicklistFormField( Map<String,? extends Object> values ) {
        setForceTextMatch( true );
        setTextEditable( false );

        this.values.putAll( values );
    }

    public PicklistFormField( ConstantWithSynonyms.Type<? extends ConstantWithSynonyms,String> constants ) {
        setForceTextMatch( true );
        setTextEditable( false );
        
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
    public void setTextEditable( boolean textEditable ) {
        this.textEditable = textEditable;
    }

    
    /**
     * If true, then the current text of the {@link #combo} is returned only if
     * it matches one of the labels. Otherwise the text is returned as is.
     */
    public void setForceTextMatch( boolean forceTextMatch ) {
        this.forceTextMatch = forceTextMatch;    
    }
    
    
    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        int comboStyle = SWT.DROP_DOWN;
        comboStyle = !textEditable 
                ? comboStyle | SWT.READ_ONLY
                : comboStyle & ~SWT.READ_ONLY;
        combo = toolkit.createCombo( parent, Collections.EMPTY_SET, comboStyle );
        
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
                Object value = getValue();
                log.debug( "modifyEvent(): combo= " + combo.getText() + ", value= " + value );
                
                if (forceTextMatch && combo.getText().length() > 0 && value == null) {
                    site.setErrorMessage( "Wert entspricht keiner der Vorgaben: " + combo.getText() );
                }
                else {
                    site.setErrorMessage( null );
                }
                // null ist not allowed as text of the combo, so if the loadedValue was null,
                // then map "" value to null; otherwise the world would see that value as changed
                if (value != null && value.equals( "" ) && loadedValue == null) {
                    value = null;
                }
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

    
    public IFormField setEnabled( boolean enabled ) {
        combo.setEnabled( enabled );
        return this;
    }


    public IFormField setValue( Object value ) {
        combo.setText( "" );
        combo.deselectAll();

        if (forceTextMatch && value != null) {
            // find label for given value
            for (Map.Entry<String,Object> entry : values.entrySet()) {
                if (value.equals( entry.getValue() )) {
                    combo.setText( entry.getKey() );
                    break;
                }
            }
        }
        else {
            combo.setText( value != null ? (String)value : "" );
        }
        return this;
    }

    
    /**
     * Returns the current value depending on the {@link #forceTextMatch} flag.
     * If true, then the current text of the {@link #combo} is returned only if
     * it matches one of the labels. Otherwise the text is returned as is.
     * 
     * @return
     */
    protected Object getValue() {
        String text = combo.getText();
        return forceTextMatch ? values.get( text ) : text; 
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
        site.setFieldValue( getValue() );
    }

}

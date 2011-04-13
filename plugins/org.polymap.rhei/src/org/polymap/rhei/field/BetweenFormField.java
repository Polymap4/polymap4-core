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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class BetweenFormField
        implements IFormField, IFormFieldListener {

    private static Log log = LogFactory.getLog( BetweenFormField.class );

    private IFormFieldSite      site;
    
    private IFormField          field1, field2;
    
    private Object              newValue1, newValue2;


    public BetweenFormField( IFormField field1, IFormField field2 ) {
        super();
        this.field1 = field1;
        this.field2 = field2;
    }

    public void init( IFormFieldSite _site ) {
        this.site = _site;
        field1.init( site );
        field2.init( site );
    }

    public void dispose() {
        site.removeChangeListener( this );
        if (field1 != null) {
            field1.dispose();
            field1 = null;
        }
        if (field2 != null) {
            field2.dispose();
            field2 = null;
        }
    }

    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        Composite contents = toolkit.createComposite( parent );
        RowLayout layout = new RowLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.spacing = 3;
//        layout.justify = true;
        layout.center = true;
        contents.setLayout( layout );

        Control field1Control = field1.createControl( contents, toolkit );
        toolkit.createLabel( contents, "bis" );
        Control field2Control = field2.createControl( contents, toolkit );

        site.addChangeListener( this );
        
        contents.pack( true );
        return contents;
    }

    public void setEnabled( boolean enabled ) {
        field1.setEnabled( enabled );
        field2.setEnabled( enabled );
    }

    public void setValue( Object value ) {
        throw new RuntimeException( "Not yet implemented." );
    }

    public void load() throws Exception {
        assert field1 != null && field2 != null : "Control is null, call createControl() first.";
        field1.load();
        field2.load();
    }

    public void store() throws Exception {
        site.setFieldValue( new Object[] {newValue1, newValue2} );
    }

    public void fieldChange( FormFieldEvent ev ) {
        log.debug( "fieldChange(): ev=" + ev );
        
        Object source = ev.getSource();
        if (ev.getEventCode() == VALUE_CHANGE && ev.getSource() == field1) {
            newValue1 = ev.getNewValue();
//            newValue2 = newValue2 == null ? newValue1 : newValue2;
        }
        if (ev.getEventCode() == VALUE_CHANGE && ev.getSource() == field2) {
            newValue2 = ev.getNewValue();
//            newValue1 = newValue1 == null ? newValue2 : newValue1;
        }
        if (newValue1 instanceof Comparable && newValue2 instanceof Comparable) {
            Comparable c1 = (Comparable)newValue1;
            Comparable c2 = (Comparable)newValue2;
            if (c1.compareTo( c1 ) > 0) {
                newValue2 = newValue1;
            }
        }
    }
    
}

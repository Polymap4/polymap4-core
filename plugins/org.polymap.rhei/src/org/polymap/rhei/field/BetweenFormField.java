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
        implements IFormField {

    private static Log log = LogFactory.getLog( BetweenFormField.class );

    private IFormFieldSite      site;
    
    private IFormField          field1, field2;
    
    private Object              newValue1, newValue2;

    private Object[]            loadedValue;


    public BetweenFormField( IFormField field1, IFormField field2 ) {
        super();
        this.field1 = field1;
        this.field2 = field2;
    }

    
    public void init( IFormFieldSite _site ) {
        this.site = _site;
        
        // field1
        field1.init( new DelegateSite( site ) {
            
            public Object getFieldValue()
                    throws Exception {
                return loadedValue != null ? loadedValue[0] : null;
            }
            
            public void setFieldValue( Object value )
                    throws Exception {
                throw new RuntimeException( "not yet implemented." );
            }
        });
        
        // field2
        field2.init( new DelegateSite( site ) {
            
            public Object getFieldValue()
                    throws Exception {
                return loadedValue != null ? loadedValue[1] : null;
            }

            public void setFieldValue( Object value )
                    throws Exception {
                throw new RuntimeException( "not yet implemented." );
            }
        });
    }

    
    public void dispose() {
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
        assert field1 != null || field2 != null : "Control is null, call createControl() first.";
        
        // FIXME makes DateField be dirty?
        if (site.getFieldValue() == null) {
            loadedValue = null;
            field1.load();
            field2.load();
        }
        else if (site.getFieldValue() instanceof Object[]) {
            loadedValue = (Object[])site.getFieldValue();
            field1.load();
            field2.load();
        }
        else {
            log.warn( "Unknown value type: " + site.getFieldValue() );
        }
    }

    
    public void store() throws Exception {
        site.setFieldValue( new Object[] {newValue1, newValue2} );
    }

    
    public void fireEvent( Object eventSrc, int eventCode, Object newValue ) {
        log.debug( "fireEvent(): ev=" + eventCode + ", newValue=" + newValue );
        
        if (eventCode == IFormFieldListener.VALUE_CHANGE && eventSrc == field1) {
            newValue1 = newValue;
//            newValue2 = newValue2 == null ? newValue1 : newValue2;
        }
        if (eventCode == IFormFieldListener.VALUE_CHANGE && eventSrc == field2) {
            newValue2 = newValue;
//            newValue1 = newValue1 == null ? newValue2 : newValue1;
        }
        if (newValue1 instanceof Comparable && newValue2 instanceof Comparable) {
            Comparable c1 = (Comparable)newValue1;
            Comparable c2 = (Comparable)newValue2;
            if (c1.compareTo( c1 ) > 0) {
                newValue2 = newValue1;
            }
        }
        
        Object value = newValue1 != null || newValue2 != null
                ? new Object[] {newValue1, newValue2}
                : null;
        site.fireEvent( this, eventCode, value );
    }
    
    
    /**
     * 
     */
    abstract class DelegateSite
            implements IFormFieldSite {
        
        private IFormFieldSite      delegate;

        
        public DelegateSite( IFormFieldSite delegate ) {
            this.delegate = delegate;
        }

        public void addChangeListener( IFormFieldListener l ) {
            delegate.addChangeListener( l );
        }

        public void fireEvent( Object source, int eventCode, Object newValue ) {
            BetweenFormField.this.fireEvent( source, eventCode, newValue );
        }

        public String getErrorMessage() {
            return delegate.getErrorMessage();
        }

        public String getFieldName() {
            return delegate.getFieldName();
        }

        public IFormEditorToolkit getToolkit() {
            return delegate.getToolkit();
        }

        public boolean isDirty() {
            return delegate.isDirty();
        }

        public boolean isValid() {
            return delegate.isValid();
        }

        public void removeChangeListener( IFormFieldListener l ) {
            delegate.removeChangeListener( l );
        }

        public void setErrorMessage( String msg ) {
            delegate.setErrorMessage( msg );
        }
        
    }
    
}

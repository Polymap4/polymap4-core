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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;

import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * A date/time form field based on the {@link DateTime} widget.
 * <p> 
 * FIXME - Implementation depends on Locale.GERMANY.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class DateTimeFormField
        implements IFormField {

    private static Log log = LogFactory.getLog( DateTimeFormField.class );

    private IFormFieldSite      site;
    
    private DateTime            dateTime;
    
    private boolean             enabled = true;

    private Date                loadedValue;
    
    /** Special value representing a "null" as the propety value. */
    private Date                nullValue;


    public void init( IFormFieldSite _site ) {
        this.site = _site;

        // The DateTime field supports seconds only, so we need the current
        // time without the millis for the nullValue in order to make it
        // comparable to the result of the DateTime
        Calendar cal = Calendar.getInstance( Locale.GERMANY );
        cal.set( Calendar.MILLISECOND, 0 );
        this.nullValue = cal.getTime();
    }

    
    public void dispose() {
    }

    
    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        dateTime = toolkit.createDateTime( parent, new Date(), SWT.MEDIUM | SWT.DROP_DOWN );
        dateTime.setEnabled( enabled );
        dateTime.setBackground( enabled ? FormEditorToolkit.textBackground : FormEditorToolkit.textBackgroundDisabled );
        
        // selection(modify) listener
        dateTime.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                Calendar cal = Calendar.getInstance( Locale.GERMANY );
                cal.set( dateTime.getYear(), dateTime.getMonth(), dateTime.getDay(),
                        dateTime.getHours(), dateTime.getMinutes(), dateTime.getSeconds() );
                Date date = cal.getTime();
                log.info( "widgetSelected(): test= " + date );
                
                site.fireEvent( DateTimeFormField.this, IFormFieldListener.VALUE_CHANGE, 
                        loadedValue == null && date.equals( nullValue ) ? null : date );
            }
            
        });
        // focus listener
        dateTime.addFocusListener( new FocusListener() {
            public void focusLost( FocusEvent event ) {
                site.fireEvent( DateTimeFormField.this, IFormFieldListener.FOCUS_LOST, null );
            }
            public void focusGained( FocusEvent event ) {
                site.fireEvent( DateTimeFormField.this, IFormFieldListener.FOCUS_GAINED, null );
            }
        });
        return dateTime;
    }

    
    public IFormField setEnabled( boolean enabled ) {
        this.enabled = enabled;
        if (dateTime != null) {
            dateTime.setEnabled( enabled );
            dateTime.setBackground( enabled ? FormEditorToolkit.textBackground : FormEditorToolkit.textBackgroundDisabled );
        }
        return this;
    }

    
    public IFormField setValue( Object value ) {
        Date date = (Date)value;
        Calendar cal = Calendar.getInstance( Locale.GERMANY );
        cal.setTime( date );
        
        // modify the orig value; otherwise millis may differ as DateTime field
        // does not support millis
        cal.set( Calendar.MILLISECOND, 0 );
        date.setTime( cal.getTimeInMillis() );

        dateTime.setDate( cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH ), cal.get( Calendar.DATE ) );
        dateTime.setTime( cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ) );

        // the above calls does not seem to fire events
        site.fireEvent( DateTimeFormField.this, IFormFieldListener.VALUE_CHANGE,
                loadedValue == null && date.equals( nullValue ) ? null : date );
        return this;
    }

    
    public void load() throws Exception {
        assert dateTime != null : "Control is null, call createControl() first.";

        if (site.getFieldValue() == null) {
            loadedValue = null;
            setValue( nullValue );

//            // from the source of DateTime.setDate()
//            dateTime.setDate( 9996, 0, 1 );
//            dateTime.setTime( 0, 0, 0 );
        }
        else if (site.getFieldValue() instanceof Date) {
            loadedValue = (Date)site.getFieldValue();
            setValue( loadedValue );
        }
        else {
            log.warn( "Unknown value type: " + site.getFieldValue() );
        }
    }

    
    public void store() throws Exception {
        if (dateTime.getYear() != 9996) {
            Calendar cal = Calendar.getInstance( Locale.GERMANY );
            cal.set( dateTime.getYear(), dateTime.getMonth(), dateTime.getDay(),
                    dateTime.getHours(), dateTime.getMinutes(), dateTime.getSeconds() );
            site.setFieldValue( cal.getTime() );
        }
    }

}

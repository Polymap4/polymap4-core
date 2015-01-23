/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.ui;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RadioButtonGroup 
        implements SelectionListener {

    private static Log log = LogFactory.getLog( RadioButtonGroup.class );
    
    private Set<Button>         buttons = new HashSet();
    
    private Button              selected;
    

    public void dispose() {
        for (Button btn : buttons) {
            remove( btn );
        }
        buttons.clear();
    }
    
    
    public void add( Button btn ) {
        if (buttons.add( btn )) {
            btn.addSelectionListener( this );
        }
    }


    public void remove( Button btn ) {
        if (buttons.remove( btn )) {
            btn.removeSelectionListener( this );
        }
    }


    @Override
    public void widgetSelected( SelectionEvent ev ) {
        if (((Button)ev.getSource()).getSelection()) {
            if (selected != null) {
                selected.setSelection( false );
                new SelectionEvent( selected, selected, 0 ).processEvent();
                
//                //selected.notifyListeners( SWT.Selection, new Event() );
//                for (Listener listener : selected.getListeners( SWT.Selection )) {
//                    if (listener instanceof TypedListener) {
//                        SelectionListener l = (SelectionListener)((TypedListener)listener).getEventListener();
//                        
//                        Event underlyingEvent = new Event();
//                        underlyingEvent.widget = selected;
//                        SelectionEvent event = new SelectionEvent( new Event() );
//                        l.widgetSelected( event );
//                    }
//                }
                selected = null;
            }
            selected = (Button)ev.getSource();
        }
    }


    @Override
    public void widgetDefaultSelected( SelectionEvent ev ) {
        widgetSelected( ev );
    }
    
}

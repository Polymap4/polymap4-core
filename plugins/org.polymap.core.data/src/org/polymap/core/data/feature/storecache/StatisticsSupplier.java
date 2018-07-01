/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.core.data.feature.storecache;

import java.util.Date;

import java.text.DateFormat;
import java.time.Duration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.data.feature.storecache.StoreCacheProcessor.Timestamp;
import org.polymap.core.data.pipeline.Param;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.runtime.DurationFormat;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class StatisticsSupplier
        implements Param.UISupplier<Object> {

    private DateFormat      df = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM, Polymap.getSessionLocale() );
    
    private DurationFormat  ddf = DurationFormat.getInstance( Polymap.getSessionLocale() );

    private String          layerId;

    private Label           l;


    @Override
    public Control createContents( Composite parent, Param<Object> param, PipelineProcessorSite site )  {
        this.layerId = site.layerId.get();
        
        Composite container = new Composite( parent, SWT.NONE );
        container.setLayout( FormLayoutFactory.defaults().spacing( 3 ).margins( 0, 0, 3, 0 ).create() );

        // Label
        l = new Label( container, SWT.NONE );
        l.setFont( UIUtils.italic( l.getFont() ) );
        updateUI();
        
        //
        Button btn = new Button( container, SWT.PUSH );
        btn.setText( "FLUSH CACHE" );
        btn.setToolTipText( "Reset timestamp so that next access will re-fetch contents from backend store" );
        btn.addSelectionListener( UIUtils.selectionListener( ev -> {
            Timestamp.of( layerId ).clear();
            updateUI();
        }));
        
        // layout
        FormDataFactory.on( l ).fill().noBottom();
        FormDataFactory.on( btn ).top( l ).left( 30 ).right( 70 ).bottom( 100 );
        
        return container;
    }

    
    protected void updateUI() {
        Timestamp timestamp = Timestamp.of( layerId );
        if (timestamp.get() > 0) {
            long ago = System.currentTimeMillis() - timestamp.get();
            l.setText( "Last update: " + df.format( new Date( timestamp.get() ) ) + "  (" + ddf.format( Duration.ofMillis( ago ) ) + ")" );
        }
        else {
            l.setText( "Cache is empty or update forced  " );
        }
        l.getParent().layout( true );
    }
    
}

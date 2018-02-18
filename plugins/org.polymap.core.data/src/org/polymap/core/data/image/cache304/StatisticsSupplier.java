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
package org.polymap.core.data.image.cache304;

import java.text.DateFormat;

import org.apache.commons.io.FileUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

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

    private Composite       container;
    

    @Override
    public Control createContents( Composite parent, Param<Object> param, PipelineProcessorSite site )  {
        layerId = site.layerId.get();

        container = new Composite( parent, SWT.NONE );
        container.setLayout( FormLayoutFactory.defaults().spacing( 3 ).margins( 0, 0, 3, 0 ).create() );

        updateUI();
        return container;
    }

    
    protected Label newLabel( Composite parent, String label, Object value, Label top ) {
        Label l = new Label( parent, SWT.NONE );
        l.setFont( UIUtils.italic( l.getFont() ) );
        l.setText( label + " : " );
        FormDataFactory.on( l ).fill().right( 30 ).noBottom();
        
        Label v = new Label( parent, SWT.NONE );
        v.setFont( UIUtils.italic( l.getFont() ) );
        v.setText( value.toString() + "  ");
        FormDataFactory.on( v ).fill().left( l).noBottom();

        if (top != null) {
            FormDataFactory.on( l ).top( top );            
            FormDataFactory.on( v ).top( top );            
        }
        return l;
    }
    
    
    protected void updateUI() {
        // fields
        CacheStatistics stats = Cache304.instance().statistics();
        Label l = newLabel( container, "Cache size", 
                FileUtils.byteCountToDisplaySize( stats.layerStoreSize( layerId ) ) +
                "  (total " + FileUtils.byteCountToDisplaySize( stats.totalStoreSize() ) + ")", 
                null );
        l = newLabel( container, "Cached tiles", 
                stats.layerTileCount( layerId ), l );
        l = newLabel( container, "Hit/Miss", 
                stats.layerHitCount( layerId ) + " / " + stats.layerMissCount( layerId ), l );

        // button
        Button btn = new Button( container, SWT.PUSH );
        btn.setText( "FLUSH CACHE" );
        //btn.setToolTipText( "Flush all cached tiles" );
        btn.addSelectionListener( UIUtils.selectionListener( ev -> {
            Cache304.instance().updateLayer( layerId, null );
            UIUtils.disposeChildren( container );
            updateUI();
            container.layout( true );
        }));
        
        // layout
        FormDataFactory.on( btn ).top( l ).left( 30 ).right( 70 ).bottom( 100 );
    }
    
}

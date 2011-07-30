/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.data.ui.featuretable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.runtime.ListenerList;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureTableViewer
        extends TableViewer {

    private static Log log = LogFactory.getLog( FeatureTableViewer.class );

    /** Property type that is fired when the content of the viewer has changed. */
    public static final String          PROP_CONTENT_SIZE = "contentsize";
    
    private List<IFeatureTableColumn>   displayed = new ArrayList();

    private List<IFeatureTableColumn>   available = new ArrayList();

    private Map<String,CellEditor>      editors = new HashMap();
    
    private ListenerList<PropertyChangeListener> listeners = new ListenerList();


    public FeatureTableViewer( Composite parent, int style ) {
        super( parent, style | SWT.VIRTUAL );

        getTable().setLinesVisible( true );
        getTable().setHeaderVisible( true );
        getTable().setLayout( new TableLayout() );
        
        getTable().addSelectionListener( new SelectionAdapter() {
            public void widgetDefaultSelected( SelectionEvent e ) {
                log.info( "calling setInput() ..." );
            }
        } );
    }


    public void dispose() {
        listeners.clear();
        listeners = null;
        displayed.clear();
        available.clear();
        editors.clear();
    }


    public int getItemCount() {
        return doGetItemCount();
    }
    
    
    public void addColumn( IFeatureTableColumn column ) {
        column.setViewer( this );
        column.newViewerColumn();

        available.add( column );
        displayed.add( column );
    }


    public void setContent( final PipelineFeatureSource fs, final Filter filter ) {
        setContentProvider( new DeferredFeatureContentProvider( this, fs, filter, "BID" ) );
        setInput( fs );
    }


    public void setContent( FeatureCollection coll ) {
        setContentProvider( new FeatureCollectionContentProvider( coll ) );
    }


    public void setContent( IFeatureContentProvider provider ) {
        super.setContentProvider( provider );
    }


    public boolean addPropertyChangeListener( PropertyChangeListener listener ) {
        return listeners.add( listener );
    }


    public boolean removePropertyChangeListener( PropertyChangeListener listener ) {
        return listeners.remove( listener );
    }


    protected void firePropChange( final String name, Object oldValue, final Object newValue ) {
        final PropertyChangeEvent ev = new PropertyChangeEvent( this, name, oldValue, newValue );

        Display display = getTable().getDisplay();
        
        display.asyncExec( new Runnable() {
        
            public void run() {
                if (PROP_CONTENT_SIZE.equals( name ) ) {
                    setItemCount( (Integer)newValue );
                }
                for (PropertyChangeListener l : listeners.getListeners()) {
                    l.propertyChange( ev );
                }
            }
        });
    }

}

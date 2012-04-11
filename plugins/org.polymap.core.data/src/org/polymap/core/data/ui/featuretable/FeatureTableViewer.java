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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.geotools.feature.FeatureCollection;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

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
    public static final String              PROP_CONTENT_SIZE = "contentsize";

    private static final Color              LOADING_FOREGROUND = Graphics.getColor( 0xa0, 0xa0, 0xa0 );
    
    private Map<String,IFeatureTableColumn> displayed = new HashMap();

    private Map<String,IFeatureTableColumn> available = new HashMap();

    private Map<String,CellEditor>          editors = new HashMap();
    
    private ListenerList<PropertyChangeListener> listeners = new ListenerList();

    private Color                           foreground;


    public FeatureTableViewer( Composite parent, int style ) {
        super( parent, style | SWT.VIRTUAL );

        ColumnViewerToolTipSupport.enableFor( this );
        getTable().setLinesVisible( true );
        getTable().setHeaderVisible( true );
        getTable().setLayout( new TableLayout() );

        this.foreground = getTable().getForeground();
        
        //setUseHashlookup( true );
    }


    public void dispose() {
        listeners.clear();
        listeners = null;
        displayed.clear();
        available.clear();
        editors.clear();
    }


    public int getElementCount() {
        return doGetItemCount();
    }
    

    public IFeatureTableElement[] getElements() {
        IFeatureTableElement[] result = new IFeatureTableElement[ getElementCount() ];
        for (int i=0; i<getElementCount(); i++) {
            result[i] = (IFeatureTableElement)getElementAt( i );
        }
        return result;
    }

    
    public IFeatureTableElement[] getSelectedElements() {
        IStructuredSelection sel = (IStructuredSelection)getSelection();
        IFeatureTableElement[] result = new IFeatureTableElement[ sel.size() ];
        int i = 0;
        for (Iterator it=sel.iterator(); it.hasNext(); i++) {
            result[i] = (IFeatureTableElement)it.next();
        }
        return result;
    }

    
    /**
     * Selects the element with the given feature id.
     * 
     * @param fid
     * @param reveal
     */
    public void selectElement( final String fid, boolean reveal ) {
        assert fid != null;
        IFeatureTableElement search = new IFeatureTableElement() {
            public Object getValue( String name ) {
                throw new RuntimeException( "not yet implemented." );
            }
    
            public String fid() {
                return fid;
            }
            
            public boolean equals( Object other ) {
                return other instanceof IFeatureTableElement
                        ? fid.equals( ((IFeatureTableElement)other).fid() )
                        : false;
            }

            public int hashCode() {
                return fid.hashCode();
            }
        };
        
        int index = -1;
        if (getContentProvider() instanceof IDeferredFeatureContentProvider) {
            // find index from content provider
            index = ((IDeferredFeatureContentProvider)getContentProvider()).findElement( search );
            // select table
            getTable().setSelection( index );
            log.debug( "getTable().getSelectionIndex(): " + getTable().getSelectionIndex() );
            if (reveal) {
                getTable().showSelection();
            }
            // fire event
            ISelection sel = getSelection();
            log.debug( "getSelection(): " + sel );
            updateSelection( sel );
        }
        else {
            ISelection sel = new StructuredSelection( search );
            setSelection( sel, reveal );
        }
        getTable().layout();
    }


    public void addColumn( IFeatureTableColumn column ) {
        column.setViewer( this );
        TableViewerColumn viewerColumn = column.newViewerColumn();
        viewerColumn.getColumn().setData( "name", column.getName() );

        available.put( column.getName(), column );
        displayed.put( column.getName(), column );
    }


    /**
     * Set the content of this viewer. A {@link DeferredFeatureContentProvider} is
     * used to fetch and sort the features.
     * 
     * @param fs
     * @param filter
     */
    public void setContent( final PipelineFeatureSource fs, final Filter filter ) {
        TableColumn sortColumn = getTable().getSortColumn();
        int sortDir = SWT.DOWN;
        if (sortColumn == null) {
            sortColumn = getTable().getColumn( 0 );
            getTable().setSortColumn( sortColumn );
            getTable().setSortDirection( sortDir );
        }
        else {
            sortDir = getTable().getSortDirection();
        }
        
        String colName = (String)sortColumn.getData( "name" );
        IFeatureTableColumn sortTableColumn = displayed.get( colName );

        setContentProvider( new DeferredFeatureContentProvider2( this, fs, filter,
                sortTableColumn.newComparator( sortDir ) ) );
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
                if (getTable().isDisposed()) {
                    return;
                }
//                if (PROP_CONTENT_SIZE.equals( name ) ) {
//                    getTable().setForeground( Graphics.getColor( 0x70, 0x70, 0x80 ) );
//                }
                for (PropertyChangeListener l : listeners.getListeners()) {
                    l.propertyChange( ev );
                }
            }
        });
    }


    /**
     * Sorts the table entries by delegating the call to the content provider.
     * <p/>
     * Must be caled only if the content provider is a {@link DeferredFeatureContentProvider}!
     * 
     * @param comparator
     * @param dir
     * @param column
     */
    protected void sortContent( Comparator<IFeatureTableElement> comparator, int dir, TableColumn column ) {
        IContentProvider contentProvider = getContentProvider();
        // deferred
        if (contentProvider instanceof IDeferredFeatureContentProvider) {
            ((IDeferredFeatureContentProvider)contentProvider).setSortOrder( comparator );
        }
        // normal
        else {
            setComparator( new ViewerComparator( comparator ) {
                public int compare( Viewer viewer, Object e1, Object e2 ) {
                    return getComparator().compare( e1, e2 );
                }
            });
        }
        getTable().setSortColumn( column );
        getTable().setSortDirection( dir );
    }

    
    protected void markTableLoading( final boolean loading ) {
        Display display = getTable().getDisplay();
        display.asyncExec( new Runnable() {
            public void run() {
                if (getTable().isDisposed()) {
                    return;
                }
                if (loading) {
                    getTable().setForeground( LOADING_FOREGROUND );
//                    setBusy( true );
                }
                else {
                    getTable().setForeground( foreground );
//                    setBusy( false );
                }
            }
        });
    }

}

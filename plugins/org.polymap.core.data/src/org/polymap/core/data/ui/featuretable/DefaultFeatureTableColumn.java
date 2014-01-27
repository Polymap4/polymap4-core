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
import java.util.Date;

import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultFeatureTableColumn
        implements IFeatureTableColumn {

    private static Log log = LogFactory.getLog( DefaultFeatureTableColumn.class );

    private FeatureTableViewer      viewer;

    private PropertyDescriptor      prop;

    private boolean                 editing;
    
    private EditingSupport          editingSupport;

    private String                  header;

    private int                     weight = -1;

    private int                     minimumWidth = -1;
    
    private ColumnLabelProvider     labelProvider = new DefaultCellLabelProvider();
    
    private int                     align = -1;
    
    private boolean                 sortable = true;

    private TableViewerColumn       viewerColumn;


    public DefaultFeatureTableColumn( PropertyDescriptor prop ) {
        super();
        assert prop != null : "Argument is null.";
        this.prop = prop;
    }

    public void setViewer( FeatureTableViewer viewer ) {
        this.viewer = viewer;
        this.editingSupport = editing ? new DefaultEditingSupport( viewer ) : null;
    }
    
    public String getName() {
        return prop.getName().getLocalPart();
    }
    
    public DefaultFeatureTableColumn setLabelProvider( ColumnLabelProvider labelProvider ) {
        this.labelProvider = labelProvider;
        return this;
    }

    public DefaultFeatureTableColumn setHeader( String header ) {
        this.header = header;
        return this;
    }

    public DefaultFeatureTableColumn setWeight( int weight, int minimumWidth) {
        this.weight = weight;
        this.minimumWidth = minimumWidth;
        return this;
    }
    
    public DefaultFeatureTableColumn setAlign( int align ) {
        this.align = align;
        return this;
    }

    public DefaultFeatureTableColumn setEditing( boolean editing ) {
        assert viewer == null : "Call before table is created.";
        // defer creation of editingSupport to setViewer()
        this.editing = editing;
        return this;
    }

//    public CellEditor getCellEditor( Composite parent ) {
//        if (cellEditor == null) {
//            Class binding = prop.getType().getBinding();
//            cellEditor = new BasicTypeCellEditor( parent, binding );
//        }
//        return cellEditor;
//    }


    public EditingSupport getEditingSupport() {
        throw new RuntimeException( "not yet implemented" );
    }

    public boolean isSortable() {
        return sortable;
    }
    
    public void setSortable( boolean sortable ) {
        this.sortable = sortable;
    }

    @Override
    public void sort( int dir ) {
        assert viewerColumn != null : "Add this column to the viewer before calling sort()!";
        Comparator<IFeatureTableElement> comparator = newComparator( dir );
        viewer.sortContent( comparator, dir, viewerColumn.getColumn() );        
    }
    
    
    public TableViewerColumn newViewerColumn() {
        assert viewerColumn == null;
        
        if (align == -1) {
            align = Number.class.isAssignableFrom( prop.getType().getBinding() )
                    || Date.class.isAssignableFrom( prop.getType().getBinding() )
                    ? SWT.RIGHT : SWT.LEFT;
        }

        viewerColumn = new TableViewerColumn( viewer, align );
        viewerColumn.getColumn().setMoveable( true );
        viewerColumn.getColumn().setResizable( true );
        
        viewerColumn.setLabelProvider( new LoadingCheckLabelProvider( labelProvider ) );
        String normalizedName = StringUtils.capitalize( getName() );
        viewerColumn.getColumn().setText( header != null ? header : normalizedName );
        
        if (editingSupport != null) {
            viewerColumn.setEditingSupport( editingSupport );
        }
        
        // sort listener for supported prop bindings
        Class propBinding = prop.getType().getBinding();
        if (sortable &&
                (String.class.isAssignableFrom( propBinding )
                || Number.class.isAssignableFrom( propBinding )
                || Date.class.isAssignableFrom( propBinding ))) {

            viewerColumn.getColumn().addListener( SWT.Selection, new Listener() {
                public void handleEvent( Event ev ) {
                    TableColumn sortColumn = viewer.getTable().getSortColumn();
                    final TableColumn selectedColumn = (TableColumn)ev.widget;
                    int dir = viewer.getTable().getSortDirection();
                    //log.info( "Sort: sortColumn=" + sortColumn.getText() + ", selectedColumn=" + selectedColumn.getText() + ", dir=" + dir );

                    if (sortColumn == selectedColumn) {
                        dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                    } 
                    else {
                        dir = SWT.DOWN;
                    }
                    Comparator<IFeatureTableElement> comparator = newComparator( dir );
                    viewer.sortContent( comparator, dir, selectedColumn );
                }
            });
        }
        
        TableLayout tableLayout = (TableLayout)viewer.getTable().getLayout();

        if (weight > -1) {
            tableLayout.addColumnData( new ColumnWeightData( weight, minimumWidth, true ) );            
        }
        else if (String.class.isAssignableFrom( propBinding )) {
            tableLayout.addColumnData( new ColumnWeightData( 20, 120, true ) );
        }
        else {
            tableLayout.addColumnData( new ColumnWeightData( 10, 80, true ) );            
        }
        return viewerColumn;
    }

    
    public Comparator<IFeatureTableElement> newComparator( int sortDir ) {
        Comparator<IFeatureTableElement> result = new Comparator<IFeatureTableElement>() {
            
            private String  sortPropName = getName();            
            private Class   propBinding = prop.getType().getBinding();
            
            public int compare( IFeatureTableElement elm1, IFeatureTableElement elm2 ) {
                Object value1 = elm1.getValue( sortPropName );
                Object value2 = elm2.getValue( sortPropName );
                
                if (value1 == null && value2 == null) {
                    return 0;
                }
                else if (value1 == null) {
                    return -1;
                }
                else if (value2 == null) {
                    return 1;
                }
                else if (String.class.isAssignableFrom( propBinding )) {
                    return ((String)value1).compareToIgnoreCase( (String)value2 );
                }
                else if (Number.class.isAssignableFrom( propBinding )) {
                    return (int)(((Number)value1).doubleValue() - ((Number)value2).doubleValue());
                }
                else if (Date.class.isAssignableFrom( propBinding )) {
                    return ((Date)value1).compareTo( (Date)value2 );
                }
                else {
                    return value1.toString().compareTo( value2.toString() );
                }
            }
        };
        return sortDir == SWT.UP ? new ReverseComparator( result ) : result;
    }

    
    /**
     * 
     */
    public class DefaultCellLabelProvider
            extends ColumnLabelProvider {
            
        public String getText( Object elm ) {
            try {
                IFeatureTableElement featureElm = (IFeatureTableElement)elm;
                //log.info( "getText(): fid=" + featureElm.fid() + ", prop=" + prop.getName().getLocalPart() );

                Object value = featureElm.getValue( getName() );
                return value != null ? value.toString() : "";
            }
            catch (Exception e) {
                log.warn( "", e );
                return "Fehler: " + e.getLocalizedMessage();
            }
        }

        public String getToolTipText( Object elm ) {
            if (elm != null) {
                try {
                    IFeatureTableElement featureElm = (IFeatureTableElement)elm;
                    Object value = featureElm.getValue( getName() );
                    return value != null ? value.toString() : null;
                }
                catch (Exception e) {
                    log.warn( "", e );
                    return null;
                }
            }
            return null;
        }
    }

    
    /**
     * 
     */
    public class DefaultEditingSupport
            extends EditingSupport {

        public DefaultEditingSupport( ColumnViewer viewer ) {
            super( viewer );
        }

        protected boolean canEdit( Object elm ) {
            return true;
        }

        protected CellEditor getCellEditor( Object elm ) {
            return new BasicTypeCellEditor( (Composite)viewer.getControl(), prop.getType().getBinding() );
        }

        protected Object getValue( Object elm ) {
            try {
                IFeatureTableElement featureElm = (IFeatureTableElement)elm;
                Object value = featureElm.getValue( getName() );
                return value;
            }
            catch (Exception e) {
                return "Fehler: " + e.getLocalizedMessage();
            }
        }

        protected void setValue( Object elm, Object value ) {
            IFeatureTableElement featureElm = (IFeatureTableElement)elm;
            featureElm.setValue( getName(), value );
        }
    }

    
    /**
     * 
     */
    class LoadingCheckLabelProvider
            extends ColumnLabelProvider {
    
        private ColumnLabelProvider     delegate;

        public LoadingCheckLabelProvider( ColumnLabelProvider delegate ) {
            this.delegate = delegate;
        }

        public String getText( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? "Laden..."
                    : delegate.getText( element );
        }

        public String getToolTipText( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? null : delegate.getToolTipText( element );
        }

        public Image getImage( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? null : delegate.getImage( element );
        }

        public Color getForeground( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? FeatureTableViewer.LOADING_FOREGROUND
                    : delegate.getForeground( element );
        }

        public Color getBackground( Object element ) {
            return element == FeatureTableViewer.LOADING_ELEMENT
                    ? FeatureTableViewer.LOADING_BACKGROUND
                    : delegate.getBackground( element );
        }

        public void addListener( ILabelProviderListener listener ) {
            delegate.addListener( listener );
        }

//        public void update( ViewerCell cell ) {
//            delegate.update( cell );
//        }

        public void dispose() {
            delegate.dispose();
        }

        public boolean isLabelProperty( Object element, String property ) {
            return delegate.isLabelProperty( element, property );
        }

        public Font getFont( Object element ) {
            return delegate.getFont( element );
        }

        public void removeListener( ILabelProviderListener listener ) {
            delegate.removeListener( listener );
        }

        public Image getToolTipImage( Object object ) {
            return delegate.getToolTipImage( object );
        }

        public Color getToolTipBackgroundColor( Object object ) {
            return delegate.getToolTipBackgroundColor( object );
        }

        public Color getToolTipForegroundColor( Object object ) {
            return delegate.getToolTipForegroundColor( object );
        }

        public Font getToolTipFont( Object object ) {
            return delegate.getToolTipFont( object );
        }

        public Point getToolTipShift( Object object ) {
            return delegate.getToolTipShift( object );
        }

        public boolean useNativeToolTip( Object object ) {
            return delegate.useNativeToolTip( object );
        }

        public int getToolTipTimeDisplayed( Object object ) {
            return delegate.getToolTipTimeDisplayed( object );
        }

        public int getToolTipDisplayDelayTime( Object object ) {
            return delegate.getToolTipDisplayDelayTime( object );
        }

        public int getToolTipStyle( Object object ) {
            return delegate.getToolTipStyle( object );
        }

        public void dispose( @SuppressWarnings("hiding") ColumnViewer viewer, ViewerColumn column ) {
            delegate.dispose( viewer, column );
        }
        
    }

}

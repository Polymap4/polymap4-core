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

import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;


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

    private CellEditor              cellEditor;

    private String                  header;


    public DefaultFeatureTableColumn( PropertyDescriptor prop ) {
        super();
        this.prop = prop;
    }

    public void setViewer( FeatureTableViewer viewer ) {
        this.viewer = viewer;
    }

    public String getName() {
        return prop.getName().getLocalPart();
    }
    
    public DefaultFeatureTableColumn setHeader( String header ) {
        this.header = header;
        return this;
    }

    public DefaultFeatureTableColumn setCellEditor( CellEditor cellEditor ) {
        this.cellEditor = cellEditor;
        return this;
    }

    public CellEditor getCellEditor( Composite parent ) {
        if (cellEditor == null) {
            Class binding = prop.getType().getBinding();
            cellEditor = new BasicTypeCellEditor( parent, binding );
        }
        return cellEditor;
    }


    public EditingSupport getEditingSupport() {
        throw new RuntimeException( "not yet implemented" );
    }

    
    public TableViewerColumn newViewerColumn() {
        int align = Number.class.isAssignableFrom( prop.getType().getBinding() )
                ? SWT.RIGHT : SWT.CENTER;

        TableViewerColumn viewerColumn = new TableViewerColumn( viewer, align );
        viewerColumn.getColumn().setMoveable( true );
        viewerColumn.getColumn().setResizable( true );
        
        viewerColumn.setLabelProvider( newLabelProvider() );
        viewerColumn.getColumn().setText( header != null ? header : getName() );

        // sort listener for supported prop bindings
        Class propBinding = prop.getType().getBinding();
        if (String.class.isAssignableFrom( propBinding )
                || Number.class.isAssignableFrom( propBinding )) {

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

        if (String.class.isAssignableFrom( propBinding )) {
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
                    return ((String)value1).compareTo( (String)value2 );
                }
                else if (Number.class.isAssignableFrom( propBinding )) {
                    return (int)(((Number)value1).doubleValue() - ((Number)value2).doubleValue());
                }
                else {
                    return value1.toString().compareTo( value2.toString() );
                }
            }
        };
        return sortDir == SWT.UP ? new ReverseComparator( result ) : result;
    }

    
    protected CellLabelProvider newLabelProvider() {
        return new ColumnLabelProvider() {
            
            public String getText( Object elm ) {
                try {
                    IFeatureTableElement featureElm = (IFeatureTableElement)elm;
                    //log.info( "getText(): fid=" + featureElm.fid() + ", prop=" + prop.getName().getLocalPart() );

                    Object value = featureElm.getValue( getName() );
                    return value != null ? value.toString() : "";
                }
                catch (Exception e) {
                    return "Fehler: " + e.getLocalizedMessage();
                }
            }

            public String getToolTipText( Object elm ) {
                if (elm != null) {
                    IFeatureTableElement featureElm = (IFeatureTableElement)elm;
                    Object value = featureElm.getValue( getName() );
                    return value != null ? value.toString() : null;
                }
                return null;
            }
            
        };
    }

}

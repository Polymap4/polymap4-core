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

import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
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

    private CellLabelProvider       labelProvider;

    private String                  header;


    public DefaultFeatureTableColumn( PropertyDescriptor prop ) {
        super();
        this.prop = prop;
    }

    public void setViewer( FeatureTableViewer viewer ) {
        this.viewer = viewer;
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


    public TableViewerColumn newViewerColumn() {
        TableViewerColumn viewerColumn = new TableViewerColumn( viewer, SWT.CENTER /*| SWT.BORDER*/ );
//        viewerColumn.setEditingSupport( getEditingSupport() );
        viewerColumn.setLabelProvider( getLabelProvider() );
        viewerColumn.getColumn().setText(
                header != null ? header : prop.getName().getLocalPart() );

        TableLayout tableLayout = (TableLayout)viewer.getTable().getLayout();
        tableLayout.addColumnData( new ColumnWeightData( 10, 50, true ) );
        return viewerColumn;
    }


    public EditingSupport getEditingSupport() {
        throw new RuntimeException( "not yet implementd" );
    }


    public CellLabelProvider getLabelProvider() {
        if (labelProvider == null) {
            labelProvider = new ColumnLabelProvider() {
                public String getText( Object elm ) {
                    try {
                        IFeatureTableElement featureElm = (IFeatureTableElement)elm;
                        //log.info( "getText(): fid=" + featureElm.fid() + ", prop=" + prop.getName().getLocalPart() );
                        
                        Object value = featureElm.getValue( prop.getName().getLocalPart() );
                        return value != null ? value.toString() : "";
                    }
                    catch (Exception e) {
                        return "Fehler: " + e.getLocalizedMessage();
                    }
                }
            };
        }
        return labelProvider;
    }

}

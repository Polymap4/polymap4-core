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
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;


/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureTableViewer
        extends TableViewer {

    private static Log log = LogFactory.getLog( FeatureTableViewer.class );

    private List<IFeatureTableColumn>       displayed = new ArrayList();

    private List<IFeatureTableColumn>       available = new ArrayList();

    private Map<String,CellEditor>          editors;


    public FeatureTableViewer( Composite parent, int style ) {
        super( parent, style | SWT.VIRTUAL );

        getTable().setLinesVisible( true );
        getTable().setHeaderVisible( true );
        getTable().setLayout( new TableLayout() );
    }


    public void addColumn( IFeatureTableColumn column ) {
        column.setViewer( this );
        available.add( column );
        displayed.add( column );
        column.newViewerColumn();
    }


    public void setContent( FeatureCollection coll ) {
        setContentProvider( new FeatureCollectionContentProvider( coll ) );
    }


    public void setContent( IFeatureContentProvider provider ) {
        super.setContentProvider( provider );
        //setLabelProvider( new FeatureTableLabelProvider( this ) );
    }

}

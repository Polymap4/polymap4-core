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
package org.polymap.core.data.ui.featuretable;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 *  A search text field for {@link FeatureTableViewer}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureTableSearchField
        extends ViewerFilter {

    private static Log log = LogFactory.getLog( FeatureTableSearchField.class );
    
    private FeatureTableViewer  viewer;
    
    private Composite           container;
    
    private Text                searchTxt;
    
    private Label               clearBtn;
    
    private List<String>        searchPropNames;

    protected String            filterText;

    
    public FeatureTableSearchField( FeatureTableViewer _viewer, Composite _parent, Iterable<String> _searchPropNames ) {
        this.viewer = _viewer;
        this.searchPropNames = Lists.newArrayList( _searchPropNames );

        container = new Composite( _parent, SWT.NONE );
        container.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        clearBtn = new Label( container, SWT.PUSH | SWT.SEARCH );
        clearBtn.setToolTipText( "Zurücksetzen" );
        clearBtn.setImage( DataPlugin.getDefault().imageForName( "icons/etool16/delete_edit.gif" ) );
        clearBtn.setLayoutData( FormDataFactory.filled().top( 0, 5 ).right( 100, -5 ).left( -1 ).create() );
        clearBtn.addMouseListener( new MouseAdapter() {
            public void mouseUp( MouseEvent e ) {
                searchTxt.setText( "" );
            }
        });
        clearBtn.setVisible( false );

        searchTxt = new Text( container, SWT.SEARCH | SWT.CANCEL );
        searchTxt.setLayoutData( FormDataFactory.filled().create() );
        searchTxt.moveBelow( clearBtn );

        searchTxt.setText( "Suchen..." );
        searchTxt.setToolTipText( "Suchbegriff: min. 3 Zeichen" );
        searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
        searchTxt.addFocusListener( new FocusListener() {
            @Override
            public void focusLost( FocusEvent ev ) {
                if (searchTxt.getText().length() == 0) {
//                    searchTxt.setText( "Suchen..." );
//                    searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
                    clearBtn.setVisible( false );
                }
            }
            @Override
            public void focusGained( FocusEvent ev ) {
                if (searchTxt.getText().startsWith( "Suchen" )) {
                    searchTxt.setText( "" );
                    searchTxt.setForeground( Graphics.getColor( 0x00, 0x00, 0x00 ) );
                }
            }
        });

        viewer.addFilter( FeatureTableSearchField.this );
        
        searchTxt.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                filterText = searchTxt.getText().toLowerCase();
                
                // defer refresh for 3s
                new UIJob( "" ) {
                    String myFilterText = filterText;
                    @Override
                    protected void runWithException( IProgressMonitor monitor ) throws Exception {
                        if (myFilterText != filterText) {
                            log.info( "Text changed: " + myFilterText + " -> " + filterText );
                            return;
                        }
                        getDisplay().asyncExec( new Runnable() {
                            @Override
                            public void run() {
                                clearBtn.setVisible( filterText.length() > 0 );
                                viewer.refresh();
                            }
                        });
                    }
                }.schedule( 2000 );
            }
        });
    }

    
    public void dispose() {
        viewer.removeFilter( FeatureTableSearchField.this );
    }

    
    public Composite getControl() {
        return container;
    }
    

    // ViewerFilter
        
    @Override
    public boolean select( Viewer _viewer, Object parentElm, Object elm ) {
        if (filterText != null /*&& filterText.length() >= 3*/) {
            IFeatureTableElement feature = (IFeatureTableElement)elm;
            
            for (String filterPart : StringUtils.split( filterText.toLowerCase() )) {
                boolean partFound = false;                
                for (String propName : searchPropNames) {
                    try {
                        ColumnLabelProvider labelProvider = viewer.getColumn( propName ).getLabelProvider();
                        String text = labelProvider.getText( feature );
                        //Object value = feature.getValue( propName );
                        if (text != null && text.toLowerCase().contains( filterPart )) {
                            partFound = true; 
                            break;
                        }
                        String tooltip = labelProvider.getToolTipText( feature );
                        if (tooltip != null && tooltip.toLowerCase().contains( filterPart )) {
                            partFound = true; 
                            break;
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
                if (!partFound) {
                    return false;
                }
            }
            return true;
        }
        else {
            return true;
        }
    }
    
}

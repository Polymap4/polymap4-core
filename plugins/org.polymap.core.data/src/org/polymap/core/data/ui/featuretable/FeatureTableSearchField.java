/* 
 * polymap.org
 * Copyright (C) 2013, Falko Br�utigam. All rights reserved.
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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 *  A search text field for {@link FeatureTableViewer}.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class FeatureTableSearchField {

    private static Log log = LogFactory.getLog( FeatureTableSearchField.class );
    
    private FeatureTableViewer  viewer;
    
    private Composite           container;
    
    private Text                searchTxt;
    
    private Label               clearBtn;
    
    private TextFilter          filter;
    
    private List<String>        searchPropNames;

    
    public FeatureTableSearchField( FeatureTableViewer _viewer, Composite _parent, Iterable<String> _searchPropNames ) {
        this.viewer = _viewer;
        this.searchPropNames = Lists.newArrayList( _searchPropNames );

        container = new Composite( _parent, SWT.NONE );
        container.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );

        clearBtn = new Label( container, SWT.PUSH | SWT.SEARCH );
        clearBtn.setToolTipText( "Zur�cksetzen" );
        clearBtn.setImage( DataPlugin.getDefault().imageForName( "icons/etool16/delete_edit.gif" ) );
        clearBtn.setLayoutData( FormDataFactory.filled().top( 0, 5 ).right( 100, -5 ).left( -1 ).create() );
        clearBtn.addMouseListener( new MouseAdapter() {
            public void mouseUp( MouseEvent e ) {
                searchTxt.setText( "" );
            }
        });
//        clearBtn.addSelectionListener( new SelectionAdapter() {
//            public void widgetSelected( SelectionEvent e ) {
//                searchTxt.setText( "" );
//            }
//        });
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
        searchTxt.addModifyListener( new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent ev ) {
                clearBtn.setVisible( searchTxt.getText().length() > 0 );
                
                if (filter != null) {
                    viewer.removeFilter( filter );
                }
                if (searchTxt.getText().length() > 2) {
                    viewer.addFilter( filter = new TextFilter( searchTxt.getText() ) );
                }
            }
        });
    }

    
    public void dispose() {
    }

    
    public Composite getControl() {
        return container;
    }
    

    /**
     * 
     */
    class TextFilter
            extends ViewerFilter {
        
        private String          text;
        
        public TextFilter( String text ) {
            this.text = text;
        }

        @Override
        public boolean select( Viewer _viewer, Object parentElm, Object elm ) {
            IFeatureTableElement feature = (IFeatureTableElement)elm;
            for (String propName : searchPropNames) {
                Object value = feature.getValue( propName );
                if (value != null && value.toString().toLowerCase().contains( text.toLowerCase() )) {
                    return true;
                }
            }
            return false;
        }

    }
    
}

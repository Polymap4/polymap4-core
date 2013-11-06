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

import java.util.ArrayList;
import java.util.List;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.ViewerFilter;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * A set of predefined filters for {@link FeatureTableViewer}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureTableFilterBar {

    private static Log log = LogFactory.getLog( FeatureTableFilterBar.class );
    
    private FeatureTableViewer      viewer;
    
    private Composite               container;
    
    private List<FilterDescriptor>  filters = new ArrayList();
    
    private FilterDescriptor        last;
    
    
    public FeatureTableFilterBar( FeatureTableViewer viewer, Composite parent ) {
        this.viewer = viewer;

        container = new Composite( parent, SWT.NONE );
        container.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
    }

    
    public Control getControl() {
        return container;
    }


    public FilterDescriptor add( final ViewerFilter vfilter ) {
        FormDataFactory layoutData = FormDataFactory.filled().right( -1 );
        if (last != null) {
            layoutData.left( last.btn );
        }

        final FilterDescriptor descriptor = new FilterDescriptor( vfilter );

        final Button btn = new Button( container, SWT.TOGGLE );
        btn.setLayoutData( layoutData.create() );
        btn.addSelectionListener( new SelectionAdapter() {
            private ViewerFilter    myFilter = vfilter;
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                if (btn.getSelection()) {
                    //
                    for (FilterDescriptor filter : filters) {
                        if (filter != descriptor &&
                                filter.group != null && filter.group.equals( descriptor.group )) {
                            filter.btn.setSelection( false );
                            viewer.removeFilter( filter.vfilter );
                        }
                    }
                    
                    viewer.addFilter( myFilter );
                }
                else {
                    viewer.removeFilter( myFilter );
                }
            }
        });

        descriptor.btn = btn;
        filters.add( descriptor );
        return last = descriptor;
    }

    
    /**
     * 
     */
    public class FilterDescriptor {
        
        private Button          btn;
        
        private Filter          filter;
        
        private ViewerFilter    vfilter;
        
        private Predicate       predicate;

        private String          group;

        
        protected FilterDescriptor( ViewerFilter vfilter ) {
            this.vfilter = vfilter;
        }

        public FilterDescriptor setName( String name ) {
            btn.setText( name );
            return this;
        }
        
        public FilterDescriptor setTooltip( String tooltip ) {
            btn.setToolTipText( tooltip );
            return this;
        }

        public FilterDescriptor setIcon( Image icon ) {
            btn.setImage( icon );
            return this;
        }

        public FilterDescriptor setGroup( String group ) {
            this.group = group;
            return this;
        }
        
        public Button getControl() {
            return btn;
        }
        
    }
    
}

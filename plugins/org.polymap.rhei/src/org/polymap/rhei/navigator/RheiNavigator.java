/* 
 * polymap.org
 * Copyright 2010, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.navigator;

import java.util.HashSet;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.CommonNavigator;

import org.polymap.core.model.event.ModelChangeEvent;
import org.polymap.core.model.event.ModelChangeListener;
import org.polymap.core.model.event.PropertyEventFilter;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.project.ui.DefaultPartListener;

/**
 * Spread the Rhei while listening to Charlotte McKinnon... :) 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public class RheiNavigator
        extends CommonNavigator {

    private Log log = LogFactory.getLog( RheiNavigator.class );

    public static final String ID = "org.polymap.rhei.RheiNavigator";

    /** The map that is currently displayed. */
    private IMap                map;
    
    private IWorkbenchPage      page;

    private PartListener        partListener;

    private ModelChangeListener modelListener;


    public void createPartControl( Composite parent ) {
        super.createPartControl( parent );
        
        // selection listener
        getSite().getPage().addSelectionListener( new ISelectionListener() {
            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
                if (!sel.isEmpty() && sel instanceof StructuredSelection) {
                    Object elm = ((StructuredSelection)sel).getFirstElement();
                    log.debug( "page selection: elm= " + elm );
                    if (elm instanceof IMap) {
                        setInputMap( (IMap)elm );
                    }
                }
//                else {
//                    selectedMap = null;
//                }
            }
        });
        
        // part listener
        partListener = new PartListener();
        page = getSite().getWorkbenchWindow().getActivePage();
        page.addPartListener( partListener );
    }

    
    public void dispose() {
        super.dispose();
        if (partListener != null) {
            page.removePartListener( partListener );
            page = null;
            partListener = null;
        }
    }

    
    protected void setInputMap( IMap map ) {
        if (map != null && !map.equals( this.map )) {
            if (this.map != null && modelListener != null) {
                ProjectRepository.instance().removeModelChangeListener( modelListener );
                modelListener = null;
            }
            this.map = map;
            getCommonViewer().setInput( this.map );
            getCommonViewer().refresh();
            
            modelListener = new ModelChangeListener() {
                public void modelChanged( ModelChangeEvent ev ) {
                    log.debug( "ev= " + ev + ", display= " + Display.getCurrent() );
                    getCommonViewer().getControl().getDisplay().asyncExec( new Runnable() {
                        public void run() {
                            getCommonViewer().setInput( RheiNavigator.this.map );
                            getCommonViewer().refresh();
                        }
                    });
                }
            };
            ProjectRepository.instance().addModelChangeListener( modelListener, PropertyEventFilter.ALL );
        }
    }


    protected Object getInitialInput() {
        Item root = new Item("root", null);
        Random rand = new Random();
        for(int i = 0; i < 5; ++i) {
            Item child = new Item("child" + i, root);
            for(int j = 0; j < rand.nextInt(10); ++j) {
                Item c = new Item("child" + i + j, child);
            }
        }
        return root;
    }

    
    /**
     * 
     */
    class PartListener
            extends DefaultPartListener {
        
        public void partActivated( IWorkbenchPart part ) {
            log.debug( "part= " + part );
            if (part instanceof IEditorPart) {
                IEditorPart editor = (IEditorPart)part;
                IMap newMap = (IMap)editor.getEditorInput().getAdapter( IMap.class );
                if (newMap != null) {
                    setInputMap( newMap );
                }
            }
        }

        public void partDeactivated( IWorkbenchPart part ) {
            log.debug( "part= " +part );
        }

    }
    
    
    /**
     * 
     */
    class Item {
        private String name;
        private HashSet<Item> items;
        private Item parent;
        
        public Item(String title, Item parent) {
            this.name = title;
            this.parent = parent;
            items = new HashSet<Item>();
            if(parent != null) parent.add(this);
        }

        private void add(Item item) {
            items.add(item);
        }
        
        public Item[] getChildren() { 
            return items.toArray(new Item[items.size()]);
        }

        public String getName() {
            return name;
        }

        public Item getParent() {
            return parent;
        }
    }

}

/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, All rights reserved.
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
package org.polymap.rhei.navigator;

import net.refractions.udig.internal.ui.IDropTargetProvider;
import net.refractions.udig.internal.ui.UDIGViewerDropAdapter;
import net.refractions.udig.ui.IDropAction;
import net.refractions.udig.ui.IDropHandlerListener;
import net.refractions.udig.ui.UDIGDragDropUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ViewSite;
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
 */
public class RheiNavigator
        extends CommonNavigator 
        implements IDropTargetProvider {

    private Log log = LogFactory.getLog( RheiNavigator.class );

    public static final String ID = "org.polymap.rhei.RheiNavigator";

    /** The map that is currently displayed. */
    private IMap                        map;
    
    private IWorkbenchPage              page;

    private PartListener                partListener;

    private ModelChangeListener         modelListener;

    private UDIGViewerDropAdapter       dropAdapter;


    public void createPartControl( Composite parent ) {
        super.createPartControl( parent );
        
        getCommonViewer().getTree().addListener( SWT.MouseUp, new Listener() {
            public void handleEvent( Event ev ) {
                log.info( "Mouse up, at: " + ev.x + ", " + ev.y );
            }
        });
        getCommonViewer().addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                log.info( "Double clicked: " + ev );
                String[] menuIds = ((ViewSite)getSite()).getContextMenuIds();
                log.info( "Context menus: " + Arrays.asList( menuIds ) );
            }
        });

        getSite().setSelectionProvider( getCommonViewer() );

        // DnD support
        UDIGDragDropUtilities.addDragSupport( getCommonViewer().getControl(), getCommonViewer() );
        dropAdapter = (UDIGViewerDropAdapter)UDIGDragDropUtilities.addDropSupport( getCommonViewer(), this, true, true );
        dropAdapter.getDropHandler().addListener( new IDropHandlerListener() {
            public void noAction( Object data ) {
                log.info( "DnD: no action ..." );
                getViewSite().getActionBars().getStatusLineManager().setMessage( "Kein passendes Ziel." );
                getViewSite().getActionBars().getStatusLineManager().setErrorMessage( "Kein passendes Ziel." );
            }
            public void starting( IDropAction action ) {
            }
            public void done( IDropAction action, Throwable error ) {
            }
        });

//        // selection listener
//        getSite().getPage().addSelectionListener( new ISelectionListener() {
//            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
//                if (!sel.isEmpty() && sel instanceof StructuredSelection) {
//                    Object elm = ((StructuredSelection)sel).getFirstElement();
//                    log.debug( "page selection: elm= " + elm );
//                    if (elm instanceof IMap) {
//                        setInputMap( (IMap)elm );
//                    }
//                }
//            }
//        });
        
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


    public Object getTarget( DropTargetEvent ev ) {
        log.info( "DnD: ev= " + ev );
        return this;
    }


//    protected Object getInitialInput() {
//        Item root = new Item("root", null);
//        Random rand = new Random();
//        for(int i = 0; i < 5; ++i) {
//            Item child = new Item("child" + i, root);
//            for(int j = 0; j < rand.nextInt(10); ++j) {
//                Item c = new Item("child" + i + j, child);
//            }
//        }
//        return root;
//    }

    
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
    
    
//    /**
//     * 
//     */
//    class Item {
//        private String name;
//        private HashSet<Item> items;
//        private Item parent;
//        
//        public Item(String title, Item parent) {
//            this.name = title;
//            this.parent = parent;
//            items = new HashSet<Item>();
//            if(parent != null) parent.add(this);
//        }
//
//        private void add(Item item) {
//            items.add(item);
//        }
//        
//        public Item[] getChildren() { 
//            return items.toArray(new Item[items.size()]);
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public Item getParent() {
//            return parent;
//        }
//    }

}

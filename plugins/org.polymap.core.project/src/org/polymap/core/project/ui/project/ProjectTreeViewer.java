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
package org.polymap.core.project.ui.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.PlatformUI;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectPlugin;
import org.polymap.core.project.ui.EntityContentProvider;
import org.polymap.core.project.ui.LabeledLabelProvider;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class ProjectTreeViewer
        extends TreeViewer {

    private static Log log = LogFactory.getLog( ProjectTreeViewer.class );
    
    
    public ProjectTreeViewer( Composite parent, int style ) {
        super( parent, style );
        setData( WidgetUtil.CUSTOM_WIDGET_ID, "projectViewer" );

        setContentProvider( new ProjectContentProvider() );
        
        ILabelProvider lp = new ProjectLabelProvider();
        ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
        setLabelProvider( new DecoratingLabelProvider( lp, decorator ) );
        
        setSorter( new NameSorter() );
    }

    
    public void setRootMap( IMap map ) {
        assert map != null;
        setInput( map );        
    }

    
    /**
     * 
     */
    class ProjectContentProvider
            extends EntityContentProvider {

        protected Collection _getChildren( Object parent ) {
            log.debug( "parent: " + parent );
            if (parent instanceof IMap) {
                IMap map = (IMap)parent;
                List result = new ArrayList();
                result.addAll( map.getMaps() );
                result.addAll( map.getLayers() );
                return result;
            }
//            else if (parent instanceof ILayer) {
//                return ((ILayer)parent).getLayers();
//            }
            else {
                log.warn( "unhandled parent type: " + parent );
                return Collections.EMPTY_LIST;
            }
        }

    }
    
    
    /**
     * 
     */
    class ProjectLabelProvider
            extends LabeledLabelProvider {

        public Image getImage( Object elm ) {
            if (elm instanceof IMap) {
                return ProjectPlugin.getDefault().imageForName( "icons/obj16/map_obj.gif" );
            }
            else if (elm instanceof ILayer) {
                return ProjectPlugin.getDefault().imageForName( "icons/obj16/layer_obj.gif" );
            }
            return null;
        }

    }
    
    
    /**
     * 
     */
    class NameSorter
            extends ViewerSorter {

        public int category( Object elm ) {
            if (elm instanceof IMap) {
                return 0;
            }
            else if (elm instanceof ILayer) {
                return 1;
            }
            else {
                return 10;
            }
        }
        
    }



}

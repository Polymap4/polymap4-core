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
package org.polymap.rhei.internal.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.filter.IFilter;

/**
 * 
 * @see FilterLabelProvider
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version ($Revision$)
 */
public class FilterContentProvider
        implements ITreeContentProvider {


    public Object[] getChildren( Object elm ) {
        // folder
        if (elm instanceof ILayer) {
            return new Object[] { new FiltersFolderItem( (ILayer)elm ) };
        }
        // filters
        else if (elm instanceof FiltersFolderItem) {
            FiltersFolderItem folder = (FiltersFolderItem)elm;
//            FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
//            Filter filter = ff.propertyGreaterThan( ff.property( "POPULATION"), ff.literal( 12 ) );

            // extensions
            List<IFilter> result = new ArrayList();
            for (FilterProviderExtension ext : FilterProviderExtension.allExtensions()) {
                try {
                    List<IFilter> filters = ext.newFilterProvider().addFilters( folder.getLayer() );
                    if (filters != null) {
                        result.addAll( filters );
                    }
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );                }
            }
            
//            TransientFilter allFilter = new TransientFilter( 
//                    "__allFilter__", folder.getLayer(),
//                    "Alle", null, Filter.INCLUDE, Long.MAX_VALUE );
//            
//            TransientFilter myFilter = new TransientFilter( 
//                    "__myFilter__", folder.getLayer(),
//                    "Meine offenen...", null, Filter.INCLUDE, Long.MAX_VALUE );
//            
//            TransientFilter _2010Filter = new TransientFilter( 
//                    "__2010Filter__", folder.getLayer(),
//                    "2010...", null, Filter.INCLUDE, Long.MAX_VALUE );
//            
//            TransientFilter _2009Filter = new TransientFilter( 
//                    "__2009Filter__", folder.getLayer(),
//                    "2009...", null, Filter.INCLUDE, Long.MAX_VALUE );
            
            return result.toArray();            
        }
        return null;
    }


    public Object getParent( Object elm ) {
        if (elm instanceof ILayer) {
            return ((ILayer)elm).getMap();
        }
        return null;
    }


    public boolean hasChildren(Object element) {
        return getChildren(element) != null;
    }

    
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    
    public void dispose() {
    }

    
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }
    
}

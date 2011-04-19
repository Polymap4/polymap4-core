/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
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
package org.polymap.rhei.filter;

import java.util.Set;

import org.opengis.filter.Filter;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.project.ILayer;

/**
 * Simple filter without UI. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class TransientFilter
        implements IFilter {

    private String                  id;

    private ILayer                  layer;
    
    private String                  label;
    
    private Set<String>             keywords;
    
    private Filter                  filter;
    
    private long                    maxResults;

    
    public TransientFilter( String id, ILayer layer, String label, Set<String> keywords, Filter filter,
            long maxResults ) {
        this.id = id;
        this.layer = layer;
        this.label = label;
        this.keywords = keywords;
        this.filter = filter;
        this.maxResults = maxResults;
    }

    public String getId() {
        return id;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public String getLabel() {
        return label;
    }

    public Filter createFilter( IFilterEditorSite site ) {
        return filter;
    }

    public boolean hasControl() {
        return false;
    }

    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        throw new RuntimeException( "not implemented." );
    }

    public long getMaxResults() {
        return maxResults;
    }

    public ILayer getLayer() {
        return layer;
    }

}

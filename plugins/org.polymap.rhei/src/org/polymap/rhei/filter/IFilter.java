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
 * Provides the logic to create a concrete {@link Filter} instance for a given
 * layer (feature type).
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFilter {

    public String getId();

    public String getLabel();

    public Set<String> getKeywords();

    /**
     * Returns the layer this filter is associated with.
     */
    public ILayer getLayer();


    /**
     * Returns true if this filter need input from the user to create the
     * concrete filter, that is this filter needs a control to be displayed,
     * which is returned by {@link #createControl(Composite)}.
     */
    public boolean hasControl();

    /**
     * Creates UI elements in order to get the parameters of the filter
     * from the user.
     */
    public Composite createControl( Composite parent, IFilterEditorSite site );

    /**
     * Create a new {@link Filter} instance.
     *
     * @return Newly created filter instance, or null if the was an error.
     */
    public Filter createFilter( IFilterEditorSite site );

    public int getMaxResults();

}

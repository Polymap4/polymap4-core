/*
 * polymap.org 
 * Copyright (C) 2016-2018, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.ui;

import java.io.IOException;

import org.opengis.filter.Filter;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.ConstantFilter;

/**
 * 
 *
 * @author Steffen Stundzig
 */
public class AlwaysTrueEditor
        extends StylePropertyEditor<ConstantFilter> {

    private static final IMessages i18n = Messages.forPrefix( "AlwaysTrueEditor" );

    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return super.init( site ) && Filter.class.isAssignableFrom( targetType( site ) );
    }


    @Override
    public void updateProperty() {
        prop.createValue( ConstantFilter.defaults( true ) );
    }


    @Override
    public Composite createContents( Composite parent ) {
        try {
            prop.get().setFilter( Filter.INCLUDE );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        return parent;
    }
    
}

/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
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

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.NoValue;

/**
 * Editor that simply sets null values, if necessary.
 *
 * @author Steffen Stundzig
 */
public class NoValueEditor
        extends StylePropertyEditor<NoValue> {

    private static final IMessages i18n = Messages.forPrefix( "NoValue" );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( final StylePropertyFieldSite site ) {
        return site.prop.get().info().isNullable() ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( NoValue.defaults() );
    }


    @Override
    public Composite createContents( Composite parent ) {
        prop.createValue( NoValue.defaults() );
        prop.get().noValue.set( System.currentTimeMillis() );
        return parent;
    }
    
    @Override
    public boolean canHandleCurrentValue() {
        return prop.get() == null || prop.get() instanceof NoValue;
    }
}

/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.ui.feature;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.AttributeValue;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.ui.UIUtils;

/**
 * Editor for {@link AttributeValue}s.
 *
 * @author Falko Bräutigam
 */
public class AttributeValueEditor<T>
        extends StylePropertyEditor<AttributeValue<T>> {

    private static final IMessages i18n = Messages.forPrefix( "AttributeValueEditor" );

    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return super.init( site ) && site.featureType.isPresent() && !matchingAttributeNames().isEmpty(); 
    }


    @Override
    public void updateProperty() {
        prop.createValue( AttributeValue.defaults( "", null, null ) );
    }

    
    protected List<String> matchingAttributeNames() {
        return featureType().getDescriptors().stream()
                .filter( pd -> isCompatible( pd.getType().getBinding() ) )
                .map( pd -> pd.getName().getLocalPart() )
                .collect( Collectors.toList() );
    }

    
    protected boolean isCompatible( Class type ) {
        return site().targetType.get().isAssignableFrom( type )
                || (Number.class.isAssignableFrom( site().targetType.get() ) && Number.class.isAssignableFrom( type ));
    }
    
    
    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        List<String> attributes = matchingAttributeNames();
        combo.setItems( attributes.toArray( new String[attributes.size()] ) );
        
        combo.select( attributes.indexOf( prop.get().attributeName.get() ) );
        combo.addSelectionListener( UIUtils.selectionListener( ev -> {
            prop.get().attributeName.set( attributes.get( combo.getSelectionIndex() ) );
        }));
        return contents;
    }


    @Override
    public boolean isValid() {
        return !StringUtils.isBlank( prop.get().attributeName.get() );
    }
    
}

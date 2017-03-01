/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.data.process.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class NotSupportedSupplier
        extends InputFieldSupplier {

    @Override
    public String label() {
        return "Not supported";
    }

    @Override
    public void createContents( Composite parent ) {
        Label l = new Label( parent, SWT.NONE );
        l.setText( "Type: " + site.fieldInfo.get().type.get().getSimpleName() );
    }

}

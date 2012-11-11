/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.eclipse.jface.preference.ColorSelector;

/**
 * Factory of SWT controls adapted to work in
 * {@link IEditorTool#createPanelControl(org.eclipse.swt.widgets.Composite) tool
 * panels}. In addition to changing their presentation properties (fonts, colors
 * etc.), various listeners are attached to make them behave correctly in the form
 * context.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IToolingToolkit {

    public void dispose();

    public Label createLabel( Composite parent, String text, int... styles );
    
    public CCombo createCombo( Composite parent, Iterable<String> values, int... styles );

    public CCombo createCombo( Composite parent, String[] values, int... styles );

    public Button createButton( Composite parent, String text, int... styles );

    public Spinner createSpinner( Composite parent, int... styles );

    public ColorSelector createColorSelector( Composite parent );

}

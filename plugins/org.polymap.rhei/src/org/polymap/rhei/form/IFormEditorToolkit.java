/*
 * polymap.org Copyright 2010, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * $Id: $
 */

package org.polymap.rhei.form;

import java.util.Date;
import java.util.Set;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;

import org.polymap.rhei.filter.FilterEditor;

/**
 * Provides a general factory facade for the creation of UI element. Concrete
 * implementations are provided for {@link FormEditor}, {@link FilterEditor}
 * and maybe other usecases.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public interface IFormEditorToolkit {

    public abstract Button createButton( Composite parent, String text, int style );

    public abstract Composite createComposite( Composite parent, int style );

    public abstract Composite createComposite( Composite parent );

    public abstract Composite createCompositeSeparator( Composite parent );

    public abstract ExpandableComposite createExpandableComposite( Composite parent,
            int expansionStyle );

    public abstract Form createForm( Composite parent );

    public abstract FormText createFormText( Composite parent, boolean trackFocus );

    public abstract Hyperlink createHyperlink( Composite parent, String text, int style );

    public abstract ImageHyperlink createImageHyperlink( Composite parent, int style );

    public abstract Label createLabel( Composite parent, String text, int style );

    public abstract Label createLabel( Composite parent, String text );

    public abstract ScrolledPageBook createPageBook( Composite parent, int style );

    public abstract ScrolledForm createScrolledForm( Composite parent );

    public abstract Section createSection( Composite parent, int sectionStyle );

    public abstract Label createSeparator( Composite parent, int style );

    public abstract Table createTable( Composite parent, int style );

    public abstract Text createText( Composite parent, String value, int style );

    public abstract Text createText( Composite parent, String value );

    public abstract Tree createTree( Composite parent, int style );

    public abstract Combo createCombo( Composite parent, Set<String> values );
    
    public abstract Combo createCombo( Composite parent, Set<String> values, int style );
    
    public abstract DateTime createDateTime( Composite parent, Date value );
    
    public abstract DateTime createDateTime( Composite parent, Date value, int style );
    
}
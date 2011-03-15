/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */
package org.polymap.core.data.ui.featureTypeEditor;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.fieldassist.ControlDecoration;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * A text field that can be used together with an {@link FeatureTypeEditor} to
 * modify the name of the FeatureType of the editor. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FeatureTypeNameText
        extends Text {
    
    private FeatureTypeEditor       fte;
    

    public FeatureTypeNameText( Composite parent, FeatureTypeEditor fte, Object layoutData ) {
        super( parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        this.fte = fte;

        fte.errorDecorator = new ControlDecoration( this, SWT.TOP | SWT.LEFT );
        Image image = PlatformUI.getWorkbench().getSharedImages().getImage(
                ISharedImages.IMG_DEC_FIELD_ERROR );
        fte.errorDecorator.setImage( image );
        
        if (fte.viewer != null) {
            SimpleFeatureType input = ((SimpleFeatureType)fte.viewer.getInput());
            if (input != null) {
                setText( input.getTypeName() );
            }
        }
        if (layoutData != null) {
            setLayoutData( layoutData );
        }
        else {
            setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
        }

        setFocus();
        NameListener listener = new NameListener();
        addKeyListener(listener);
        addFocusListener(listener);
    }


    /**
     * 
     */
    class NameListener 
            implements KeyListener, FocusListener {

        public void keyPressed( KeyEvent e ) {
            SimpleFeatureType ft = (SimpleFeatureType)fte.viewer.getInput();
            if (e.character == SWT.ESC) {
                setText( ft.getTypeName() );
            }
            else if (e.character == SWT.Selection) {
                SimpleFeatureTypeBuilder ftB = new SimpleFeatureTypeBuilder();
                ftB.init( ft );
                ftB.setName( getText() );
                SimpleFeatureType featureType = ftB.buildFeatureType();
                fte.viewer.setInput( featureType );
            }
            else {
                fte.errorDecorator.hide();
            }
        }

        public void keyReleased( KeyEvent e ) {
            SimpleFeatureType ft = (SimpleFeatureType)fte.viewer.getInput();
            SimpleFeatureTypeBuilder ftB = new SimpleFeatureTypeBuilder();
            ftB.init(ft);
            ftB.setName( getText() );
            SimpleFeatureType featureType = ftB.buildFeatureType();
            fte.viewer.setInput( featureType );
        }

        public void focusGained( FocusEvent e ) {
            int end = getText().length();
            setSelection(0, end);
        }

        public void focusLost( FocusEvent e ) {
            SimpleFeatureType ft = (SimpleFeatureType)fte.viewer.getInput();
            SimpleFeatureTypeBuilder ftB = new SimpleFeatureTypeBuilder();
            ftB.init(ft);
            ftB.setName( getText() );
            SimpleFeatureType featureType = ftB.buildFeatureType();
            fte.viewer.setInput( featureType );
        }

    }

}

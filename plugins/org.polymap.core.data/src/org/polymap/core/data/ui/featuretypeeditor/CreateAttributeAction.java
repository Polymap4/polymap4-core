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
package org.polymap.core.data.ui.featuretypeeditor;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.Action;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class CreateAttributeAction
        extends Action {

    private FeatureTypeEditor       fte;
    
    
    public CreateAttributeAction( FeatureTypeEditor fte ) {
        super();
        this.fte = fte;
        
        setId( "org.polymap.core.data.ui.featuretypeeditor.createAttributeAction" ); //$NON-NLS-1$
        setText( Messages.get( "CreateAttributeAction_label" ) );
        setToolTipText( Messages.get( "CreateAttributeAction_tip" ) );
        setImageDescriptor( DataPlugin.getDefault().imageDescriptor( "icons/etool16/add.gif" ) ); //$NON-NLS-1$
    }

    public void runWithEvent( Event event ) {
        SimpleFeatureType ft = (SimpleFeatureType)fte.viewer.getInput();
        SimpleFeatureTypeBuilder ftB = fte.builderFromFeatureType( ft );
        int index = 0;
        while (true) {
            try {
                ftB.add( Messages.get( "CreateAttributeAction_defaultAttrName" ) + index, String.class );
                break;
            }
            catch (IllegalArgumentException e) {
                index++;
            }
        }
        SimpleFeatureType featureType = ftB.buildFeatureType();
        fte.viewer.setInput( featureType );
        // TODO check if it is better to do something and then: viewer.refresh(false);
    }

}

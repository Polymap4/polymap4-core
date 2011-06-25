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

import java.util.Iterator;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import net.refractions.udig.internal.ui.Images;

import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.polymap.core.data.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class DeleteAttributeAction
        extends Action {

    private FeatureTypeEditor       fte;
    
    
    public DeleteAttributeAction( FeatureTypeEditor fte ) {
        super();
        this.fte = fte;
        
        setId( "org.polymap.core.data.ui.featuretypeeditor.deleteAttributeAction" ); //$NON-NLS-1$
        setText( Messages.get( "DeleteAttributeAction_label" ) );
        setToolTipText( Messages.get( "DeleteAttributeAction_tip" ) );
        setImageDescriptor( Images.getDescriptor( "elcl16/delete.gif" ) ); //$NON-NLS-1$
    }

    public void runWithEvent( Event event ) {
        SimpleFeatureType ft = (SimpleFeatureType)fte.viewer.getInput();
        SimpleFeatureTypeBuilder ftB = fte.builderFromFeatureType( ft );
        IStructuredSelection selection = (IStructuredSelection)fte.viewer.getSelection();
        for (Iterator<AttributeDescriptor> iter = selection.iterator(); iter.hasNext();) {
            AttributeDescriptor element = iter.next();
            ftB.remove( element.getLocalName() );
        }
        SimpleFeatureType featureType = ftB.buildFeatureType();
        fte.viewer.setInput( featureType );
    }

}

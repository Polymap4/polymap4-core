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

package org.polymap.core.project.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.polymap.core.model.MObject;
import org.polymap.core.project.Labeled;

/**
 * A {@link LabelProvider} for {@link MObject} models. The 'label' and 'name'
 * features are used as labels.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class LabeledLabelProvider
        extends BaseLabelProvider
        implements ILabelProvider {

    private static Log log = LogFactory.getLog( LabeledLabelProvider.class );
    
    
    public Image getImage( Object elm ) {
        return null;
    }


    public String getText( Object elm ) {
        if (elm instanceof Labeled) {
            Labeled obj = (Labeled)elm;
            return obj.getLabel();
        }
        else {
            log.warn( "Element is not instanceof Labeled: " + elm );
            return elm.toString();
        }
    }


    public void dispose() {
        super.dispose();
    }

}

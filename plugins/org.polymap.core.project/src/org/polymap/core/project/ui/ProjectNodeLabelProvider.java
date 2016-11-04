/* 
 * polymap.org
 * Copyright 2009-2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.project.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.project.ProjectNode;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.DefaultInt;
import org.polymap.core.runtime.config.Mandatory;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectNodeLabelProvider
        extends CellLabelProvider
        implements ILabelProvider {

    private static Log log = LogFactory.getLog( ProjectNodeLabelProvider.class );
    
    public enum PropType {
        Label, Description
    }
    
    @Mandatory
    public Config2<ProjectNodeLabelProvider,PropType>   propType;
    
    @Mandatory
    @DefaultInt( -1 )
    public Config2<ProjectNodeLabelProvider,Integer>    abbreviate;
    
    
    public ProjectNodeLabelProvider( PropType propType ) {
        ConfigurationFactory.inject( this );
        this.propType.set( propType );
    }


    @Override
    public void update( ViewerCell cell ) {
        Object elm = cell.getElement();
        cell.setText( getText( elm ) );
    }


    @Override
    public String getText( Object elm ) {
        if (elm instanceof ProjectNode) {
            if (propType.get() == PropType.Label) {
                String result = ((ProjectNode)elm).label.get();
                return abbreviate( result );
            }
            else if (propType.get() == PropType.Description) {
                String result = ((ProjectNode)elm).description.opt().orElse( "..." );
                return abbreviate( result );
            }
            else {
                throw new RuntimeException( "Unknown: " + propType );
            }
        }
        else {
            log.warn( "Element is not instanceof Labeled: " + elm );
            return elm.toString();
        }
    }

    protected String abbreviate( String s ) {
        return abbreviate.get() > 0 ? StringUtils.abbreviate( s, abbreviate.get() ) : s;
    }

    @Override
    public Image getImage( Object elm ) {
        return null;
    }

}

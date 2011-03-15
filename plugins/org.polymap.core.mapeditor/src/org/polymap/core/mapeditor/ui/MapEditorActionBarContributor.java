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

package org.polymap.core.mapeditor.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.EditorActionBarContributor;

import org.polymap.core.mapeditor.MapEditor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MapEditorActionBarContributor
        extends EditorActionBarContributor {

    private static Log log = LogFactory.getLog( MapEditorActionBarContributor.class );

    private MapEditor               editor;
    

    public MapEditorActionBarContributor() {
    }


    public void setActiveEditor( IEditorPart targetEditor ) {
        log.debug( "editor= " + targetEditor );
        if (targetEditor instanceof MapEditor) {
            this.editor = (MapEditor)targetEditor;
        }
    }
    

//    public void contributeToCoolBar( ICoolBarManager coolBarManager ) {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }


    public void contributeToToolBar( IToolBarManager manager ) {
        manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );

//        manager.add( new MouseModeAction() );
    }


}

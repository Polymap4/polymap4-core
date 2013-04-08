/*
 * polymap.org
 * Copyright 2009-2013, Polymap GmbH. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;

import org.polymap.core.project.ui.layer.LayerNavigator;
import org.polymap.core.project.ui.project.ProjectView;
import org.polymap.core.runtime.Polymap;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 *         <li>24.06.2009: created</li>
 * @version $Revision: $
 */
public class ProjectPerspective
        implements IPerspectiveFactory {

    private Log log = LogFactory.getLog( ProjectPerspective.class );


    public void createInitialLayout( final IPageLayout layout ) {
        log.debug( "..." );
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible( true );

        IFolderLayout topLeft = layout.createFolder(
                "topLeft", IPageLayout.LEFT, 0.23f, editorArea );
        IFolderLayout bottomLeft = layout.createFolder( "bottomLeft", IPageLayout.BOTTOM, 0.25f, "topLeft" );
        IFolderLayout topRight = layout.createFolder( "topRight", IPageLayout.RIGHT, 0.70f, editorArea );
        //IFolderLayout bottomRight = layout.createFolder( "bottomRight", IPageLayout.BOTTOM, 0.50f, "topRight" );
        IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder( "bottom", IPageLayout.BOTTOM, 0.70f, editorArea );

        topLeft.addView( ProjectView.ID );

        bottomLeft.addView( LayerNavigator.ID );
        bottomLeft.addPlaceholder( "org.polymap.rhei.filter.FilterView:*" );

        topRight.addView( "net.refractions.udig.catalog.ui.CatalogView" );
//        topRight.addView( "org.polymap.core.mapeditor.ToolingView" );
        topRight.addPlaceholder( "org.polymap.geocoder.*" );

        topRight.addView( "org.eclipse.ui.cheatsheets.views.CheatSheetView" );
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                new OpenCheatSheetAction( "org.polymap.core.cheatsheet.welcome" ).run();
            }
        });

        bottom.addPlaceholder( "org.polymap.core.data.FeatureSelectionView:*" );
        bottom.addPlaceholder( "org.polymap.*" );        
        bottom.addPlaceholder( "org.polymap.*:*" );
        bottom.addPlaceholder( "org.eclipse.*" );


        // add shortcuts to show view menu
        layout.addShowViewShortcut( "net.refractions.udig.catalog.ui.CatalogView" );
    }
    
}

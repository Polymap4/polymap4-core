/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.catalog.ui;

import java.util.List;

import net.refractions.udig.catalog.ui.ConnectionFactoryManager;
import net.refractions.udig.catalog.ui.UDIGConnectionFactoryDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.catalog.CatalogPlugin;
import org.polymap.core.catalog.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CatalogImportMenu
        extends ContributionItem {

    private static Log log = LogFactory.getLog( CatalogImportMenu.class );


    @Override
    public void fill( final ToolBar parent, int index ) {
        final ToolItem item = new ToolItem( parent, SWT.DROP_DOWN, index );
        Image icon = CatalogPlugin.getDefault().imageForName( "icons/etool16/add.gif" );
        item.setImage( icon );
        item.setToolTipText( i18n( "tip" ) );
        
        item.addSelectionListener( new SelectionListener() {

            public void widgetSelected( SelectionEvent ev ) {
                widgetDefaultSelected( ev );
            }

            public void widgetDefaultSelected( final SelectionEvent ev ) {
                if (ev.detail == SWT.ARROW) {
                    Menu menu = new Menu( parent );
                    menu.setLocation( parent.toDisplay( ev.x, ev.y ) );
                    menu.setVisible( true );
                    
                    fillSubMenu( menu );
                }
                else {
                    new CatalogImport().open();                    
                }
            }
        });
    }

    
    protected void fillSubMenu( Menu menu ) {
        List<UDIGConnectionFactoryDescriptor> descriptors = ConnectionFactoryManager.instance().getConnectionFactoryDescriptors();

        for (final UDIGConnectionFactoryDescriptor descriptor : descriptors) {
            MenuItem menuItem = new MenuItem( menu, SWT.PUSH );
            menuItem.setText( descriptor.getLabel( 0 ) );

            // icon
            ImageDescriptor image = descriptor.getImage( 0 );
            if (image != null) {
                menuItem.setImage( CatalogPlugin.getDefault().imageForDescriptor( image, descriptor.getId() ) );
            }

            // listener
            menuItem.addSelectionListener( new SelectionListener() {
                
                public void widgetSelected( SelectionEvent ev ) {
                    widgetDefaultSelected( ev );
                }
                
                public void widgetDefaultSelected( SelectionEvent ev ) {
                    log.info( "SelectionEvent: " + ev );
                    new CatalogImport( descriptor ).open();
                }
            });
        }
    }
    
    
    protected String i18n( String key, Object... args ) {
        return Messages.get( "CatalogImportMenu_" + key, args );
    }
    
}

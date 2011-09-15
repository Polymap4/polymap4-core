/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.operation;

import java.util.concurrent.atomic.AtomicReference;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;

import net.refractions.udig.catalog.IGeoResource;
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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.operation.FeatureOperationFactory.IContextProvider;
import org.polymap.core.data.ui.featureselection.FeatureSelectionView;

/**
 * Contributes feature operations to the {@link IGeoResource} context menu.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelectionViewMenuContribution
        extends ContributionItem
        implements IContextProvider {

    private static Log log = LogFactory.getLog( FeatureSelectionViewMenuContribution.class );


    public void fill( final ToolBar parent, int index ) {
        ToolItem item = new ToolItem( parent, SWT.DROP_DOWN, index);
        Image icon = DataPlugin.getDefault().imageForName( "icons/etool16/feature_ops.gif" );
        item.setImage( icon );
        
        item.addSelectionListener( new SelectionListener() {

            public void widgetSelected( SelectionEvent ev ) {
                widgetDefaultSelected( ev );
            }

            public void widgetDefaultSelected( final SelectionEvent e ) {
                if (e.detail == SWT.ARROW) {
                    Menu menu = new Menu( parent );

                    FeatureOperationFactory factory = FeatureOperationFactory.forContext( 
                            FeatureSelectionViewMenuContribution.this );
                    
                    for (final Action action : factory.actions()) {
                        MenuItem menuItem = new MenuItem( menu, SWT.PUSH );
                        menuItem.setText( action.getText() );
                        
                        // icon
                        ImageDescriptor descriptor = action.getImageDescriptor();
                        if (descriptor != null) {
                            Image icon2 = DataPlugin.getDefault().imageForDescriptor( 
                                    descriptor, action.getText() + "_icon" );
                            menuItem.setImage( icon2 );
                        }

                        menuItem.addSelectionListener( new SelectionListener() {

                            public void widgetSelected( SelectionEvent se ) {
                                widgetDefaultSelected( se );
                            }

                            public void widgetDefaultSelected( SelectionEvent se ) {
                                action.run();
                            }

                        });
                    }
                    menu.setVisible( true );
                } 
//                else {
//                    if (current != null)
//                        current.runAction();
//                }
            }

        });

    }


    public DefaultOperationContext newContext() {
        try {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IWorkbenchPart part = page.getActivePart();
            
            final AtomicReference<FeatureSelectionView> view = new AtomicReference();
            if (part instanceof FeatureSelectionView) {
                view.set( (FeatureSelectionView)part );
            }
            else {
                for (IViewReference ref : page.getViewReferences()) {
                    log.info( "VIEW: " + ref.getId() + ", " + ref.getSecondaryId() );
                }
                return null;
            }

            final PipelineFeatureSource fs = view.get().getFeatureStore();

            // The context
            return new DefaultOperationContext() {

                private FeatureCollection   fc;

                /**
                 * Lazily init.
                 */
                private void checkInit() 
                throws Exception {
                    if (fc == null) {
                        DefaultQuery query = new DefaultQuery( fs.getSchema().getName().getLocalPart(),
                                view.get().getFilter() );
                        fc = fs.getFeatures( query );
                    }

                }

                public FeatureCollection features() 
                throws Exception {
                    checkInit();
                    return fc;
                }

                public FeatureSource featureSource()
                throws Exception {
                    return fs;
                }

            };
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        return null;
    }
    
}

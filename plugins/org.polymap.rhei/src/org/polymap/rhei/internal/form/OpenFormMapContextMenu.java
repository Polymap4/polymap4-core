/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.internal.form;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.mapeditor.contextmenu.ContextMenuSite;
import org.polymap.core.mapeditor.contextmenu.IContextMenuContribution;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiPlugin;
import org.polymap.rhei.form.FormEditor;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OpenFormMapContextMenu
        extends ContributionItem
        implements IContextMenuContribution {

    private static Log log = LogFactory.getLog( OpenFormMapContextMenu.class );
    
    private ContextMenuSite         site;


    public IContextMenuContribution init( ContextMenuSite _site ) {
        this.site = _site;
        setVisible( false );
        for (ILayer layer : site.getMap().getLayers()) {
            if (layer.isVisible()) {
                int size = site.coveredFeatures( layer ).size();
                if (size > 0 && size < 5) {
                    setVisible( true );
                    break;
                }
            }
        }
        return this;
    }


    public String getMenuGroup() {
        return GROUP_HIGH;
    }


    public void fill( final Menu parent, final int index ) {
        for (final ILayer layer : site.getMap().getLayers()) {
            
            if (layer.isVisible()) {
            
                FeatureCollection features = site.coveredFeatures( layer );
                try {
                    final PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, true );

                    features.accepts( new FeatureVisitor() {
                        public void visit( final Feature feature ) {
                            String label = Messages.get( "OpenFormMapContextMenu_label", 
                                    StringUtils.abbreviate( feature.getIdentifier().getID(), 20, 25 ), 
                                    layer.getLabel() );
                            
                            Action action = new Action( label ) {
                                public void run() {
                                    FormEditor.open( fs, feature, layer );
                                }            
                            };
                            action.setImageDescriptor( RheiPlugin.imageDescriptorFromPlugin(
                                    RheiPlugin.PLUGIN_ID, "icons/etool16/open_form_editor.gif" ) );
                            new ActionContributionItem( action ).fill( parent, index );
                        }
                    }, null );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiPlugin.PLUGIN_ID, this, "", e );
                }
            }
        }
    }

}

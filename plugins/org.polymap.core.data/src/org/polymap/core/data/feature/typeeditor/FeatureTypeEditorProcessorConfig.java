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
package org.polymap.core.data.feature.typeeditor;

import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.MultiPolygon;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.internal.ui.UiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.dialogs.PropertyPage;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.pipeline.ProcessorExtension.ProcessorPropertyPage;
import org.polymap.core.data.ui.featuretypeeditor.CreateAttributeAction;
import org.polymap.core.data.ui.featuretypeeditor.DeleteAttributeAction;
import org.polymap.core.data.ui.featuretypeeditor.FeatureTypeEditor;
import org.polymap.core.data.ui.featuretypeeditor.ValueViewerColumn;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.PipelineHolder;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * The configuration UI for {@link FeatureTypeEditorProcessor}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class FeatureTypeEditorProcessorConfig
        extends PropertyPage
        implements ProcessorPropertyPage {

    private static final Log log = LogFactory.getLog( FeatureTypeEditorProcessorConfig.class );

    private PipelineHolder          holder;

    /** Resolved from {@link #holder} if its a {@link ILayer} or {@link #setSourceFeatureType(SimpleFeatureType)}. */
    private SimpleFeatureType       sourceFeatureType;

    private Properties              props;

    private FeatureTypeEditor       fte;

    private FeatureTypeMapping      mappings;

    /** Read from the props. */
    private SimpleFeatureType       featureType;


    public void init( PipelineHolder _holder, Properties _props ) {
        this.holder = _holder;
        this.props = _props;

        // get mappings
        String serialized = props.getProperty( "mappings", null );
        if (serialized != null) {
            mappings = new FeatureTypeMapping( serialized );
        }
        // default mappings
        else {
            try {
                log.debug( "Initializing default mappings:" );
                mappings = new FeatureTypeMapping();
                AttributeMapping mapping = new AttributeMapping( "name1", String.class, null, "ID", "_alles_wieder_gleich_" );
                mappings.put( mapping );
                log.debug( "    mapping: " + mapping );
                mapping = new AttributeMapping( "the_geom", MultiPolygon.class, CRS.decode( "EPSG:31468" ), null, null );
                mappings.put( mapping );
                log.debug( "    mapping: " + mapping );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
        featureType = mappings.newFeatureType();
    }


    public void dispose() {
        super.dispose();
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().dispose();
            mappings = null;
            featureType = null;
        }
    }


    public void setSourceFeatureType( SimpleFeatureType sourceFeatureType ) {
        this.sourceFeatureType = sourceFeatureType;
    }


    public boolean performOk() {
        // check the mappings; the FeateTypeEditor works directly on the FeatureType
        // and has no idea about our mappings, so we need to synchronize
        featureType = fte.getFeatureType();
        for (Iterator<AttributeMapping> it=mappings.iterator(); it.hasNext(); ) {
            AttributeMapping mapping = it.next();
            AttributeDescriptor attr = featureType.getDescriptor( mapping.name );
            //
            if (attr == null) {
                log.debug( "Mapping is not in the result FeatureType: " + mapping.name );
                it.remove();
                continue;
            }
            //
            mapping.binding = attr.getType().getBinding();
            if (attr instanceof GeometryDescriptor) {
                mapping.crs = ((GeometryDescriptor)attr).getCoordinateReferenceSystem();
            }
        }

        String serialized = mappings.serialize();
        log.debug( ":: " + serialized );

        props.put( "mappings", serialized );
//        props.put( "featureTypeName", nameText.getText() );

        return true;
    }


    protected Control createContents( Composite parent ) {
        noDefaultAndApplyButton();

        Composite contents = new Composite( parent, SWT.BORDER );

        FormData dTable = new FormData();

        if (sourceFeatureType == null) {
            // FIXME directly accessing type of ds bypassing upstream procs
            if (holder instanceof ILayer) {
                try {
                    IGeoResource geores = ((ILayer)holder).getGeoResource();
                    if (geores == null) {
                        throw new IllegalStateException( "No GeoResource for layer: " + ((ILayer)holder).getLabel() );
                    }
                    FeatureSource fs = geores.resolve( FeatureSource.class, null );
                    // EntitieProviders do not support FeatureSource
                    // #71: http://polymap.org/biotop/ticket/71
                    sourceFeatureType = fs != null
                            ? (SimpleFeatureType)fs.getSchema()
                            : geores.resolve( SimpleFeatureType.class, null );
                    if (sourceFeatureType == null) {
                        throw new IllegalStateException( "No FeatureType for layer: " + ((ILayer)holder).getLabel() );                        
                    }
                    log.debug( "        DataSource schema: " + sourceFeatureType );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                    sourceFeatureType = null;
                    log.warn( "", e );
                }

                if (sourceFeatureType == null) {
                    Text msg = new Text( contents, SWT.NONE );
                    msg.setText( "Error while creating the processor config panel." );
                    return contents;
                }
            }
            else {
                new RuntimeException( "Unhandled pipeline holder type: " + holder );
            }
        }

        // fte
        fte = new FeatureTypeEditor();
        fte.addViewerColumn( new ValueViewerColumn( mappings, sourceFeatureType ) );

        fte.createTable( contents, dTable, featureType, true );

        // buttons
        Button createBtn = createButton( contents, new CreateAttributeAction( fte ) );
        Button deleteBtn = createButton( contents, new DeleteAttributeAction( fte ) );

        // layout
        FormLayout layout = new FormLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.spacing = 3;
        contents.setLayout( layout );

        dTable.left = new FormAttachment( 0 );
        dTable.right = new FormAttachment( createBtn, -2 );
        dTable.top = new FormAttachment( 0 );
        dTable.bottom = new FormAttachment( 100 );

        FormData dButton = new FormData();
        dButton.right = new FormAttachment( 100 );
        dButton.top = new FormAttachment( 0 );
        createBtn.setLayoutData( dButton );

        dButton = new FormData();
        dButton.right = new FormAttachment( 100 );
        dButton.top = new FormAttachment( createBtn, 3 );
        deleteBtn.setLayoutData( dButton );

        return contents;
    }


    protected Button createButton( Composite composite, final IAction action ) {
        final Button button = new Button( composite, SWT.PUSH );
        button.setToolTipText( action.getToolTipText() );

        // FIXME UiPlugin...
        ImageRegistry images = UiPlugin.getDefault().getImageRegistry();
        Image image = images.get( action.getId() );
        if (image == null || image.isDisposed()) {
            images.put( action.getId(), action.getImageDescriptor() );
            image = images.get( action.getId() );
        }
        button.setImage( image );

        button.addListener( SWT.Selection, new Listener() {
            public void handleEvent( Event event ) {
                action.runWithEvent( event );
            }
        });
        return button;
    }

}
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
package org.polymap.core.data.feature.filter;

import java.util.Properties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.filter.Filter;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.rwt.widgets.codemirror.CodeMirror;

import org.eclipse.ui.dialogs.PropertyPage;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.pipeline.ProcessorExtension.ProcessorPropertyPage;
import org.polymap.core.project.PipelineHolder;
import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 * @see FeatureFilterProcessor
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureFilterProcessorConfig
        extends PropertyPage
        implements ProcessorPropertyPage {

    private static Log log = LogFactory.getLog( FeatureFilterProcessorConfig.class );


    public static String encodeFilter( Filter filter ) 
    throws IOException {
        OGCConfiguration config = new org.geotools.filter.v1_1.OGCConfiguration();
        Encoder encoder = new Encoder( config );
        encoder.setIndenting( true );
        encoder.setIndentSize( 4 );
        ByteArrayOutputStream bout = new ByteArrayOutputStream( 4096 );
        encoder.encode( filter, org.geotools.filter.v1_0.OGC.Filter, bout );
        return bout.toString( "UTF8" );
    }


    public static Filter decodeFilter( String encoded ) 
    throws IOException, SAXException, ParserConfigurationException {
        Configuration config = new org.geotools.filter.v1_1.OGCConfiguration();
        Parser parser = new Parser( config );

        return (Filter)parser.parse( new ByteArrayInputStream( encoded.getBytes( "UTF8" ) ) );
    }


    // instance *******************************************

    private PipelineHolder          holder;

    private Properties              props;
    
    private String                  encodedFilter;


    public void init( PipelineHolder _holder, Properties _props ) {
        this.holder = _holder;
        this.props = _props;

        // get filter
        encodedFilter = props.getProperty( "filter" );
        if (encodedFilter == null) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "No 'filter' property found.", null );
        }
    }


    protected Control createContents( Composite parent ) {
        noDefaultAndApplyButton();

//        ScrolledComposite contents = new ScrolledComposite( parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
//        contents.setExpandHorizontal( true );
//        contents.setExpandVertical( true );
//        contents.setShowFocusedControl( true );
        
        Composite contents = new Composite( parent, SWT.BORDER );
        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        contents.setLayoutData( data );

        contents.setLayout( new FormLayout() );

//        Text xmlText = new Text( contents, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL );
//        xmlText.setText( encodedFilter );
        
        CodeMirror xmlEditor = new CodeMirror( contents, SWT.V_SCROLL | SWT.H_SCROLL );
        xmlEditor.setLayoutData( new SimpleFormData( 0 ).fill().create() );
        xmlEditor.setText( encodedFilter );
        
//        contents.setContent( xmlEditor );
        return contents;
    }
    
}

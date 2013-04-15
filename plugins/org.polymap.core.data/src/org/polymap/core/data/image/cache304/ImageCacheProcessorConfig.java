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
package org.polymap.core.data.image.cache304;

import java.util.Properties;

import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import org.polymap.core.data.pipeline.ProcessorExtension.ProcessorPropertyPage;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.PipelineHolder;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.TypedProperties;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImageCacheProcessorConfig
        extends FieldEditorPreferencePage
        implements ProcessorPropertyPage {

    private static Log log = LogFactory.getLog( ImageCacheProcessorConfig.class );

    private PipelineHolder          holder;
    
    private Properties              props;

    private IntegerFieldEditor      totalCacheSizeField;

    private IntegerFieldEditor      timeToLiveField;

    private IntegerFieldEditor      maxTotalSizeField;

    private NumberFormat            mbFormat;
    
    private Cache304                cache;

    
    public void init( PipelineHolder _holder, Properties _props ) {
        this.holder = _holder;
        this.props = _props;
        this.cache = Cache304.instance();
        
        //setPreferenceStore( Cache304.instance().prefs );
        setDescription( "TileCache Settings / Statistics" );

        mbFormat = NumberFormat.getInstance( Polymap.getSessionLocale() );
        mbFormat.setMaximumFractionDigits( 0 );
        mbFormat.setMinimumFractionDigits( 0 );
    }

    
    protected void createFieldEditors() {
        noDefaultAndApplyButton();

        TypedProperties params = new TypedProperties( props );

        Integer value = null;
        
        timeToLiveField = new IntegerFieldEditor(
                Cache304.PROP_MAX_TILE_LIVETIME, "Max tile livetime (hours)", getFieldEditorParent() );
        timeToLiveField.setValidRange( 0, 100000 );
        value = params.getInt( Cache304.PROP_MAX_TILE_LIVETIME, Cache304.DEFAULT_MAX_TILE_LIVETIME );
        timeToLiveField.setStringValue( String.valueOf( value ) );
        addField( timeToLiveField );
        
        maxTotalSizeField = new IntegerFieldEditor(
                Cache304.PREF_TOTAL_STORE_SIZE, "Max total size (MB)", getFieldEditorParent() );
        maxTotalSizeField.setValidRange( 1, 100000 );
        maxTotalSizeField.setStringValue( mbFormat.format( cache.getMaxTotalSize() / 1024 / 1024 ) );
        addField( maxTotalSizeField );
        
        // statistics
        //new SeparatorFieldEditor( "sep1", "", getFieldEditorParent() );
        
        CacheStatistics stats = Cache304.statistics();
     
        // hit count
        Composite fieldParent = getFieldEditorParent();
        StringFieldEditor hitsField = new StringFieldEditor( "hits", "Cache hits", fieldParent );
        hitsField.setEnabled( false, fieldParent );
        hitsField.setStringValue( mbFormat.format( stats.layerHitCount( (ILayer)holder ) ) );

        // miss count
        fieldParent = getFieldEditorParent();
        StringFieldEditor missField = new StringFieldEditor( "misses", "Cache misses", fieldParent );
        missField.setEnabled( false, fieldParent );
        missField.setStringValue( mbFormat.format( stats.layerMissCount( (ILayer)holder ) ) );

        // layer size
        fieldParent = getFieldEditorParent();
        StringFieldEditor layerSizeField = new StringFieldEditor( "layerSize", "Layer cache size (MB)", fieldParent );
        layerSizeField.setEnabled( false, fieldParent );
        layerSizeField.setStringValue( mbFormat.format( (double)stats.layerStoreSize( cache, (ILayer)holder ) / 1024 / 1024 ) );

        // layer tile count
        fieldParent = getFieldEditorParent();
        StringFieldEditor tileCountField = new StringFieldEditor( "tileCount", "Tiles", fieldParent );
        tileCountField.setEnabled( false, fieldParent );
        tileCountField.setStringValue( mbFormat.format( stats.layerTileCount( cache, (ILayer)holder ) ) );

        // store size
        fieldParent = getFieldEditorParent();
        StringFieldEditor totalSizeField = new StringFieldEditor( "totalSize", "Total cache size (MB)", fieldParent );
        totalSizeField.setEnabled( false, fieldParent );
        totalSizeField.setStringValue( mbFormat.format( (double)stats.totalStoreSize( cache ) / 1024 / 1024 ) );
    }

    
//    public boolean okToLeave() {
//        checkState();
//        return super.okToLeave();
//    }


    protected void updateApplyButton() {
        super.updateApplyButton();
        log.info( "message: " + getErrorMessage() );
//        getContainer().updateButtons();
//        getContainer().updateMessage();
    }


    public boolean performOk() {
//        props.setProperty( CacheManager.PARAM_MAX_ELEMENTS_IN_MEMORY, maxInMemoryField.getStringValue() );
//        props.setProperty( CacheManager.PARAM_MAX_ELEMENTS_ON_DISK, maxOnDiskField.getStringValue() );
        int timeToLive = Integer.parseInt( timeToLiveField.getStringValue() );
        props.setProperty( Cache304.PROP_MAX_TILE_LIVETIME, 
                 String.valueOf( timeToLive ) );
        
        long maxTotalSize = (long)maxTotalSizeField.getIntValue() * 1024 * 1024;
        cache.setMaxTotalSize( maxTotalSize );
        
        return true;
    }

    
    /**
     * 
     */
    class SeparatorFieldEditor
            extends FieldEditor {

        public SeparatorFieldEditor( String name, String labelText, Composite parent ) {
            super( name, labelText, parent );
        }

        public int getNumberOfControls() {
            return 0;
        }

        protected void doFillIntoGrid( Composite parent, int numColumns ) {
            Label separator = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
            separator.setText( getLabelText() );
            //separator.setForeground( Graphics.getColor( 255, 255, 255 ) );
        }

        protected void adjustForNumColumns( int numColumns ) {
        }

        protected void doLoad() {
        }

        protected void doLoadDefault() {
        }

        protected void doStore() {
        }
        
    }
}

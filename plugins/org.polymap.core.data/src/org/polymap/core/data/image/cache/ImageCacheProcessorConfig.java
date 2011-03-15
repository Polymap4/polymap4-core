package org.polymap.core.data.image.cache;

import java.util.Properties;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;

import org.polymap.core.data.pipeline.ProcessorExtension.ProcessorPropertyPage;
import org.polymap.core.project.PipelineHolder;
import org.polymap.core.runtime.TypedProperties;

/**
 * The configuration UI for {@link ImageCacheProcessor}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class ImageCacheProcessorConfig
        extends FieldEditorPreferencePage
        implements ProcessorPropertyPage {

    private PipelineHolder          holder;
    
    private Properties              props;

    private IntegerFieldEditor      maxInMemoryField;
    
    private IntegerFieldEditor      maxOnDiskField;
    
    private IntegerFieldEditor      timeToLiveField;
    
    
    public void init( PipelineHolder _holder, Properties _props ) {
        this.holder = _holder;
        this.props = _props;
    }

    
    public void dispose() {
        super.dispose();
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().dispose();
        }
    }


    public boolean performOk() {
        props.setProperty( CacheManager.PARAM_MAX_ELEMENTS_IN_MEMORY, maxInMemoryField.getStringValue() );
        props.setProperty( CacheManager.PARAM_MAX_ELEMENTS_ON_DISK, maxOnDiskField.getStringValue() );
        int timeToLive = Integer.parseInt( timeToLiveField.getStringValue() );
        props.setProperty( CacheManager.PARAM_TIME_TO_LIVE, 
                 String.valueOf( timeToLive * 3600 ) );
        return true;
    }


    protected void createFieldEditors() {
        noDefaultAndApplyButton();

        TypedProperties params = new TypedProperties( props );
        
        maxInMemoryField = new IntegerFieldEditor(
                CacheManager.PARAM_MAX_ELEMENTS_IN_MEMORY, "Max in memory (tiles)", getFieldEditorParent() );
        maxInMemoryField.setValidRange( 0, 100 );
        int value = params.getInt( CacheManager.PARAM_MAX_ELEMENTS_IN_MEMORY, 20 );
        maxInMemoryField.setStringValue( String.valueOf( value ) );

        maxOnDiskField = new IntegerFieldEditor(
                CacheManager.PARAM_MAX_ELEMENTS_ON_DISK, "Max on disk (tiles)", getFieldEditorParent() );
        maxOnDiskField.setValidRange( 0, 1000000 );
        value = params.getInt( CacheManager.PARAM_MAX_ELEMENTS_ON_DISK, 10000 );
        maxOnDiskField.setStringValue( String.valueOf( value ) );

        timeToLiveField = new IntegerFieldEditor(
                CacheManager.PARAM_TIME_TO_LIVE, "Time to live (hours)", getFieldEditorParent() );
        timeToLiveField.setValidRange( 0, Integer.MAX_VALUE );
        value = params.getInt( CacheManager.PARAM_TIME_TO_LIVE, 24*3600 );
        timeToLiveField.setStringValue( String.valueOf( ((int)value/3600) ) );
    }

}
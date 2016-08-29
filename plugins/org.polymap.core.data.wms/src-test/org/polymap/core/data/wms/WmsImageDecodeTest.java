package org.polymap.core.data.wms;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.polymap.core.data.image.ImageDecodeProcessor;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.data.wms.WmsRenderProcessor;

public class WmsImageDecodeTest {
    
    @Test
    public void testImageEncodeCompatibelFeatureRender() throws Exception {
        ImageDecodeProcessor proc1 = new ImageDecodeProcessor();
        WmsRenderProcessor proc2 = new WmsRenderProcessor();
        
        ProcessorSignature sig1 = new ProcessorSignature( proc1 );
        ProcessorSignature sig2 = new ProcessorSignature( proc2 );
        System.out.println( "ImageDecodeProcessor:\n" + sig1 );
        System.out.println( "WmsRenderProcessor:\n" + sig2 );
        assertTrue( sig1.isCompatible( sig2 ) );
    }
}


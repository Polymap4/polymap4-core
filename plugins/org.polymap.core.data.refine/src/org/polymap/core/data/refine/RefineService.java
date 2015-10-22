package org.polymap.core.data.refine;

import java.io.File;
import java.io.InputStream;

import org.polymap.core.data.refine.impl.FormatAndOptions;
import org.polymap.core.data.refine.impl.ImportResponse;

import com.google.refine.importing.ImportingJob;

public interface RefineService {

    <T extends FormatAndOptions> ImportResponse<T> importStream( InputStream in, String fileName,
            String mimeType, T options );


    <T extends FormatAndOptions> ImportResponse<T> importFile( File in, T options );


    void updateOptions( ImportingJob job, FormatAndOptions options );
}

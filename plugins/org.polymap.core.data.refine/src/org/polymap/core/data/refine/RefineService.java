package org.polymap.core.data.refine;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.polymap.core.data.refine.impl.FormatAndOptions;
import org.polymap.core.data.refine.impl.ImportResponse;

import com.google.refine.importing.ImportingJob;
import com.google.refine.model.Project;

public interface RefineService {

    <T extends FormatAndOptions> ImportResponse<T> importStream(InputStream in, String fileName, String mimeType,
            T options, IProgressMonitor monitor);

    <T extends FormatAndOptions> ImportResponse<T> importFile(File in, T options, IProgressMonitor monitor);

    void updateOptions(ImportingJob job, FormatAndOptions options, IProgressMonitor monitor);

    Project createProject(ImportingJob job, FormatAndOptions options, IProgressMonitor monitor);
}

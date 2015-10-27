package org.polymap.core.data.refine.impl;

import com.google.refine.importing.ImportingJob;
import com.google.refine.model.Project;

public class ImportResponse<T extends FormatAndOptions> {

    private ImportingJob     job;

    private T options;


    public void setJob( ImportingJob job ) {
        this.job = job;
    }


    public void setOptions( T options ) {
        this.options = options;
    }


    public ImportingJob job() {
        return job;
    }


    public T options() {
        return options;
    }
}

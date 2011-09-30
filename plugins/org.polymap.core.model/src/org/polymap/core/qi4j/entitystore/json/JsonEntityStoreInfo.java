package org.polymap.core.qi4j.entitystore.json;

import java.io.File;
import java.io.Serializable;


public final class JsonEntityStoreInfo
        implements Serializable {

    private File        dir;

    public JsonEntityStoreInfo( File dir ) {
        this.dir = dir;
    }

    public File getDir() {
        return dir;
    }
    
}
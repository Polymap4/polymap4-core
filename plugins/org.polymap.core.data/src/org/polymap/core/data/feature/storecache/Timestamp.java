/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.feature.storecache;

import java.util.function.Function;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import org.polymap.core.CorePlugin;
import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.feature.storecache.StoreCacheProcessor.Task;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;

/**
 * {@link File} based timestamp.
 * 
 * @author Falko Bräutigam
 */
public class Timestamp {

    public static Timestamp of( String layerId ) {
        return new Timestamp( layerId );
    }
    
    // instance ***************************************
    
    private String          layerId;
    
    private Lazy<File>      file = new PlainLazyInit( () -> {
        File cacheDir = new File( CorePlugin.getCacheLocation( DataPlugin.getDefault() ), "storecache" );
        return new File( cacheDir, layerId );
    });
    
    private Timestamp( String layerId ) {
        this.layerId = layerId;
    }

    public long get() {
        try {
            return file.get().exists() 
                    ? Long.parseLong( FileUtils.readFileToString( file.get(), "UTF-8" ) )
                    : 0;
        }
        catch (NumberFormatException | IOException e) {
            throw new RuntimeException( e );
        }
    }        
    
    public <E extends Exception> void checkSet( Function<Long,Boolean> check, Task<E> set ) throws E {
        if (check.apply( get() )) {
            try {
                boolean ok = set.run();
                if (ok) {
                    long timestamp = System.currentTimeMillis();

                    // XXX no real atomic check but may give a hint
                    //assert !file.get().exists() || timestamp == Long.parseLong( FileUtils.readFileToString( f, "UTF-8" ) );
                    FileUtils.writeStringToFile( file.get(), Long.toString( timestamp ), "UTF-8" );
                }
            }
            catch (IOException e) {
                throw new RuntimeException( e );                    
            }
        }
    }
    
    public void clear() {
        try {
            Files.deleteIfExists( file.get().toPath() );
        }
        catch (IOException e) {
            throw new RuntimeException( e );                    
        }
    }
    
}
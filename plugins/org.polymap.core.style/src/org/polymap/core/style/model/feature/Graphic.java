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
package org.polymap.core.style.model.feature;

import java.util.List;
import java.util.Optional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.Lists;

import org.polymap.core.style.StylePlugin;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class Graphic {

    public enum WellKnownMark {
        Square, Circle, Cross, X, Triangle, Star
    }

    private String          markOrName;
    
    public Graphic( String markOrUrl ) {
        this.markOrName = markOrUrl;
    }

    public Optional<WellKnownMark> mark() {
        try {
            return Optional.of( WellKnownMark.valueOf( markOrName ) );
        }
        catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    public Optional<String> url() {
        if (mark().isPresent()) {
            return Optional.empty();
        }
        else {
            File f = findGraphic( markOrName )
                    .orElseThrow( () -> new RuntimeException( "No such graphic file: " + markOrName ) );
            return Optional.of( "file://" + f.getAbsolutePath() );
        }
    }

    public Optional<String> format() {
        return url().map( url -> format( url ) );
    }
    
    
    // Graphics store **************************************
    
    public static String defaultGraphic() {
        File f = graphicFile( "default.svg" );
        if (!f.exists()) {
            try (
                InputStream in = StylePlugin.instance().getBundle().getResource( "/resources/icons/map-marker.svg" ).openStream();
            ){
                FileUtils.copyInputStreamToFile( in, f );
            }
            catch (IOException e) {
                throw new RuntimeException( "default.svg not found.", e );
            }
        }
        return f.getName();
    }
    
    
    public static List<String> allGraphics() {
        return Lists.newArrayList( StylePlugin.graphicsStore().list() );
    }
    
    
    public static String uploadGraphic( String name, InputStream in ) throws IOException {
        try (
            InputStream _in = in
        ){
            // check supported extension
            format( name );
            
            String uniqueName = name;
            for (int i=2; findGraphic( name ).isPresent(); i++) {
                uniqueName = FilenameUtils.getBaseName( name ) + i + FilenameUtils.getExtension( name );
            }
            File f = new File( StylePlugin.graphicsStore(), uniqueName );
            FileUtils.copyInputStreamToFile( in, f );
            return uniqueName;
        }
    }
    
    
    public static Optional<File> findGraphic( String name ) {
        File result = graphicFile( name );
        return result.exists() ? Optional.of( result ) : Optional.empty();
    }
    
    
    protected static File graphicFile( String name ) {
        return new File( StylePlugin.graphicsStore(), name );
    }

    
    protected static String format( String name ) {
        String ext = FilenameUtils.getExtension( name.toLowerCase() );
        if (ext.equals( "png" )) {
            return "image/png";
        }
        else if (ext.equals( "jpg" ) || ext.equals( "jpeg" )) {
            return "image/jpg";
        }
        else if (ext.equals( "gif" )) {
            return "image/gif";
        }
        else if (ext.equals( "svg" )) {
            return "image/svg+xml";
        }
        else {
            throw new RuntimeException( "Unhandled file extension: " + ext ); 
        }
    }

}

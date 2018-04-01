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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.polymap.core.style.StylePlugin;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class Graphic {

    private static final Log log = LogFactory.getLog( Graphic.class );
    
    public enum WellKnownMark {
        Circle, Square, Cross, X, Triangle, Star
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
        return url().map( url -> {
            try {return format( url );} 
            catch (IOException e) { throw new RuntimeException( e ); }
        });
    }
    
    
    // Graphics store **************************************
    
    public static String defaultGraphic() {
        File f = graphicFile( "default.svg" );
        if (!f.exists()) {
            try (
                InputStream in = StylePlugin.instance().getBundle().getResource( "/resources/icons/map-marker.svg" ).openStream();
            ){
                FileUtils.copyInputStreamToFile( in, f );
                equipSvg( f );
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
            for (int i=2; findGraphic( uniqueName ).isPresent(); i++) {
                uniqueName = FilenameUtils.getBaseName( name ) + i + "." + FilenameUtils.getExtension( name );
            }
            File f = new File( StylePlugin.graphicsStore(), uniqueName );
            FileUtils.copyInputStreamToFile( in, f );
            
            equipSvg( f );
            return uniqueName;
        }
    }
    
    
    public static void equipSvg( File f ) {
        try {
            // read/parse
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory( parser );
            Document doc = factory.createDocument( f.toURI().toURL().toString() );  
            
            // modify
            List<String> NAMES = Lists.newArrayList( "circle", "ellipse", "line", "mesh", "path", "polygon", "polyline", "rect" );
            NodeList elms = doc.getElementsByTagName( "*" );
            for (int i=0; i<elms.getLength(); i++) {
                Node node = elms.item( i );
                if (node instanceof Element && NAMES.contains( node.getNodeName() )) {
                    ((Element)node).setAttribute( "fill", "param(fill-color)" );
                    ((Element)node).setAttribute( "fill-opacity", "param(fill-opacity)" );
                    ((Element)node).setAttribute( "stroke", "param(stroke-color)" );
                    ((Element)node).setAttribute( "stroke-opacity", "param(stroke-opacity)" );
                    ((Element)node).setAttribute( "stroke-width", "param(stroke-width)" );
                }
            }
            // write
            try (
                FileWriterWithEncoding out = new FileWriterWithEncoding( f, "UTF-8" )
            ){
                SVGTranscoder t = new SVGTranscoder();
                t.transcode( new TranscoderInput( doc ), new TranscoderOutput( out ) );
            }
            log.info( "SVG: " + FileUtils.readFileToString( f, "UTF-8" ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public static Optional<File> findGraphic( String name ) {
        File result = graphicFile( name );
        return result.exists() ? Optional.of( result ) : Optional.empty();
    }
    
    
    protected static File graphicFile( String name ) {
        return new File( StylePlugin.graphicsStore(), name );
    }

    
    /**
     * 
     *
     * @param name
     * @return
     * @throws IOException If the extension is not supported.
     */
    protected static String format( String name ) throws IOException {
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
            throw new IOException( "Unhandled file extension: " + ext ); 
        }
    }

}

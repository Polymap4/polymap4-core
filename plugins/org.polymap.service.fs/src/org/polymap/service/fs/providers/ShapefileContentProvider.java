/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service.fs.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;

import org.polymap.service.fs.Messages;
import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.IContentDeletable;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentSite;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.Range;

/**
 * Provides a shapefile folder for every parent folder that exposes an {@link ILayer}
 * as source.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapefileContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( ShapefileContentProvider.class );

    
    public List<? extends IContentNode> getChildren( IPath path, IContentSite site ) {
        IContentFolder parent = site.getFolder( path );
        
        // files
        if (parent instanceof ShapefileFolder) {
            List<IContentNode> result = new ArrayList();
            
            // shapefile
            ILayer layer = ((ShapefileFolder)parent).getLayer();
            ShapefileContainer container = new ShapefileContainer( layer, site );
            for (String fileSuffix : ShapefileGenerator.FILE_SUFFIXES) {
                result.add( new ShapefileFile( path, this, (ILayer)parent.getSource()
                        , container, fileSuffix ) );
            }
            // snapshot.txt
            result.add( new SnapshotFile( path, this, (ILayer)parent.getSource(), container, site ) );
            // shape-zip
            result.add( new ShapeZipFile( path, this, (ILayer)parent.getSource(), container ) );
            return result;
        }

        // folder
        else if (parent instanceof ProjectContentProvider.LayerFolder) {
            ILayer layer = (ILayer)parent.getSource();
            try {
                // try to build an fs for it
                PipelineFeatureSource.forLayer( layer, true );
                return Collections.singletonList( new ShapefileFolder( path, this, layer ) );
            }
            catch (Exception e) {
                log.info( "Layer has no feature source: " + layer.getLabel() );
            }
        }
        return null;
    }
    

    /*
     * 
     */
    public class ShapefileFolder
            extends DefaultContentFolder {

        public ShapefileFolder( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( "shapefile", parentPath, provider, layer );
        }
        
        public ILayer getLayer() {
            return (ILayer)getSource();
        }
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält die <b>Shapefile-Daten</b> der Ebene \"" + getLayer().getLabel() + "\".";
        }
    }

    
    /*
     * 
     */
    public class ShapefileFile
            extends DefaultContentNode
            implements IContentFile, IContentWriteable, IContentDeletable {

        private ShapefileContainer  container;
        
        private String              fileSuffix;
        
        
        public ShapefileFile( IPath parentPath, IContentProvider provider, ILayer layer,
                ShapefileContainer container, String fileSuffix ) {
            super( layer.getLabel() + "." + fileSuffix, parentPath, provider, layer );
            this.container = container;
            this.fileSuffix = fileSuffix;
        }

        
        public void delete()
        throws BadRequestException {
            // QGIS deletes files before writing, so support delete but do nothing.
            log.debug( "delete: " + fileSuffix + " (skipping)" );
        }


        public Long getContentLength() {
            if (container.exception != null) {
                log.warn( "", container.exception );
                return null;
            }
            else {
                return container.getFileSize( fileSuffix );                
            }
        }


        public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
        throws IOException, BadRequestException {
            log.debug( "range: " + range + ", params: " + params + ", contentType: " + contentType );
            
            if (container.exception != null) {
                log.warn( "", container.exception );
            }
            else {
                InputStream in = container.getInputStream( fileSuffix );                
                try {
                    IOUtils.copy( in, out );
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
            }
        }


        public void replaceContent( InputStream in, Long length )
        throws IOException, BadRequestException {
            log.debug( "replace: " + fileSuffix + " : " + length );
            OutputStream out = container.getOutputStream( fileSuffix );
            try {
                IOUtils.copy( in, out );
            }
            finally {
                IOUtils.closeQuietly( out );
            }
        }


        public String getContentType( String accepts ) {
            return "application/octec-stream";
        }


        public Long getMaxAgeSeconds() {
            return (long)60;
        }


        public Date getModifiedDate() {
            return container.lastModified;
        }

    }


    /*
     * 
     */
    public static class SnapshotFile
            extends DefaultContentNode
            implements IContentFile, IContentDeletable {

        private static final FastDateFormat df = FastDateFormat.getInstance( "yyyy-MM-dd@HH-mm-ss" );
        
        private ShapefileContainer          container;
        
        private IContentSite                site;
        
        
        public SnapshotFile( IPath parentPath, IContentProvider provider, ILayer layer,
                ShapefileContainer container, IContentSite site ) {
            super( "snapshot.txt", parentPath, provider, layer );
            this.container = container;
            this.site = site;
        }


        public void delete()
        throws BadRequestException {
            container.flush();   
        }


        public byte[] content() {
            try {
                String modified = df.format( container.lastModified );
                return Messages.get( site.getLocale(), "SnapshotFile_content", 
                        ((ILayer)getSource()).getLabel(), modified ).getBytes( "UTF-8" );
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException( e );
            }
        }


        public Long getContentLength() {
            return (long)content().length;
        }


        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType )
        throws IOException, BadRequestException {
            out.write( content() );
        }


        public String getContentType( String accepts ) {
            return "text/txt";
        }


        public Long getMaxAgeSeconds() {
            return (long)60;
        }


        public Date getModifiedDate() {
            return container.lastModified;
        }
    }
    

    /*
     * 
     */
    public class ShapeZipFile
            extends DefaultContentNode
            implements IContentFile {

        private ShapefileContainer      container;
        
        private SoftReference<byte[]>   contentRef;
        
        private Date                    modified = new Date();
        
        
        public ShapeZipFile( IPath parentPath, IContentProvider provider, ILayer layer,
                ShapefileContainer container ) {
            super( layer.getLabel() + ".shp.zip", parentPath, provider, layer );
            this.container = container;
        }

        
        public String getContentType( String accepts ) {
            return "application/zip";
        }


        public Long getMaxAgeSeconds() {
            return (long)60;
        }


        public Date getModifiedDate() {
            return modified;
        }


        public Long getContentLength() {
            try {
                return (long)checkInitContent().length;
            }
            catch (Exception e) {
                throw new RuntimeException( "", e );
            }
        }


        public void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType )
        throws IOException, BadRequestException {
            log.debug( "range: " + range + ", params: " + params + ", contentType: " + contentType );
            try {
                byte[] content = checkInitContent();
                out.write( content );
            }
            catch (Exception e) {
                throw new RuntimeException( "", e );
            }
        }

        
        protected synchronized byte[] checkInitContent() 
        throws Exception {
            if (container.exception != null) {
                log.warn( "Last exception from container: ", container.exception );
                throw container.exception;
            }

            byte[] result = contentRef != null ? contentRef.get() : null;
            
            if (result == null || container.lastModified.after( modified )) {
                // generate shapefile
                container.getFileSize( "shp" );

                if (container.exception != null) {
                    log.warn( "Last exception from container: ", container.exception );
                    throw container.exception;
                }

                InputStream in = null;
                try {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream( 1024 * 1024 );
                    ZipOutputStream zipOut = new ZipOutputStream( bout );

                    String basename = container.layer.getLabel();

                    for (String fileSuffix : ShapefileGenerator.FILE_SUFFIXES) {
                        zipOut.putNextEntry( new ZipEntry( basename + "." + fileSuffix ) );
                        in = container.getInputStream( fileSuffix );
                        IOUtils.copy( in, zipOut );
                        in.close();
                        
                    }
                    zipOut.close();
                    
                    result = bout.toByteArray();
                    contentRef = new SoftReference( result );
                    modified = new Date();
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
                finally {
                    IOUtils.closeQuietly( in );
                }
            }
            return result;
        }
    }

}

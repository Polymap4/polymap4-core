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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.geotools.geojson.feature.FeatureJSON;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.Range;

/**
 * Provides a Geo JSON encoded file in every parent folder that exposes an {@link ILayer}
 * as source.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class GeoJsonContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( GeoJsonContentProvider.class );
    

    public List<? extends IContentNode> getChildren( IPath path ) {
        IContentFolder parent = getSite().getFolder( path );
        
        // file
        if (parent instanceof GeoJsonFolder) {
            return Collections.singletonList( 
                    new GeoJsonFile( path, this, (ILayer)parent.getSource() ) );
        }
        // folder
        else if (parent instanceof ProjectContentProvider.LayerFolder) {
            return Collections.singletonList( 
                    new GeoJsonFolder( path, this, (ILayer)parent.getSource() ) );
        }
        return null;
    }
    

    /*
     * 
     */
    public static class GeoJsonFolder
            extends DefaultContentFolder {

        public GeoJsonFolder( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( "geojson", parentPath, provider, layer );
        }

        public ILayer getLayer() {
            return (ILayer)getSource();
        }
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enth�lt die Daten der Ebene \"" + getLayer().getLabel() 
                    + "\" im <b>GeoJSON-Format</b>.";
        }
    }


    /*
     * 
     */
    public static class GeoJsonFile
            extends DefaultContentNode
            implements IContentFile {

        private byte[]              content;
        
        private Exception           lastException;
        
        private Date                modified;
        
        
        public GeoJsonFile( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( layer.getLabel() + ".json", parentPath, provider, layer );
            try {
                lastException = null;
                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( getLayer(), false );

                FeatureJSON encoder = new FeatureJSON();
                encoder.setEncodeFeatureBounds( false );
                encoder.setEncodeFeatureCollectionBounds( false );
                encoder.setEncodeFeatureCollectionCRS( false );
                encoder.setEncodeFeatureCRS( false );

                ByteArrayOutputStream out = new ByteArrayOutputStream( 128*1024 );
                encoder.writeFeatureCollection( fs.getFeatures(), out );
                
                content = out.toByteArray();
                modified = new Date();
            }
            catch (Exception e) {
                log.warn( "", e );
                lastException = e;
            }
        }


        public int getSizeInMemory() {
            return (content != null ? content.length : 0) + super.getSizeInMemory();
        }


        public ILayer getLayer() {
            return (ILayer)getSource();
        }


        public Long getContentLength() {
            if (lastException != null) {
                log.warn( "", lastException );
                return null;
            }
            else {
                return (long)content.length;
            }
        }


        public String getContentType( String accepts ) {
            return "application/json";
        }


        public Long getMaxAgeSeconds() {
            return (long)60;
        }


        public Date getModifiedDate() {
            return modified;
        }


        public void sendContent( final OutputStream out, Range range, Map<String, String> params, String contentType )
        throws IOException, BadRequestException {
            log.debug( "range: " + range + ", params: " + params + ", contentType: " + contentType );
            
            if (lastException != null) {
                log.warn( "", lastException );
            }
            else {
                out.write( content );
            }
        }

    }

}

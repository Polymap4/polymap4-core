/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTWriter;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.data.FeatureChangeTracker;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.model.event.IModelHandleable;
import org.polymap.core.model.event.IModelStoreListener;
import org.polymap.core.model.event.ModelChangeTracker;
import org.polymap.core.model.event.ModelStoreEvent;
import org.polymap.core.model.event.ModelStoreEvent.EventType;
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
 * Provides a CSV folder/file in every parent folder that exposes an {@link ILayer}
 * as source.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CsvContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( CsvContentProvider.class );
    
    public static final FastDateFormat  df = DateFormatUtils.ISO_DATE_FORMAT;
    
    public static final String          CHARSET = "UTF-8";

    
    public List<? extends IContentNode> getChildren( IPath path ) {
        IContentFolder parent = getSite().getFolder( path );
        
        // file
        if (parent instanceof CsvFolder) {
            return Collections.singletonList( 
                    new CsvFile( path, this, (ILayer)parent.getSource() ) );
        }
        // folder
        else if (parent instanceof ProjectContentProvider.LayerFolder) {
            return Collections.singletonList( 
                    new CsvFolder( path, this, (ILayer)parent.getSource() ) );
        }
        return null;
    }
    

    /*
     * 
     */
    public static class CsvFolder
            extends DefaultContentFolder {

        public CsvFolder( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( "csv", parentPath, provider, layer );
        }

        public ILayer getLayer() {
            return (ILayer)getSource();
        }
        
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis enthält die Daten der Ebene \"" + getLayer().getLabel() 
                    + "\" im <b>CSV-Format</b>.";
        }
    }


    /*
     * 
     */
    public static class CsvFile
            extends DefaultContentNode
            implements IContentFile, IModelStoreListener {

        /** GZipped bytes. */
        private byte[]              content;
        
        /** Size of the unzipped content. */
        private long                contentLength;
        
        private IOException         lastException;
        
        private Date                modified;
        
        
        public CsvFile( IPath parentPath, IContentProvider provider, ILayer layer ) {
            super( layer.getLabel() + ".csv", parentPath, provider, layer );
            initContent();

            ModelChangeTracker.instance().addListener( this );
        }


        public void dispose() {
            log.info( "DISPOSE");
            ModelChangeTracker.instance().removeListener( this );
        }


        public boolean isValid() {
            return true;
        }

        public void modelChanged( final ModelStoreEvent ev ) {
            log.info( "ev= " + ev );
            log.info( "session=" + getSite().getSessionContext() );
            if (ev.getEventType() == EventType.COMMIT 
                    // flush also if shapefile has changed for example
                    //&& !ev.isMySession() 
                    // layer or features changed?
                    && (ev.hasChanged( (IModelHandleable)getLayer() ) 
                            || ev.hasChanged( FeatureChangeTracker.layerHandle( getLayer() ) ))) {
                log.info( "FLUSHING : " + getParentPath() );
                getSite().invalidateFolder( getSite().getFolder( getParentPath() ) );                    
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
                throw new RuntimeException( lastException );
            }
            else {
                return contentLength;
            }
        }


        public String getContentType( String accepts ) {
            return "text/csv";
        }


        public Long getMaxAgeSeconds() {
            return (long)60;
        }


        public Date getModifiedDate() {
            if (lastException != null) {
                throw new RuntimeException( lastException );
            }
            else {
                return modified;
            }
        }


        public void sendContent( final OutputStream out, Range range, Map<String, String> params, String contentType )
        throws IOException, BadRequestException {
            if (lastException != null) {
                log.warn( "", lastException );
                throw lastException;
            }
            else {
                IOUtils.copy( new GZIPInputStream( new ByteArrayInputStream( content ) ), out );
            }
        }

        
        protected void initContent() {
            lastException = null;
            // all features
            FeatureIterator it = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                CountingOutputStream cout = new CountingOutputStream( new GZIPOutputStream( bout ) );
                Writer writer = new OutputStreamWriter( cout, CHARSET );

                CsvPreference prefs = new CsvPreference('"', ';', "\r\n");  //CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
                CsvListWriter csvWriter = new CsvListWriter( writer, prefs );

                PipelineFeatureSource fs = PipelineFeatureSource.forLayer( getLayer(), false );
                it = fs.getFeatures().features();
                int count = 0;
                boolean noHeaderYet = true;
                
                while (it.hasNext()) {
                    Feature feature = it.next();

                    // header
                    if (noHeaderYet) {
                        List<String> header = new ArrayList( 32 );
                        for (Property prop : feature.getProperties()) {
                            Class<?> binding = prop.getType().getBinding();
                            if (Number.class.isAssignableFrom( binding )
                                    || Boolean.class.isAssignableFrom( binding )
                                    || Date.class.isAssignableFrom( binding )
                                    || String.class.isAssignableFrom( binding )) {
                                header.add( prop.getName().getLocalPart() );
                            }
                            else if (Point.class.isAssignableFrom( binding )) {
                                header.add( "X" );
                                header.add( "Y" );
                            }
                            else if (Geometry.class.isAssignableFrom( binding )) {
                                header.add( "Geometry" );
                            }
                        }
                        csvWriter.writeHeader( header.toArray(new String[header.size()]) );
                        noHeaderYet = false;
                    }

                    // all properties
                    List line = new ArrayList( 32 );
                    for (Property prop : feature.getProperties()) {
                        Class binding = prop.getType().getBinding();
                        Object value = prop.getValue();

                        // Point
                        if (Point.class.isAssignableFrom( binding )) {
                            Point point = (Point)value;
                            line.add( value != null ? point.getX() : "" );
                            line.add( value != null ? point.getY() : "" );
                        }
                        // other Geometry
                        else if (Geometry.class.isAssignableFrom( binding )) {
                            if (value != null) {
                                WKTWriter wkt = new WKTWriter();
                                wkt.setFormatted( false );
                                line.add( wkt.write( (Geometry)value ) );
                            }
                            else {
                                line.add( "" );
                            }
                        }
                        // Number
                        else if (Number.class.isAssignableFrom( binding )) {
                            line.add( value != null ? value.toString() : "" );
                        }
                        // Boolean
                        else if (Boolean.class.isAssignableFrom( binding )) {
                            line.add( value == null ? "" :
                                ((Boolean)value).booleanValue() ? "ja" : "nein");
                        }
                        // Date
                        else if (Date.class.isAssignableFrom( binding )) {
                            line.add( value != null ? df.format( (Date)value ) : "" );
                        }
                        // String
                        else if (String.class.isAssignableFrom( binding )) {
                            String s = value != null ? (String)value : "";
                            // Excel happens to interprete decimal value otherwise! :(
                            s = StringUtils.replace( s, "/", "-" );
                            line.add( s );
                        }
                        // other
                        else {
                            log.debug( "skipping: " + prop.getName().getLocalPart() + " type:" + binding );
                        }
                    }
                    log.debug( "LINE: " + line );
                    csvWriter.write( line );
                }
                csvWriter.close();
                writer.close();
                content = bout.toByteArray();
                contentLength = cout.getByteCount();
                modified = new Date();
            }
            catch (IOException e) {
                log.warn( "", e);
                lastException = e;
            }
            catch (Exception e) {
                log.warn( "", e);
                lastException = new IOException( e );
            }
            finally {
                if (it != null) { it.close(); }
            }
        }
    }

}

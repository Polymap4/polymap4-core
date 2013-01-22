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
package org.polymap.core.data.operations.feature;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * General feature to CSV export tool.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CsvExporter {

    private static Log log = LogFactory.getLog( CsvExporter.class );
    
    private CsvPreference           prefs = new CsvPreference('"', ';', "\r\n");
    
    private Locale                  locale = Locale.getDefault();
    
    private String                  charset = "UTF-8";

    private NumberFormat            nf = NumberFormat.getInstance( locale );

    private FastDateFormat          df = DateFormatUtils.ISO_DATE_FORMAT;

    
    /**
     * Constructs a new exporter with the following defaults:
     * <ul>
     * <li>prefs = new CsvPreference('"', ';', "\r\n")</li>
     * <li>locale = Locale.getDefault()</li>
     * <li>charset = "UTF-8"</li>
     * <li>nf = NumberFormat.getInstance( locale )</li>
     * <li>df = DateFormatUtils.ISO_DATE_FORMAT;</li>
     * </ul>
     */
    public CsvExporter() {
    }
    
    public CsvPreference prefs() {
        return prefs;
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    public CsvExporter setLocale( Locale locale ) {
        assert locale != null;
        this.locale = locale;
        this.nf = NumberFormat.getInstance( locale );
        return this;
    }
    
    public String getCharset() {
        return charset;
    }

    public CsvExporter setCharset( String charset ) {
        assert charset != null;
        this.charset = charset;
        return this;
    }
    
    public NumberFormat getNumberFormat() {
        return nf;
    }
    
    public CsvExporter setNumberFormat( NumberFormat nf ) {
        assert nf != null;
        this.nf = nf;
        return this;
    }
    
    public FastDateFormat getDateFormat() {
        return df;
    }
    
    public CsvExporter setDateFormat( FastDateFormat df ) {
        assert df != null;
        this.df = df;
        return this;
    }


    /**
     *
     * @param features
     * @param out
     * @param monitor
     * @throws IOException If an IO error occured during write.
     * @throws OperationCanceledException If the monitor was canceled during write.
     */
    public void write( FeatureCollection features, OutputStream out, IProgressMonitor monitor ) 
    throws IOException, OperationCanceledException {
        // all features
        FeatureIterator it = null;
        try {
            Writer writer = new OutputStreamWriter( out, charset );
            CsvListWriter csvWriter = new CsvListWriter( writer, prefs );

            it = features.features();
            int count = 0;
            boolean noHeaderYet = true;
            
            while (it.hasNext()) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                if ((++count % 100) == 0) {
                    monitor.subTask( "Objekte: " + count++ );
                    monitor.worked( 100 );
                }
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
                        else if (Geometry.class.isAssignableFrom( binding )) {
                            header.add( "X" );
                            header.add( "Y" );
                        }
//                        else if (Geometry.class.isAssignableFrom( binding )) {
//                            header.add( "Geometry" );
//                        }
                    }
                    csvWriter.writeHeader( header.toArray(new String[header.size()]) );
                    noHeaderYet = false;
                }

                // all properties
                List line = new ArrayList( 32 );
                for (Property prop : feature.getProperties()) {
                    Class binding = prop.getType().getBinding();
                    Object value = prop.getValue();

                    // Geometry/Point
                    if (Geometry.class.isAssignableFrom( binding )) {
                        Point point = value != null ? ((Geometry)value).getCentroid() : null;
                        log.debug( "Point: " + point );
                        line.add( point != null ? nf.format( point.getX() ) : "" );
                        line.add( point != null ? nf.format( point.getY() ) : "" );
                    }
//                    // other Geometry
//                    else if (Geometry.class.isAssignableFrom( binding )) {
//                        if (value != null) {
//                            WKTWriter wkt = new WKTWriter();
//                            wkt.setFormatted( false );
//                            line.add( wkt.write( (Geometry)value ) );
//                        }
//                        else {
//                            line.add( "" );
//                        }
//                    }
                    // null
                    else if (value == null) {
                        line.add( "" );
                    }
                    // Float
                    else if (Float.class.isAssignableFrom( binding )) {
                        line.add( nf.format( ((Float)value).doubleValue() ) );
                    }
                    // Double
                    else if (Double.class.isAssignableFrom( binding )) {
                        line.add( nf.format( ((Double)value).doubleValue() ) );
                    }
                    // Integer
                    else if (Integer.class.isAssignableFrom( binding )) {
                        line.add( nf.format( ((Integer)value).longValue() ) );
                    }
                    // Integer
                    else if (Integer.class.isAssignableFrom( binding )) {
                        line.add( nf.format( ((Integer)value).longValue() ) );
                    }
                    // Number
                    else if (Number.class.isAssignableFrom( binding )) {
                        line.add( nf.format( ((Number)value).doubleValue() ) );
                    }
                    // Boolean
                    else if (Boolean.class.isAssignableFrom( binding )) {
                        line.add( ((Boolean)value).booleanValue() ? "ja" : "nein");
                    }
                    // Date
                    else if (Date.class.isAssignableFrom( binding )) {
                        line.add( df.format( (Date)value ) );
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
        }
        finally {
            if (it != null) { it.close(); }
        }
    }

}

/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com
 * Copyright 2011 Polymap GmbH. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.polymap.core.data.ui.csvimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.NumberFormat;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Provides CSV read/write capabilities based on SuperCSV
 * (http://supercsv.sourceforge.net).
 * <p/>
 * Some of the code was initially taken from eu.hydrologis.jgrass packages.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class CsvImporter {

    private static Log log = LogFactory.getLog( CsvImporter.class );
    
    private Preferences         prefs;
    
    private String[]            header;
    
    private List<String[]>      lines = null;
    
    private byte[]              data;
    
    /**
     * The CSV lines that produced errors during last
     * {@link #createFeatureCollection(CoordinateReferenceSystem, LinkedHashMap, IProgressMonitorJGrass)}.
     */
    private List<Integer>       featureErrors = new ArrayList();

    private NumberFormat        nf = NumberFormat.getInstance( Locale.GERMANY );
    

    /**
     * 
     * @param in The stream to read from. The content is copied from the stream
     *        and the stream is closed.
     * @param prefs The prefs to be used. If null then standard prefs are set.
     * @throws IOException
     */
    public CsvImporter( InputStream in, Preferences prefs ) 
    throws IOException {
        super();
        assert in != null : "in == null";
        this.prefs = prefs != null ? prefs : new Preferences( '"', ',', "\r\n", false );
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copyThenClose( in, out );
        data = out.toByteArray();
    }
    

    /**
     * 
     * @param csvFile
     * @param prefs The prefs to be used. If null then standard prefs are set.
     */
    public CsvImporter( Preferences prefs ) {
        super();
        this.prefs = prefs != null ? prefs : new Preferences( '"', ',', "\r\n", false );
    }


    public void setInputStream( InputStream in ) 
    throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copyThenClose( in, out );
        data = out.toByteArray();
        lines = null;
    }

    
    public byte[] getData() {
        return data;
    }
    
    
    protected void checkReadLines() {
        if (lines != null && !prefs.hasChanged()) {
            return;
        }
        ICsvListReader csv = new CsvListReader( 
                new InputStreamReader( new ByteArrayInputStream( data ), prefs.fileEncoding ), prefs );
        try {
            header = prefs.isUseHeader() ? csv.getCSVHeader( true ) : null;

            lines = new ArrayList( 256 );
            List<String> line = null;
            while ((line = csv.read()) != null) {
                lines.add( line.toArray( new String[line.size()]) );
            }
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }

    
    
    /**
     * This {@link NumberFormat} is used to parse/format coordinate values.
     */
    public NumberFormat getNumberFormat() {
        return nf;
    }


    /**
     * Use this to get and set the preferences of this importer.
     */
    public Preferences prefs() {
        return prefs;    
    }

    public String[] getHeader() {
        return header;
    }
    
    public String[] getLine( int lineIndex ) {
        checkReadLines();
        return lines.get( lineIndex );
    }
                         
    public Iterable<String[]> getLines() {
        return lines;    
    }
    
    public void setLine( int lineIndex, String[] values ) 
    throws IOException {
        checkReadLines();
        lines.set( lineIndex, values );
    }
    
    
    /**
     *
     * @param tableValues 
     * @throws IOException
     */
    public List<Object[]> getColumns( int lineIndex, List<Object[]> defaultValues ) 
    throws IOException {
        checkReadLines();
        
        List<Object[]> result = new ArrayList();

        if (lineIndex >= lines.size() ) {
            throw new IndexOutOfBoundsException( "Lines in CSV file: " + lines.size() );
        }

        String[] line = lines.get( lineIndex );
        for (int i=0; i<line.length; i++ ) {
            Object[] value = new Object[3];

            Object[] defaultValue = defaultValues.size() > i ? defaultValues.get( i ) : null;

            // name
            value[0] = defaultValue != null && !((String)defaultValue[0]).startsWith( "Field" )
                    ? defaultValue[0]
                    : header != null ? header[i] : "Field" + i;

            // value
            value[1] = line[i];

            // type
            value[2] = defaultValue != null
                    ? defaultValue[2]
                    : 3;


            // XXX falko: since there is no check for multiple X/Y/Z column setting a default column is confusing
            //                if (i == 0) {
            //                    try {
            //                        Double.parseDouble( (String)value[1] );
            //                        value[2] = 0;
            //                    } catch (NumberFormatException e) {
            //                        value[2] = 3;
            //                    }
            //                } 
            //                else if (i == 1) {
            //                    try {
            //                        Double.parseDouble((String)value[1]);
            //                        value[2] = 1;
            //                    } catch (NumberFormatException e) {
            //                        value[2] = 3;
            //                    }
            //                } 
            //                else {
            //                    value[2] = 3;
            //                }
            result.add( value );
        }
        return result;
    }


    /**
     * Convert a csv file to a FeatureCollection. <b>This for now supports only
     * point geometries</b>.<br>
     * For different crs it also performs coor transformation.
     * <p>
     * <b>NOTE: this doesn't support date attributes</b>
     * </p>
     * <p>
     * This code was initially taken from
     * {@link eu.hydrologis.jgrass.libs.utils.features.FeatureUtilities}.
     * 
     * @param crs the crs to use.
     * @param fieldsAndTypes the {@link Map} of filed names and
     *        {@link JGrassConstants#CSVTYPESARRAY types}.
     * @param monitor progress monitor.
     * @return the created {@link FeatureCollection}
     * @throws Exception
     */
    @SuppressWarnings("nls")
    public FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
            CoordinateReferenceSystem crs,
            LinkedHashMap<String, Integer> fieldsAndTypesIndex, 
            IProgressMonitorJGrass monitor ) 
            throws Exception {

        checkReadLines();
        
        GeometryFactory gf = new GeometryFactory();
        Map<String, Class> typesMap = JGrassConstants.CSVTYPESCLASSESMAP;
        String[] typesArray = JGrassConstants.CSVTYPESARRAY;

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName( "csvimport" );
        b.setCRS( crs );
        b.add( "the_geom", Point.class );

        int xIndex = -1;
        int yIndex = -1;
        // the bbox of all imported points
        Envelope bbox = null;
        Set<String> fieldNames = fieldsAndTypesIndex.keySet();
        String[] fieldNamesArray = fieldNames.toArray( new String[fieldNames.size()] );
        for (int i=0; i<fieldNamesArray.length; i++) {
            String fieldName = fieldNamesArray[i];
            Integer typeIndex = fieldsAndTypesIndex.get( fieldName );

            if (typeIndex == 0) {
                xIndex = i;
            }
            else if (typeIndex == 1) {
                yIndex = i;
            }
            else {
                Class class1 = typesMap.get( typesArray[typeIndex] );
                b.add( fieldName, class1 );
                log.debug( "    field: name=" + fieldName + ", type=" + class1 + ", index=" + i );
            }
        }
        SimpleFeatureType featureType = b.buildFeatureType();

        // FeatureCollection
        FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = 
                FeatureCollections.newCollection();
        
        try {
            Collection<Integer> orderedTypeIndexes = fieldsAndTypesIndex.values();
            Integer[] orderedTypeIndexesArray = orderedTypeIndexes
                    .toArray( new Integer[orderedTypeIndexes.size()] );

            featureErrors.clear();
            int featureId = 0;
            monitor.beginTask( "Reading CSV Data", lines.size() );
            int count = 0;
            for (String[] line : lines) {
                monitor.worked( 1 );
                if (monitor.isCanceled()) {
                    return newCollection;
                }
                try {
                    SimpleFeatureBuilder builder = new SimpleFeatureBuilder( featureType );
                    Object[] values = new Object[fieldNames.size() - 1];

                    try {
                        double x = line[xIndex].length() > 0
                                ? nf.parse( line[ xIndex ] ).doubleValue()
                                : -1;  //bbox.centre().x;
                        double y = line[yIndex].length() > 0
                                ? nf.parse( line[ yIndex ] ).doubleValue()
                                : -1;  //bbox.centre().y;
                                
                        if (x <= 0 || y <= 0) {
                            log.info( "        Missing geom. skipping this object!" );
                            continue;
                        }
                        Point point = gf.createPoint( new Coordinate( x, y ) );
                        values[0] = point;
                        if (bbox != null) {
                            bbox.expandToInclude( point.getCoordinate() ); 
                        } else {
                            bbox = point.getEnvelope().getEnvelopeInternal();
                        }
                    }
                    catch (Exception e) {
                        // don't break the entire run
                        log.warn( "Error while parsing ccordinates."
                                + " index=" + count 
                                + " | xIndex=" + xIndex + ", value=" + line[xIndex] 
                                + " | yIndex=" + yIndex + ", value=" + line[yIndex] 
                                + " (" + e.toString() + ")" );
                    }

                    int objIndex = 1;
                    for (int i=0; i<orderedTypeIndexesArray.length; i++) {
                        if (i == xIndex || i == yIndex) {
                            continue;
                        }

                        String value = line[ i ];
                        int typeIndex = orderedTypeIndexesArray[i];
                        String typeName = typesArray[typeIndex];
                        if (typeName.equals( typesArray[3] )) {
                            values[objIndex] = value;
                        }
                        else if (typeName.equals( typesArray[4] )) {
                            //values[objIndex] = new Double( value );
                            values[objIndex] = nf.parse( value );
                        }
                        else if (typeName.equals( typesArray[5] )) {
                            values[objIndex] = new Integer( value );
                        }
                        else {
                            throw new IllegalArgumentException( "An undefined value type was found" );
                        }
                        objIndex++;
                    }
                    builder.addAll( values );

                    SimpleFeature feature = builder.buildFeature( 
                            featureType.getTypeName() + "." + featureId );
                    newCollection.add( feature );
                    count++;
                }
                catch (Exception e) {
                    featureErrors.add( featureId );
                    log.warn( "Error while creating FeatureCollection.", e );
                }
                featureId++;
            }
            monitor.done();

        }
        catch (Exception e) {
            //JGrassLibsPlugin.log( "JGrassLibsPlugin problem", e ); //$NON-NLS-1$
            e.printStackTrace();
            throw e;
        }
        return newCollection;
    }

    
    /**
     * 
     *
     * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
     */
    public static class Preferences
            extends CsvPreference {

        private Charset             fileEncoding = Charset.forName( "ISO-8859-1" );
        
        private boolean             useHeader;
        
        private boolean             hasChanged = false;
        
        
        public Preferences( char quoteChar, int delimiterChar, String endOfLineSymbols, boolean useHeader ) {
            super( quoteChar, delimiterChar, endOfLineSymbols );
            this.useHeader = useHeader;
        }

        public boolean hasChanged() {
            boolean result = hasChanged;
            this.hasChanged = false;
            return result;
        }

        public Charset getFileEncoding() {
            return fileEncoding;
        }

        public void setFileEncoding( Charset fileEncoding ) {
            this.fileEncoding = fileEncoding;
        }

        public void setFileEncoding( String fileEncoding ) {
            this.fileEncoding = Charset.forName( fileEncoding );
        }

        public boolean isUseHeader() {
            return useHeader;
        }
        
        public Preferences setUseHeader( boolean useHeader ) {
            this.useHeader = useHeader;
            this.hasChanged = true;
            return this;
        }

        public CsvPreference setDelimiterChar( int delimiterChar ) {
            super.setDelimiterChar( delimiterChar );
            this.hasChanged = true;
            return this;
        }

        public CsvPreference setEndOfLineSymbols( String endOfLineSymbols ) {
            super.setEndOfLineSymbols( endOfLineSymbols );
            this.hasChanged = true;
            return this;
        }

        public CsvPreference setQuoteChar( char quoteChar ) {
            super.setQuoteChar( quoteChar );
            this.hasChanged = true;
            return this;
        }
        
    }

}

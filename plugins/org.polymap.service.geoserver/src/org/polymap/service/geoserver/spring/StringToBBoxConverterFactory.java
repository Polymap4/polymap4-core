/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.service.geoserver.spring;

import java.util.List;

import org.geoserver.ows.util.KvpUtils;
import org.geoserver.platform.ServiceException;
import org.geotools.factory.Hints;
import org.geotools.util.Converter;
import org.geotools.util.ConverterFactory;

import com.vividsolutions.jts.geom.Envelope;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StringToBBoxConverterFactory implements ConverterFactory {

    /** Delimeter for inner value lists in the KVPs */
    protected static final String INNER_DELIMETER = ",";    

    /* (non-Javadoc)
     * @see org.geotools.util.ConverterFactory#createConverter(java.lang.Class, java.lang.Class, org.geotools.factory.Hints)
     */
    @Override
    public Converter createConverter( Class<?> source, Class<?> target, Hints hints ) {
        if(String.class.isAssignableFrom( source ) && Envelope.class.isAssignableFrom( target )) {
            return new Converter() {
                public Object convert(Object source, Class target) throws Exception {
                    String bboxValue = String.valueOf(source);
                    return parseBbox(bboxValue);
                }
            };
        }
        return null;
    }

    // copied from https://github.com/geoserver/geoserver/blob/master/src/main/src/main/java/org/vfny/geoserver/util/requests/readers/KvpRequestReader.java#L361
    protected Envelope parseBbox(String bboxParam) throws ServiceException {
        Envelope bbox = null;
        Object[] bboxValues = readFlat(bboxParam, INNER_DELIMETER).toArray();

        if (bboxValues.length != 4) {
            throw new ServiceException(bboxParam + " is not a valid pair of coordinates",
                getClass().getName());
        }

        try {
            double minx = Double.parseDouble(bboxValues[0].toString());
            double miny = Double.parseDouble(bboxValues[1].toString());
            double maxx = Double.parseDouble(bboxValues[2].toString());
            double maxy = Double.parseDouble(bboxValues[3].toString());
            bbox = new Envelope(minx, maxx, miny, maxy);

            if (minx > maxx) {
                throw new ServiceException("illegal bbox, minX: " + minx + " is "
                    + "greater than maxX: " + maxx);
            }

            if (miny > maxy) {
                throw new ServiceException("illegal bbox, minY: " + miny + " is "
                    + "greater than maxY: " + maxy);
            }
        } catch (NumberFormatException ex) {
            throw new ServiceException(ex, "Illegal value for BBOX parameter: " + bboxParam,
                getClass().getName() + "::parseBbox()");
        }

        return bbox;
    }
    
    /**
     * Reads a tokenized string and turns it into a list. In this method, the
     * tokenizer is quite flexible. Note that if the list is unspecified (ie. is
     * null) or is unconstrained (ie. is ''), then the method returns an empty
     * list.
     *
     * @param rawList
     *            The tokenized string.
     * @param delimiter
     *            The delimeter for the string tokens.
     *
     * @return A list of the tokenized string.
     */
    protected static List readFlat(String rawList, String delimiter) {
        return KvpUtils.readFlat(rawList,delimiter);
    }    
}

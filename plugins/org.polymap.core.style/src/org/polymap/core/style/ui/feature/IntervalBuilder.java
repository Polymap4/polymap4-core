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
package org.polymap.core.style.ui.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Builds (linear) intervals between a given start and end, and maps values of
 * the given target value range.
 * 
 * <S> The source type.
 * <T> The target type.
 * @author Falko Bräutigam
 */
public class IntervalBuilder {

    private static final Log log = LogFactory.getLog( IntervalBuilder.class );
    
    public class Interval {
        public double   start, end;
        public double   value;
    }
    
    
    protected List<Interval> calculate( double start, double end, double mappedStart, double mappedEnd, int breakpoints ) {
        int intervals = breakpoints + 1;
        double dx = end - start;
        double dxi = dx / intervals;
        double dy = mappedEnd - mappedStart;
        //double dyi = dy / intervals;

        // anstieg: wert pro scale
        double q = dy / dx;
        
        List<Interval> result = new ArrayList( intervals );
        for (int i=0; i<intervals; i++) {
            Interval interval = new Interval();
            interval.start = start + (dxi * i);
            interval.end = start + (dxi * (i+1));
            double xi = (dxi * i) + (dxi / 2);
            double yi = xi * q;
            interval.value = mappedStart + yi;  // mean value of the interval
            result.add( interval );
        }
        return result;
    }
    
}

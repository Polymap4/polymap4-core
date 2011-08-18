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
package org.polymap.service.fs.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Range {

    private static Log log = LogFactory.getLog( Range.class );

    private final long          start;

    private final long          finish;


    public Range(long start, long finish) {
        this.start = start;
        this.finish = finish;
    }

    public long getStart() {
        return start;
    }

    public long getFinish() {
        return finish;
    }

    public String toString() {
        return "bytes " + start + "-" + finish;
    }

}

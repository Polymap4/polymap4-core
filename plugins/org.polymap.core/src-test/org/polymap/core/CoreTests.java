/* 
 * polymap.org
 * Copyright 2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.core;

import org.polymap.core.runtime.cache.test.ConcurrentMapTest;
import org.polymap.core.runtime.mp.test.MPTest;
import org.polymap.core.runtime.mp.test.SimpleTest;
import org.polymap.core.runtime.recordstore.test.LuceneRecordStoreTest;

import junit.framework.Test;
import junit.framework.TestSuite;


public class CoreTests {

    public static Test suite() {
        TestSuite suite = new TestSuite( "Test for org.polymap.core" );
        //$JUnit-BEGIN$
        suite.addTestSuite( MPTest.class );
        suite.addTestSuite( SimpleTest.class );
        suite.addTestSuite( ConcurrentMapTest.class );
        suite.addTestSuite( LuceneRecordStoreTest.class );
        //$JUnit-END$
        return suite;
    }
}

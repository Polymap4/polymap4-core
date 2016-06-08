/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.style.ui.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.polymap.core.style.model.UIOrder;
import org.polymap.core.style.ui.UIOrderComparator;

import org.polymap.model2.Composite;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * @author Steffen Stundzig
 */
@RunWith(MockitoJUnitRunner.class)
public class UIComparatorTest {

    @Mock
    private PropertyInfo<? extends Composite> o1;

    @Mock
    private UIOrder o1ui;

    @Mock
    private PropertyInfo<? extends Composite> o2;

    @Mock
    private UIOrder o2ui;


    @Test
    public void compareNulls() throws Exception {
        assertEquals(1, new UIOrderComparator().compare( null, o2 ), 0 );
        assertEquals(-1, new UIOrderComparator().compare( o1, null ), 0 );
        assertEquals(1, new UIOrderComparator().compare( o1, o2 ), 0 );
    }

    @Test
    public void compareOrders() throws Exception {
        when( o1.getAnnotation( UIOrder.class ) ).thenReturn( o1ui);
        when( o2.getAnnotation( UIOrder.class ) ).thenReturn( o2ui);
        when( o1ui.value() ).thenReturn( 10 );
        when( o1ui.value() ).thenReturn( 20 );
        
        assertEquals(1, new UIOrderComparator().compare( o1, o2 ), 0 );
    }

    @Test
    public void compareEqualOrders() throws Exception {
        when( o1.getAnnotation( UIOrder.class ) ).thenReturn( o1ui);
        when( o2.getAnnotation( UIOrder.class ) ).thenReturn( o2ui);
        when( o1ui.value() ).thenReturn( 10 );
        when( o1ui.value() ).thenReturn( 10 );
        
        assertEquals(1, new UIOrderComparator().compare( o1, o2 ), 0 );
    }
}

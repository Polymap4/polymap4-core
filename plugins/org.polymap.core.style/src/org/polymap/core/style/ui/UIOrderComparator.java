/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.ui;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.UIOrder;

import org.polymap.model2.Composite;
import org.polymap.model2.runtime.PropertyInfo;

/**
 * Compare property infos by UIOrder annotation. Infos without an annotation are
 * sorted to the end.
 * 
 *
 * @author Steffen Stundzig
 */
public class UIOrderComparator
        implements Comparator<PropertyInfo<? extends Composite>> {

    private static Log log = LogFactory.getLog( UIOrderComparator.class );


    @Override
    public int compare( PropertyInfo<? extends Composite> o1, PropertyInfo<? extends Composite> o2 ) {
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        UIOrder o1Order = o1.getAnnotation( UIOrder.class );
        UIOrder o2Order = o2.getAnnotation( UIOrder.class );
        if (o1Order == null) {
            return 1;
        }
        if (o2Order == null) {
            return -1;
        }
        // never return 0
        if (o1Order.value() == o2Order.value()) {
            return 1;
        }
        if (o1Order.value() < o2Order.value()) {
            return -1;
        }
        return 1;
    }
}

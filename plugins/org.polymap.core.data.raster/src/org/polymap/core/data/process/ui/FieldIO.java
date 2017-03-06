/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.core.data.process.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.process.FieldInfo;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class FieldIO {
    
    private static final Log log = LogFactory.getLog( FieldIO.class );

    // XXX make this extensible
    public static final List<Class<? extends FieldIO>> ALL = Lists.newArrayList(
        BooleanSupplier.class,
        NumberSupplier.class,
        StringSupplier.class
    );

    
    public static FieldIO[] forField( FieldViewerSite site ) {
        List<FieldIO> result = new ArrayList();
        for (Class<? extends FieldIO> cl : ALL) {
            try {
                FieldIO editor = cl.newInstance();
                if (editor.init( site )) {
                    result.add( editor );
                }
            }
            catch (Exception e) {
                log.warn( "", e);
                //throw new RuntimeException( e );
            }
        }
        if (result.isEmpty()) {
            NotSupportedSupplier notSupported = new NotSupportedSupplier();
            notSupported.init( site );
            result.add( notSupported );
        }
        return result.toArray( new FieldIO[result.size()] );
    }
    

    // instance *******************************************
    
    protected FieldViewerSite       site;
    
    
    /**
     * The human readable label/title of this field IO. This is used in a combo box
     * in the UI.
     */
    public abstract String label();

    /**
     * Checks if this field can handle the given {@link FieldInfo#type}.
     *
     * @return True if this supplier can handle the {@link FieldInfo field} in the
     *         given site.
     */
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        this.site = site;
        return true;
    }

    /**
     * Creates the UI of this field. 
     */
    public abstract void createContents( Composite parent );
    
}

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
package org.polymap.core.project.ui.properties;

import java.text.NumberFormat;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.polymap.core.runtime.Polymap;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EnvelopPropertySource
        implements IPropertySource {

    private static Log log = LogFactory.getLog( EnvelopPropertySource.class );

    public static NumberFormat      nf;
    
    static {
        nf = NumberFormat.getInstance( Polymap.getSessionLocale() );
        nf.setMaximumFractionDigits( 6 );
    }

    // instance *******************************************
    
    private ReferencedEnvelope      envelop;
    
    private boolean                 editable;

    private double                  minx, miny, maxx, maxy;

    
    public EnvelopPropertySource( ReferencedEnvelope envelop ) {
        this.envelop = envelop;
        
    }

    public boolean isEditable() {
        return editable;
    }
    
    public EnvelopPropertySource setEditable( boolean editable ) {
        this.editable = editable;
        return this;
    }


    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new NumberPropertyDescriptor( "minx", "MinX" ).setFormat( nf ).setEditable( editable ), 
                new NumberPropertyDescriptor( "miny", "MinY" ).setFormat( nf ).setEditable( editable ), 
                new NumberPropertyDescriptor( "maxx", "MaxX" ).setFormat( nf ).setEditable( editable ), 
                new NumberPropertyDescriptor( "maxy", "MaxY" ).setFormat( nf ).setEditable( editable ) };
    }


    public Object getEditableValue() {
        return nf.format( envelop.getMinX() ) + " : " + nf.format( envelop.getMaxX() ) + " - "
                + nf.format( envelop.getMinY() ) + " : " + nf.format( envelop.getMaxY() );
    }


    public Object getPropertyValue( Object id ) {
        if (id.equals( "minx" )) {
            return minx = envelop.getMinX();
        }
        else if (id.equals( "miny" )) {
            return miny = envelop.getMinY();
        }
        else if (id.equals( "maxx" )) {
            return maxx = envelop.getMaxX();
        }
        else if (id.equals( "maxy" )) {
            return maxy = envelop.getMaxY();
        }
        return null;
    }


    public void setPropertyValue( Object id, Object value ) {
        log.info( "id=" + id + ", value=" + value );
        if (id.equals( "minx" )) {
            minx = ((Number)value).doubleValue();
        }
        else if (id.equals( "miny" )) {
            miny = ((Number)value).doubleValue();
        }
        else if (id.equals( "maxx" )) {
            maxx = ((Number)value).doubleValue();
        }
        else if (id.equals( "maxy" )) {
            maxy = ((Number)value).doubleValue();
        }
        envelop.init( minx, maxx, miny, maxy );
    }


    public boolean isPropertySet( Object id ) {
        return false;
    }


    public void resetPropertyValue( Object id ) {
        throw new RuntimeException( "not yet implemented." );
    }
}

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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

    
    // instance *******************************************
    
    private ReferencedEnvelope      envelop;
    
    private double                  minx, miny, maxx, maxy;

    protected final NumberFormat    nf;

    /** True if editable. */
    private PropertyChangeListener  listener;
    

    public EnvelopPropertySource( ReferencedEnvelope envelop ) {
        this.envelop = envelop;
        
        nf = NumberFormat.getInstance( Polymap.getSessionLocale() );
        nf.setMaximumFractionDigits( 6 );
    }

    public boolean isEditable() {
        return listener != null;
    }
    
    public EnvelopPropertySource setEditable( PropertyChangeListener listener ) {
        this.listener = listener;
        return this;
    }


    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new NumberPropertyDescriptor( "minx", "MinX" ).setFormat( nf ).setEditable( isEditable() ), 
                new NumberPropertyDescriptor( "miny", "MinY" ).setFormat( nf ).setEditable( isEditable() ), 
                new NumberPropertyDescriptor( "maxx", "MaxX" ).setFormat( nf ).setEditable( isEditable() ), 
                new NumberPropertyDescriptor( "maxy", "MaxY" ).setFormat( nf ).setEditable( isEditable() ) };
    }


    public Object getEditableValue() {
        if (envelop != null) {
            return nf.format( envelop.getMinX() ) + " : " + nf.format( envelop.getMaxX() ) + " - "
                    + nf.format( envelop.getMinY() ) + " : " + nf.format( envelop.getMaxY() );
        }
        else {
            return "-";
        }
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
        
        if (listener != null) {
            listener.propertyChange( new PropertyChangeEvent( this, "envelop", null, envelop ) );
        }
    }


    public boolean isPropertySet( Object id ) {
        return false;
    }


    public void resetPropertyValue( Object id ) {
        throw new RuntimeException( "not yet implemented." );
    }
}

/* 
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * $Id$
 */

package org.polymap.core.model.plain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.polymap.core.model.MFeature;
import org.polymap.core.model.MObject;
import org.polymap.core.model.MObjectClass;
import org.polymap.core.model.ModelRuntimeException;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class PlainMObjectClass
        extends MObjectClass {
    
    private Class           cl;

    
    public PlainMObjectClass( Class cl ) {
        this.cl = cl;
    }


    /**
     * Get the names of all features of the given {@link MObject} class.
     * 
     * @return Empty list if there are no features found.
     */
    public List<MFeature> getFeatures() {
        List<MFeature> result = new ArrayList();
        Field[] fields = cl.getDeclaredFields();
        for (Field field : fields) {
            MFeature feature = checkField( field.getName() );
            if (feature != null) {
                result.add( feature );
            }
        }
        return Collections.unmodifiableList( result );
    }
    
    
    /**
     * Returns the feature for the given name.
     * 
     * @param featureName
     * @params Signals that the method should throw an exception instead of returning null.
     * @throws ModelRuntimeException If no such feature exists and failFast is set.
     */
    public MFeature getFeature( String featureName, boolean failFast ) {
        MFeature result = checkField( featureName );
        if (result == null && failFast) {
            throw new ModelRuntimeException( "No such feature: " + featureName );
        }
        return result;
    }


    private MFeature checkField( String fieldName ) {
        try {
            Field field = cl.getDeclaredField( fieldName );
            if (Modifier.isStatic( field.getModifiers() )) {
                if (MFeature.class.isAssignableFrom( field.getType() ))  {
                    return (MFeature)field.get( null );
                }
            }
            return null;
        }
        catch (SecurityException e) {
            throw e;
        }
        catch (NoSuchFieldException e) {
            return null;
            //throw new ModelRuntimeException( "No such feature: " + fieldName );
        }
        catch (Exception e) {
            throw new ModelRuntimeException( e + " (feature: " + fieldName + ")" );
        }
    }
    
}

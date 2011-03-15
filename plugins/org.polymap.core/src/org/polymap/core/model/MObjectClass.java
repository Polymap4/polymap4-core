/*
 * polymap.org Copyright 2009, Polymap GmbH, and individual contributors as
 * indicated by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 * 
 * $Id$
 */

package org.polymap.core.model;

import java.util.List;

/**
 * Provides access to the features of a model object class.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class MObjectClass {

    /**
     * Get the names of all features of the given {@link MObject} class.
     * 
     * @return Empty list if there are no features found.
     */
    public abstract List<MFeature> getFeatures();


    /**
     * Returns the feature for the given name.
     * 
     * @param featureName
     * @params Signals that the method should throw an exception instead of returning null.
     * @throws ModelRuntimeException If no such feature exists and failFast is set.
     */
    public abstract MFeature getFeature( String featureName, boolean failFast );
    
 
    public MAttribute getAttribute( String attrName, boolean failFast ) {
        MFeature feature = getFeature( attrName, failFast );
        if (feature instanceof MAttribute) {
            return (MAttribute)feature;
        }
        else {
            if (failFast) {
                throw new ModelRuntimeException( "No such attribute: " + attrName );
            } else {
                return null;
            }
        }
    }

    public MReference getReference( String refName, boolean failFast ) {
        MFeature feature = getFeature( refName, failFast );
        if (feature instanceof MReference) {
            return (MReference)feature;
        }
        else {
            if (failFast) {
                throw new ModelRuntimeException( "No such reference: " + refName );
            } else {
                return null;
            }
        }
    }

}
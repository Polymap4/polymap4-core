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

package org.polymap.core.model;

/**
 * Provides an abstract base implementation of {@link MObject}. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public abstract class MObjectImpl
        implements MObject {

    private MId             id;
    
    private MDomain         domain;

    
    public void attach( MDomain _domain, MId _id ) {
        this.domain = _domain;
        this.id = _id;
    }

    public void detach() {
        this.domain = null;
    }
    
    
    public MId getId() {
        return id;
    }

    
    public MDomain getDomain() {
        return domain;
    }

    
    public MObjectClass getObjectClass() {
        return getDomain().getObjectClass( getClass() );
    }

    
    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof MObject) {
            MId rhsId = ((MObject)obj).getId();
            return (id != null && rhsId != null && id.equals( rhsId) );
        }
        else {
            return false;
        }
    }


    public int hashCode() {
        return id.hashCode();
    }

    
    protected void fireChangeEvent( String featureName, Object oldValue, Object newValue ) {
        getDomain().fireChangeEvent( this, featureName, oldValue, newValue );
    }
    
}

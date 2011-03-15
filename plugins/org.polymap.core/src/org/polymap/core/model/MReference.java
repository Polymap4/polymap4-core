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
 * A relation to exactly one {@link MObject}.
 * <p>
 * This is used as a static member of {@link MObject} implementing classes.
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> <li>02.11.2009:
 *         created</li>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class MReference<ONE>
        extends MFeature {
    
    private boolean             isLeft;
    

    public MReference( String featureName, boolean isLeft ) {
        super( featureName );
        this.isLeft = isLeft;
    }


    public ONE get( MObject obj ) {
        return (ONE)obj.getDomain().getRelatedObject( obj, featureName, isLeft );
    }
    

    public void set( MObject obj1, ONE obj2 ) {
        obj1.getDomain().createRelation( obj1, (MObject)obj2, featureName, isLeft );
    }

    
    public void remove( MObject obj1, ONE obj2 ) {
        obj1.getDomain().removeRelation( obj1, (MObject)obj2, featureName, isLeft );
    }

    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MReference)) {
            return false;
        }
        MReference rhs = (MReference)obj;
        return featureName.equals( rhs.featureName ) &&
                isLeft != rhs.isLeft;
    }

}

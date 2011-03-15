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

package org.polymap.core.model.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.polymap.core.model.MObject;
import org.polymap.core.model.MObjectClass;
import org.polymap.core.model.MReferences;
import org.polymap.core.model.MSerializerContext;

/**
 * The XML/JAXP representation of a model domain.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@XmlType
@XmlRootElement
class XmlDomain {
    
    @XmlElement
    List<XmlObject>                 objects = new ArrayList();      

    
    public XmlDomain() {
        super();
    }


    /**
     * Marshall. 
     */
    public XmlDomain( MSerializerContext context ) {
        // objects / attributes
        for (MObject obj : context.getDomain().objects()) {
            XmlObject xmlObj = new XmlObject( obj );
            objects.add( xmlObj );
            
            String objName = context.getObjectName( obj );
            if (objName != null) {
                xmlObj.objName = objName;
            }
        }
    }


    /**
     * Unmarshall.
     * @throws ClassNotFoundException 
     */
    public void load( MSerializerContext context ) 
            throws ClassNotFoundException {
        
        // load objects / attributes
        for (XmlObject xmlObj : objects) {
            MObject obj = xmlObj.load( context );
            
            if (xmlObj.objName != null) {
                context.setObjectName( obj, xmlObj.objName );
            }
        }
        
        // init references
        for (XmlObject xmlObj : objects) {
            for (XmlRef xmlRef : xmlObj.references) {
                MObject obj = context.getObject( xmlObj.id );
                MObjectClass ocl = obj.getObjectClass();
                MReferences ref = (MReferences)ocl.getFeature( xmlRef.name, false );
                if (ref != null) {
                    ref.add( obj, context.getObject( xmlRef.refId ) );
                }
            }
        }

    }
    
}

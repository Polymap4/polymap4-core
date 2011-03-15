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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import org.polymap.core.model.MAttribute;
import org.polymap.core.model.MFeature;
import org.polymap.core.model.MObject;
import org.polymap.core.model.MObjectClass;
import org.polymap.core.model.MReference;
import org.polymap.core.model.MReferences;
import org.polymap.core.model.MSerializerContext;


/**
 * The XML/JAXP representation of a domain model object.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
@XmlType
class XmlObject {
    
    @XmlAttribute(required = true)
    String                  classname;

    @XmlID
    String                  id;

    @XmlAttribute
    String                  objName;

    //@XmlElementWrapper(name="attrs")
    @XmlElement
    List<XmlAttr>           attributes = new ArrayList();

    @XmlElement
    List<XmlRef>            references = new ArrayList();

    
    public XmlObject() {
        super();
    }


    /**
     * Marshall.
     */
    public XmlObject( MObject obj ) {
        this.classname = obj.getClass().getName();
        this.id = obj.getId().toString();
        
        MObjectClass ocl = obj.getObjectClass();
        
        for (MFeature feature : ocl.getFeatures()) {
            // attribute
            if (feature instanceof MAttribute) {
                MAttribute attr = (MAttribute)feature;
                this.attributes.add( new XmlAttr( attr.getName(), attr.getValue( obj ) ) );
            }
            // references
            else if (feature instanceof MReferences) {
                MReferences refs = (MReferences)feature;
                for (Object ref : refs.get( obj )) {
                    this.references.add( new XmlRef( refs.getName(), (MObject)ref ) );
                }
            }
            else if (feature instanceof MReference) {
                //
            }
            else {
                throw new IllegalStateException( "Unhandled feature type: " + feature );
            }
        }
    }

    
    /**
     * Unmarshall.
     * @throws ClassNotFoundException 
     */
    public MObject load( MSerializerContext context ) 
            throws ClassNotFoundException {
        MObject obj = context.createObject( classname, id );        
        MObjectClass ocl = obj.getObjectClass();
        
        for (XmlAttr xmlAttr : attributes) {
            MAttribute attr = ocl.getAttribute( xmlAttr.name, false );
            if (attr != null) {
                attr.set( obj, xmlAttr.value.equals( XmlAttr.VALUE_NULL ) ? null : xmlAttr.value );
            }
        }
        return obj;
    }    
    
}

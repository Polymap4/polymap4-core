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

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.polymap.core.model.MDomain;
import org.polymap.core.model.MSerializerContext;
import org.polymap.core.model.ModelRuntimeException;

/**
 * XML model serializer based on JAXB. 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public class XmlSerializer {

    private MSerializerContext  context;
    
    private MDomain             domain;

    
    public XmlSerializer( MSerializerContext context ) {
        if (context == null) {
            throw new IllegalArgumentException( "context == null" );
        }
        this.context = context;
        this.domain = context.getDomain();
    }
    
    
    public void store( OutputStream out )
            throws JAXBException {
        XmlDomain xmlDomain = new XmlDomain( context );
        JAXBContext jc = JAXBContext.newInstance( XmlDomain.class );

        Marshaller m = jc.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.marshal( xmlDomain, out );
    }
    

    public void load( InputStream in )
            throws JAXBException, ClassNotFoundException, ModelRuntimeException {
        JAXBContext jc = JAXBContext.newInstance( XmlDomain.class );

        Unmarshaller um = jc.createUnmarshaller();
        XmlDomain xmlDomain  = (XmlDomain)um.unmarshal( in );
        xmlDomain.load( context );
    }
    
}

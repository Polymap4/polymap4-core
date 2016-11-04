/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.core.runtime.text;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.config.Configurable;

/**
 * Work in progress!
 * 
 * @author Falko Bräutigam
 */
public class XmlBuilder
        extends Configurable {

    private static final Log log = LogFactory.getLog( XmlBuilder.class );
    
    public static final String              DEFAULT_ENCODING = "UTF-8";

    public static final DateFormat          DF = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
    
    @FunctionalInterface
    public interface SubtreeBuilder {
        public void handle() throws Exception;
    }


    // instance *******************************************

    private XMLStreamWriter             writer;
    
    private Lazy<JAXBContext>           jaxbContext;

    
    public XmlBuilder() throws XMLStreamException, FactoryConfigurationError {
        this( DEFAULT_ENCODING );
    }

    
    public XmlBuilder( String encoding ) throws XMLStreamException, FactoryConfigurationError {
        ByteArrayOutputStream buf = new ByteArrayOutputStream( 4096 );
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter( buf, encoding );
        //writer = new IndentingXMLStreamWriter( writer );
    }
    
    
    public XmlBuilder( XMLStreamWriter writer ) {
        this.writer = writer;
    }
    
    
    public void setJaxbContextPath( String jaxbContextPath ) {
        assert jaxbContext == null;
        jaxbContext = new CachedLazyInit( () -> {
            try {
                return JAXBContext.newInstance( jaxbContextPath/*, CswRequest.class.getClassLoader()*/ );
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        });
    }
    
    
    protected XMLStreamWriter out() {
        assert writer != null;
        return writer;        
    }
    
    
    protected void writeElement( String uri, String name, SubtreeBuilder hierarchyHandler ) throws Exception {
        out().writeStartElement( uri, name );
        hierarchyHandler.handle();
        out().writeEndElement();
    }
    
    
//    protected void writeElement( Config prop, SubtreeBuilder hierarchyHandler ) throws Exception {
//        RequestElement a = prop.info().getAnnotation( RequestElement.class );
//        assert a != null : "No @RequestElement annotation on Config property: " + prop;
//        out().writeStartElement( a.prefix(), a.value() );
//        out().writeCharacters( prop.get().toString() );
//        hierarchyHandler.handle();
//        out().writeEndElement();
//    }
//    
//    
//    protected void writeAttributes( Config... props ) throws XMLStreamException {
//        for (Config prop : props) {
//            RequestAttr a = prop.info().getAnnotation( RequestAttr.class );
//            assert a != null : "No @RequestAttr annotation on Config property: " + prop;
//            out().writeAttribute( a.value(), prop.get().toString() );
//        }
//    }
    
    
    protected void writeObject( JAXBElement jaxb ) throws Exception {
        Marshaller marshaller = jaxbContext.get().createMarshaller();
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        marshaller.setProperty( Marshaller.JAXB_FRAGMENT, Boolean.TRUE );
        
        marshaller.marshal( jaxb, out() );
    }
    
}

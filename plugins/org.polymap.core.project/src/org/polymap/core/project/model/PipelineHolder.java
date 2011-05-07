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

package org.polymap.core.project.model;

import java.util.HashSet;
import java.util.Set;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.spi.util.Base64Encoder;

import org.polymap.core.project.PipelineProcessorConfiguration;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @version POLYMAP3 ($Revision$)
 * @since 3.0
 */
public interface PipelineHolder
        extends org.polymap.core.project.PipelineHolder {

    /**
     * Serialized {@link PipelineProcessorConfiguration}.
     */
    @Optional
    @UseDefaults
    Property<String>            processors();
    

    /**
     * The mixin.
     */
    public abstract static class Mixin
            implements PipelineHolder {

        private static Log log = LogFactory.getLog( PipelineHolder.class );

        /** Cache of the deserialized data. */
        Set<PipelineProcessorConfiguration> _processors;
        
    
        public synchronized PipelineProcessorConfiguration[] getProcessorConfigs() {
            if (_processors == null) {
                _processors = new HashSet();
                
                String serialized = processors().get();
                
                if (serialized != null && serialized.length()>0) {
                    try {
                        byte[] bytes = serialized.getBytes( "UTF-8" );
                        bytes = Base64Encoder.decode( bytes );
                        ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
                        ObjectInputStream oin = new ObjectInputStream( bin );

                        int count = oin.readInt();
                        for (int i=0; i<count; i++) {
                            PipelineProcessorConfiguration proc = (PipelineProcessorConfiguration)oin.readObject();
                            _processors.add( proc );
                        }
                        oin.close();
                    }
                    catch (Exception e) {
                        log.error( e.getLocalizedMessage(), e );
                    }
                }
            }
            return (PipelineProcessorConfiguration[]) _processors.toArray( new PipelineProcessorConfiguration[_processors.size()]);
        }

        
        public synchronized void setProcessorConfigs( PipelineProcessorConfiguration[] procs ) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream( bout );
                
                out.writeInt( procs.length );

                _processors.clear();
                for (PipelineProcessorConfiguration proc : procs) {
                    out.writeUnshared( proc );
                    _processors.add( proc );
                }
                out.close();
                byte[] bytes = Base64Encoder.encode( bout.toByteArray(), true );
                String stringValue = new String( bytes, "UTF-8" );
                
                processors().set( stringValue );
            }
            catch (Exception e) {
                log.error( e.getMessage(), e );
            }
        }
        
    }
    
//  public void addProcessor( PipelineProcessorConfiguration processor ) {
//  if (!processors().get().add( processor )) {
//      throw new IllegalArgumentException( "Processor was already added: " + processor );
//  }
//}
//
//public void removeProcessor( PipelineProcessorConfiguration processor ) {
//  if (!processors().get().remove( processor )) {
//      throw new IllegalArgumentException( "Processor not found: " + processor );
//  }
//}

}

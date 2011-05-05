/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 */

package org.polymap.core.qi4j.idgen;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

/**
 * This service tried to generate human readable identities. The ids
 * are build from the composite name and creation time. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Mixins(HRIdentityGeneratorService.Mixin.class)
public interface HRIdentityGeneratorService
        extends IdentityGenerator, ServiceComposite {

    public class Mixin
            implements IdentityGenerator {

        private static final Log log = LogFactory.getLog( HRIdentityGeneratorService.class );

        private static final FastDateFormat df = FastDateFormat.getInstance("yyyyMMdd-HHmm");
        
        private String                      lastDateFormat;
        
        private int                         count;
        
        
        public Mixin() {
        }


        public synchronized String generate( Class<? extends Identity> compositeType ) {
            StringBuffer result = new StringBuffer( 128 );
            
            result.append( compositeType.getSimpleName() );
            
            result.append( "-" );
            
            String dateFormat = df.format( System.currentTimeMillis() );
            count = dateFormat.equals( lastDateFormat ) ? count+1 : 0;
            lastDateFormat = dateFormat;
            result.append( dateFormat );

            result.append( "-" );
            result.append( count );
            log.debug( "generated ID: " + result.toString() );
            return result.toString();
        }

    }

}

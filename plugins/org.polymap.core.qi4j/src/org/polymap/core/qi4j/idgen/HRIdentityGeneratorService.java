/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
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
 * $Id: $
 */

package org.polymap.core.qi4j.idgen;

import java.util.Date;
import java.util.Random;

import java.text.NumberFormat;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This service tried to generate human readable identities. The ids
 * are build from the composite name and creation time. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
@Mixins(HRIdentityGeneratorService.Mixin.class)
public interface HRIdentityGeneratorService
        extends IdentityGenerator, ServiceComposite {

    public class Mixin
            implements IdentityGenerator {

        private static final Log log = LogFactory.getLog( HRIdentityGeneratorService.class );

        private static NumberFormat nf;

        private Random  random = new Random();
        
        
        static {
            nf = NumberFormat.getIntegerInstance();
            nf.setMaximumIntegerDigits( 2 );
            nf.setMinimumIntegerDigits( 2 );
        }
        
        public Mixin() {
        }


        public String generate( Class<? extends Identity> compositeType ) {
            Date now = new Date();
            StringBuffer result = new StringBuffer( 128 );
            result.append( compositeType.getSimpleName() );
            result.append( "_" );
            result.append( now.getYear()+1900 )
                    .append( nf.format( now.getMonth() ) )
                    .append( nf.format( now.getDay() ) );
            result.append( "_" );
            result.append( nf.format( now.getHours() ) )
                    .append( nf.format( now.getMinutes() ) )
                    .append( now.getSeconds() );
            result.append( "_" );
            result.append( random.nextInt( 999 ) );
            log.info( "generated ID: " + result.toString() );
            return result.toString();
        }

    }

}

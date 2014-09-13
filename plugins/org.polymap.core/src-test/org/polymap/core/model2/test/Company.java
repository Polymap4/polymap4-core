/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.model2.test;

import javax.annotation.Nullable;

import org.polymap.core.model2.Association;
import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.Concerns;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.MaxOccurs;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.store.feature.SRS;

/**
 * A complex entity.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SRS( "EPSG:4326" )
public class Company
        extends Entity {

    protected Property<String>              name;
    
    @Nullable
    protected Association<Employee>         chief;

    @Nullable
    protected Property<Address>             address;
    
    protected CollectionProperty<Address>   moreAddresses;
    
    @MaxOccurs(100)
    @Concerns( {LogConcern.class} )
    protected CollectionProperty<String>    docs;

    
//    @MaxOccurs(100)
//    protected CollectionProperty<Employee>  employees;
    

//    public void addEmployee( Employee employee ) {
//        methodProlog( "addEmployee", employee );
//        employees.add( employee );
//        //employee.company().s
//    }
    
}

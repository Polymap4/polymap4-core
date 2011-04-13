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
package org.polymap.rhei.calculator.spi;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.calculator.ICalculator;

/**
 * Provides a default implementation of a calculator. This can be used by
 * classes that implement {@link ICalculatorProvider}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version POLYMAP3
 * @since 3.0
 */
public abstract class DefaultCalculator
        implements ICalculator {

    private static Log log = LogFactory.getLog( DefaultCalculator.class );

    protected String                code;
    
    protected Map<String,Object>    params = new HashMap();
    
    
    public DefaultCalculator( String code ) {
        this.code = code;
    }

    
    public String getCode() {
        return code;
    }


    public void setParam( String name, JSONObject value ) {
        params.put( name, value );
    }

    
    public void setParam( String name, Object value ) {
        params.put( name, value );
    }


    public Map<String,Object> getParams() {
        return params;
    }
    
    
    public JSONObject getJsonResult( String key ) {
        return (JSONObject)getResult( key );
    }

}

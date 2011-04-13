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
package org.polymap.rhei.calculator;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.calculator.spi.BeanShellCalculatorProvider;
import org.polymap.rhei.calculator.spi.ICalculatorProvider;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version POLYMAP3
 * @since 3.0
 */
public class CalculatorSupport {

    private static Log log = LogFactory.getLog( CalculatorSupport.class );

    // static factory *************************************

    private static CalculatorSupport        instance;
    
    /**
     *
     */
    public static CalculatorSupport instance() {
        if (instance == null) {
            instance = new CalculatorSupport();
        }
        return instance;
    }
    
    
    // instance *******************************************
    
    private Map<String,ICalculatorProvider> providers = new HashMap();
    
    
    protected CalculatorSupport() {
        // XXX make this extendible
        BeanShellCalculatorProvider provider = new BeanShellCalculatorProvider();
        providers.put( provider.getId(), provider );
    }
    
    
    public ICalculator newCalculator( String code, String lang ) {
        ICalculatorProvider provider = providers.get( lang );
        if (provider != null) {
            return provider.newCalculator( code );
        }
        else {
            return null;
        }
    }
    
}

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

import java.util.Map;

import java.io.PrintStream;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;

import org.polymap.rhei.calculator.ICalculator;
import org.polymap.rhei.calculator.ParseException;
import org.polymap.rhei.calculator.TargetException;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version POLYMAP3
 * @since 3.0
 */
public class BeanShellCalculatorProvider
        implements ICalculatorProvider {

    private static Log log = LogFactory.getLog( BeanShellCalculatorProvider.class );


    public String getId() {
        return "java";
    }


    public ICalculator newCalculator( String code ) {
        return new BeanShellCalculator( code );
    }


    public void eval( ICalculator calculator )
    throws ParseException, TargetException {
        
        BeanShellCalculator bsc = (BeanShellCalculator)calculator;
        
        if (bsc.interpreter == null) {
            bsc.interpreter = new Interpreter( bsc );
        }
        
        try {
            // out/err stream
            bsc.interpreter.setConsole( bsc );
            
            // params
            for (Map.Entry<String,Object> entry : bsc.getParams().entrySet()) {
                log.debug( "   param: " + entry.getKey() + " = " + entry.getValue() );
                bsc.interpreter.set( entry.getKey(), entry.getValue() );
            }
            bsc.interpreter.set( "out", bsc.getOut() );
            bsc.interpreter.set( "err", bsc.getErr() );
            bsc.interpreter.setShowResults( true );
            
            // eval
            bsc.interpreter.eval( bsc.getCode() );
            
            bsc.interpreter.getOut().flush();
            bsc.interpreter.getErr().flush();
        }
        catch (EvalError e) {
            // XXX add more info to the exceptions
            if (e instanceof bsh.ParseException) {
                throw new ParseException( bsc, e );
            }
            else if (e instanceof bsh.TargetError) {
                throw new TargetException( bsc, e );
            }
            else {
                throw new ParseException( bsc, e );
            }
        }
    }
    

    /**
     * 
     */
    class BeanShellCalculator
            extends DefaultCalculator
            implements ConsoleInterface {

        private Interpreter         interpreter;

        private PrintStream         out = System.out;
        
        private PrintStream         err = System.err;
        
        
        public BeanShellCalculator( String code ) {
            super( code );
        }

        public void setOut( PrintStream out ) {
            this.out = out;
        }
        
        public void setErr( PrintStream err ) {
            this.err = err;
        }
        
        public PrintStream getOut() {
            return out;
        }
        
        public PrintStream getErr() {
            return err;
        }

        public void eval() 
        throws ParseException, TargetException {
            BeanShellCalculatorProvider.this.eval( this );
        }

        public Object getResult( String name ) {
            try {
                return interpreter.get( name );
            }
            catch (EvalError e) {
                throw new RuntimeException( e );
            }
        }

        public void error( Object arg0 ) {
            err.print( arg0 );
        }

        public Reader getIn() {
            return null;
        }

        public void print( Object arg0 ) {
            out.print( arg0 );
        }

        public void println( Object arg0 ) {
            out.println( arg0 );
        }
        
    }

}

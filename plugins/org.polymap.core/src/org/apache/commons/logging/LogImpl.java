/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.apache.commons.logging;

import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.rightPad;

/**
 * See {@link LogFactory}.
 * 
 * @see LogFactory
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LogImpl
        implements Log {

    protected static final int      MAX_NAME_WIDTH = 23;
    
    protected enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }

    // instance *******************************************
    
    private String      name;
    
    private Level       currentLevel;

    
    protected LogImpl( String name, Level currentLevel ) {
        super();
        this.name = name;
        this.currentLevel = currentLevel;
    }

    public void setLevel( Level currentLevel ) {
        this.currentLevel = currentLevel;
    }

    public Level getLevel() {
        return currentLevel;
    }

    @Override
    public final void info( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.INFO )) {
            log( Level.INFO, msg, e );
        }
    }
    
    @Override
    public final void info( Object msg ) {
        if (isLevelEnabled( Level.INFO )) {
            log( Level.INFO, msg );
        }
    }
    
    @Override
    public final void warn( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.WARN )) {
            log( Level.WARN, msg, e );
        }
    }
    
    @Override
    public final void warn( Object msg ) {
        if (isLevelEnabled( Level.WARN )) {
            log( Level.WARN, msg );
        }
    }
    
    @Override
    public final void debug( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.DEBUG )) {
            log( Level.DEBUG, msg, e );
        }
    }
    
    @Override
    public final void debug( Object msg ) {
        if (isLevelEnabled( Level.DEBUG )) {
            log( Level.DEBUG, msg );
        }
    }
    
    @Override
    public final void trace( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.TRACE )) {
            log( Level.TRACE, msg, e );
        }
    }
    
    @Override
    public final void trace( Object msg ) {
        if (isLevelEnabled( Level.TRACE )) {
            log( Level.TRACE, msg );
        }
    }
    
    @Override
    public final void error( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.ERROR )) {
            log( Level.ERROR, msg, e );
        }
    }
    
    @Override
    public final void error( Object msg ) {
        if (isLevelEnabled( Level.ERROR )) {
            log( Level.ERROR, msg );
        }
    }
    
    @Override
    public final void fatal( Object msg, Throwable e ) {
        if (isLevelEnabled( Level.FATAL )) {
            log( Level.FATAL, msg, e );
        }
    }
    
    @Override
    public final void fatal( Object msg ) {
        if (isLevelEnabled( Level.FATAL )) {
            log( Level.FATAL, msg );
        }
    }
    
    protected void log( Level level, Object msg, Throwable... e ) {
        if (isLevelEnabled( level )) {
            StringBuilder buf = new StringBuilder( 128 );
            buf.append( "[" ).append( level ).append( "]" ).append( rightPad( "", 5 - level.toString().length() ) );
            buf.append( " " ).append( abbreviate( rightPad( name, MAX_NAME_WIDTH ), MAX_NAME_WIDTH ) ).append( ":" );
            buf.append( " " ).append(  msg.toString() );
            
            if (level.ordinal() >= Level.WARN.ordinal()) {
                System.err.println( buf.toString() );
            }
            else {
                System.out.println( buf.toString() );                
            }
            
            for (int i=0; i<e.length; i++) {
                e[i].printStackTrace( System.err );
            }
        }
    }

    /**
     * Are debug messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    public final boolean isDebugEnabled() {
        return isLevelEnabled( Level.DEBUG );
    }

    /**
     * Are error messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    public final boolean isErrorEnabled() {
        return isLevelEnabled( Level.ERROR );
    }

    /**
     * Are fatal messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    public final boolean isFatalEnabled() {
        return isLevelEnabled( Level.FATAL );
    }

    /**
     * Are info messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    public final boolean isInfoEnabled() {
        return isLevelEnabled( Level.INFO );
    }

    /**
     * Are trace messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    public final boolean isTraceEnabled() {
        return isLevelEnabled( Level.TRACE );
    }

    /**
     * Are warn messages currently enabled?
     * <p>
     * This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    public final boolean isWarnEnabled() {
        return isLevelEnabled( Level.WARN );
    }
    
    protected boolean isLevelEnabled( Level level ) {
        return currentLevel.ordinal() <= level.ordinal();
    }
    
}

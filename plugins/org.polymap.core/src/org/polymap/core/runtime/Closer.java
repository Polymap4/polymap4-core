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
package org.polymap.core.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides chained, automatic and quietly closing of {@link AutoCloseable}s.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Closer {

    private static Log log = LogFactory.getLog( Closer.class );
    
    public static Closer with( AutoCloseable... closeables ) {
        return new Closer( closeables );
    }

    public static Closer create() {
        return new Closer( new AutoCloseable[] {} );
    }

    
    // instance *******************************************
    
    private List<AutoCloseable>     closeables = new ArrayList(); 
    private List<Throwable>         exceptions = new ArrayList();
    
    
    protected Closer( AutoCloseable[] closeables ) {
        this.closeables.addAll( Arrays.asList( closeables ) );
    }


    /**
     * Runs the given Callable and {@link #close(AutoCloseable) close} all closeables
     * specified by {@link #with(AutoCloseable...)}. All exceptions thrown during
     * executing the given code or when closing are not rethrown but catched and
     * collected internally.
     *
     * @param exe The code to execute.
     * @return this.
     */
    public Closer runAndClose( Callable exe ) {
        try {
            exe.call();
        }
        catch (Throwable e) {
            exceptions.add( e );
        }
        finally {
            closeables.forEach( e -> close( e ) );
        }
        return this;
    }
    
    
    public Closer runAndClose( Runnable exe ) {
        return runAndClose( new Callable() {
            public Object call() throws Exception {
                exe.run();
                return null;
            }
        });
    }
    
    
    /**
     * Quietly closes the given closeable. A possible Exception during close() is not
     * rethrown but catched and collected internally.
     *
     * @param closeable
     * @return this
     */
    public Closer close( AutoCloseable closeable ) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        }
        catch (Throwable e) {
            exceptions.add( e );
        }
        return this;
    }
    
    
    /**
     * Quietly closes the given closeable. A possible Exception during close() is not
     * rethrown but catched and collected internally.
     *
     * @param closeable
     * @return null This allows to set a variable to null after close.
     */
    public <T> T closeAndNull( AutoCloseable closeable ) {
        close( closeable );
        return null;
    }
    
    
    public <E extends Throwable> Closer rethrow( Class<E> match ) throws E {
        for (Throwable e : exceptions) {
            if (match.isAssignableFrom( e.getClass() )) {
                throw (E)e;
            }
        }
        return this;
    }

    
    public <E extends Throwable> Closer rethrowOrWrap( Class<E> match ) throws E {
        for (Throwable e : exceptions) {
            if (match.isAssignableFrom( e.getClass() )) {
                throw (E)e;
            }
            else {
                throw new RuntimeException( e );
            }
        }
        return this;
    }
    
}

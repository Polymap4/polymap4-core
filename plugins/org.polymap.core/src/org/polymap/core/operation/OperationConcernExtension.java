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

package org.polymap.core.operation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.CorePlugin;
import org.polymap.core.operation.OperationSupport.OperationInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
class OperationConcernExtension {

    private static Log log = LogFactory.getLog( OperationConcernExtension.class );

    static final String         EXTENSION_POINT_ID = CorePlugin.PLUGIN_ID + ".operation.concerns";
    
    static List<OperationConcernExtension> extensions = new ArrayList();

    
    static {
        IConfigurationElement[] exts = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( EXTENSION_POINT_ID );
        log.info( "Operation concern extensions found: " + exts.length ); //$NON-NLS-1$
        
        for (IConfigurationElement ext : exts) {
            try {
                extensions.add( new OperationConcernExtension( ext ) );
            }
            catch (CoreException e) {
                log.warn( "Failed to init extension: ", e );
            }
        }
    }

    
    /**
     * The implementations of the methods execute(), undo() und redo() executing
     */
    interface Function {
        boolean isValid( OperationConcernExtension ext );
        boolean before( OperationConcernExtension ext );
        void after( OperationConcernExtension ext );
    }

    /**
     * For all extensions do the given
     * {@link Function#before(OperationConcernExtension)}. Check validity of the
     * extension and handle exceptions.
     */
    protected static boolean forAllBefore( Function function, OperationInfo info ) {
        for (OperationConcernExtension ext : extensions) {
            try {
                if (function.isValid( ext )) {
                    if (!function.before( ext )) {
                        info.vetoOperationExecution = true;
                    }
                    info.concerns.add( ext );
                }
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        return info.vetoOperationExecution;
    }

    /**
     * For all extensions do the given
     * {@link Function#after(OperationConcernExtension)}. Handle exceptions.
     */
    protected static void forAllAfter( Function function, OperationInfo info ) {
        for (int i=info.concerns.size()-1; i>=0; i--) {
            try {
                OperationConcernExtension ext = (OperationConcernExtension)info.concerns.get( i );
                function.after( ext );
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        info.concerns.clear();
    }
 

    public static void execute( IOperationHistory history, final IUndoableOperation op, final IProgressMonitor monitor, final OperationInfo info ) 
    throws Exception {
        Function execute = new Function() {
            public boolean isValid( OperationConcernExtension ext ) {
                return ext.concern.handles( op, info, monitor );
            }
            public boolean before( OperationConcernExtension ext ) {
                return ext.concern.beforeExecute( op, monitor, info );
            }
            public void after( OperationConcernExtension ext ) {
                ext.concern.afterExecute( op, monitor, info );
            }
        };

        forAllBefore( execute, info );
        try {
            if (!info.vetoOperationExecution) {
                history.execute( op, monitor, info );
            }
        }
        catch (Exception e) {
            info.exception = e;
        }
        forAllAfter( execute, info );
        
        if (info.exception != null) {
            throw info.exception;
        }
    }

    
    public static void undo( IOperationHistory history, final IUndoContext context, final IProgressMonitor monitor, final OperationInfo info )
    throws Exception {
        final IUndoableOperation op = history.getUndoOperation( context );
        Function undo = new Function() {
            public boolean isValid( OperationConcernExtension ext ) {
                return ext.concern.handles( op, info, monitor );
            }
            public boolean before( OperationConcernExtension ext ) {
                return ext.concern.beforeUndo( op, monitor, info );
            }
            public void after( OperationConcernExtension ext ) {
                ext.concern.afterUndo( op, monitor, info );
            }
        };

        forAllBefore( undo, info );
        try {
            history.undo( context, monitor, info );
        }
        catch (Exception e) {
            info.exception = e;
        }
        forAllAfter( undo, info );

        if (info.exception != null) {
            throw info.exception;
        }
    }

    
    public static void redo( IOperationHistory history, final IUndoContext context, final IProgressMonitor monitor, final OperationInfo info )
    throws Exception {
        final IUndoableOperation op = history.getUndoOperation( context );
        Function redo = new Function() {
            public boolean isValid( OperationConcernExtension ext ) {
                return ext.concern.handles( op, info, monitor );
            }
            public boolean before( OperationConcernExtension ext ) {
                return ext.concern.beforeRedo( op, monitor, info );
            }
            public void after( OperationConcernExtension ext ) {
                ext.concern.afterRedo( op, monitor, info );
            }
        };

        forAllBefore( redo, info );
        try {
            history.redo( context, monitor, info );
        }
        catch (Exception e) {
            info.exception = e;
        }
        forAllAfter( redo, info );

        if (info.exception != null) {
            throw info.exception;
        }
    }

    
    // instance *******************************************

    IConfigurationElement       elm;
    
    IOperationConcern           concern;

    OperationConcernExtension( IConfigurationElement elm ) 
    throws CoreException {
        this.elm = elm;
        concern = (IOperationConcern)elm.createExecutableExtension( "class" );
    }
    
}

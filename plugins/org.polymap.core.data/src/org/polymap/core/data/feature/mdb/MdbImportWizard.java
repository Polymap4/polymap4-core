/* 
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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
package org.polymap.core.data.feature.mdb;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.healthmarketscience.jackcess.Database;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MdbImportWizard
        extends Wizard
        implements IImportWizard {

    private static Log log = LogFactory.getLog( MdbImportWizard.class );

    private MdbImportPage           importPage;
    

    public MdbImportWizard() {
    }


    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        addPage( importPage = new MdbImportPage() );
    }

    
    public void dispose() {
    }


    public boolean canFinish() {
        return true;
    }


    public boolean performFinish() {
        Database db = null;
        try {
            db = Database.open( importPage.dbFile, true );
            MdbImportOperation op = new MdbImportOperation( db, importPage.tableNames );
            OperationSupport.instance().execute( op, false, true );
            return true;
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Fehler beim importieren.", e );
            return false;
        }
        finally {
            try {
                db.close();
            }
            catch (IOException e) {
                log.error( e, e );
            }
        }
    }

}

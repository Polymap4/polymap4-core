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
package org.polymap.core.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class OperationWizardPage
        extends WizardPage {

    private static Log log = LogFactory.getLog( OperationWizardPage.class );


    protected OperationWizardPage( String pageName ) {
        super( pageName );
    }

    protected OperationWizardPage( String pageName, String title, ImageDescriptor titleImage ) {
        super( pageName, title, titleImage );
    }

    public OperationWizard getWizard() {
        return (OperationWizard)super.getWizard();
    }

}

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
package org.polymap.rhei.internal.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import org.polymap.core.data.ui.featureTable.FeatureSelectionView;
import org.polymap.core.data.ui.featuretable.SimpleFeatureTableElement;
import org.polymap.rhei.form.FormEditor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version ($Revision$)
 */
public class OpenFormAction
        implements IViewActionDelegate {

    private static Log log = LogFactory.getLog( OpenFormAction.class );

    private FeatureSelectionView        view;
    
    private SimpleFeatureTableElement   selectedElm;
    
    
    public void init( IViewPart _view ) {
        this.view = (FeatureSelectionView)_view;
    }


    public void run( IAction action ) {
        FormEditor.open( view.getFeatureStore(), selectedElm.feature() );
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        selectedElm = null;
        
        if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
            Object elm = ((IStructuredSelection)sel).getFirstElement();
            
            if (elm instanceof SimpleFeatureTableElement) {
                selectedElm = (SimpleFeatureTableElement)elm; 
            }
            else {
                log.warn( "Unknow table element type: " + elm.getClass().getSimpleName() );
            }
        }
        action.setEnabled( view != null && selectedElm != null );
    }

}

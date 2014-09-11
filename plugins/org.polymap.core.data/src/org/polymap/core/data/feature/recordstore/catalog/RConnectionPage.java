/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.core.data.feature.recordstore.catalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.data.Messages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RConnectionPage
        extends AbstractUDIGImportPage
        implements UDIGConnectionPage, ModifyListener {

    private String          dbname;
    

    public RConnectionPage() {
        super( i18n( "dialogTitle" ) );
    }


    @Override
    public void createControl( Composite parent ) {
        Composite composite = new Composite( parent, SWT.NONE );
        setControl( composite );
        FormLayout layout = new FormLayout();
        layout.spacing = 5;
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        composite.setLayout( layout );
        
        Label l = new Label( composite, SWT.NONE );
        l.setText( i18n( "dialogMsg" ) );
        l.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        
        Text text = new Text( composite, SWT.BORDER );
        text.setLayoutData( FormDataFactory.filled().top( l ).bottom( -1 ).create() );
        dbname = i18n( "dbName", Polymap.instance().getUser().getName() );
        text.setText( dbname );
        text.addModifyListener( this );
    }
    
    
    @Override
    public void modifyText( ModifyEvent e ) {
        // getState().getErrors().clear();
        dbname = ((Text)e.getSource()).getText();

        setErrorMessage( null );
        setPageComplete( true );

        if (!StringUtils.containsNone( dbname, "/\\@" )) {
            setErrorMessage( i18n( "validationError" ) );
            setPageComplete( false );
        }
        
        getWizard().getContainer().updateButtons();
    }

    
    @Override
    public Collection<IService> getServices() {
        return super.getServices();
    }


    @Override
    public Map<String,Serializable> getParams() {
        Map<String,Serializable> params = new HashMap();
        params.put( RDataStoreFactory.DBTYPE.key, (Serializable)RDataStoreFactory.DBTYPE.sample );
        params.put( RDataStoreFactory.DATABASE.key, dbname );
        return params;

//        RDataStoreFactory factory = RServiceExtension.factory();
//        RDataStore ds = factory.createNewDataStore( params );
//        try {
//            List<Name> typeNames = ds.getNames();
//            log.info( "RDataStore: " + typeNames );
//        }
//        catch( Exception e) {
//            throw new RuntimeException( e );
//        }
//
//        RServiceImpl service = (RServiceImpl)new RServiceExtension().createService( null, params );
//
//        return null;
    }


    protected static String i18n( String key, Object... args ) {
        return Messages.get( "CreateRDataStoreAction_" + key, args );
    }
    
}

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
 *
 * $Id:$
 */
package org.polymap.rhei.form.json;

import java.util.Date;
import java.util.Locale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import org.eclipse.jface.action.Action;

import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @version POLYMAP3 ($Revision: $)
 * @since 3.0
 */
public class JsonForm
        implements IFormEditorPage {

    private static Log log = LogFactory.getLog( JsonForm.class );
    
    static final int                FIELD_SPACING_H = 5;
    static final int                FIELD_SPACING_V = 1;
    static final int                SECTION_SPACING = 8;

    private JSONObject              json;

    private IFormEditorPageSite     site;

    private IFormEditorToolkit      tk;
    
    
    protected JsonForm() {
    }
    
    
    public JsonForm( JSONObject json ) {
        this.json = json;
    }
    
    
    /**
     * 
     * @param url URL to load the contents of the JSON from.
     * @throws JSONException 
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */
    public JsonForm( URL url ) 
    throws JSONException, UnsupportedEncodingException, IOException {
        Reader in = null;        
        try {
            in = new BufferedReader( new InputStreamReader( url.openStream(), "ISO-8859-1" ) );
            json = new JSONObject( new JSONTokener( in ) );
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    
    protected void setJson( JSONObject json ) {
        this.json = json;
    }

    
    public String getId() {
        try {
            return json.getString( "id" );
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: id", e );
        }
    }


    public String getTitle() {
        try {
            return json.getString( "title" );
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: title", e );
        }
    }


    public void createFormContent( IFormEditorPageSite _site ) {
        log.debug( "createFormContent(): json= " + json );
        this.site = _site;
        this.tk = site.getToolkit();

        site.setFormTitle( getTitle() );
        site.getPageBody().setLayout( new FormLayout() );
        Composite client = site.getPageBody();

        try {
            JSONArray fields = json.getJSONArray( "fields" );
            for (int i=0; i<fields.length(); i++) {
                JSONObject field_json = fields.getJSONObject( i );
                
                Composite field = newFormField( client, field_json );
                setFieldLayoutData( field );
            }
        }
        catch (JSONException e) {
            throw new RuntimeException( "JSON form does not contain field: " + e, e );
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException( "Field type not valid: " + e, e );
        }
    }

    
    protected Composite newFormField( Composite parent, JSONObject field_json )
    throws JSONException, ClassNotFoundException {
        IFormField formField = null;
        IFormFieldValidator validator = null;
        
        // check type -> build default field/validator
        String valueTypeName = field_json.optString( "type" );
        
        if (valueTypeName != null) {
            Class valueType = Thread.currentThread().getContextClassLoader().loadClass( valueTypeName );
            // Date
            if (Date.class.isAssignableFrom( valueType )) {
                formField = new DateTimeFormField();
            }
            // String
            else if (String.class.isAssignableFrom( valueType )) {
                formField = new StringFormField();
            }
            // Integer
            else if (Integer.class.isAssignableFrom( valueType )) {
                formField = new StringFormField();
                validator = new NumberValidator( Integer.class, Locale.getDefault() );
            }
            // Float
            else if (Integer.class.isAssignableFrom( valueType )) {
                formField = new StringFormField();
                validator = new NumberValidator( Integer.class, Locale.getDefault(), 10, 2 );
            }
            else {
                throw new RuntimeException( "Unhandled valueType: " + valueType );
            }
        }

        // create the form field
        String label = field_json.optString( "label" );
        Composite result = site.newFormField( parent, 
                new PropertyAdapter( field_json ), formField, validator, label );
        return result;
    }


    public Action[] getEditorActions() {
        return null;
    }

    
    // layout *********************************************
    
    private Composite lastLayoutElm = null;
    
    private Layout newLayout() {
        if (lastLayoutElm != null) {
            // close last element of the previous section
            ((FormData)lastLayoutElm.getLayoutData()).bottom = new FormAttachment( 100, -2 );
        }
        lastLayoutElm = null;
        return new FormLayout();
    }
    
    private Composite setFieldLayoutData( Composite field ) {
        assert field.getParent().getLayout() instanceof FormLayout;
        
        FormData layoutData = new FormData();
        layoutData.left = new FormAttachment( 20, FIELD_SPACING_H );
        layoutData.right = new FormAttachment( 80, -FIELD_SPACING_H );
        layoutData.top = lastLayoutElm != null
                ? new FormAttachment( lastLayoutElm, FIELD_SPACING_V )
                : new FormAttachment( 0 );
        field.setLayoutData( layoutData );
        
        lastLayoutElm = field;
        return field;
    }
    
}

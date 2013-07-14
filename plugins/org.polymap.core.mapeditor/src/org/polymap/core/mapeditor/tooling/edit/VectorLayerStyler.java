/* 
 * polymap.org
 * Copyright 2012-2013, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling.edit;

import java.util.HashMap;
import java.util.Map;

import java.awt.Color;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;

import org.eclipse.ui.IMemento;

import org.polymap.core.mapeditor.Messages;
import org.polymap.core.mapeditor.tooling.DefaultEditorTool;

import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class VectorLayerStyler {

    private static Log log = LogFactory.getLog( VectorLayerStyler.class );
    
    public static final String      CONTROL_INTENT = "vectorStyler";
    
    private static final RGB        COLOR_STANDARD = new RGB( 0xe0, 0x05, 0x05 );

    private static final String     SYMBOL_SIMPLECROSS = "OpenLayers.Renderer.symbol.simplecross = [5,0, 5,10, 5,5, 0,5, 10,5, 5,5];";

    
    public enum Intent {
        standard,   // = "default",
        hover,      // = "temporary",
        select      // = "select"
    }
    
    // instance *******************************************
    
    private Map<String,Object>      standard = new HashMap();
    
    private Map<String,Object>      hover = new HashMap();
    
    private Map<String,Object>      select = new HashMap();

    private ColorSelector           lineColor;

    private Spinner                 lineWidth;

    private CCombo                  dashList;
    
    private IMemento                memento;


    /**
     * Constructs a new instance with default style.
     */
    public VectorLayerStyler( IMemento memento ) {
        this.memento = memento;
        
        standard.put( "strokeWidth", 1.6f );
        standard.put( "strokeColor", COLOR_STANDARD );
        standard.put( "strokeDashstyle", "solid" );
        standard.put( "strokeOpacity", 1 );
        standard.put( "fillColor", COLOR_STANDARD );
        standard.put( "fillOpacity", 0.0 );
        standard.put( "graphicName", "circle" );

        if (memento != null && memento.getFloat( "strokeWidth" ) != null) {
            standard.put( "strokeWidth", memento.getFloat( "strokeWidth" ) );
            RGB strokeColor = new RGB( memento.getInteger( "strokeColor_red" ), memento.getInteger( "strokeColor_green" ), memento.getInteger( "strokeColor_blue" ));
            standard.put( "strokeColor", strokeColor );
            standard.put( "strokeDashstyle", memento.getString( "strokeDashstyle" ) );
        }
        
        calculateHoverSelectStyle();
    }

    
    /**
     * Dispose all UI controls created for this styler.
     */
    public void dispose() {
        if (lineWidth != null) {
            Composite parent = lineWidth.getParent();
            
            for (Control child : parent.getChildren()) {
                if (VectorLayerStyler.CONTROL_INTENT.equals( child.getData( DefaultEditorTool.CONTROL_INTENT_DATA ) )) {
                    child.dispose();
                }
            }
            lineWidth = null;
        }
    }

    
    /**
     * 
     * @param style Style attributes to be added to {@link #standard} style.
     * @param calculate True specifies that {@link #calculateHoverSelectStyle()} is
     *        to called after setting {@link #standard} style.
     */
    public void changeStyles( Map<String,Object> style, boolean calculate ) {
        this.standard.putAll( style );
        if (calculate) {
            calculateHoverSelectStyle();
        }
        updatePanelControl();
        styleChanged( createStyleMap() );
    }
    
    
    /**
     * Calculates styles for {@link #hover} and {@link #select} intent based on the
     * {@link #standard} style. Override in order to change the standard behaviour.
     */
    protected void calculateHoverSelectStyle() {
        hover = Maps.newHashMap( standard );
        RGB rgb = (RGB)standard.get( "strokeColor" );
        
        // just brighter
//        Color c = new Color( rgb.red, rgb.green, rgb.blue ).brighter().brighter().brighter();
        
        // gray
//        int gray = (int)((0.299 * rgb.red) + (0.587 * rgb.green) + (0.114 * rgb.blue));
//        Color c = new Color( gray, gray, gray ).brighter();
        
        HSLColor hsl = new HSLColor( new Color( rgb.red, rgb.green, rgb.blue ) );
        Color c = hsl.adjustShade( 40 ).adjustSaturation( 100 ).toRGB();
        
        // hover
        hover.put( "strokeColor", new RGB( c.getRed(), c.getGreen(), c.getBlue() ) );
        hover.put( "strokeDashstyle", "solid" );
        Number strokeWidth = (Number)hover.get( "strokeWidth" );
        hover.put( "strokeWidth", strokeWidth );
        hover.put( "fillOpacity", 0.25 );
    
//        hsl = new HSLColor( new Color( rgb.red, rgb.green, rgb.blue ) );
//        c = hsl.adjustHue( 180 ).adjustShade( 10 ).toRGB();

        // select
        select = Maps.newHashMap( standard );
        select.put( "strokeColor", new RGB( c.getRed(), c.getGreen(), c.getBlue() ) );
        select.put( "strokeDashstyle", "solid" );
        strokeWidth = (Number)select.get( "strokeWidth" );
        select.put( "strokeWidth", strokeWidth );
        select.put( "fillOpacity", 0.20 );
    }

    
    /**
     * 
     *
     * @param map
     * @return
     */
    protected Style createStyle( Map<String,Object> map ) {
        Style result = new Style();
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            // String
            if (entry.getValue() instanceof String) {
                String value = (String)entry.getValue();
                log.trace( "    " + entry.getKey() + ": String: " + value );
                result.setAttribute( entry.getKey(), value );
            }
            // Number
            else if (entry.getValue() instanceof Number) {
                String value = entry.getValue().toString();
                log.trace( "    " + entry.getKey() + ": Number: " + value );
                result.setAttribute( entry.getKey(), value );
            }
            // Color
            else if (entry.getValue() instanceof RGB) {
                RGB rgb = (RGB)entry.getValue();
                String hex = new StringBuilder( 8 ).append( '#' )
                        .append( StringUtils.leftPad( Integer.toHexString( rgb.red ), 2, '0' ) )
                        .append( StringUtils.leftPad( Integer.toHexString( rgb.green ), 2, '0' ) )
                        .append( StringUtils.leftPad( Integer.toHexString( rgb.blue ), 2, '0' ) ).toString();
                log.trace( "    " + entry.getKey() + ": RGB: " + hex );
                result.setAttribute( entry.getKey(), hex );
            }
            else {
                throw new RuntimeException( "Unknown style attribute type: " + entry );
            }
        }
        return result;
    }
    
    
    protected StyleMap createStyleMap() {
        StyleMap styleMap = new StyleMap();
        if (standard != null) {
            styleMap.setIntentStyle( "default", createStyle( standard ) );
        }
        if (hover != null) {
            styleMap.setIntentStyle( "temporary", createStyle( hover ) );
        }
        if (select != null) {
            styleMap.setIntentStyle( "select", createStyle( select ) );
        }
        // XXX hack to get custom symbolizer defined
        styleMap.addObjModCode( SYMBOL_SIMPLECROSS );
        return styleMap;
    }


    public void createPanelControl( Composite parent, VectorLayerStylerAware tool ) {
        // lineColor
        lineColor = tool.getSite().getToolkit().createColorSelector( parent );
        tool.layoutControl( i18n( "colorLabel" ), lineColor.getButton() );
        lineColor.getButton().setData( DefaultEditorTool.CONTROL_INTENT_DATA, CONTROL_INTENT );
        lineColor.addListener( new IPropertyChangeListener() {
            public void propertyChange( org.eclipse.jface.util.PropertyChangeEvent ev ) {
                standard.put( "strokeColor", lineColor.getColorValue() );
                standard.put( "fillColor", lineColor.getColorValue() );
                calculateHoverSelectStyle();
                styleChanged( createStyleMap() );
            }
        });

        // lineWidth
        lineWidth = tool.getSite().getToolkit().createSpinner( parent );
        lineWidth.setMaximum( 100 );
        lineWidth.setMinimum( 10 );
        lineWidth.setDigits( 1 );
        tool.layoutControl( i18n( "lineWidthLabel" ), lineWidth );
        lineWidth.setData( DefaultEditorTool.CONTROL_INTENT_DATA, CONTROL_INTENT );
        lineWidth.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {
                standard.put( "strokeWidth", ((float)lineWidth.getSelection()) / 10 );
                calculateHoverSelectStyle();
                styleChanged( createStyleMap() );
            }
        });
        
        // dashstyle
        dashList = tool.getSite().getToolkit().createCombo( parent, new String[]
                { "solid", "dot", "dash", "dashdot", "longdash", "longdashdot" } );
        dashList.select( 0 );
        dashList.setEditable( false );
        tool.layoutControl( i18n( "dashListLabel" ), dashList );
        dashList.setData( DefaultEditorTool.CONTROL_INTENT_DATA, CONTROL_INTENT );
        dashList.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                standard.put( "strokeDashstyle", dashList.getItem( dashList.getSelectionIndex() ) );
                calculateHoverSelectStyle();
                styleChanged( createStyleMap() );
            }
        });
    
        updatePanelControl();
    }

    
    protected void updatePanelControl() {
        if (lineColor != null) {
            lineColor.setColorValue( (RGB)standard.get( "strokeColor" ) );
            lineWidth.setSelection( (int)(((Number)standard.get( "strokeWidth" )).floatValue() * 10) );
        }
        if (dashList != null) {
            String dashstyle = (String)standard.get( "strokeDashstyle" );
            String[] items = dashList.getItems();
            for (int i=0; i<items.length; i++) {
                if (items[i].equals( dashstyle )) {
                    dashList.select( i );
                }
            }
        }
    }

    
    /**
     * Override to get informed about style changes via UI.
     * 
     * @param newStyleMap Newly created {@link StyleMap}. The receiver is responsible of
     *        disposing this StyleMap properly when it is no longer used.
     */
    protected void styleChanged( StyleMap newStyleMap ) {
        if (memento != null) {
            memento.putFloat( "strokeWidth", (Float)standard.get( "strokeWidth" ) );
            memento.putString( "strokeDashstyle", (String)standard.get( "strokeDashstyle" ) );
            RGB strokeColor = (RGB)standard.get( "strokeColor" );
            memento.putInteger( "strokeColor_red", strokeColor.red );
            memento.putInteger( "strokeColor_green", strokeColor.green );
            memento.putInteger( "strokeColor_blue", strokeColor.blue );
        }
    }

    
    protected String i18n( String key, Object... args ) {
        return Messages.get( "VectorLayerStyler_" + key, args );    
    }

}

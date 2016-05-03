/*
 * polymap.org Copyright (C) 2016 individual contributors as indicated by
 * the @authors tag. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.ui;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;

import org.apache.commons.lang3.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 * @author Steffen Stundzig <steffen@mapzone.io>
 */
public class ColorChooser {

    private static final IMessages i18n = Messages.forPrefix( "ColorChooser" );

    private static final int   COLORBOX_HEIGHT = 200;

    private static final int   COLORBOX_WIDTH  = 200;

    private static final int   PALETTE_BOX_SIZE        = 12;

    private static final int   PALETTE_BOXES_IN_ROW    = 14;

    private static final int   COLOR_DISPLAY_BOX_SIZE  = 76;

    private static final int   MAX_RGB_COMPONENT_VALUE = 255;

    // Color components
    private static final int   RED                     = 0;

    private static final int   GREEN                   = 1;

    private static final int   BLUE                    = 2;

    enum COLOR_WIDGET_TYPE {
        PREPARE, BOX, HEX, SPINNER, DISPLAY, OLD_DISPLAY, PALETTE
    };


    private class PaletteListener
            extends MouseAdapter {

        private final Composite panelBody;

        private final RGB       rgb;

        public PaletteListener( Composite panelBody, RGB rgb ) {
            this.panelBody = panelBody;
            this.rgb = rgb;
        }

        @Override
        public void mouseDown( MouseEvent event ) {
            updateColorWidgets( panelBody, rgb, COLOR_WIDGET_TYPE.PALETTE );
        }
    }


    private class SpinnerListener
            implements ModifyListener {

        private final Composite panelBody;

        private final Spinner   spinner;

        private final int       colorIndex;

        public SpinnerListener( Composite panelBody, Spinner spinner, int colorIndex ) {
            this.panelBody = panelBody;
            this.spinner = spinner;
            this.colorIndex = colorIndex;
        }

        public void modifyText( ModifyEvent event ) {
            if (spinnerListenerActive) {
                updateColorFomSpinner( panelBody, colorIndex, spinner.getSelection() );
            }
        }
    }

    // Palette colors
    private static final RGB[] PALETTE_COLORS          = new RGB[] { new RGB( 0, 0, 0 ), new RGB( 70, 70, 70 ),
            new RGB( 120, 120, 120 ), new RGB( 153, 0, 48 ), new RGB( 237, 28, 36 ), new RGB( 255, 126, 0 ),
            new RGB( 255, 194, 14 ), new RGB( 255, 242, 0 ), new RGB( 168, 230, 29 ), new RGB( 34, 177, 76 ),
            new RGB( 0, 183, 239 ), new RGB( 77, 109, 243 ), new RGB( 47, 54, 153 ), new RGB( 111, 49, 152 ),
            new RGB( 255, 255, 255 ), new RGB( 220, 220, 220 ), new RGB( 180, 180, 180 ), new RGB( 156, 90, 60 ),
            new RGB( 255, 163, 177 ), new RGB( 229, 170, 122 ), new RGB( 245, 228, 156 ), new RGB( 255, 249, 189 ),
            new RGB( 211, 249, 188 ), new RGB( 157, 187, 97 ), new RGB( 153, 217, 234 ), new RGB( 112, 154, 209 ),
            new RGB( 84, 109, 142 ), new RGB( 181, 165, 213 ) };

    
    // instance *******************************************
    
    private RGB                rgb;

    private Label              colorDisplay;

    private Canvas             colorBox;

    private Image              colorBoxImage;

    private Canvas             colorBoxMarker;

    private Spinner            spRed;

    private Spinner            spBlue;

    private Spinner            spGreen;

    private Text               colorHex;

    private boolean            spinnerListenerActive = true, hexListenerActive = true;


    public ColorChooser( RGB rgb ) {
        this.setRGB( rgb );
    }


    public String title() {
        return i18n.get( "title" );
    }


    public void createContents( Composite panelBody ) {
        panelBody.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );
        prepareOpen( panelBody );
    }


    /**
     * Returns the currently selected color in the receiver.
     *
     * @return the RGB value for the selected color, may be null
     * @see PaletteData#getRGBs
     */
    public RGB getRGB() {
        return rgb;
    }


    /**
     * Sets the receiver's selected color to be the argument.
     *
     * @param rgb the new RGB value for the selected color, may be null to let the
     *        platform select a default when open() is called
     * @see PaletteData#getRGBs
     */
    public void setRGB( RGB rgb ) {
        this.rgb = rgb;
    }


    protected void prepareOpen( Composite panelBody ) {
        createControls( panelBody );
        updateColorWidgets( panelBody, getRGB(), COLOR_WIDGET_TYPE.PREPARE );
    }


    private void createControls( Composite panelBody ) {
        Composite top = new Composite( panelBody, SWT.NONE );
        top.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );
        FormDataFactory.on( top ).top( 5 );

        Composite left = new Composite( top, SWT.NONE );
        left.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );

        colorBox = new Canvas( left, SWT.NONE );
        colorBox.setBounds( new Rectangle( 0, 0, COLORBOX_WIDTH, COLORBOX_HEIGHT ) );
        FormDataFactory.on( colorBox ).width( COLORBOX_WIDTH ).height( COLORBOX_HEIGHT );
        colorBoxMarker = new Canvas( left, SWT.NONE );
        colorBoxMarker.moveAbove( null );
        colorBoxMarker.setSize( 2, 2 );
        colorBox.addListener( SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent( Event event ) {
                handleColorBoxMarkerPositionChanged( panelBody, event.x, event.y );
            }
        } );

        Composite hexField = createHexField( left );
        FormDataFactory.on( hexField ).top( colorBox, 30 ).left( 0 ).right( 100 );

        Composite middle = new Composite( top, SWT.NONE );
        middle.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );
        FormDataFactory.on( middle ).left( left, 30 ).right( 100 );

        Composite middleTop = new Composite( middle, SWT.NONE );
        middleTop.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );
        colorDisplay = createColorAreaLabel( middleTop );
        final Label oldColorDisplay = createColorAreaLabel( middleTop );
        oldColorDisplay.addListener( SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent( Event event ) {
                Color oldColor = oldColorDisplay.getBackground();
                if (oldColor != null) {
                    updateColorWidgets( panelBody, oldColor.getRGB(), COLOR_WIDGET_TYPE.OLD_DISPLAY );
                }
            }
        } );
        if (rgb != null) {
            oldColorDisplay.setBackground( new Color( panelBody.getDisplay(), rgb ) );
        }

        FormDataFactory.on( colorDisplay ).left( 0 ).right( 50 );
        FormDataFactory.on( oldColorDisplay ).left( colorDisplay ).right( 100 );
        FormDataFactory.on( middleTop ).left( 0 ).right( 100 );

        Composite rgbArea = createRGBArea( middle );
        FormDataFactory.on( rgbArea ).top( middleTop ).left( 0 ).right( 100 );

        Composite right = createPalette( middle );
        FormDataFactory.on( right ).top( rgbArea ).left( 0 ).right( 100 );
    }


    private void handleColorBoxMarkerPositionChanged( Composite panelBody, int x, int y ) {
        int pixel = colorBoxImage.getImageData().getPixel( x, y );
        RGB newRGB = colorBoxImage.getImageData().palette.getRGB( pixel );
        updateColorWidgets( panelBody, newRGB, COLOR_WIDGET_TYPE.BOX );
        drawColorBoxMarker( panelBody, x, y );
    }


    private void drawColorBoxMarker( Composite panelBody, int x, int y ) {
        int adjustedX = x - 10;
        if (adjustedX < 0) {
            adjustedX = 0;
        }
        int adjustedY = y - 10;
        if (adjustedY < 0) {
            adjustedY = 0;
        }
        colorBoxMarker.setBounds( adjustedX, adjustedY, 20, 20 );
        colorBoxMarker.setForeground( panelBody.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
        colorBoxMarker.setBackground( null );
        colorBoxMarker.addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent e ) {
                e.gc.setForeground( panelBody.getDisplay().getSystemColor( SWT.COLOR_BLACK ) );
                e.gc.drawOval( 1, 1, 18, 18 );
                e.gc.setForeground( panelBody.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
                e.gc.drawOval( 2, 2, 16, 16 );
                e.gc.drawOval( 3, 3, 14, 14 );
                e.gc.setForeground( panelBody.getDisplay().getSystemColor( SWT.COLOR_BLACK ) );
                e.gc.drawOval( 4, 4, 12, 12 );
            }
        } );
    }


    private RGB getDefaultRGB() {
        return new RGB( 255, 255, 255 );
    }


    private void setColorBoxColor( Composite panelBody, RGB rgb ) {
        Integer[][] colorBoxColors = new Integer[COLORBOX_WIDTH][COLORBOX_HEIGHT];
        if (rgb == null) {
            rgb = getDefaultRGB();
        }
        Color color = new Color( panelBody.getDisplay(), rgb );
        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB( color.getRed(), color.getGreen(), color.getBlue(), hsb );

        int rgbValue;
        float saturationFilter = hsb[1] == 0.0 ? 0.0f : 1.0f;
        float newS, newB;
        BufferedImage bi = new BufferedImage( COLORBOX_WIDTH, COLORBOX_HEIGHT, BufferedImage.TYPE_INT_RGB );
        boolean markerSet = false;
        int markerPosX = 0, markerPosY = 0;
        for (int w = 0; w < COLORBOX_WIDTH; w += 1) {
            for (int h = COLORBOX_HEIGHT - 1; h >= 0; h -= 1) {
                newS = saturationFilter * Double.valueOf( (double)w / COLORBOX_WIDTH ).floatValue();
                newB = Double.valueOf( (double)h / COLORBOX_HEIGHT ).floatValue();
                rgbValue = java.awt.Color.HSBtoRGB( hsb[0], newS, newB );
                colorBoxColors[w][h] = rgbValue;
                if (!markerSet && Math.abs( newS - hsb[1] ) <= 0.01 && Math.abs( newB - hsb[2] ) <= 0.01) {
                    if (colorBox.getBounds().contains( w, h )) {
                        markerPosX = w;
                        markerPosY = h;
                        markerSet = true;
                    }
                }
                bi.setRGB( w, h, rgbValue );
            }
        }
        final PaletteData palette = new PaletteData( 0xff0000, 0xff00, 0xff );
        DataBuffer dataBuffer = bi.getData().getDataBuffer();
        int[] data = ((DataBufferInt)dataBuffer).getData();
        ImageData imageData = new ImageData( bi.getWidth(), bi.getHeight(), 24, palette );
        imageData.setPixels( 0, 0, data.length, data, 0 );
        if (colorBoxImage != null) {
            colorBoxImage.dispose();
        }
        colorBoxImage = new Image( Display.getDefault(), imageData );
        GC gc = new GC( colorBox );
        gc.drawImage( colorBoxImage, 0, 0 );
        gc.dispose();
        int finalMarkerPosX = markerPosX, finalMarkerPosY = markerPosY;
        colorBoxMarker.addPaintListener( new PaintListener() {

            public void paintControl( PaintEvent e ) {
                drawColorBoxMarker( panelBody, finalMarkerPosX, finalMarkerPosY );
            }
        } );
    }


    private Composite createHexField( Composite panelBody ) {
        Composite comp = new Composite( panelBody, SWT.NONE );
        comp.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );
        Label hexLabel = createLabel( comp, "hex", SWT.NONE );
        colorHex = createText( comp, "", SWT.BORDER );
        colorHex.addModifyListener( new ModifyListener() {

            @Override
            public void modifyText( ModifyEvent event ) {
                if (hexListenerActive) {
                    String value = colorHex.getText();
                    if (value.trim().length() == 6) {
                        try {
                            java.awt.Color color = java.awt.Color.decode( "#" + value.trim() );
                            RGB rgb = new RGB( color.getRed(), color.getGreen(), color.getBlue() );
                            updateColorWidgets( panelBody, rgb, COLOR_WIDGET_TYPE.HEX );
                        }
                        catch (NumberFormatException nfe) {
                            // ignore
                        }
                    }
                }
            }
        } );
        FormDataFactory.on( colorHex ).fill().left( hexLabel, 5 ).right( 100, -5 );
        return comp;
    }


    private Composite createPalette( Composite parent ) {
        Composite paletteComp = new Composite( parent, SWT.NONE );
        paletteComp.setLayout( new GridLayout( PALETTE_BOXES_IN_ROW, true ) );
        Label title = createLabel( paletteComp, "basic", SWT.NONE );
        GridData titleData = new GridData( SWT.LEFT, SWT.CENTER, true, false );
        titleData.horizontalSpan = PALETTE_BOXES_IN_ROW;
        title.setLayoutData( titleData );
        for (int i = 0; i < PALETTE_COLORS.length; i++) {
            createPaletteColorBox( paletteComp, PALETTE_COLORS[i] );
        }
        return paletteComp;
    }


    private Label createColorAreaLabel( Composite panelBody ) {
        Label colorDisplay = createLabel( panelBody, "", SWT.BORDER | SWT.FLAT );
        FormDataFactory.on( colorDisplay ).width( COLOR_DISPLAY_BOX_SIZE ).height( COLOR_DISPLAY_BOX_SIZE );
        return colorDisplay;
    }


    private Composite createRGBArea( Composite panelBody ) {
        Composite spinComp = new Composite( panelBody, SWT.NONE );
        spinComp.setLayout( new GridLayout( 2, false ) );
        createLabel( spinComp, "red", SWT.NONE );
        spRed = new Spinner( spinComp, SWT.BORDER | SWT.WRAP );
        spRed.setMaximum( MAX_RGB_COMPONENT_VALUE );
        spRed.setMinimum( 0 );
        spRed.addModifyListener( new SpinnerListener( panelBody, spRed, RED ) );
        createLabel( spinComp, "green", SWT.NONE );
        spGreen = new Spinner( spinComp, SWT.BORDER );
        spGreen.setMaximum( MAX_RGB_COMPONENT_VALUE );
        spGreen.setMinimum( 0 );
        spGreen.addModifyListener( new SpinnerListener( panelBody, spGreen, GREEN ) );
        createLabel( spinComp, "blue", SWT.NONE );
        spBlue = new Spinner( spinComp, SWT.BORDER );
        spBlue.setMaximum( MAX_RGB_COMPONENT_VALUE );
        spBlue.setMinimum( 0 );
        spBlue.addModifyListener( new SpinnerListener( panelBody, spBlue, BLUE ) );
        return spinComp;
    }


    private void updateColorBox( Composite panelBody, RGB rgb ) {
        setColorBoxColor( panelBody, rgb );
    }


    private void updateColorDisplay( Composite panelBody, RGB rgb ) {
        // Color oldBackground = colorDisplay.getBackground();
        if (rgb != null) {
            colorDisplay.setBackground( new Color( panelBody.getDisplay(), rgb ) );
        }
        else {
            colorDisplay.setBackground( null );
        }
        // if (oldBackground != null) {
        // oldColorDisplay.setBackground( oldBackground );
        // }
        // else {
        // oldColorDisplay.setBackground( colorDisplay.getBackground() );
        // }
    }


    private void updateSpinners( RGB newRGB ) {
        if (newRGB != null) {
            spinnerListenerActive = false;
            spRed.setSelection( newRGB.red );
            spGreen.setSelection( newRGB.green );
            spBlue.setSelection( newRGB.blue );
            spinnerListenerActive = true;
        }
    }


    private void updateHexField( RGB newRGB ) {
        if (newRGB != null) {
            java.awt.Color color = new java.awt.Color( newRGB.red, newRGB.green, newRGB.blue );
            // ignore alpha value
            hexListenerActive = false;
            colorHex.setText( Integer.toHexString( (color.getRGB() & 0xffffff)
                    | 0x1000000 ).substring( 1 ).toUpperCase() );
            hexListenerActive = true;
        }
    }


    private Label createPaletteColorBox( Composite parent, RGB color ) {
        Label result = createLabel( parent, "", SWT.BORDER | SWT.FLAT );
        result.setBackground( new Color( parent.getDisplay(), color ) );
        GridData data = new GridData();
        data.widthHint = PALETTE_BOX_SIZE;
        data.heightHint = PALETTE_BOX_SIZE;
        result.setLayoutData( data );
        result.addMouseListener( new PaletteListener( parent, color ) );
        return result;
    }


    private Label createLabel( Composite parent, String text, int style ) {
        Label label = new Label( parent, style );
        if (!StringUtils.isBlank( text )) {
            label.setText( i18n.get( text ) );
        }
        return label;
    }


    private Text createText( Composite parent, String text, int style ) {
        Text textt = new Text( parent, style );
        if (!StringUtils.isBlank( text )) {
            textt.setText( i18n.get( text ) );
        }
        return textt;
    }


    private void updateColorFomSpinner( Composite parent, int colorIndex, int value ) {
        if (rgb == null) {
            rgb = getDefaultRGB();
        }
        RGB newRGB = new RGB( rgb.red, rgb.green, rgb.blue );
        switch (colorIndex) {
            case RED:
                newRGB.red = value;
                break;
            case GREEN:
                newRGB.green = value;
                break;
            case BLUE:
                newRGB.blue = value;
                break;
        }
        updateColorWidgets( parent, newRGB, COLOR_WIDGET_TYPE.SPINNER );
    }


    private void updateColorWidgets( Composite parent, RGB newRGB, COLOR_WIDGET_TYPE type ) {
        if (type != COLOR_WIDGET_TYPE.BOX)
            updateColorBox( parent, newRGB );
        if (type != COLOR_WIDGET_TYPE.DISPLAY)
            updateColorDisplay( parent, newRGB );
        if (type != COLOR_WIDGET_TYPE.HEX)
            updateHexField( newRGB );
        if (type != COLOR_WIDGET_TYPE.SPINNER)
            updateSpinners( newRGB );

        updateColor( newRGB );
    }


    private void updateColor( RGB newRGB ) {
        if (newRGB != null) {
            if (rgb == null) {
                rgb = new RGB( 0, 0, 0 );
            }
            rgb.blue = newRGB.blue;
            rgb.green = newRGB.green;
            rgb.red = newRGB.red;
        }
        else {
            rgb = null;
        }
    }
}

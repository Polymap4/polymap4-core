/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.service.geoserver.spring;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.media.jai.PlanarImage;

import org.geoserver.platform.ServiceException;
import org.geoserver.wms.MapProducerCapabilities;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.map.RenderedImageMapResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Handles a GetMap request that spects a map in PDF format based on a rendered
 * image.
 * 
 * @see org.geoserver.wms.map.PDFMapResponse
 * 
 * @author Steffen Stundzig
 */
public class PDFMapResponse
        extends RenderedImageMapResponse {

    private static final Log log            = LogFactory.getLog( PDFMapResponse.class );

    static final String      MIME_TYPE      = "application/pdf";

    static final String[]    OUTPUT_FORMATS = { MIME_TYPE };


    public PDFMapResponse( WMS wms ) {
        super( OUTPUT_FORMATS, wms );
    }


    @Override
    public MapProducerCapabilities getCapabilities( String outputFormat ) {
        return new MapProducerCapabilities( false, false, false, true, null );
    }


    /**
     * Writes the PDF.
     * <p>
     * NOTE: the document seems to actually be created in memory, and being written
     * down to {@code output} once we call {@link Document#close()}. If there's no
     * other way to do so, it'd be better to actually split out the process into
     * produceMap/write?
     * </p>
     * 
     * @see org.geoserver.ows.Response#write(java.lang.Object, java.io.OutputStream,
     *      org.geoserver.platform.Operation)
     */
    @Override
    public void formatImageOutputStream( RenderedImage image, OutputStream out, WMSMapContent mapContent )
            throws ServiceException, IOException {

        final int width = mapContent.getMapWidth();
        final int height = mapContent.getMapHeight();

        log.debug( "setting up " + width + "x" + height + " image" );

        try {
            // step 1: creation of a document-object
            // width of document-object is width*72 inches
            // height of document-object is height*72 inches
            com.lowagie.text.Rectangle pageSize = new com.lowagie.text.Rectangle( width, height );

            Document document = new Document( pageSize );
            document.setMargins( 0, 0, 0, 0 );

            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance( document, out );

            // step 3: we open the document
            document.open();

            // step 4: we grab the ContentByte and do some stuff with it

            // we create a fontMapper and read all the fonts in the font
            // directory
            DefaultFontMapper mapper = new DefaultFontMapper();
            FontFactory.registerDirectories();

            // we create a template and a Graphics2D object that corresponds
            // with it
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate( width, height );
            PdfGraphics2D graphic = (PdfGraphics2D)tp.createGraphics( width, height, mapper );

            // we set graphics options
            if (!mapContent.isTransparent()) {
                graphic.setColor( mapContent.getBgColor() );
                graphic.fillRect( 0, 0, width, height );
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug( "setting to transparent" );
                }

                int type = AlphaComposite.SRC;
                graphic.setComposite( AlphaComposite.getInstance( type ) );

                Color c = new Color( mapContent.getBgColor().getRed(), mapContent.getBgColor().getGreen(), mapContent.getBgColor().getBlue(), 0 );
                graphic.setBackground( mapContent.getBgColor() );
                graphic.setColor( c );
                graphic.fillRect( 0, 0, width, height );

                type = AlphaComposite.SRC_OVER;
                graphic.setComposite( AlphaComposite.getInstance( type ) );
            }

            // Rectangle paintArea = new Rectangle(width, height);
            // Envelope dataArea = mapContent.getRenderingArea();

            graphic.drawImage( PlanarImage.wrapRenderedImage( image ).getAsBufferedImage(), 0, 0, width, height, null );

            graphic.dispose();
            cb.addTemplate( tp, 0, 0 );

            // step 5: we close the document
            document.close();
            writer.flush();
            writer.close();
        }
        catch (DocumentException t) {
            throw new ServiceException( "Error setting up the PDF", t, "internalError" );
        }
    }
}

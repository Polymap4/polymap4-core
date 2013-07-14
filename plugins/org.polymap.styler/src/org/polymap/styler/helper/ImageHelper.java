/*
 * polymap.org
 * Copyright 2009, Polymap GmbH, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.polymap.styler.helper;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.internal.graphics.ResourceFactory;


/**
 * Helper functions for Image stuff
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * 
 */

public class ImageHelper {

	private static final Log log = LogFactory.getLog(ImageHelper.class);

	
    public static Image createColorRectImage( java.awt.Color color ) {
        return ResourceFactory.findImage( convertToSWT( createColorRectBufferedImage( color ) ) );
	}
	

    public static Image createColorRectImage( RGB rgb ) {
        return ResourceFactory.findImage( convertToSWT( createColorRectBufferedImage( ColorHelper.RGB2Color( rgb ) ) ) );
    }

	
	public static BufferedImage createColorRectBufferedImage(java.awt.Color color) {
	    BufferedImage image = new BufferedImage( 10, 10, BufferedImage.TYPE_INT_ARGB );
	    Graphics2D gr2d = image.createGraphics();
	    // draw the image
	    gr2d.setColor( color );
	    gr2d.fillRect( 0, 0, 10, 10 );
	    
	    return image;
	  }

	// method stolen from the net - TODO check IP & commit the bugfix @  int[] pixelArray = new int[raster.getNumBands()];
	//  raster.getNumBands() was 3 bevore which crashed here with OutOfBounds ...
	
	public static ImageData convertToSWT(BufferedImage bufferedImage) {
		    if (bufferedImage.getColorModel() instanceof DirectColorModel) {
		      DirectColorModel colorModel = (DirectColorModel) bufferedImage
		          .getColorModel();
		      PaletteData palette = new PaletteData(colorModel.getRedMask(),
		          colorModel.getGreenMask(), colorModel.getBlueMask());
		      ImageData data = new ImageData(bufferedImage.getWidth(),
		          bufferedImage.getHeight(), colorModel.getPixelSize(),
		          palette);
		      WritableRaster raster = bufferedImage.getRaster();
		      int[] pixelArray = new int[raster.getNumBands()]; 
		      for (int y = 0; y < data.height; y++) {
		        for (int x = 0; x < data.width; x++) {
		          raster.getPixel(x, y, pixelArray);
		          int pixel = palette.getPixel(new RGB(pixelArray[0],
		              pixelArray[1], pixelArray[2]));
		          data.setPixel(x, y, pixel);
		        }
		      }
		      return data;
		    } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
		      IndexColorModel colorModel = (IndexColorModel) bufferedImage
		          .getColorModel();
		      int size = colorModel.getMapSize();
		      byte[] reds = new byte[size];
		      byte[] greens = new byte[size];
		      byte[] blues = new byte[size];
		      colorModel.getReds(reds);
		      colorModel.getGreens(greens);
		      colorModel.getBlues(blues);
		      RGB[] rgbs = new RGB[size];
		      for (int i = 0; i < rgbs.length; i++) {
		        rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
		            blues[i] & 0xFF);
		      }
		      PaletteData palette = new PaletteData(rgbs);
		      ImageData data = new ImageData(bufferedImage.getWidth(),
		          bufferedImage.getHeight(), colorModel.getPixelSize(),
		          palette);
		      data.transparentPixel = colorModel.getTransparentPixel();
		      WritableRaster raster = bufferedImage.getRaster();
		      int[] pixelArray = new int[1];
		      for (int y = 0; y < data.height; y++) {
		        for (int x = 0; x < data.width; x++) {
		          raster.getPixel(x, y, pixelArray);
		          data.setPixel(x, y, pixelArray[0]);
		        }
		      }
		      return data;
		    } 
		    return null;
		  }

	

}
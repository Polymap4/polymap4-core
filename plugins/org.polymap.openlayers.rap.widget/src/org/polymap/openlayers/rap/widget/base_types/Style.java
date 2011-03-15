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
package org.polymap.openlayers.rap.widget.base_types;

import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

/**
 * A style object that is used together with {@link StyleMap} to style the
 * features of a {@link VectorLayer}.
 * <p>
 * The following list of attributes is taken from <a href="http://dev.openlayers.org/releases/OpenLayers-2.8/doc/devdocs/files/OpenLayers/Feature/Vector-js.html#OpenLayers.Feature.Vector.OpenLayers.Feature.Vector.style"
 * >OpenLayers-2.8/doc</a>:
 * <table class="CDescriptionList" border="0" cellpadding="0" cellspacing="0">
 * <tbody>
 * <tr>
 * <td class="CDLEntry">fill</td>
 * <td class="CDLDescription">{Boolean} Set to false if no fill is desired.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">fillColor</td>
 * <td class="CDLDescription">{String} Hex fill color.&nbsp; Default is "#ee9900".</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">fillOpacity</td>
 * <td class="CDLDescription">{Number} Fill opacity (0-1).&nbsp; Default is 0.4</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">stroke</td>
 * <td class="CDLDescription">{Boolean} Set to false if no stroke is desired.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">strokeColor</td>
 * <td class="CDLDescription">{String} Hex stroke color.&nbsp; Default is
 * #ee9900.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">strokeOpacity</td>
 * <td class="CDLDescription">{Number} Stroke opacity (0-1).&nbsp; Default is 1.
 * </td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">strokeWidth</td>
 * <td class="CDLDescription">{Number} Pixel stroke width.&nbsp; Default is 1.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">strokeLinecap</td>
 * <td class="CDLDescription">{String} Stroke cap type.&nbsp; Default is
 * round.&nbsp; [butt | round | square]</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">strokeDashstyle</td>
 * <td class="CDLDescription">{String} Stroke dash style.&nbsp; Default is
 * "solid".&nbsp; [dot | dash | dashdot | longdash | longdashdot | solid]</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphic</td>
 * <td class="CDLDescription">{Boolean} Set to false if no graphic is desired.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">pointRadius</td>
 * <td class="CDLDescription">{Number} Pixel point radius.&nbsp; Default is 6.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">pointerEvents</td>
 * <td class="CDLDescription">{String} Default is "visiblePainted".</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">cursor</td>
 * <td class="CDLDescription">{String} Default is "".</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">externalGraphic</td>
 * <td class="CDLDescription">{String} Url to an external graphic that will be
 * used for rendering points.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphicWidth</td>
 * <td class="CDLDescription">{Number} Pixel width for sizing an external
 * graphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphicHeight</td>
 * <td class="CDLDescription">{Number} Pixel height for sizing an external
 * graphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphicOpacity</td>
 * <td class="CDLDescription">{Number} Opacity (0-1) for an external graphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphicXOffset</td>
 * <td class="CDLDescription">{Number} Pixel offset along the positive x axis
 * for displacing an external graphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphicYOffset</td>
 * <td class="CDLDescription">{Number} Pixel offset along the positive y axis
 * for displacing an external graphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphicZIndex</td>
 * <td class="CDLDescription">{Number} The integer z-index value to use in
 * rendering.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphicName</td>
 * <td class="CDLDescription">{String} Named graphic to use when rendering
 * points.&nbsp; Supported values include "circle" (default), "square", "star",
 * "x", "cross", "triangle".</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">graphicTitle</td>
 * <td class="CDLDescription">{String} Tooltip for an external graphic.&nbsp;
 * Only supported in Firefox and Internet Explorer.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">backgroundGraphic</td>
 * <td class="CDLDescription">{String} Url to a graphic to be used as the
 * background under an externalGraphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">backgroundGraphicZIndex</td>
 * <td class="CDLDescription">{Number} The integer z-index value to use in
 * rendering the background graphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">backgroundXOffset</td>
 * <td class="CDLDescription">{Number} The x offset (in pixels) for the
 * background graphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">backgroundYOffset</td>
 * <td class="CDLDescription">{Number} The y offset (in pixels) for the
 * background graphic.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">backgroundHeight</td>
 * <td class="CDLDescription">{Number} The height of the background
 * graphic.&nbsp; If not provided, the graphicHeight will be used.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">backgroundWidth</td>
 * <td class="CDLDescription">{Number} The width of the background width.&nbsp;
 * If not provided, the graphicWidth will be used.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">label</td>
 * <td class="CDLDescription">{String} The text for an optional label.&nbsp; For
 * browsers that use the canvas renderer, this requires either fillText or
 * mozDrawText to be available.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">labelAlign</td>
 * <td class="CDLDescription">{String} Label alignment.&nbsp; This specifies the
 * insertion point relative to the text.&nbsp; It is a string composed of two
 * characters.&nbsp; The first character is for the horizontal alignment, the
 * second for the vertical alignment.&nbsp; Valid values for horizontal
 * alignment: "l"=left, "c"=center, "r"=right.&nbsp; Valid values for vertical
 * alignment: "t"=top, "m"=middle, "b"=bottom.&nbsp; Example values: "lt", "cm",
 * "rb".&nbsp; The canvas renderer does not support vertical alignment, it will
 * always use "b".</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">fontColor</td>
 * <td class="CDLDescription">{String} The font color for the label, to be
 * provided like CSS.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">fontFamily</td>
 * <td class="CDLDescription">{String} The font family for the label, to be
 * provided like in CSS.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">fontSize</td>
 * <td class="CDLDescription">{String} The font size for the label, to be
 * provided like in CSS.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">fontWeight</td>
 * <td class="CDLDescription">{String} The font weight for the label, to be
 * provided like in CSS.</td>
 * </tr>
 * <tr>
 * <td class="CDLEntry">display</td>
 * <td class="CDLDescription">{String} Symbolizers will have no effect if
 * display is set to "none".&nbsp; All other values have no effect.</td>
 * </tr>
 * </tbody></table>
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class Style extends OpenLayersObject {

    public Style() {
        super.create("new OpenLayers.Style(OpenLayers.Util.extend({}, "
                + "OpenLayers.Feature.Vector.style[ 'default']));");
    }

    /**
     * @param attribute_name
     * @param attribute_value
     */
    public void setAttribute(String attribute_name, String attribute_value) {
        super.addObjModCode("obj.defaultStyle."+attribute_name+"='" + attribute_value + "';");
    }
    
    /**
     * @param attribute_name
     * @param attribute_value
     */
    public void setAttribute(String attribute_name, int attribute_value) {
        super.addObjModCode("obj.defaultStyle."+attribute_name+"=" + attribute_value + ";");
    }

}

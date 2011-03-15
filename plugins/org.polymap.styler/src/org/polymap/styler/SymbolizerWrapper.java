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

package org.polymap.styler;

import java.awt.Color;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.expression.Expression;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.LabelPlacement;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Halo;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.styler.helper.SymbolizerHelper;

/**
 * 
 * @author Marcus -LiGi- B&uuml;schleb < mail: ligi (at) polymap (dot) de >
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a> 
 */
public class SymbolizerWrapper {

	private Symbolizer symbolizer;
	
	// context
	private ILayer layer=null;
	private RuleWrapper rule;
	private static StyleBuilder sb=new StyleBuilder();
	
	
	static StyleFactory styleFactory = CommonFactoryFinder
	.getStyleFactory(null);
	
	
	// remember the last halo to have the last on enabling / disabling
	private Halo last_halo=null;
	
	public final static int MARK_CIRCLE=0;
	public final static int MARK_SQUARE=1;
	public final static int MARK_TRIANGLE=2;
	public final static int MARK_STAR=3;
	public final static int MARK_CROSS=4;
	public final static int MARK_X=5;
	public final static int MARK_UNKNOWN=6;
	
	
	public final static int LINECAP_BUTT=0;
	public final static int LINECAP_ROUND=1;
	public final static int LINECAP_SQUARE=2;
	
	
	public final static int LINESTYLE_SOLID=0;
	public final static int LINESTYLE_DOT=1;
	public final static int LINESTYLE_DASH=2;
	public final static int LINESTYLE_DASHDOT=3;
	public final static int LINESTYLE_LONGDASH=4;
	
	public final static float[] STROKE_STYLE_ARR_SOLID = new float[]{10.0f,0.0f};
	public final static float[] STROKE_STYLE_ARR_DOT = new float[]{1.0f,10.0f};
	public final static float[] STROKE_STYLE_ARR_DASH=new float[]{10.0f,10.0f};
	public final static float[] STROKE_STYLE_ARR_DASHDOT=new float[]{10.0f,1.0f,1.0f,1.0f,10.0f};
	public final static float[] STROKE_STYLE_ARR_LONGDASH=new float[]{20.0f,10.0f};


	public SymbolizerWrapper(Symbolizer symbolizer, ILayer layer, RuleWrapper rule) {
		this.symbolizer = symbolizer;
		this.layer = layer;
		this.rule = rule;
	}
	
	public SymbolizerWrapper(Symbolizer symbolizer) {
		this.symbolizer = symbolizer;
	}
	
	public void setMark(int type) {
		
		if (isPointSymbolizer())
		{
			
			Fill tmp_fill=getFill();
			Stroke tmp_stroke=getStroke();
			
		
			switch(type) {
			case MARK_CIRCLE:
				((PointSymbolizer) symbolizer).getGraphic().graphicalSymbols().set(0,styleFactory.getCircleMark());
				break;
			
			case MARK_SQUARE:
				((PointSymbolizer) symbolizer).getGraphic().graphicalSymbols().set(0,styleFactory.getSquareMark());
				break;
			
			case MARK_TRIANGLE:
				((PointSymbolizer) symbolizer).getGraphic().graphicalSymbols().set(0,styleFactory.getTriangleMark());
				break;
			

			case MARK_STAR:
				((PointSymbolizer) symbolizer).getGraphic().graphicalSymbols().set(0,styleFactory.getStarMark());
				break;
				
			case MARK_CROSS:
				((PointSymbolizer) symbolizer).getGraphic().graphicalSymbols().set(0,styleFactory.getCrossMark());
				break;
				
			case MARK_X:
				((PointSymbolizer) symbolizer).getGraphic().graphicalSymbols().set(0,styleFactory.getXMark());
				break;
			
			}
		
			/*((PointSymbolizer) symbolizer).getGraphic().graphicalSymbols().set(0,
			(Mark)styleFactory.externalMark(null, "a", 1) );
			*/
			
			setFill(tmp_fill);
			setStroke(tmp_stroke);
			
		}
		
	}
	
	public int getMarkType() {
		GraphicalSymbol gs=((PointSymbolizer) symbolizer).getGraphic().graphicalSymbols().get(0);
		String mark_name=""+((Mark)gs).getWellKnownName();
		
		if (mark_name.equals("Circle"))
			return MARK_CIRCLE;
		
		if (mark_name.equals("Square"))
			return MARK_SQUARE;

		if (mark_name.equals("Triangle"))
			return MARK_TRIANGLE;

		if (mark_name.equals("Star"))
			return MARK_STAR;

		if (mark_name.equals("Cross"))
			return MARK_CROSS;

		if (mark_name.equals("X"))
			return MARK_X;
		
		return MARK_UNKNOWN; 
	}
	
	public double getMarkSize() {
		return getExpressionValue( ((PointSymbolizer) symbolizer).getGraphic().getSize());
	}

	public double getMarkRotation() {
		return getExpressionValue( ((PointSymbolizer) symbolizer).getGraphic().getRotation());
	}

	public void setMarkSize(double size) {
		((PointSymbolizer) symbolizer).getGraphic().setSize(sb.literalExpression(size));
	}
	
	public void setMarkRotation(double rot) {
		((PointSymbolizer) symbolizer).getGraphic().setRotation(sb.literalExpression(rot));
	}
	
	public Symbolizer getSymbolizer() {
		
		return this.symbolizer;
	}
	
	
	public String[] getFeatureAttributes() {
		String[] res = null;
		FeatureSource fs;
		try {
            // XXX don't create a new fs every time once the fs of the layer is available via other API
            fs = PipelineFeatureSource.forLayer( layer, false );

			SimpleFeatureType schema = (SimpleFeatureType) fs.getSchema();
			// log.debug( "### Schema: type name: " + schema.getTypeName() );
			GeometryDescriptor geom = fs.getSchema().getGeometryDescriptor();
			// log.debug( "    Geometry: name=" + geom.getLocalName() +
			// ", type=" + geom.getType().getName() );
			int i = 0;
			res = new String[schema.getAttributeDescriptors().size()];
			for (AttributeDescriptor attr : schema.getAttributeDescriptors()) {
				res[i++] = "" + attr.getName();
				// log.debug( "    Attribute: name=" + attr.getName() +
				// ", type=" + attr.getType().getName() );
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}
      
	public boolean isPointSymbolizer() { 
		return (symbolizer.getClass() == org.geotools.styling.PointSymbolizerImpl.class);
	}
	
	public boolean isPolygonSymbolizer() {
		 return (symbolizer.getClass() == org.geotools.styling.PolygonSymbolizerImpl.class);
	}
	public boolean isLineSymbolizer() {
		return (symbolizer.getClass() == org.geotools.styling.LineSymbolizerImpl.class);
	}
	
	public boolean isTextSymbolizer() {
		return (symbolizer.getClass() == org.geotools.styling.TextSymbolizerImpl.class);
	}
	
	public boolean hasFill() {
		return (getFill()!=null);
	}
	
	public Fill getFill() {
		if (isPolygonSymbolizer())
			return (((PolygonSymbolizer) symbolizer).getFill());
		else if (isPointSymbolizer())
			return (((PointSymbolizer) symbolizer).getGraphic().getMarks()[0].getFill());
		
		else if (isTextSymbolizer())
			return (((TextSymbolizer) symbolizer).getFill());
		
		
		return null;
	}
	
	public void setFill(Fill new_fill) {
		if (isPolygonSymbolizer())
			((PolygonSymbolizer) symbolizer).setFill(new_fill);
		else if (isPointSymbolizer())
			((PointSymbolizer) symbolizer).getGraphic().getMarks()[0].setFill(new_fill);
		
	}
	
	
	public Fill getTextFill() {
		if (isTextSymbolizer())
			return (((TextSymbolizer) symbolizer).getFill());
		return null;
	}
	
	public boolean hasHalo() {
		return (getHalo()!=null);
	}
	
		
	public Halo getHalo() {
		return ((TextSymbolizer) symbolizer).getHalo();
	}

	public void setHalo(Halo halo2set) {
		last_halo=((TextSymbolizer) symbolizer).getHalo();
		((TextSymbolizer) symbolizer).setHalo(halo2set);
	}

	
	public void addDefaultHalo() {
		if (last_halo!=null)
			((TextSymbolizer) symbolizer).setHalo(last_halo);
		else
			((TextSymbolizer) symbolizer).setHalo(sb.createHalo(Color.BLACK,2.0 ));
	}
	
	public Fill getHaloFill() {
		
		if (isTextSymbolizer())
			return getHalo().getFill() ;
		
		return null;
	}
	

	public double getHaloRadius() {
		if (getHalo()!=null)  {
			try {
				return Double.parseDouble("" + getHalo().getRadius());		
			} catch (Exception e) {
				return 1.0;
			}
		} else return Double.NaN;
		
		
	}

	public double getExpressionValue(Expression expr) {
		try {
			return Double.parseDouble("" +expr);		
		} catch (Exception e) {
			return Double.NaN;
		}
	}
	
	public Font getFont() {
		System.out.println("getting font");
		if (isTextSymbolizer())
			return (((TextSymbolizer) symbolizer)).getFont();
		System.out.println("is not textsymboilizer font");
		return null;
	}
	
	
	public double getFillOpacity() {
		if (getFill()!=null)  {
			try {
				return Double.parseDouble("" + getFill().getOpacity());		
			} catch (Exception e) {
				return 1.0;
			}
		} else return Double.NaN;
	}
	
	public double getHaloOpacity() {
		if (getHalo()!=null)  {
			try {
				return Double.parseDouble("" + getHalo().getFill().getOpacity());		
			} catch (Exception e) {
				
				return 1.0;
			}
		} else return Double.NaN;
	}
	
	

	public boolean hasStroke() {
		return ( getStroke()!=null );
	}
	
	public boolean hasLabel() {
		return (isTextSymbolizer());
	}
	
	public String getLabel() {
		return ""+SLD.textLabel((TextSymbolizer)symbolizer);
	}
	
	/*
	public String getLabel() {
		((TextSymbolizer)symbolizer).getLabel()
	}
	*/
	
	public void setLabel(Expression  label) {
		((TextSymbolizer)symbolizer).setLabel(label);
	}
	
	public final static int ALIGN_LEFT=0;
	public final static int ALIGN_CENTER=1;
	public final static int ALIGN_RIGHT=2;
	
	public final static int ALIGN_TOP=0;
	public final static int ALIGN_MIDDLE=1;
	public final static int ALIGN_BOTTOM=2;
	
	public void dispose() {
		int index=rule.getRule().symbolizers().indexOf(symbolizer);
		rule.getRule().symbolizers().remove(index);
	}
	
	public int getLabelPlacementX() {
		LabelPlacement lp=((TextSymbolizer)symbolizer).getLabelPlacement();
		
		String Xalign= ""+((org.geotools.styling.PointPlacement)lp).getAnchorPoint().getAnchorPointX();
			
		if (Xalign.equals(""+SLD.ALIGN_LEFT))
			return ALIGN_LEFT;
		
		if (Xalign.equals(""+SLD.ALIGN_RIGHT))
			return ALIGN_RIGHT;
			
		return ALIGN_CENTER;
	}

	public int getLabelPlacementY() {
		LabelPlacement lp=((TextSymbolizer)symbolizer).getLabelPlacement();
		
		String Yalign= ""+((org.geotools.styling.PointPlacement)lp).getAnchorPoint().getAnchorPointY();
			
		if (Yalign.equals(""+SLD.ALIGN_TOP))
			return ALIGN_TOP;
		
		if (Yalign.equals(""+SLD.ALIGN_BOTTOM))
			return ALIGN_BOTTOM;
			
		return ALIGN_MIDDLE;
	}

	
	public Stroke getStroke() {
		if (isPolygonSymbolizer())
			return ((PolygonSymbolizer) symbolizer).getStroke();
		else if (isLineSymbolizer())
			return ((LineSymbolizer) symbolizer).getStroke();
		else if (isPointSymbolizer())
			return (((PointSymbolizer) symbolizer).getGraphic().getMarks()[0].getStroke());
		else return null;
	}
	
	
	public void setStroke(Stroke new_stroke) {
		if (isPolygonSymbolizer())
			((PolygonSymbolizer) symbolizer).setStroke(new_stroke);
		else if (isLineSymbolizer())
			((LineSymbolizer) symbolizer).setStroke(new_stroke);
		else if (isPointSymbolizer())
			((PointSymbolizer) symbolizer).getGraphic().getMarks()[0].setStroke(new_stroke);
	
	}
	
	
	
	
	
	
	
	public double getStrokeOpacity() {
		try {
			return Double.parseDouble(""+ getStroke().getOpacity());
		} catch (Exception e) {
			return 1.0;
		}
	}	
	
	public double getStrokeWidth() {
		try {
			return  Double.parseDouble(""+getStroke().getWidth());
		} catch (Exception e) {
			return 1.0;
		}
	}	
	
	private boolean compareFloatArrs(float[] left,float[] right){
		if (left.length!=right.length)
			return false;
		
		for ( int i=0;i<left.length;i++) 
			if (left[i]!=right[i])
				return false;
		
		return true;
	}
	public int getStrokeStyleId() {
		try {
			if (compareFloatArrs(getStroke().getDashArray(),STROKE_STYLE_ARR_SOLID))
				return LINESTYLE_SOLID;
			if (compareFloatArrs(getStroke().getDashArray(),STROKE_STYLE_ARR_DOT))
				return LINESTYLE_DOT;
			if (compareFloatArrs(getStroke().getDashArray(),STROKE_STYLE_ARR_DASH))
				return LINESTYLE_DASH;
			if (compareFloatArrs(getStroke().getDashArray(),STROKE_STYLE_ARR_DASHDOT))
				return LINESTYLE_DASHDOT;
			if (compareFloatArrs(getStroke().getDashArray(),STROKE_STYLE_ARR_LONGDASH))
				return LINESTYLE_LONGDASH;

		} catch (Exception e) {
			return 0;
		}
		
		return 0;
	}	
	public String getStrokeLineCap() {
		try {
			return  ""+getStroke().getLineCap();
		} catch (Exception e) {
			return "butt";
		}
		
		
	}	
	
	public Color getStrokeColor() {
		return SLD.color(getStroke().getColor());
	}

	public Color getFillColor() {
		return SLD.color(getFill().getColor());
	}

	public static boolean isObjSymbolizer(Object obj) {
		try {
			SymbolizerWrapper w=new SymbolizerWrapper((Symbolizer)obj);
			return (w.isLineSymbolizer() | w.isPointSymbolizer() | w.isPolygonSymbolizer() | w.isTextSymbolizer() );
		}
		catch(Exception e) {
			return false;	
		}
	}
	
	public void setName(String name) {
		if (isPolygonSymbolizer())
			((PolygonSymbolizer) symbolizer).setName(name);
		else if (isTextSymbolizer())
			((TextSymbolizer) symbolizer).setName(name);
		else if (isPointSymbolizer())
			((PointSymbolizer) symbolizer).setName(name);
		else if (isLineSymbolizer())
			((LineSymbolizer) symbolizer).setName(name);
	}

	
	public String getName() {
		String res=null;
		
		if (isPolygonSymbolizer())
			res= ((PolygonSymbolizer) symbolizer).getName();
		else if (isTextSymbolizer())
			res= ((TextSymbolizer) symbolizer).getName();
		else if (isPointSymbolizer())
			res= ((PointSymbolizer) symbolizer).getName();
		else if (isLineSymbolizer())
			res= ((LineSymbolizer) symbolizer).getName();
		
		if (res==null)	{ // symbolizer is unnamed - give it a name
			res= SymbolizerHelper.Symbolizer2TypeName(symbolizer);
			setName(res);
			}
		
		return res;
		
	}
	
	
	public boolean wrapsSymbolizer(Object o) {
		return o==symbolizer;
	}


	public RuleWrapper getRuleWrapper() {
		return rule;
	}

}
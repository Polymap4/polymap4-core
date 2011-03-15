package org.polymap.styler;

import java.awt.Color;

import org.geotools.factory.CommonFactoryFinder;

import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.TextSymbolizer;
import org.opengis.filter.FilterFactory;
import org.opengis.style.Font;

public class DefaultSymbolizers {

	static StyleFactory styleFactory = CommonFactoryFinder
			.getStyleFactory(null);

	static FilterFactory filterFactory = CommonFactoryFinder
			.getFilterFactory(null);

	public static PolygonSymbolizer getDefaultPolygonSymbolizer() {
		// create a partially opaque outline stroke
		Stroke stroke = styleFactory.createStroke(filterFactory
				.literal(Color.DARK_GRAY), filterFactory.literal(0.5),
				filterFactory.literal(0.5));

		// create a partial opaque fill
		Fill fill = styleFactory.createFill(filterFactory.literal(Color.CYAN),
				filterFactory.literal(0.5));

		/*
		 * Setting the geometryPropertyName arg to null signals that we want to
		 * draw the default geometry of features
		 */
		return styleFactory.createPolygonSymbolizer(stroke, fill, null);

	}

	public static TextSymbolizer getDefaultTextSymbolizer() {

		// create a partial opaque fill
		Fill fill = styleFactory.createFill(filterFactory.literal(Color.BLACK),
				filterFactory.literal(0.5));

		/*
		 * Setting the geometryPropertyName arg to null signals that we want to
		 * draw the default geometry of features
		 */
		TextSymbolizer sym = styleFactory.createTextSymbolizer();
		sym.setFill(fill);
		Font font = styleFactory.createFont(filterFactory.literal("Arial"),
				filterFactory.literal(true), filterFactory.literal(true),
				filterFactory.literal(7));

		sym.setFont(font);
		sym.setLabel(filterFactory.literal(""));

		return sym;
	}

	public static PointSymbolizer getDefaultPointSymbolizer() {
		Graphic gr = styleFactory.createDefaultGraphic();

		Mark mark = styleFactory.getCircleMark();

		mark.setStroke(styleFactory.createStroke(filterFactory
				.literal(Color.BLUE), filterFactory.literal(1)));

		mark
				.setFill(styleFactory.createFill(filterFactory
						.literal(Color.CYAN)));

		gr.graphicalSymbols().clear();
		gr.graphicalSymbols().add(mark);
		gr.setSize(filterFactory.literal(5));

		/*
		 * Setting the geometryPropertyName arg to null signals that we want to
		 * draw the default geometry of features
		 */
		return styleFactory.createPointSymbolizer(gr, null);
	}

	public static LineSymbolizer getDefaultLineSymbolizer() {
		Stroke stroke = styleFactory.createStroke(filterFactory
				.literal(Color.DARK_GRAY), filterFactory.literal(0.5));

		/*
		 * Setting the geometryPropertyName arg to null signals that we want to
		 * draw the default geometry of features
		 */
		return styleFactory.createLineSymbolizer(stroke, null);
	}
}

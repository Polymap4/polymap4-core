package org.polymap.styler;


import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.FeatureTypeStyle;


public class DefaultFeatureTypeStyle {

	static StyleFactory styleFactory = CommonFactoryFinder
			.getStyleFactory(null);
	
	public static FeatureTypeStyle getPlainFeatureTypeStyle() {
		FeatureTypeStyle fts= styleFactory.createFeatureTypeStyle();
		fts.setName(Messages.get().NEW_FTS);
		return fts;
	}
}

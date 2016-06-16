package org.polymap.core.style.serialize.sld;

import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.opengis.filter.expression.Expression;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.Lists;

import org.polymap.core.style.model.ScaleMappedNumbers;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.FeatureStyleSerializer.OutputFormat;

public class ScaleMappedNumbersHandler
        extends StylePropertyValueHandler<ScaleMappedNumbers,Object> {

    @Override
    public <SD extends SymbolizerDescriptor> List<SD> doHandle( Context context, ScaleMappedNumbers spv, SD sd,
            Setter<SD> setter ) {
        Iterator<Number> scales = spv.scales.iterator();
        Iterator<Number> values = spv.numberValues.iterator();
        Number defaultValue = (Number)spv.defaultNumberValue.get();

        if (context.outputFormat.get().equals( OutputFormat.GEOSERVER )) {
            Expression ife = ff.literal( defaultValue );
            List<Expression> allExpressions = Lists.newArrayList(
                    ff.function( "env", ff.literal( "wms_scale_denominator" ) ), ff.literal( defaultValue ) );

            while (scales.hasNext()) {
                assert values.hasNext();
                allExpressions.add( ff.literal( scales.next() ) );
                allExpressions.add( ff.literal( values.next() ) );
            }
            ife = ff.function( "categorize", allExpressions.toArray( new Expression[allExpressions.size()] ) );
            setter.set( sd, ife );
            return Collections.singletonList( sd );
        }

        List<SD> result = Lists.newArrayList();
        Number lastScale = null;
        while (scales.hasNext()) {
            assert values.hasNext();

            SD clone = (SD)sd.clone();

            Number currentScale = scales.next();
            // first entry with default scale
            if (lastScale == null) {
                clone.scale.set( ImmutablePair.of( null, currentScale.intValue() ) );
                setter.set( clone, ff.literal( defaultValue ) );
            }
            else {
                // regular between scales, between greater than last scale and less
                // then or equal current scale, expect the last scale
                clone.scale.set( ImmutablePair.of( lastScale.intValue() + 1,
                        scales.hasNext() ? currentScale.intValue() : currentScale.intValue() - 1 ) );
                setter.set( clone, ff.literal( values.next() ) );
            }
            result.add( clone );
            lastScale = currentScale;
        }
        // last scale with a lowerbound only
        if (values.hasNext()) {
            SD clone = (SD)sd.clone();
            clone.scale.set( ImmutablePair.of( lastScale.intValue(), null ) );
            setter.set( clone, ff.literal( values.next() ) );
            result.add( clone );
        }
        return result;
    }
}
# Styler framework

Main objectives of the styler framework are:

  - allow the user to build **complex styles** with a **simple UI** (really)
  - allow the user to start very simple and get more complex just when necessary
  - **different** target systems: GeoTools/SLD/SE and OpenLayers for example 

Examples of **complex styles** are clustering and styling depending on feature properties or the current scale of the map. And, allow to easily **mix** such complex styles! For example having the width of a line depending on current scale **and** the color depending on the value of a feature attribute.

**SLD** and the GeoTools style implementation are no suited to let end users model styles with it. The basic structure of SLD, a rule with scale dominators and filters defines the symbolizer to use, is great to implement a styler but is the exactly opposite of the mindset of the user. The user does not want to fiddle with filters "just" to make a the color of a feature depending on a given attribute.

## Basic concept

Given the above analysis, the **model** of the styler framework is made up of two main classes *Style* and *StylePropertyValue*. Sub-classes of Style, like *PointStyle* and *LineStyle* are compositions of StylePropertyValues. Every StylePropertyValue describes one aspect of the visual rendering of the feature. 

For example, **PointStyle** exposes a property *diameter* with target type *double* like this:

```java
    @UIOrder(10)
    @Description("diameter")
    @NumberRange(defaultValue = 8, to=100, increment=1.0)
    @Concerns(StylePropertyChange.Concern.class)
    public Property<StylePropertyValue<Double>> diameter;
```

This *diameter* member can by set to be an *ConstantNumber* or a *ScaleMappedNumber*. Both can operate on the target type *double*. Both have their own editor in the UI. *ConstantNumber* provides just, well, a constant number. *ScaleMappedNumber* is more complex, it provides a map from scales into values. This allows the user to express idea of using a given, concrete number on a given map scale. By attaching such a *ScaleMappedNumber* with concreate numbers to the diameter property of a PointStyle one can make the diameter dependent on the current scale of the map.

A *ConstantNumber* or *ScaleMappedNumber* can be used wherever a double is requested in a Style. For example for line width or font size.

So, in short, the model describes the visual representation (aka the style) of a layer and its data. This is **not tied** to any particular backend (SLD, OpenLayers). It rather models the "user experience" we want to achieve.

Serializing the model to a particular backend system is done a different *serializers*.

## Implementation

### References

- SLD 
  - Spec: http://www.opengeospatial.org/standards/sld
  - GeoServer: http://docs.geoserver.org/latest/en/user/styling/sld-reference/
- OpenLayers
  - http://docs.openlayers.org/library/feature_styling.html



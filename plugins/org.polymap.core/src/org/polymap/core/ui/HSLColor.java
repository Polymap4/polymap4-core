package org.polymap.core.ui;

import java.awt.Color;

/**
 * The HSLColor class provides methods to manipulate HSL (Hue, Saturation Luminance)
 * values to create a corresponding Color object using the RGB ColorSpace.
 *
 * The HUE is the color, the Saturation is the purity of the color (with respect to
 * grey) and Luminance is the brightness of the color (with respect to black and
 * white)
 *
 * The Hue is specified as an angel between 0 - 360 degrees where red is 0, green is
 * 120 and blue is 240. In between you have the colors of the rainbow. Saturation is
 * specified as a percentage between 0 - 100 where 100 is fully saturated and 0
 * approaches gray. Luminance is specified as a percentage between 0 - 100 where 0 is
 * black and 100 is white.
 *
 * In particular the HSL color space makes it easier change the Tone or Shade of a
 * color by adjusting the luminance value.
 * 
 * @author https://tips4java.wordpress.com/2009/07/05/hsl-color/
 * @author Falko Bräutigam
 */
public class HSLColor
        implements Cloneable {

    private float[]     hsl = {0, 0, 0};

    private float       alpha = 0;

    public HSLColor( Color color ) {
        float[] rgb = color.getRGBColorComponents( null );
        hsl = fromRGB( rgb[0], rgb[1], rgb[2] );
        alpha = color.getAlpha() / 255.0f;
    }

    public HSLColor( org.eclipse.swt.graphics.Color color ) {
        float f = 1f / 0xff;
        hsl = fromRGB( f*color.getRed(), f*color.getGreen(), f*color.getBlue() );
        alpha = 1;
    }

    public HSLColor( int red, int green, int blue ) {
        float f = 1f / 0xff;
        hsl = fromRGB( f*red, f*green, f*blue );
        alpha = 1;
    }

    public HSLColor( HSLColor rhs ) {
        System.arraycopy( rhs.hsl, 0, hsl, 0, 3 );
        alpha = rhs.alpha;
    }

    @Override
    protected HSLColor clone() {
        return new HSLColor( this );
    }

    protected float adjust( float value, float delta ) {
        assert delta >= -100 && delta <= 100 : "Invalid delta: " + delta;
        return Math.min( 100f, Math.max( 0f, value + delta ) );        
    }
    
    /**
     * Create a new instance with a different Hue value.
     *
     * @param delta The delta to be applied to the hue value.
     * @return Newly created instance with adjusted values;
     */
    public HSLColor adjustHue( float delta ) {
        HSLColor result = clone();
        result.hsl[0] = adjust( result.hsl[0], delta );
        return result;
    }

    /**
     * Create a new instance with a different Luminance value.
     *
     * @param delta [0..100] The delta to be applied to the Luminance value.
     * @return Newly created instance with adjusted values;
     */
    public HSLColor adjustLuminance( float delta ) {
        HSLColor result = clone();
        result.hsl[2] = adjust( result.hsl[2], delta );
        return result;
    }

    /**
     * Create a new instance with a different Saturation value.
     *
     * @param delta The delta to be applied to the Saturation value.
     * @return Newly created instance with adjusted values;
     */
    public HSLColor adjustSaturation( float delta ) {
        HSLColor result = clone();
        result.hsl[1] = adjust( result.hsl[1], delta );
        return result;
    }

    /**
     * Create a RGB Color object based on this HSLColor with a different Shade.
     * Changing the shade will return a darker color. The percent specified is a
     * relative value.
     *
     * @param percent - the value between 0 - 100
     * @return Newly created instance with adjusted values;
     */
    public HSLColor adjustShade( float percent ) {
        HSLColor result = clone();
        float multiplier = (100.0f - percent) / 100.0f;
        result.hsl[2] = Math.max( 0.0f, hsl[2] * multiplier );
        return result;
    }

    /**
     * Create a RGB Color object based on this HSLColor with a different Tone.
     * Changing the tone will return a lighter color. The percent specified is a
     * relative value.
     *
     * @param percent - the value between 0 - 100
     * @return the RGB Color object
     */
    public HSLColor adjustTone( float percent ) {
        HSLColor result = clone();
        float multiplier = (100.0f + percent) / 100.0f;
        result.hsl[2] = Math.min( 100.0f, hsl[2] * multiplier );
        return result;
    }

    /**
     * Create a RGB Color object that is the complementary color of this HSLColor.
     * This is a convenience method. The complementary color is determined by adding
     * 180 degrees to the Hue value.
     * 
     * @return the RGB Color object
     */
    public HSLColor complementary() {
        HSLColor result = clone();
        result.hsl[0] = (hsl[0] + 180.0f) % 360.0f;
        return result;
    }

    public float hue() {
        return hsl[0];
    }

    public float[] hsl() {
        return hsl;
    }

    public float luminance() {
        return hsl[2];
    }

    public float saturation() {
        return hsl[1];
    }

    public float alpha() {
        return alpha;
    }

    /**
     * Get the RGB Color object represented by this HDLColor.
     *
     * @return the RGB Color object.
     */
    public Color toAWT() {
        float[] rgb = toRGB();
        return new Color( rgb[0], rgb[1], rgb[2] );
    }

    public org.eclipse.swt.graphics.Color toSWT() {
        float[] rgb = toRGB();
        return UIUtils.getColor( (int)(rgb[0]*0xff), (int)(rgb[1]*0xff), (int)(rgb[2]*0xff) );
    }

//    public RGB toRGB() {
//        float[] rgb = toRGB();
//        return new RGB( rgb[0], rgb[1], rgb[2] );
//    }

    public String toString() {
        return "HSLColor[h=" + hsl[0] + ",s=" + hsl[1] + ",l=" + hsl[2] + ",alpha=" + alpha + "]";
    }


    /**
     * Convert a RGB Color to it corresponding HSL values.
     *
     * @return an array containing the 3 HSL values.
     */
    public static float[] fromRGB( float r, float g, float b ) {        
        // Minimum and Maximum RGB values are used in the HSL calculations
        float min = Math.min( r, Math.min( g, b ) );
        float max = Math.max( r, Math.max( g, b ) );
    
        // Calculate the Hue
        float h = 0;
    
        if (max == min)
            h = 0;
        else if (max == r)
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        else if (max == g)
            h = (60 * (b - r) / (max - min)) + 120;
        else if (max == b)
            h = (60 * (r - g) / (max - min)) + 240;
    
        // Calculate the Luminance
    
        float l = (max + min) / 2;
    
        // Calculate the Saturation
    
        float s = 0;
    
        if (max == min)
            s = 0;
        else if (l <= .5f)
            s = (max - min) / (max + min);
        else
            s = (max - min) / (2 - max - min);
    
        return new float[] { h, s * 100, l * 100 };
    }

    /**
     * Convert HSL values to a RGB Color.
     *
     * @param h Hue is specified as degrees in the range 0 - 360.
     * @param s Saturation is specified as a percentage in the range 1 - 100.
     * @param l Lumanance is specified as a percentage in the range 1 - 100.
     * @param alpha the alpha value between 0 - 1
     *
     * @returns the RGB Color object
     */
    protected float[] toRGB() {
        assert hsl[1] >= 0.0f && hsl[1] <= 100.0f;
        assert hsl[2] >= 0.0f && hsl[2] <= 100.0f;
        assert alpha >= 0.0f && alpha <= 1.0f;
    
        // Formula needs all values between 0 - 1.
    
        float h = hsl[0] % 360.0f;
        h /= 360f;
        float s = hsl[1] / 100f;
        float l = hsl[2] / 100f;
    
        float q = 0;
    
        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);
    
        float p = 2 * l - q;
    
        float r = Math.max( 0, hueToRGB( p, q, h + (1.0f / 3.0f) ) );
        float g = Math.max( 0, hueToRGB( p, q, h ) );
        float b = Math.max( 0, hueToRGB( p, q, h - (1.0f / 3.0f) ) );
    
        r = Math.min( r, 1.0f );
        g = Math.min( g, 1.0f );
        b = Math.min( b, 1.0f );
    
        return new float[] {r, g, b, alpha};
    }

    protected float hueToRGB( float p, float q, float h ) {
        if (h < 0)
            h += 1;

        if (h > 1)
            h -= 1;

        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1) {
            return q;
        }

        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }

        return p;
    }
    
    
    // Test ***********************************************
    
    public static void main( String[] args ) throws Exception {
        HSLColor c = new HSLColor( 200, 100, 100 );
        System.out.println( c + " -> " + c.toAWT() );
        
        HSLColor l = c.adjustLuminance( 10 );
        System.out.println( l + " -> " + l.toAWT() );
        
        HSLColor s = c.adjustSaturation( 10 );
        System.out.println( s + " -> " + s.toAWT() );
    }
}
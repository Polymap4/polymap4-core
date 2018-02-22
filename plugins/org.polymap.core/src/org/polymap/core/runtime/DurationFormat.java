/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.runtime;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.time.Duration;

/**
 * Parse and format {@link Duration} with just one, simple pattern: 123d(ay) 12h(our) 12m(in) 12s(ec) 
 *
 * @author Falko Bräutigam
 */
public class DurationFormat
        extends Format {

    public final Pattern PATTERN = Pattern.compile( 
            "((\\d{1,3})d(ay)*)*\\s*" +   // 123d(ay) 
            "((\\d{1,2})h(our)*)*\\s*" +  // 12h(our) 
            "((\\d{1,2})m(in)*)*\\s*" +   // 12m(in) 
            "((\\d{1,2})s(ec)*)*\\s*",    // 12s(ec) 
            Pattern.CASE_INSENSITIVE );

    public static DurationFormat getInstance( Locale locale ) {
        return new DurationFormat();
    }

    
    // instance *******************************************
    
    @Override
    public StringBuffer format( Object obj, StringBuffer result, FieldPosition pos ) {
        Duration v = (Duration)obj;
        if (v.toDays() > 0) {
            result.append( v.toDays() ).append( "d" );
            v = v.minusDays( v.toDays() );
        }
        if (v.toHours() > 0) {
            result.append( result.length() > 0 ? " ":"" ).append( v.toHours() ).append( "h" );
            v = v.minusHours( v.toHours() );
        }
        if (v.toMinutes() > 0) {
            result.append( result.length() > 0 ? " ":"" ).append( v.toMinutes() ).append( "m" );
            v = v.minusMinutes( v.toMinutes() );
        }
        if (v.getSeconds() > 0) {
            result.append( result.length() > 0 ? " ":"" ).append( v.getSeconds() ).append( "s" );
        }
        return result;
    }

    
    public Optional<Duration> parse( String source ) {
        return Optional.ofNullable( (Duration)parseObject( source, null ) );
    }

    
    @Override
    public Object parseObject( String source, ParsePosition pos ) {
        assert pos == null || pos.getIndex()==0 : "ParsePosition not yet supported.";
        
        Matcher matcher = PATTERN.matcher( source );
        if (matcher.matches()) {
            return Duration.ofDays( parseLong( defaultIfBlank( matcher.group( 2 ), "0" ) ) )
                    .plus( Duration.ofHours( parseLong( defaultIfBlank( matcher.group( 5 ), "0" ) ) ) )
                    .plus( Duration.ofMinutes( parseLong( defaultIfBlank( matcher.group( 8 ), "0" ) ) ) )
                    .plus( Duration.ofSeconds( parseLong( defaultIfBlank( matcher.group( 11 ), "0" ) ) ) );
        }
        else {
            return null;
        }
    }
        
}

package com.brentcroft.tools;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;


/**
 * Utility class for up-casting String values to other desired Types.
 * <p/>
 *
 * @author ADobson
 */
public class StringUpcaster
{
    public static < T > T upcast( final String value, final Class< T > type )
    {
        return upcast( value, type, null );
    }


    public static < T > T upcast( final String value, final Class< T > type, String defaultValue )
    {
        if ( type == null )
        {
            throw new NullPointerException( "type cannot be null" );
        }

        if ( value != null && ! value.trim().isEmpty() )
        {
            return convert( type, value );
        }
        else if ( defaultValue != null )
        {
            return convert( type, defaultValue );
        }
        else
        {
            return null;
        }
    }


    public static < T > T[] upcast( final String[] values, final Class< T > type )
    {
        if ( type == null )
        {
            throw new NullPointerException( "type cannot be null" );
        }

        if ( values != null )
        {
            @SuppressWarnings( "unchecked" ) final T[] a = ( T[] ) Array.newInstance( type, values.length );

            for ( int i = 0, n = values.length; i < n; i++ )
            {
                a[ i ] = convert( type, values[ i ] );
            }

            return a;
        }
        else
        {
            return null;
        }
    }


    @SuppressWarnings( {"unchecked", "rawtypes"} )
    private static < T > T convert( final Class< T > type, final String rawValue )
    {
        if ( type == String.class )
        {
            return type.cast( rawValue );
        }

        if ( type == Integer.class )
        {
            return type.cast( Integer.valueOf( rawValue ) );
        }
        else if ( type == Long.class )
        {
            return type.cast( Long.valueOf( rawValue ) );
        }
        else if ( type == Boolean.class )
        {
            return type.cast( Boolean.valueOf( rawValue ) );
        }
        else if ( type == Double.class )
        {
            return type.cast( Double.valueOf( rawValue ) );
        }
        else if ( type == Float.class )
        {
            return type.cast( Float.valueOf( rawValue ) );
        }
        else if ( type == Short.class )
        {
            return type.cast( Short.valueOf( rawValue ) );
        }
        else if ( type == Byte.class )
        {
            return type.cast( Byte.valueOf( rawValue ) );
        }
        else if ( type == TimeUnit.class )
        {
            return type.cast( TimeUnit.valueOf( rawValue ) );
        }
        else if ( type.isEnum() )
        {
            // This cast is necessary
            return type.cast( Enum.valueOf( ( Class< Enum > ) type, rawValue ) );
        }

        throw new IllegalArgumentException( "No conversion class found for type [" + type.getCanonicalName() + "]" );
    }

    public static Set< Integer > toIntegerSet( String text )
    {
        if ( Objects.isNull( text ) )
        {
            return Collections.emptySet();
        }

        final String[] codes = text.split( "\\s*,\\s*" );

        final boolean validCodes = ! ( codes.length == 1 && codes[ 0 ].length() == 0 );

        final Integer[] validIntegers = validCodes
                                        ? StringUpcaster.upcast( codes, Integer.class )
                                        : new Integer[]{0};

        return Objects.nonNull( validIntegers )
               ? new LinkedHashSet<>( Arrays.asList( validIntegers ) )
               : Collections.emptySet();
    }

    public static Set< String > toStringSet( String text )
    {
        return toStringSet( ',', text );
    }

    public static Set< String > toStringSet( char SEP, String text )
    {
        return ofNullable( text )
                .map( t -> ( Set< String > ) new LinkedHashSet<>(
                        asList(
                                text.split( format( "\\s*\\%s\\s*", SEP ) ) ) ) )
                .orElse( emptySet() )
                .stream()
                .filter( Objects::nonNull )
                .filter( s -> s.length() > 0 )
                .collect( Collectors.toSet() );
    }
}

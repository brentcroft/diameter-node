package com.brentcroft.tools.el;

import java.io.Console;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Random;

import static java.lang.String.format;

public class ELFunctions
{
    public static String username()
    {
        return System.getProperty( "user.name" );
    }

    public static String replaceAll( String source, String regex, String rep )
    {
        return Optional
                .ofNullable( source )
                .map( s -> s.replaceAll(
                        Optional
                                .ofNullable( regex )
                                .orElseThrow( () -> new RuntimeException( "regex cannot be null" ) ),
                        Optional
                                .ofNullable( rep )
                                .orElseThrow( () -> new RuntimeException( "replacement cannot be null" ) )
                ) )
                .orElseThrow( () -> new RuntimeException( "source cannot be null" ) );
    }


    public static Float boxFloat( Float f )
    {
        return f;
    }


    public static Double asDouble( Double f )
    {
        return f;
    }

    public static Double pow( Double base, Double exponent )
    {
        return Math.pow( base, exponent );
    }

    public static boolean fileExists( String filename )
    {
        return new File( filename ).exists();
    }


    public static String bytesAsString( byte[] bytes )
    {
        return new String( bytes );
    }

    public static String bytesAsString( byte[] bytes, String charset ) throws UnsupportedEncodingException
    {
        return new String( bytes, charset );
    }


    public static Random random()
    {
        return new Random();
    }

    public static String console( String prompt, String defaultValue )
    {
        Console console = System.console();

        if ( console == null )
        {
            return defaultValue;
        }

        return console.readLine( prompt );
    }

    public static char[] consolePassword( String prompt, char[] defaultValue )
    {
        Console console = System.console();

        if ( console == null )
        {
            return defaultValue;
        }

        return console.readPassword( prompt );
    }

    @Deprecated()
    public static String consolePasswordAsString( String prompt, String defaultValue )
    {
        Console console = System.console();

        if ( console == null )
        {
            return defaultValue;
        }

        return String.valueOf( console.readPassword( prompt ) );
    }


    public static void consoleFormat( String format, Object... args )
    {
        Console console = System.console();

        if ( console == null )
        {
            return;
        }

        console.format( format, args );
    }

    public static void systemOutPrintln( String format, Object... args )
    {
        System.out.println( format( format, args ) );
    }
}

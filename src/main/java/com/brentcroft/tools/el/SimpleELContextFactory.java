package com.brentcroft.tools.el;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.el.*;
import java.beans.FeatureDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;


/**
 * This is a factory for making our own ELContext objects.
 * <p/>
 * <p>
 * Its probably more complicated than we need, although it doesn't use a
 * RootPropertyMapper like the SimpleContext class in JUEL.
 * <p/>
 * <p>
 * Highly influenced by this article:
 *
 * <pre>
 * http://illegalargumentexception.blogspot.co.uk/2008/04/java-using-el-outside-j2ee.html
 * </pre>
 * <p/>
 * <p>
 * Also, worth looking at the source code in JUEL.
 * <p/>
 *
 * @author ADobson
 */
@Log4j2
public class SimpleELContextFactory implements ELContextFactory
{
    private static final Map< String, Method > mappedFunctions = new HashMap<>();

    public static void mapFunction( String prefixedName, Method staticMethod )
    {
        mappedFunctions.put( prefixedName, staticMethod );
    }

    public static void mapFunctions( Map< String, Method > functions )
    {
        mappedFunctions.putAll( functions );
    }

    static
    {
        /*
         * functions available in EL expressions
         */
        try
        {
            // see:
            // http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html
            mapFunction( "c:format", String.class.getMethod( "format", String.class, Object[].class ) );
            mapFunction( "c:replaceAll", ELFunctions.class.getMethod( "replaceAll", String.class, String.class, String.class ) );


            mapFunction( "c:parseBytes", ELFunctions.class.getMethod( "bytesAsString", byte[].class, String.class ) );
            mapFunction( "c:fileExists", ELFunctions.class.getMethod( "fileExists", String.class ) );

            // math functions
            // mapFunction( "c:min", Math.class.getMethod( "max",
            // Object.class, Object.class ) );
            // mapFunction( "c:max", Math.class.getMethod( "min",
            // Object.class, Object.class ) );
            // mapFunction( "c:round", Math.class.getMethod( "round",
            // Object.class ) );
            // mapFunction( "c:ceil", Math.class.getMethod( "ceil",
            // Object.class ) );
            // mapFunction( "c:floor", Math.class.getMethod( "floor",
            // Object.class ) );

            mapFunction( "c:int", Integer.class.getMethod( "valueOf", String.class ) );
            mapFunction( "c:double", Double.class.getMethod( "valueOf", String.class ) );
            mapFunction( "c:pow", Math.class.getMethod( "pow", double.class, double.class ) );

            // capture as float
            mapFunction( "c:float", ELFunctions.class.getMethod( "boxFloat", Float.class ) );
            mapFunction( "c:random", ELFunctions.class.getMethod( "random" ) );

            mapFunction( "c:username", ELFunctions.class.getMethod( "username" ) );

            mapFunction( "c:uuid", UUID.class.getMethod( "randomUUID" ) );
            mapFunction( "c:radix", Long.class.getMethod( "toString", long.class, int.class ) );

            mapFunction( "c:currentTimeMillis", System.class.getMethod( "currentTimeMillis" ) );

            mapFunction( "c:console", ELFunctions.class.getMethod( "console", String.class, String.class ) );
            mapFunction( "c:consolePassword", ELFunctions.class.getMethod( "consolePassword", String.class, char[].class ) );
            mapFunction( "c:consolePasswordAsString", ELFunctions.class.getMethod( "consolePasswordAsString", String.class, String.class ) );
            mapFunction( "c:consoleFormat", ELFunctions.class.getMethod( "consoleFormat", String.class, Object[].class ) );
            mapFunction( "c:println", ELFunctions.class.getMethod( "systemOutPrintln", String.class, Object[].class ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to initialise function map", e );
        }

        log.debug( SimpleELContextFactory::listMappedFunctions );
    }

    public static String listMappedFunctions()
    {
        return mappedFunctions
                .entrySet()
                .stream()
                .map( entry -> format( "\n  %1$-30s = %2$s", entry.getKey(), entry.getValue() ) )
                .collect( Collectors.joining() );
    }


    public ELContext getELContext( Map< ?, ? > rootObjects )
    {
        return new SimpleELContext( rootObjects );
    }


    public ELContext getELConfigContext()
    {
        return new RootELContext( null );
    }


    static class SimpleELContext extends ELContext
    {
        @Getter
        protected final FunctionMapper functionMapper = newFunctionMapper();

        @Getter
        protected final VariableMapper variableMapper = newVariableMapper();

        private final Map< ?, ? > rootObjects;

        protected ELResolver resolver;

        public SimpleELContext( Map< ?, ? > rootObjects )
        {
            this.rootObjects = rootObjects;
        }

        @Override
        public ELResolver getELResolver()
        {
            if ( resolver == null )
            {
                resolver = new CompositeELResolver()
                {
                    {
                        add( new SimpleELResolver( rootObjects ) );
                        add( new ArrayELResolver() );
                        add( new ListELResolver() );
                        add( new BeanELResolver() );
                        add( new MapELResolver() );
                        add( new ResourceBundleELResolver() );
                    }
                };
            }
            return resolver;
        }
    }

    static FunctionMapper newFunctionMapper()
    {
        return new FunctionMapper()
        {
            @Override
            public Method resolveFunction( String prefix, String localName )
            {
                return mappedFunctions.get( ( prefix == null ? "" : prefix + ":" ) + localName );
            }
        };
    }

    static class RootELContext extends SimpleELContext
    {
        public RootELContext( Map< ?, ? > rootObjects )
        {
            super( rootObjects );
        }
    }


    static class SimpleELResolver extends ELResolver
    {
        private final ELResolver delegate = new MapELResolver();

        private final Map< ?, ? > userMap;

        public SimpleELResolver( Map< ?, ? > rootObjects )
        {
            this.userMap = rootObjects;
        }

        @Override
        public Object getValue( ELContext context, Object base, Object property )
        {
            if ( base == null )
            {
                base = userMap;
            }
            return delegate.getValue( context, base, property );
        }

        @Override
        public Class< ? > getCommonPropertyType( ELContext context, Object arg1 )
        {
            return delegate.getCommonPropertyType( context, arg1 );
        }

        @Override
        public Iterator< FeatureDescriptor > getFeatureDescriptors( ELContext context, Object arg1 )
        {
            return delegate.getFeatureDescriptors( context, arg1 );
        }

        @Override
        public Class< ? > getType( ELContext context, Object arg1, Object arg2 )
        {
            return delegate.getType( context, arg1, arg2 );
        }

        @Override
        public boolean isReadOnly( ELContext context, Object arg1, Object arg2 )
        {
            return delegate.isReadOnly( context, arg1, arg2 );
        }

        @Override
        public void setValue( ELContext context, Object arg1, Object arg2, Object arg3 )
        {
            delegate.setValue( context, arg1, arg2, arg3 );
        }
    }


    static VariableMapper newVariableMapper()
    {
        return new VariableMapper()
        {
            private Map< String, ValueExpression > variableMap = Collections.emptyMap();

            @Override
            public ValueExpression resolveVariable( String name )
            {
                return variableMap.get( name );
            }

            @Override
            public ValueExpression setVariable( String name, ValueExpression variable )
            {
                if ( variableMap.isEmpty() )
                {
                    variableMap = new HashMap<>();
                }
                return variableMap.put( name, variable );
            }
        };
    }
}

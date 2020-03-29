package com.brentcroft.tools.jstl.tag;


import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;
import lombok.extern.log4j.Log4j2;

import javax.script.*;
import java.util.Map;

import static java.lang.String.format;

@Log4j2
public class JstlScript extends AbstractJstlElement
{
    private final static String TAG = "c:script";

    private final boolean isPublic;

    private final boolean renderOutput;

    private final ScriptEngine engine;

    private CompiledScript script;


    private final static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    public final static String DEFAULT_SCRIPT_ENGINE_NAME = "js";

    private final static ScriptEngine defaultScriptEngine = scriptEngineManager.getEngineByName( DEFAULT_SCRIPT_ENGINE_NAME );


    public JstlScript( boolean publicScope, boolean renderOutput, String engineName )
    {
        this.isPublic = publicScope;
        this.renderOutput = renderOutput;

        if ( DEFAULT_SCRIPT_ENGINE_NAME.equalsIgnoreCase( engineName ) )
        {
            engine = defaultScriptEngine;
        }
        else
        {
            engine = scriptEngineManager.getEngineByName( engineName );
        }

        if ( engine == null )
        {
            throw new RuntimeException( format( TagMessages.ENGINE_NAME_NOT_FOUND, engineName ) );
        }


        innerRenderable = new JstlTemplate( this );
    }

    private void compile()
    {
        try
        {
            final String source = innerRenderable.render( EMPTY_MAP );

            script = ( ( Compilable ) engine ).compile( source );
        }
        catch ( ScriptException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void normalize()
    {
        compile();
    }


    public String render( final Map< String, Object > bindings )
    {
        if ( isDeferred() )
        {
            return toText();
        }

        if ( script == null )
        {
            return "";
        }


        /*
         * problem is that Nashorn doesn't update bindings unless it created
         * them.
         *
         * http://stackoverflow.com/questions/24142979/reading-updated-variables-
         * after-evaluating-a-script
         */
        final Bindings engineBindings = engine.createBindings();


        if ( bindings instanceof MapBindings )
        {
            ( ( MapBindings ) bindings ).copyTo( engineBindings );
        }
        else
        {
            engineBindings.putAll( bindings );
        }

        try
        {
            Object result;

            // evaluate the script
            if ( isPublic && bindings instanceof Bindings )
            {
                result = script.eval( engineBindings );

                // need to copy back all first level members
                // this must be copying out loads of other crap
                // TODO: figure out how to handle bindings
                final String[] keys = engineBindings.keySet().toArray( new String[ 0 ] );

                for ( String key : keys )
                {
                    bindings.put( key, engineBindings.get( key ) );
                }
            }
            else
            {
                if ( ! ( bindings instanceof Bindings ) && isPublic )
                {
                    log.warn( () -> "Map is not an instance of script Bindings: public visibility not available!" );
                }

                result = script.eval( engineBindings );
            }

            return ( renderOutput && result != null ) ? result.toString() : "";
        }
        catch ( ScriptException e )
        {
            throw new RuntimeException( e );
        }
    }

    public String toText()
    {
        return String.format( "<%s%s%s>%s</%s>",
                TAG,
                ! isPublic ? "" : " public=\"true\"",
                ! renderOutput ? "" : " render=\"true\"",
                innerRenderable,
                TAG );
    }
}

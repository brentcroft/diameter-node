package com.brentcroft.tools.jstl.tag;


import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class JstlCatch extends AbstractJstlElement
{
    private final static String TAG = "c:script";

    private final String exceptionName;


    public JstlCatch( String exceptionName )
    {
        this.exceptionName = exceptionName;

        innerRenderable = new JstlTemplate( this );
    }

    public String render( Map< String, Object > bindings )
    {
        if ( isDeferred() )
        {
            return toText();
        }

        try
        {
            // protect external bindings from pollution in local scope
            return innerRenderable.render( new MapBindings( bindings ) );

        }
        catch ( Throwable t )
        {
            bindings.put( exceptionName, t );

            log.debug( () -> "Caught exception and inserted as [" + exceptionName + "]: " + t );

            return "";
        }
    }

    public String toText()
    {
        return String.format( "<%s var=\"%s\">%s</%s>", TAG, exceptionName, innerRenderable, TAG );
    }
}
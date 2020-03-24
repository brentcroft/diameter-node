package com.brentcroft.tools.jstl.tag;

import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;

import java.util.Map;

@Log4j2
public class JstlLog extends AbstractJstlElement
{
    private final static String TAG = "c:log";

    private final Level level;

    public JstlLog( Level level )
    {
        this.level = level;

        innerRenderable = new JstlTemplate( this );
    }


    public String render( Map< String, Object > bindings )
    {
        if ( isDeferred() )
        {
            return toText();
        }

        if ( log != null )
        {
            if ( log.getLevel().intLevel() >= level.intLevel() )
            {
                // protect external bindings from pollution in local scope
                final String msg = innerRenderable.render( new MapBindings( bindings ) );

                log.log( level, msg );
            }
        }
        return "";
    }


    public String toText()
    {
        return String.format( "<%s level=\"%s\">%s</%s>", TAG, level, innerRenderable, TAG );
    }
}

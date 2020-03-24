package com.brentcroft.tools.jstl.tag;


import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;

import java.util.Map;

public class JstlComment extends AbstractJstlElement
{
    private final static String TAG = "c:comment";

    public JstlComment()
    {
        innerRenderable = new JstlTemplate( this );
    }

    public String render( Map< String, Object > bindings )
    {
        // protect external bindings from pollution in local scope
        return "<!--" + innerRenderable.render( new MapBindings( bindings ) ) + "-->";
    }

    public String toText()
    {
        return String.format( "<%s>%s</%s>", TAG, innerRenderable, TAG );
    }

}
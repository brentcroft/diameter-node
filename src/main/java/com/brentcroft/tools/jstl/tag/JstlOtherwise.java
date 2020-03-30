package com.brentcroft.tools.jstl.tag;

import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Map;

public class JstlOtherwise extends AbstractJstlElement
{
    public final static String TAG = "c:otherwise";

    public JstlOtherwise()
    {
        innerRenderable = new JstlTemplate( this );
    }

    public String render( Map< String, Object > bindings )
    {
        if ( isDeferred() )
        {
            return toText();
        }
        // protect external bindings from pollution in local scope
        return innerRenderable.render( new MapBindings( bindings ) );
    }

    public String toText()
    {
        return String.format( "<%s>%s</%s>", TAG, innerRenderable, TAG );
    }

    @Override
    public void emitNodeEvents( Element element, Map< String, Object > bindings, JstlDocument.NodeListEmitter emitter ) throws SAXException
    {
        emitter.emitListEvents( element.getChildNodes(), bindings );
    }
}
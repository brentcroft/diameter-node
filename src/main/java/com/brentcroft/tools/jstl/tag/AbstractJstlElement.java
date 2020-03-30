package com.brentcroft.tools.jstl.tag;


import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Map;

public abstract class AbstractJstlElement implements JstlElement
{
    protected final static MapBindings EMPTY_MAP = new MapBindings();

    protected JstlTemplate innerRenderable;
    protected boolean deferred = false;


    public JstlTemplate getInnerJstlTemplate()
    {
        return innerRenderable;
    }

    public boolean isDeferred()
    {
        return deferred;
    }

    public void setDeferred( boolean deferred )
    {
        this.deferred = deferred;
    }


    public String toString()
    {
        return toText() + "\n";
    }


    @Override
    public void emitNodeEvents( Element element, Map< String, Object > bindings, JstlDocument.NodeListEmitter emitter ) throws SAXException
    {

    }
}

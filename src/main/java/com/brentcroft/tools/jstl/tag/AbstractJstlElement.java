package com.brentcroft.tools.jstl.tag;


import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;

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
}

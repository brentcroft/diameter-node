package com.brentcroft.tools.jstl;

import com.brentcroft.tools.jstl.tag.JstlElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Renderable decomposition of a text stream into a list of
 * <code>Renderable</code> elements.
 */
public class JstlTemplate implements Renderable
{
    private final List< Renderable > elements = new ArrayList<>();

    private final JstlElement parent;


    public JstlTemplate( JstlElement parent )
    {
        this.parent = parent;
    }


    public JstlElement getParent()
    {
        return parent;
    }


    /**
     * The key function of a template is to render itself (using a Map of
     * objects providing the EL namespace).
     *
     * @param rootObjects the objects in context during rendering
     * @return String the rendered output as a String
     */
    public String render( Map< String, Object > rootObjects )
    {
        final StringBuilder out = new StringBuilder();

        for ( Renderable element : elements )
        {
            out.append( element.render( rootObjects ) );
        }

        return out.toString();
    }

    public List< Renderable > getElements()
    {
        return elements;
    }


    public void addRenderable( Renderable renderable )
    {
        elements.add( renderable );
    }


    public String toString()
    {
        final StringBuilder out = new StringBuilder();

        for ( Renderable element : elements )
        {
            out.append( element );
        }

        return out.toString();
    }

}

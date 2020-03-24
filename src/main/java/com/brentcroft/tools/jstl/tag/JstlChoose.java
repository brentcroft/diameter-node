package com.brentcroft.tools.jstl.tag;

import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.Renderable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JstlChoose extends AbstractJstlElement
{
    private final static String TAG = "c:choose";

    public JstlChoose()
    {
        innerRenderable = new JstlTemplate( this );
    }

    @Override
    public void normalize()
    {
        final List< Renderable > elements = innerRenderable.getElements();

        if ( elements != null )
        {
            List< Renderable > elementsToRemove = null;

            for ( Renderable r : elements )
            {
                if ( r instanceof JstlWhen || r instanceof JstlOtherwise )
                {
                    continue;
                }

                if ( elementsToRemove == null )
                {
                    elementsToRemove = new ArrayList< Renderable >();
                }

                // remove from elements
                elementsToRemove.add( r );
            }

            if ( elementsToRemove != null )
            {
                elements.removeAll( elementsToRemove );
            }
        }
    }


    public String render( Map< String, Object > rootObjects )
    {
        if ( isDeferred() )
        {
            return toText();
        }

        final List< Renderable > elements = innerRenderable.getElements();

        if ( elements != null )
        {
            for ( Renderable r : elements )
            {
                if ( r instanceof JstlWhen && ( ( JstlWhen ) r ).test( rootObjects ) )
                {
                    return r.render( rootObjects );
                }
                else if ( r instanceof JstlOtherwise )
                {
                    return r.render( rootObjects );
                }
            }
        }

        return "";
    }

    public String toText()
    {
        return String.format( "<%s>%s</%s>", TAG, innerRenderable, TAG );
    }

}
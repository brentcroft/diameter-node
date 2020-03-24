package com.brentcroft.tools.jstl.tag;

import com.brentcroft.tools.el.ELTemplateManager;
import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;

import javax.el.ValueExpression;
import java.util.Map;

public class JstlIf extends AbstractJstlElement
{
    private final static String TAG = "c:if";

    private final String testEL;

    private ValueExpression valueExpression;

    private final ELTemplateManager elTemplateManager;


    public JstlIf( ELTemplateManager elTemplateManager, String testEL )
    {
        this.elTemplateManager = elTemplateManager;
        this.testEL = testEL;

        valueExpression = null;
        innerRenderable = new JstlTemplate( this );
    }

    private void compile()
    {
        valueExpression = elTemplateManager.getValueExpression( testEL, EMPTY_MAP, Boolean.class );
    }

    @Override
    public void normalize()
    {
        compile();
    }

    public String render( Map< String, Object > bindings )
    {
        if ( isDeferred() )
        {
            return toText();
        }

        final Object value = valueExpression.getValue( elTemplateManager.getELContext( bindings ) );

        if ( value instanceof Boolean && ( ( Boolean ) value ) )
        {
            // protect external bindings from pollution in local scope
            return innerRenderable.render( new MapBindings( bindings ) );
        }
        else
        {
            return "";
        }
    }

    public String toText()
    {
        return String.format( "<%s test=\"%s\">%s</%s>", TAG, testEL, innerRenderable, TAG );
    }
}

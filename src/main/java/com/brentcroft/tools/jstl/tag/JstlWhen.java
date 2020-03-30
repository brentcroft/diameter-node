package com.brentcroft.tools.jstl.tag;


import com.brentcroft.tools.el.ELTemplateManager;
import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.el.ValueExpression;
import java.util.Map;


public class JstlWhen extends AbstractJstlElement
{
    public final static String TAG = "c:when";

    protected String testEL;

    protected ValueExpression valueExpression;

    private final ELTemplateManager elTemplateManager;

    public JstlWhen( ELTemplateManager elTemplateManager, String testEL )
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


    public boolean test( Map< ?, ? > rootObjects )
    {
        final Object value = valueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );

        return ( value instanceof Boolean ) && ( Boolean ) value;
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
        return String.format( "<%s test=\"%s\">%s</%s>", TAG, testEL, innerRenderable, TAG );
    }


    @Override
    public void emitNodeEvents( Element element, Map< String, Object > bindings, JstlDocument.NodeListEmitter emitter ) throws SAXException
    {
        emitter.emitListEvents( element.getChildNodes(), bindings );
    }
}
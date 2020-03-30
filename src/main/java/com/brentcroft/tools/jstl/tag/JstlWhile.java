package com.brentcroft.tools.jstl.tag;

import com.brentcroft.tools.el.ELTemplateManager;
import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.el.ValueExpression;
import java.util.Map;

public class JstlWhile extends AbstractJstlElement
{
    private final static String TAG = "c:while";

    private String testEL;

    private ValueExpression valueExpression;

    private final ELTemplateManager elTemplateManager;

    private final String varStatus;


    public JstlWhile( ELTemplateManager elTemplateManager, String testEL, String varStatus )
    {
        this.elTemplateManager = elTemplateManager;
        this.testEL = testEL;

        this.varStatus = varStatus;

        innerRenderable = new JstlTemplate( this );
    }


    private void compile()
    {
        valueExpression = elTemplateManager.getValueExpression( testEL, EMPTY_MAP, Object.class );
    }

    @Override
    public void normalize()
    {
        compile();
    }

    public String render( Map< String, Object > rootObjects )
    {
        if ( isDeferred() )
        {
            return toText();
        }

        final LoopTagStatus< Object > loopTagStatus = new LoopTagStatus<>( null, null, null );

        Object value = valueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );

        final StringBuilder b = new StringBuilder();

        while ( value instanceof Boolean && ( ( Boolean ) value ) )
        {
            // protect external bindings from pollution in the loop
            // scope
            final MapBindings localObjects = new MapBindings( rootObjects );


            localObjects.put( varStatus, loopTagStatus );


            b.append( innerRenderable.render( localObjects ) );

            loopTagStatus.increment();

            // but always test in outer scope
            value = valueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );
        }

        return b.toString();
    }

    public String toText()
    {
        return String.format( "<%s test=\"%s\">%s</%s>", TAG, testEL, innerRenderable, TAG );
    }

    @Override
    public void emitNodeEvents( Element element, Map< String, Object > bindings, JstlDocument.NodeListEmitter emitter ) throws SAXException
    {
        final LoopTagStatus< Object > loopTagStatus = new LoopTagStatus<>( null, null, null );

        // protect external bindings from pollution in the loop
        // scope
        final MapBindings localObjects = new MapBindings( bindings );
        localObjects.put( varStatus, loopTagStatus );

        // in global scope
        Object value = valueExpression.getValue( elTemplateManager.getELContext( localObjects ) );

        while ( value instanceof Boolean && ( ( Boolean ) value ) )
        {
            emitter.emitListEvents( element.getChildNodes(), bindings );

            loopTagStatus.increment();

            value = valueExpression.getValue( elTemplateManager.getELContext( localObjects ) );
        }
    }
}
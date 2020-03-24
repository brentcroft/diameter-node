package com.brentcroft.tools.jstl.tag;

import com.brentcroft.tools.el.ELTemplateManager;
import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.MapBindings;

import javax.el.ValueExpression;
import java.util.Arrays;
import java.util.Map;

public class JstlForEach extends AbstractJstlElement
{
    private final static String TAG = "c:foreach";

    protected String itemsEL;

    protected ValueExpression itemsValueExpression;

    protected ValueExpression beginValueExpression;

    protected ValueExpression endValueExpression;

    protected ValueExpression stepValueExpression;

    protected final ELTemplateManager elTemplateManager;


    private final String var;

    private final String varStatus;

    private final String beginEl;

    private final String endEl;

    private final String stepEl;

    public JstlForEach( ELTemplateManager elTemplateManager, String itemsEL, String var, String varStatus, String beginEl, String endEl, String stepEl )
    {
        if ( ( itemsEL == null || itemsEL.trim().length() < 1 ) && ( beginEl == null || endEl == null ) )
        {
            throw new RuntimeException( TagMessages.INCONSISTENT_FOREACH );
        }

        this.elTemplateManager = elTemplateManager;

        this.itemsEL = itemsEL;
        this.var = var;
        this.varStatus = varStatus;


        this.beginEl = beginEl;
        this.endEl = endEl;
        this.stepEl = stepEl;

        this.innerRenderable = new JstlTemplate( this );
    }


    private void compile()
    {
        if ( itemsEL != null )
        {
            itemsValueExpression = elTemplateManager.getValueExpression( itemsEL, EMPTY_MAP, Object.class );
        }
        if ( beginEl != null )
        {
            beginValueExpression = elTemplateManager.getValueExpression( beginEl, EMPTY_MAP, Object.class );
        }
        if ( endEl != null )
        {
            endValueExpression = elTemplateManager.getValueExpression( endEl, EMPTY_MAP, Object.class );
        }
        if ( stepEl != null )
        {
            stepValueExpression = elTemplateManager.getValueExpression( stepEl, EMPTY_MAP, Object.class );
        }
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


        final StringBuilder b = new StringBuilder();

        Integer begin = null;
        Integer end = null;
        Integer step = null;

        if ( beginValueExpression != null )
        {
            Object value = beginValueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );

            if ( ! ( value instanceof Number ) )
            {
                throw new RuntimeException( "EL expression for \"begin\" attribute does not resolve to an integer! " + value );
            }

            begin = ( ( Number ) value ).intValue();
        }

        if ( endValueExpression != null )
        {
            Object value = endValueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );

            if ( ! ( value instanceof Number ) )
            {
                throw new RuntimeException( "EL expression for \"end\" attribute does not resolve to an integer! " + value );
            }

            end = ( ( Number ) value ).intValue();
        }

        if ( stepValueExpression != null )
        {
            Object value = stepValueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );

            if ( ! ( value instanceof Number ) )
            {
                throw new RuntimeException( "EL expression for \"step\" attribute does not resolve to an integer! " + value );
            }

            step = ( ( Number ) value ).intValue();
        }


        final LoopTagStatus< Object > loopTagStatus = new LoopTagStatus< Object >( begin, end, step );

        if ( itemsValueExpression == null )
        {
            // end is inclusive
            for ( loopTagStatus.setIndex( begin ); loopTagStatus.getIndex() <= end; loopTagStatus.increment( step ) )
            {
                // protect external bindings from pollution in the loop
                // scope
                final MapBindings localObjects = new MapBindings( rootObjects );

                localObjects.put( varStatus, loopTagStatus );

                b.append( innerRenderable.render( localObjects ) );
            }
        }
        else
        {
            Object value = itemsValueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );


            if ( value instanceof Object[] )
            {
                value = Arrays.asList( ( Object[] ) value );
            }

            if ( value instanceof Iterable< ? > )
            {
                for ( Object item : ( Iterable< ? > ) value )
                {
                    // maybe skip to begin'th item
                    if ( loopTagStatus.getBegin() != null && loopTagStatus.getIndex() < loopTagStatus.getBegin() )
                    {
                        loopTagStatus.increment();
                        continue;
                    }
                    // maybe skip from end'th item
                    else if ( loopTagStatus.getEnd() != null && loopTagStatus.getIndex() > loopTagStatus.getEnd() )
                    {
                        break;
                    }


                    // protect external bindings from pollution in the loop
                    // scope
                    final MapBindings localObjects = new MapBindings( rootObjects );

                    localObjects.put( var, item );
                    localObjects.put( varStatus, loopTagStatus.withCurrent( item ) );


                    b.append( innerRenderable.render( localObjects ) );

                    loopTagStatus.increment();
                }
            }
            else
            {
                b.append( innerRenderable.render( rootObjects ) );
            }
        }

        return b.toString();
    }

    public String toText()
    {
        return String.format( "<%s items=\"%s\"%s%s%s%s%s>%s</%s>",
                TAG,
                itemsEL,
                var == null ? "" : " var=\"" + var + "\"",
                varStatus == null ? "" : " varStatus=\"" + varStatus + "\"",
                beginEl == null ? "" : " begin=\"" + beginEl + "\"",
                endEl == null ? "" : " end=\"" + endEl + "\"",
                stepEl == null ? "" : " step=\"" + stepEl + "\"",
                innerRenderable,
                TAG );
    }
}
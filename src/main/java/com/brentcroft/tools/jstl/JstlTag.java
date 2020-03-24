package com.brentcroft.tools.jstl;


import com.brentcroft.tools.StringUpcaster;
import com.brentcroft.tools.jstl.JstlTemplateManager.JstlTemplateHandler;
import com.brentcroft.tools.jstl.tag.*;
import org.apache.logging.log4j.Level;

import java.util.Map;

import static java.lang.String.format;

/**
 * A set of tags (aligned with JSP and JSTL) that can be used in templates.
 * <p/>
 *
 * @author ADobson
 * @see <a
 * href="http://docs.oracle.com/javaee/5/jstl/1.1/docs/tlddocs/c/tld-summary.html">JSTL
 * 1.1.Tag Library API</a>
 */
public enum JstlTag
{
    /**
     * Simple conditional tag, which evaluates its body if the supplied
     * condition is true and optionally exposes a Boolean scripting variable
     * representing the evaluation of this condition.
     * <p/>
     *
     * <pre>
     * &lt;c:if test="${ expression }"&gt;
     *    YOUR BLOCK GOES HERE
     * &lt;/c:if&gt;
     * </pre>
     *
     * @see <a
     * href="http://docs.oracle.com/javaee/5/jstl/1.1/docs/tlddocs/c/if.html">JSTL
     * coreTag if</a>
     */
    IF
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlIf(
                            templateHandler.getELTemplateManager(),
                            expressionize( getAttributeValueNotEmpty( attributes, "test", String.class ) ) );
                }
            },


    /**
     * Simple conditional looping tag, which evaluates its body repeatedly while
     * the supplied condition is true.
     * <p/>
     *
     * <pre>
     * &lt;c:while test="${ expression }"&gt;YOUR BLOCK GOES HERE&lt;/c:while&gt;
     * </pre>
     * <p>
     * <p/>
     * NB: We have one because its convenient.
     *
     * @see <a href="http://www.coderanch.com/t/281500/JSP/java/loop-tag-JSTL"
     * >Why no while loop tag in JSTL?</a>
     */
    WHILE
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateManager.JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlWhile(
                            templateHandler.getELTemplateManager(),
                            expressionize( getAttributeValueNotEmpty( attributes, "test", String.class ) ),
                            getAttributeValue( attributes, "varStatus", "varStatus", String.class ) );
                }
            },


    /**
     * The basic iteration tag, accepting many different collection types and
     * supporting sub-setting and other functionality.
     * <p/>
     * With collections or arrays:
     *
     * <pre>
     * &lt;c:foreach items="${ items }" &gt;YOUR BLOCK GOES HERE&lt;/c:foreach&gt;
     *
     * &lt;c:foreach items="${ items }" var="item" &gt;Item is: ${ item }&lt;/c:foreach&gt;
     *
     * &lt;c:foreach items="${ items }" var="item" varStatus="status" &gt;Item [${ status.index }] is: ${ item }&lt;/c:foreach&gt;
     * </pre>
     * <p/>
     * or with <code>begin</code>, <code>end</code> and optional
     * <code>step</code>:
     *
     * <pre>
     * &lt;c:foreach begin="${ 3 }" end="${ 8 }" [step="2"] &gt;YOUR BLOCK GOES HERE&lt;/c:foreach&gt;
     *
     * &lt;c:foreach begin="${ 3 }" end="${ 8 }" varStatus="status" &gt;Item [${ status.index }]&lt;/c:foreach&gt;
     * </pre>
     * <p>
     * <p>
     * <p/>
     * or with everything:
     *
     * <pre>
     * &lt;c:foreach items="${ items }" begin="${ 3 }" end="${ 8 }" [step="2"] &gt;YOUR BLOCK GOES HERE&lt;/c:foreach&gt;
     *
     * &lt;c:foreach items="${ items }" begin="${ 3 }" end="${ 8 }" var="item" &gt;Item is: ${ item }&lt;/c:foreach&gt;
     *
     * &lt;c:foreach items="${ items }" begin="${ 3 }" end="${ 8 }" var="item" varStatus="status" &gt;Item [${ status.index }] is: ${ item }&lt;/c:foreach&gt;
     * </pre>
     *
     * @see <a
     * href="http://docs.oracle.com/javaee/5/jstl/1.1/docs/tlddocs/c/forEach.html"
     * >JSTL coreTag forEach</a>
     */
    FOREACH
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateManager.JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlForEach(
                            templateHandler.getELTemplateManager(),

                            // maybe empty only if begin and end are set
                            getAttributeValue( attributes, "items", null, String.class ),

                            // optional items with sensible defaults
                            // var only meaningful if items is not null
                            getAttributeValue( attributes, "var", "item", String.class ),

                            // always valid as in simplest case just counts iterations
                            getAttributeValue( attributes, "varStatus", "varStatus", String.class ),

                            //

                            expressionize( getAttributeValue( attributes, "begin", null, String.class ) ),
                            expressionize( getAttributeValue( attributes, "end", null, String.class ) ),
                            expressionize( getAttributeValue( attributes, "step", "1", String.class ) )
                    );
                }
            },


    /**
     * Simple tag that encloses its inner content within a HTML comment
     * delimiters.
     * <p/>
     *
     * <pre>
     * &lt;c:comment&gt;YOUR BLOCK GOES HERE&lt;/c:comment&gt;
     * </pre>
     */
    COMMENT
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlComment();
                }
            },


    /**
     * Simple conditional tag that establishes a context for mutually exclusive
     * conditional operations, marked by &lt;c:when&gt; and &lt;c:otherwise&gt;.
     * <p/>
     * NB: Note that any immediate children: elements, text, or otherwise, which
     * are not &lt;c:when&gt; or &lt;c:otherwise&gt;, are silently discarded
     * during parsing.
     *
     * <pre>
     * &lt;c:choose&gt;
     *  &lt;c:when test="${ expression }" &gt;YOUR BLOCK GOES HERE&lt;/c:when&gt;
     *  &lt;c:otherwise&gt;YOUR BLOCK GOES HERE&lt;/c:otherwise&gt;
     * &lt;/c:choose&gt;
     * </pre>
     *
     * @see <a
     * href="http://docs.oracle.com/javaee/5/jstl/1.1/docs/tlddocs/c/choose.html"
     * >JSTL coreTag choose</a>
     */
    CHOOSE
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlChoose();
                }
            },

    /**
     * Sub tag of &lt;choose&gt; that includes its body if its condition
     * evaluates to 'true'.
     *
     * <pre>
     *  &lt;c:when test="${ expression }" &gt;YOUR BLOCK GOES HERE&lt;/c:when&gt;
     * </pre>
     *
     * @see <a
     * href="http://docs.oracle.com/javaee/5/jstl/1.1/docs/tlddocs/c/when.html"
     * >JSTL coreTag when</a>
     */
    WHEN
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlWhen(
                            templateHandler.getELTemplateManager(),
                            expressionize( getAttributeValueNotEmpty( attributes, "test", String.class ) ) );
                }
            },

    /**
     * Sub tag of &lt;choose&gt; that follows &lt;when&gt; tags and runs only if
     * all of the prior conditions evaluated to 'false'.
     *
     * <pre>
     *  &lt;c:otherwise&gt;YOUR BLOCK GOES HERE&lt;/c:otherwise&gt;
     * </pre>
     *
     * @see <a
     * href="http://docs.oracle.com/javaee/5/jstl/1.1/docs/tlddocs/c/otherwise.html"
     * >JSTL coreTag otherwise</a>
     */
    OTHERWISE
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlOtherwise();
                }
            },


    /**
     * Catches any Throwable that occurs in its body and exposes it using the
     * value of the attribute <code>var</code>, or else "caughtException" if the
     * attribute <code>var</code> is empty or doesn't exist.
     * <p/>
     * NB: This is different to the JSTL specification which doesn't enforce a
     * default name, and hence exposure.
     *
     * <pre>
     *  &lt;c:catch var="caughtException" &gt;YOUR BLOCK GOES HERE&lt;/c:catch&gt;
     * </pre>
     *
     * @see <a
     * href="http://docs.oracle.com/javaee/5/jstl/1.1/docs/tlddocs/c/catch.html"
     * >JSTL coreTag catch</a>
     */
    CATCH
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlCatch(
                            getAttributeValueNotEmpty( attributes, "var", "caughtException", String.class ).trim() );
                }
            },


    /**
     * This is not part of JSTL, but is part of JSP itself.
     * <p/>
     * NB: Any inner content found during parsing will cause an error: this tag
     * should always be used in the short form as below.
     *
     *
     * <pre>
     *  &lt;c:include page="fred-bloggs.tpl"/&gt;
     * </pre>
     *
     * @see <a
     * href="http://www.coderanch.com/how-to/java/IncludesActionDirective"
     * >JSP actionTag include</a>
     * <p>
     * We only do dynamic linking, not static.
     */
    INCLUDE
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    final boolean isRelative = getAttributeValue( attributes, "relative", "true", Boolean.class );

                    final String rawUri = getAttributeValueNotEmpty( attributes, "page", String.class );

                    return new JstlInclude(
                            templateHandler,
                            isRelative ? templateHandler.relativizeUri( rawUri ) : rawUri );
                }
            },


    /**
     * This is not part of JSTL, but is convenient.
     * <p/>
     * If no level is specified then the default level is WARNING.
     *
     * <pre>
     *  &lt;c:log level="INFO" &gt;YOUR BLOCK TO LOG GOES HERE&lt;/c:catch&gt;
     * </pre>
     */
    LOG
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlLog(
                            Level.toLevel( getAttributeValue( attributes, "level", "WARN", String.class ).toUpperCase() )
                    );
                }
            },


    /**
     * This is not part of JSTL, but is convenient.
     * <p/>
     *
     * <pre>
     * &lt;c:script [public="true"] [render="false"] [engine="js"]&gt;
     *  YOUR SCRIPT GOES HERE
     * &lt;/c:script&gt;
     * </pre>
     * <p/>
     * The script is compiled during parsing.<br/>
     * The script is executed during rendering.
     * <p/>
     * All the attributes are optional, with default values, however:
     * <p/>
     * <ol>
     * <li>By default, or if <code>public</code> is <code>true</code>, the
     * script is executed in the context of the current bindings; potentially
     * polluting the current scope. <br/>
     * Otherwise, if <code>public</code> exists and is not <code>true</code>
     * then the script is executed in the context of new Hierarchical bindings
     * with the current bindings as parent.</li>
     *
     * <li>By default, or if <code>render</code> <b>is not</b> <code>true</code>
     * , the result of script execution is ignored.<br/>
     * Otherwise, if <code>render</code> exists and <b>is</b> <code>true</code>
     * then the result of script execution is returned on rendering.</li>
     * </ol>
     */
    SCRIPT
            {
                @Override
                public JstlElement newJstlElement( JstlTemplateManager.JstlTemplateHandler templateHandler, Map< String, String > attributes )
                {
                    return new JstlScript(
                            getAttributeValue( attributes, "public", "true", Boolean.class ),
                            getAttributeValue( attributes, "render", "false", Boolean.class ),
                            getAttributeValueNotEmpty( attributes, "engine", "js", String.class )
                    );
                }
            },

    ;

    public abstract JstlElement newJstlElement( JstlTemplateManager.JstlTemplateHandler handler, Map< String, String > attributes );


    private static String expressionize( String expression )
    {
        if ( expression != null )
        {
            final String expTrim = expression.trim();

            if ( ! expTrim.startsWith( "${" ) && ! expTrim.endsWith( "}" ) )
            {
                // don't need to trim
                return "${" + expression + "}";
            }
            else
            {
                // need to trim: e.g. "  ${ red }   "
                return expTrim;
            }
        }

        //
        return expression;
    }

    private static < T > T getAttributeValueNotEmpty( Map< String, String > attributes, String name, final Class< T > type )
    {
        final T value = getAttributeValue( attributes, name, type );

        if ( value != null && value.toString().trim().length() > 0 )
        {
            return value;
        }

        throw new RuntimeException( format( TagMessages.REQ_ATTR_EMPTY, name ) );
    }

    private static < T > T getAttributeValue( Map< String, String > attributes, String name, final Class< T > type )
    {
        if ( attributes != null && attributes.containsKey( name ) )
        {
            return StringUpcaster.upcast( attributes.get( name ), type );
        }

        throw new RuntimeException( format( TagMessages.REQ_ATTR_MISSING, name ) );
    }

    private static < T > T getAttributeValueNotEmpty( Map< String, String > attributes, String name, String defaultValue, final Class< T > type )
    {
        final T value = getAttributeValue( attributes, name, defaultValue, type );

        if ( value != null && value.toString().trim().length() > 0 )
        {
            return value;
        }

        throw new RuntimeException( format( TagMessages.OPT_ATTR_EMPTY, name ) );
    }


    private static < T > T getAttributeValue( Map< String, String > attributes, String name, String defaultValue, final Class< T > type )
    {
        String value = ( attributes != null && attributes.containsKey( name ) ) ? attributes.get( name ) : defaultValue;

        return StringUpcaster.upcast( value, type );
    }
}
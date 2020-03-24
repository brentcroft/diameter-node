package com.brentcroft.tools.jstl;


import com.brentcroft.tools.el.ELFilter;
import com.brentcroft.tools.el.ELTemplateManager;
import com.brentcroft.tools.jstl.tag.JstlElement;
import com.brentcroft.tools.jstl.tag.TagHandler;
import com.brentcroft.tools.jstl.tag.TagMessages;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;
import static com.brentcroft.tools.el.ELTemplateManager.readUrl;
import static java.lang.String.format;
import static java.util.Objects.nonNull;


/**
 * Maintains a set of JstlTemplates (i.e. text containing JstlTags and EL
 * expressions) and provides two methods:
 * <code>expandText( text, context )</code>, and
 * <code>expandUri( uri, context )</code>, to expand JstlTemplates within the
 * context of a map of objects provided at expansion time.
 * <p/>
 * <p>
 * JstlTemplates are either anonymous (and not cached), or are loaded from a
 * uri, compiled and cached (using the uri as a key).
 * <p/>
 *
 * @author ADobson
 * @see JstlTag
 * @see ELTemplateManager
 */
@Log4j2
public class JstlTemplateManager
{
    public static final String DEFAULT_TEMPLATE_EXTENSION = ".tpl";

    public static String TAG_PREFIX = "c:";

    public static final String TAG_REGEX = "</" + TAG_PREFIX + "(\\w+)>|<" + TAG_PREFIX + "(\\w+)((\\s*\\w+\\=\\\"[^\\\"]*\\\"|\\s*\\w+\\='[^']*')*)\\s*(/?)>";

    public static final String ATTRIBUTE_REGEX = "(\\w+)=(\\\"([^\\\"]*)\\\"|'([^']*)')";

    public static final String COMMENT_REGEX = "(?s)<!--.*?-->";


    public static final Pattern TAG_SELECTOR_PATTERN = Pattern.compile( TAG_REGEX );

    public static final Pattern ATTRIBUTE_SELECTOR_PATTERN = Pattern.compile( ATTRIBUTE_REGEX );

    public static final Pattern COMMENT_SELECTOR_PATTERN = Pattern.compile( COMMENT_REGEX );

    private boolean stripComments = true;


    private final ELTemplateManager elTemplateManager = new ELTemplateManager();

    private final Map< String, JstlTemplate > templates = new HashMap<>();

    public JstlTemplateManager withELFilter( ELFilter elFilter )
    {
        getELTemplateManager().setValueExpressionFilter( elFilter );
        return this;
    }

    public JstlTemplateManager withStripComments( boolean stripComments )
    {
        setStripComments( stripComments );
        return this;
    }


    public ELTemplateManager getELTemplateManager()
    {
        return elTemplateManager;
    }


    /**
     * Expands the supplied <code>jstlText</code> so that all JSTL (and EL) tags
     * are replaced with their values calculated with respect to the supplied
     * map of root objects.
     *
     * @param jstlText the jstlText to be expanded
     * @return the expanded jstlText
     */
    public String expandText( String jstlText, Map< String, Object > rootObjects )
    {
        return buildTemplate( jstlText ).render( rootObjects );
    }


    /**
     * Expands the supplied <code>jstlText</code> so that all JSTL (and EL) tags
     * are replaced with their values calculated with respect to the supplied
     * map of root objects, in the context of the supplied uri (e.g. for
     * relativizing embedded paths)..
     *
     * @param jstlText the jstlText to be expanded
     * @param uri      a uri against which any embedded paths are relativized
     * @return the expanded jstlText
     */
    public String expandText( String jstlText, String uri, Map< String, Object > rootObjects )
    {
        return buildTemplate( jstlText, uri ).render( rootObjects );
    }


    /**
     * Find a template and return it's rendering of a Map by the template.
     * <p/>
     * If a template is not already cached (with the key
     * <code>templateUri</code>) then a new template is built (and cached) by
     * opening, and parsing the stream from <code>templateUri</code>.
     * <p/>
     * Return the rendering of the Map <code>rootObjects</code> by the template.
     * <p/>
     *
     * @param uri         identifies a template
     * @param rootObjects a Map of root objects (to make accessible in EL expressions in
     *                    the template)
     * @return the rendering of the Map <code>rootObjects</code> by the
     * specified template <code>templateUri</code>
     */
    public String expandUri( final String uri, Map< String, Object > rootObjects )
    {
        // if no period then tack a default extension on the end
        final int lastIndexOfPeriod = uri.lastIndexOf( '.' );
        final String newUri = ( lastIndexOfPeriod > - 1 ) ? uri : ( uri + DEFAULT_TEMPLATE_EXTENSION );

        // find
        if ( ! templates.containsKey( newUri ) )
        {
            loadTemplate( newUri, null );
        }

        // render
        return templates.get( newUri ).render( rootObjects );
    }

    /**
     * Only allow one thread to load a template at any one time.
     * <p/>
     * If the desired template has already been loaded then just return
     * otherwise load and cache the template.
     *
     * @param uri the uri of the template to load.
     */
    public synchronized void loadTemplate( final String uri, final JstlTemplateHandler parentHandler )
    {
        // find
        if ( templates.containsKey( uri ) )
        {
            return;
        }

        // build and cache
        templates.put( uri, new JstlTemplateBuilder().build( uri, parentHandler ) );

        log.info( () -> "Loaded template: " + uri );
    }


    /**
     * Builds an anonymous <code>JstlTemplate</code> from the supplied text.
     * <p/>
     * The template is not cached (it has no uri).
     *
     * @param jstlText the text to be decomposed into a JstlTemplate
     * @return the new JstlTemplate
     */
    public JstlTemplate buildTemplate( String jstlText )
    {
        final JstlTemplateHandler handler = new JstlTemplateHandler( null, null );

        new JstlTemplateBuilder().parse( jstlText, handler );

        return handler.build();
    }

    /**
     * Builds an anonymous <code>JstlTemplate</code> from the supplied text, in
     * the context of the supplied uri (e.g. for relativizing embedded paths).
     * <p/>
     * The template is not cached.
     *
     * @param jstlText the text to be decomposed into a JstlTemplate
     * @return the new JstlTemplate
     */
    public JstlTemplate buildTemplate( String jstlText, final String uri )
    {
        final JstlTemplateHandler handler = new JstlTemplateHandler( uri, null );

        new JstlTemplateBuilder().parse( jstlText, handler );

        return handler.build();
    }


    /**
     * Switch off/on the stripping of HTML style comments.
     * <p/>
     * This is switched on by default.
     *
     * @param stripComments
     */
    public void setStripComments( boolean stripComments )
    {
        this.stripComments = stripComments;
    }


    /**
     * Handler implementation that develops a JstlTemplate.
     *
     * @author ADobson
     */
    public class JstlTemplateHandler implements TagHandler
    {
        private final JstlTemplate root = new JstlTemplate( null );

        private final Stack< JstlTemplate > stack = new Stack< JstlTemplate >();

        private final Stack< String > tagStack = new Stack< String >();

        private final JstlTemplateHandler parentHandler;

        private final String uri;

        public JstlTemplateHandler( String uri, JstlTemplateHandler parentHandler )
        {
            this.uri = uri;
            this.parentHandler = parentHandler;
            stack.push( root );
        }


        public void text( String text )
        {
            if ( stack.peek() == null )
            {
                throw new RuntimeException( format( TagMessages.PARSER_ERROR_UNEXPECTED_TEXT, tagStack.peek() ) );
            }

            stack.peek().addRenderable( elTemplateManager.buildTemplate( text ).withUri( uri ) );

        }


        public void open( String tag, Map< String, String > attributes )
        {
            final JstlTag jstlType = JstlTag.valueOf( tag.toUpperCase() );

            final JstlElement jstlElement = jstlType.newJstlElement( this, attributes );


            if ( stack.peek() == null )
            {
                throw new RuntimeException( format( TagMessages.PARSER_ERROR_UNEXPECTED_ELEMENT, tag, tagStack.peek() ) );
            }

            if ( nonNull( attributes ) && attributes.containsKey( "deferred" ) )
            {
                jstlElement.setDeferred( Boolean.parseBoolean( attributes.get( "deferred" ) ) );
            }

            stack.peek().addRenderable( jstlElement );


            // this can be null for JstlElements that don't have inner templates
            stack.push( jstlElement.getInnerJstlTemplate() );

            //
            tagStack.push( tag );
        }


        public void close( String tag )
        {
            if ( tagStack.isEmpty() )
            {
                throw new RuntimeException( format( TagMessages.PARSER_ERROR_EMPTY_STACK, tag ) );
            }

            final String stackTag = tagStack.peek();

            if ( ! tag.equals( stackTag ) )
            {
                throw new RuntimeException( format( TagMessages.PARSER_ERROR_SEQUENCE_ERROR, tag, stackTag ) );
            }
            else
            {
                tagStack.pop();
            }


            final JstlTemplate template = stack.pop();

            //
            if ( template != null )
            {
                final JstlElement jstlElement = template.getParent();

                if ( jstlElement != null )
                {
                    jstlElement.normalize();
                }
            }
        }


        public JstlTemplate build()
        {
            final JstlTemplate peekStack = stack.peek();

            if ( peekStack != root )
            {
                final String stackTag = tagStack.isEmpty() ? null : tagStack.peek();

                throw new RuntimeException( format( TagMessages.PARSER_ERROR_SEQUENCE_ERROR2, stackTag, peekStack, root ) );
            }
            return root;
        }

        public JstlTemplate peekStack()
        {
            if ( stack.isEmpty() )
            {
                return null;
            }
            else
            {
                return stack.peek();
            }
        }

        public ELTemplateManager getELTemplateManager()
        {
            return elTemplateManager;
        }

        public JstlTemplateHandler getParent()
        {
            return parentHandler;
        }

        public String getUri()
        {
            return uri;
        }

        public void loadTemplate( final String uri )
        {
            JstlTemplateManager.this.loadTemplate( uri, this );
        }

        public String expandUri( final String uri, Map< String, Object > rootObjects )
        {
            return JstlTemplateManager.this.expandUri( uri, rootObjects );
        }

        public String relativizeUri( String relativeUri )
        {
            if ( uri == null )
            {
                return relativeUri;
            }

            boolean isfwdSlash = true;

            int p = uri.lastIndexOf( '/' );

            if ( p < 0 )
            {
                isfwdSlash = false;
                p = uri.lastIndexOf( '\\' );
            }

            if ( p < 0 )
            {
                return relativeUri;
            }

            return uri.substring( 0, p ) + ( isfwdSlash ? "/" : "\\" ) + relativeUri;
        }
    }


    public static URL normalizeToFileUrl( Class< ? > clazz, String templateUri ) throws MalformedURLException
    {
        final URL url = clazz.getClassLoader().getResource( templateUri );

        if ( url != null )
        {
            log.debug( () -> "Got URL from class loader: " + url );

            return url;
        }

        return new File( templateUri ).toURI().toURL();
    }

    /**
     * Builder implementation that parses text loaded from a uri.
     *
     * @author ADobson
     */
    class JstlTemplateBuilder
    {

        /*
         * Normalise, read, and parse to a JsltTemplate, the contents obtained
         * from a uri.
         */
        public JstlTemplate build( final String uri, final JstlTemplateHandler parentHandler )
        {
            try
            {
                return new JstlTemplateHandler( uri, parentHandler )
                {
                    {
                        parse( readUrl(
                                getLocalFileURL( getClass(), uri ) ), this );
                    }
                }.build();
            }
            catch ( Exception e )
            {
                throw ( RuntimeException ) e;
            }
        }


        private void parse( String text, TagHandler handler )
        {
            if ( text == null )
            {
                return;
            }

            final Matcher matcher = TAG_SELECTOR_PATTERN.matcher( text );

            int lastPosition = 0;

            while ( matcher.find() )
            {
                final int start = matcher.start();
                final int end = matcher.end();


                if ( start > lastPosition )
                {
                    handler.text( maybeStripComments( text.substring( lastPosition, start ) ) );
                }

                lastPosition = end;

                final boolean isEndTag = ( matcher.group( 1 ) != null );

                // relying on the ordering of the regex: see note at top
                final String tag = isEndTag ? matcher.group( 1 ) : matcher.group( 2 );


                final boolean isShortTag = ! isEndTag && ( "/".equalsIgnoreCase( matcher.group( 5 ) ) );


                if ( isEndTag )
                {
                    handler.close( tag );
                }
                else
                {
                    handler.open( tag, getAttributes( matcher.group( 3 ) ) );

                    if ( isShortTag )
                    {
                        handler.close( tag );
                    }
                }
            }

            if ( lastPosition < ( text.length() ) )
            {
                handler.text( maybeStripComments( text.substring( lastPosition ) ) );
            }
        }


        private Map< String, String > getAttributes( String text )
        {
            Map< String, String > p = null;

            final Matcher matcher = ATTRIBUTE_SELECTOR_PATTERN.matcher( text );

            while ( matcher.find() )
            {
                if ( p == null )
                {
                    p = new HashMap<>();
                }

                final String doubleQuoted = matcher.group( 3 );
                final String singleQuoted = matcher.group( 4 );

                p.put( matcher.group( 1 ), doubleQuoted != null ? doubleQuoted : singleQuoted );
            }

            return p;
        }
    }

    public String maybeStripComments( String text )
    {
        return stripComments ? COMMENT_SELECTOR_PATTERN.matcher( text ).replaceAll( "" ) : text;
    }
}

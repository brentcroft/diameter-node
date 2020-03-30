package com.brentcroft.tools.jstl.tag;

import com.brentcroft.diameter.simple.SimpleServer;
import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.JstlTemplateManager;
import com.brentcroft.tools.jstl.JstlTemplateManager.JstlTemplateHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;
import static java.lang.String.format;
import static java.util.Objects.isNull;

public class JstlInclude extends AbstractJstlElement
{
    private final static String TAG = "c:include";

    private final JstlTemplateManager.JstlTemplateHandler jstlTemplateHandler;

    private final String uri;


    public JstlInclude( JstlTemplateHandler jstlTemplateHandler, String uri )
    {
        this.jstlTemplateHandler = jstlTemplateHandler;

        this.uri = uri;

        JstlTemplateHandler parent = jstlTemplateHandler.getParent();

        while ( parent != null )
        {
            if ( uri.equalsIgnoreCase( parent.getUri() ) )
            {
                throw new RuntimeException( format( TagMessages.INCLUDE_CIRCULARITY, uri ) );
            }

            parent = parent.getParent();
        }

        // we're not lazy
        jstlTemplateHandler.loadTemplate( uri );
    }

    public String render( Map< String, Object > bindings )
    {
        if ( isDeferred() )
        {
            return toText();
        }

        return jstlTemplateHandler.expandUri( uri, bindings );
    }

    public String toText()
    {
        return String.format( "<%s page=\"%s\">%s</%s>", TAG, uri, innerRenderable, TAG );
    }

    @Override
    public void emitNodeEvents( Element element, Map< String, Object > bindings, JstlDocument.NodeListEmitter emitter ) throws SAXException
    {
        if ( isDeferred() )
        {
            return;
        }

        String key = "INCLUDES";

        NodeList includes = ( NodeList ) element.getUserData( key );

        if ( isNull( includes ) )
        {
            try
            {
                includes = DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse( new InputSource( getLocalFileURL( SimpleServer.class, uri ).openStream() ) )
                        .getChildNodes();

                element.setUserData( key, includes, null );
            }
            catch ( ParserConfigurationException | IOException e )
            {
                throw new SAXException( e );
            }
        }

        emitter.emitListEvents( includes, bindings );
    }
}
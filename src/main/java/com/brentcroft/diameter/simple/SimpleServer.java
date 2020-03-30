package com.brentcroft.diameter.simple;

import com.brentcroft.diameter.JstlProcessor;
import com.brentcroft.diameter.sax.Items;
import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.MapBindings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.client.impl.DictionarySingleton;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.XMLConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Getter
@Setter
public class SimpleServer extends StackImpl implements Stack, Items
{
    @Getter
    private final SessionFactory factory;

    public static void main( String[] args )
    {
        String serverConfigUri = isNull( args ) || args.length < 1
                                 ? "server.xml"
                                 : args[ 0 ];

        ServerConfigParser serverConfigParser = new ServerConfigParser();

        JstlDocument jstlDocument = new JstlDocument();

        jstlDocument.getBindings().put( "model", new MapBindings() );

        jstlDocument.setContentHandler( serverConfigParser );

        serverConfigParser.setModel( jstlDocument.getBindings() );

        try
        {
            jstlDocument.setDocument(
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse( new InputSource( getLocalFileURL( SimpleServer.class, serverConfigUri ).openStream() ) ) );

            jstlDocument.renderEvents();
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            if ( nonNull( serverConfigParser.getServer() ) )
            {
                serverConfigParser.getServer().stop( 1 );
            }
        }
    }

    public SimpleServer( Configuration config )
    {
        try
        {
            factory = init( config );

            Thread.sleep( 500L );
        }
        catch ( InterruptedException | IllegalDiameterStateException | InternalException e )
        {
            throw new RuntimeException( e );
        }
    }


    @Log4j2
    public static class ServerConfigParser extends DefaultHandler
    {
        @Getter
        private SimpleServer server;

        @Setter
        private Map< String, Object > model;

        public void startDocument()
        {
            log.info( "Starting server events ..." );
        }

        public void endDocument()
        {
            log.info( "Finished server events" );
        }

        public void startElement( String uri, String localName, String qName, Attributes attributes )
                throws SAXException
        {
            switch ( qName )
            {
                case "server":

                    newServerEvent( attributes );
                    break;

                case "listener":

                    newListenerEvent( attributes );
                    break;

                case "start":

                    newStartEvent( attributes );
                    break;
            }
        }

        private void newServerEvent( Attributes attributes ) throws SAXException
        {
            if ( nonNull( server ) )
            {
                throw new SAXException( "new server event but server is not null" );
            }

            if ( ! ATTR.DIAMETER_CONFIG_URI.hasAttribute( attributes ) )
            {
                throw new SAXException( format( "Missing server attribute: %s", ATTR.DIAMETER_CONFIG_URI.getAttribute() ) );
            }

            try
            {
                if ( ATTR.DICTIONARY_URI.hasAttribute( attributes ) )
                {
                    DictionarySingleton
                            .getDictionary(
                                    getLocalFileURL( SimpleServer.class, ATTR.DICTIONARY_URI.getAttribute( attributes ) )
                                            .openStream() );
                }

                server = new SimpleServer(
                        new XMLConfiguration(
                                getLocalFileURL(
                                        SimpleServer.class,
                                        ATTR.DIAMETER_CONFIG_URI.getAttribute( attributes ) )
                                        .openStream() ) );
            }
            catch ( Exception e )
            {
                throw new SAXException( e );
            }
        }

        private void newListenerEvent( Attributes attributes ) throws SAXException
        {
            if ( isNull( server ) )
            {
                throw new SAXException( "new listener event but server is null" );
            }

            for ( ATTR ia : EnumSet.of( ATTR.TEMPLATE_URI, ATTR.VENDOR_ID, ATTR.AUTH_APP_ID ) )
            {
                if ( ! ia.hasAttribute( attributes ) )
                {
                    throw new SAXException( format( "Missing listener attribute: %s", ia.getAttribute() ) );
                }
            }
            int vendorId = Integer.parseInt( ATTR.VENDOR_ID.getAttribute( attributes ) );
            int authAppId = Integer.parseInt( ATTR.AUTH_APP_ID.getAttribute( attributes ) );
            String templateUri = ATTR.TEMPLATE_URI.getAttribute( attributes );


            JstlProcessor processor = new JstlProcessor();

            processor.getModel().withParent( model );

            processor.setTemplateUri( templateUri );

            SimpleStackListener stackListener = new SimpleStackListener();

            stackListener.setName(
                    format(
                            "%s:%s@%s",
                            vendorId,
                            authAppId,
                            templateUri.lastIndexOf( '/' ) > - 1
                            ? templateUri.substring( templateUri.lastIndexOf( '/' ) + 1 )
                            : templateUri ) );

            stackListener.setDiameterRequestProcessor( processor );

            try
            {
                server
                        .unwrap( Network.class )
                        .addNetworkReqListener(
                                stackListener,
                                ApplicationId.createByAuthAppId( vendorId, authAppId ) );
            }
            catch ( Exception e )
            {
                throw new SAXException( e );
            }
        }


        private void newStartEvent( Attributes attributes ) throws SAXException
        {
            if ( isNull( server ) )
            {
                throw new SAXException( "new start event but server is null" );
            }

            for ( ATTR ia : EnumSet.of( ATTR.MODE, ATTR.STACK_CREATION_TIMEOUT, ATTR.TIME_UNIT ) )
            {
                if ( ! ia.hasAttribute( attributes ) )
                {
                    throw new SAXException( format( "Missing start attribute: %s", ia.getAttribute() ) );
                }
            }

            try
            {
                server.start(
                        Mode.valueOf( ATTR.MODE.getAttribute( attributes ) ),
                        Long.parseLong( ATTR.STACK_CREATION_TIMEOUT.getAttribute( attributes ) ),
                        TimeUnit.valueOf( ATTR.TIME_UNIT.getAttribute( attributes ) )
                );
            }
            catch ( IllegalDiameterStateException | InternalException e )
            {
                throw new SAXException( e );
            }
        }
    }
}

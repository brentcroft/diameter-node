package com.brentcroft.diameter.simple;

import com.brentcroft.diameter.JSTLProcessor;
import com.brentcroft.diameter.sax.Items;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.client.impl.DictionarySingleton;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.XMLConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Getter
@Setter
@Log4j2
public class SimpleServer extends StackImpl implements Stack
{
    private static final int DEFAULT_STACK_CREATION_TIMEOUT_MS = 30000;

    public static void main( String[] args )
    {
        String serverConfigUri = "diameter/server.xml";

        try
        {
            ServerConfigParser configParser = new ServerConfigParser();

            SAXParserFactory
                    .newInstance()
                    .newSAXParser()
                    .parse( getLocalFileURL( SimpleServer.class, serverConfigUri )
                            .openStream(), configParser );

            configParser
                    .getServer()
                    .start(
                            Mode.ALL_PEERS,
                            DEFAULT_STACK_CREATION_TIMEOUT_MS,
                            TimeUnit.MILLISECONDS
                    );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public SimpleServer( Configuration config )
    {
        try
        {
            init( config );
        }
        catch ( IllegalDiameterStateException | InternalException e )
        {
            throw new RuntimeException( e );
        }
    }


    private static class ServerConfigParser extends DefaultHandler
    {
        @Getter
        private SimpleServer server;

        public void startElement( String uri, String localName, String qName, Attributes attributes )
                throws SAXException
        {
            switch ( qName )
            {
                case "server":

                    if ( nonNull( server ) )
                    {
                        throw new SAXException( "new server event but server is not null" );
                    }

                    for ( Items.ATTR ia : EnumSet.of( Items.ATTR.DICTIONARY_URI, Items.ATTR.DIAMETER_CONFIG_URI ) )
                    {
                        if ( ! ia.hasAttribute( attributes ) )
                        {
                            throw new SAXException( format( "Missing server attribute: %s", ia.getAttribute() ) );
                        }
                    }

                    try
                    {
                        DictionarySingleton
                                .getDictionary(
                                        getLocalFileURL( SimpleServer.class, Items.ATTR.DICTIONARY_URI.getAttribute( attributes ) )
                                                .openStream() );

                        server = new SimpleServer(
                                new XMLConfiguration(
                                        getLocalFileURL(
                                                SimpleServer.class,
                                                Items.ATTR.DIAMETER_CONFIG_URI.getAttribute( attributes ) )
                                                .openStream() ) );
                    }
                    catch ( Exception e )
                    {
                        throw new SAXException( e );
                    }

                    break;


                case "listener":

                    if ( isNull( server ) )
                    {
                        throw new SAXException( "new listener event but server is null" );
                    }

                    for ( Items.ATTR ia : EnumSet.of( Items.ATTR.TEMPLATE_URI, Items.ATTR.VENDOR_ID, Items.ATTR.AUTH_APP_ID ) )
                    {
                        if ( ! ia.hasAttribute( attributes ) )
                        {
                            throw new SAXException( format( "Missing listener attribute: %s", ia.getAttribute() ) );
                        }
                    }

                    JSTLProcessor processor = new JSTLProcessor();

                    processor.setTemplateUri( Items.ATTR.TEMPLATE_URI.getAttribute( attributes ) );

                    SimpleStackListener stackListener = new SimpleStackListener();

                    stackListener.setDiameterRequestProcessor( processor );

                    try
                    {
                        server
                                .unwrap( Network.class )
                                .addNetworkReqListener(
                                        stackListener,
                                        ApplicationId
                                                .createByAuthAppId(
                                                        Integer.parseInt( Items.ATTR.VENDOR_ID.getAttribute( attributes ) ),
                                                        Integer.parseInt( Items.ATTR.AUTH_APP_ID.getAttribute( attributes ) ) ) );

                    }
                    catch ( Exception e )
                    {
                        throw new SAXException( e );
                    }
            }
        }
    }
}

package com.brentcroft.diameter.simple;

import com.brentcroft.diameter.DiameterModel;
import com.brentcroft.diameter.sax.DiameterWriter;
import com.brentcroft.diameter.sax.Items;
import com.brentcroft.tools.jstl.JstlDocument;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.client.impl.DictionarySingleton;
import org.jdiameter.client.impl.StackImpl;
import org.jdiameter.client.impl.helpers.XMLConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.brentcroft.diameter.DiameterRequestProcessor.serializeAnswer;
import static com.brentcroft.diameter.DiameterRequestProcessor.serializeRequest;
import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;
import static com.brentcroft.tools.jstl.MapBindings.jstl;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Getter
@Log4j2
public class SimpleClient extends StackImpl implements Stack, Items
{
    @Getter
    private final SessionFactory factory;


    public static void main( String[] args )
    {
        String clientConfigUri = isNull( args ) || args.length < 1
                                 ? "client.xml"
                                 : args[ 0 ];

        JstlDocument jstlDocument = new JstlDocument();

        ClientConfigParser clientConfigParser = new ClientConfigParser();

        try
        {


            clientConfigParser.setModel( jstlDocument.getBindings() );

            jstlDocument.setContentHandler( clientConfigParser );

            jstlDocument.setDocument(
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse( new InputSource( getLocalFileURL( SimpleClient.class, clientConfigUri ).openStream() ) ) );

            jstlDocument.renderEvents();
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            if ( nonNull( clientConfigParser.getClient() ) )
            {
                clientConfigParser.getClient().stop( 1 );
            }
        }
    }


    public SimpleClient( Configuration config )
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

    public void sendRequest( Session session, Request request, EventListener< Request, Answer > listener )
    {
        try
        {
            session.send( request, listener );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }


    public static void buildRequest( Request request, String requestXmlText )
    {
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            SAXSource saxSource = new SAXSource();
            saxSource.setInputSource( new InputSource( new StringReader( requestXmlText ) ) );

            DiameterWriter diameterWriter = new DiameterWriter();

            diameterWriter.setRequest( request );

            SAXResult transformResult = new SAXResult();
            transformResult.setHandler( diameterWriter );

            transformer.transform( saxSource, transformResult );
        }
        catch ( TransformerException e )
        {
            throw new RuntimeException( e );
        }
    }


    public Answer sendRequest( Session session, Request request )
    {
        try
        {
            final Answer[] answers = new Answer[ 1 ];
            final boolean[] timedout = new boolean[]{false};

            sendRequest(
                    session,
                    request,
                    new EventListener< Request, Answer >()
                    {
                        public void receivedSuccessMessage( Request request, Answer answer )
                        {
                            answers[ 0 ] = answer;
                        }

                        public void timeoutExpired( Request request )
                        {
                            timedout[ 0 ] = true;
                        }
                    } );

            new WaitUntil( 50, 10 * 1000, () -> answers[ 0 ] != null || timedout[ 0 ] );

            return answers[ 0 ];
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static class WaitUntil
    {
        public WaitUntil( long increment, long maxWait, Supplier< Boolean > until )
        {
            final long startedWaiting = System.currentTimeMillis();

            while ( ! until.get() && System.currentTimeMillis() - startedWaiting < maxWait )
            {
                try
                {
                    Thread.sleep( increment );
                }
                catch ( InterruptedException e )
                {
                    return;
                }
            }
        }
    }

    @Log4j2
    public static class ClientConfigParser extends DefaultHandler
    {
        @Getter
        private SimpleClient client;

        @Getter
        private Session session;

        @Getter
        private ApplicationId applicationId;

        @Getter
        public String destinationRealm;

        @Getter
        private java.util.Stack< Request > request = new java.util.Stack<>();

        @Getter
        private java.util.Stack< DiameterWriter > diameterWriter = new java.util.Stack<>();

        @Getter
        private java.util.Stack< Answer > answer = new java.util.Stack<>();

        @Getter
        @Setter
        private Map< String, Object > model;

        public void startDocument()
        {
            log.info( "Starting client events ..." );
        }

        public void endDocument()
        {
            log.info( "Finished client events" );
        }

        public void endElement( String uri, String localName, String qName )
        {
            switch ( qName )
            {
                case "client":
                    client = null;
                    break;

                case "session":
                    session = null;
                    break;

                case "applicationId":
                    applicationId = null;
                    break;

                case "destination":
                    destinationRealm = null;
                    break;

                case "request":
                    diameterWriter.peek().endElement( uri, localName, qName );
                    diameterWriter.pop();

                    request.pop();
                    break;

                case "send":
                    answer.pop();
                    break;

                default:
                    if ( ! diameterWriter.isEmpty() )
                    {
                        diameterWriter.peek().endElement( uri, localName, qName );
                    }
            }
        }

        public void startElement( String uri, String localName, String qName, Attributes attributes )
                throws SAXException
        {
            switch ( qName )
            {
                case "client":
                    newClientEvent( attributes );
                    break;

                case "start":
                    newStartEvent( attributes );
                    break;

                case "stop":
                    newStopEvent( attributes );
                    break;

                case "session":
                    newSessionEvent( attributes );
                    break;

                case "applicationId":
                    newApplicationEvent( attributes );
                    break;

                case "destination":
                    newDestinationEvent( attributes );
                    break;

                case "request":
                    newRequestEvent( attributes );
                    diameterWriter.peek().startElement( uri, localName, qName, attributes );
                    break;

                case "send":
                    newSendEvent( attributes );
                    break;

                case "answer":
                    newAnswerEvent( attributes );
                    break;

                default:
                    if ( ! diameterWriter.isEmpty() )
                    {
                        diameterWriter.peek().startElement( uri, localName, qName, attributes );
                    }
            }
        }

        private void newAnswerEvent( Attributes attributes ) throws SAXException
        {
            if ( answer.isEmpty() )
            {
                throw new SAXException( "new answer event but answer is empty" );
            }

            log.info( () -> format( "answer: \n%s", serializeAnswer( answer.peek() ) ) );
        }

        private void newSendEvent( Attributes attributes ) throws SAXException
        {
            if ( isNull( client ) )
            {
                throw new SAXException( "new send event but client is null" );
            }

            if ( isNull( session ) )
            {
                throw new SAXException( "new send event but session is null" );
            }

            if ( request.isEmpty() )
            {
                throw new SAXException( "new send event but request is empty" );
            }

            log.info( () -> format( "request: \n%s", serializeRequest( request.peek() ) ) );

            answer.push( client.sendRequest( session, request.peek() ) );

            // TODO: until get replaced
            model.putAll( DiameterModel.getModel( request.peek() ) );
            model.putAll( DiameterModel.getModel( answer.peek() ) );
        }


        private void newRequestEvent( Attributes attributes ) throws SAXException
        {
            if ( isNull( session ) )
            {
                throw new SAXException( "new request event but session is null" );
            }

            if ( isNull( applicationId ) )
            {
                throw new SAXException( "new request event but applicationId is null" );
            }

            if ( isNull( destinationRealm ) )
            {
                throw new SAXException( "new request event but destinationRealm is null" );
            }

            for ( ATTR ia : EnumSet.of( ATTR.COMMAND_CODE ) )
            {
                if ( ! ia.hasAttribute( attributes ) )
                {
                    throw new SAXException( format( "Missing request attribute: %s", ia.getAttribute() ) );
                }
            }


            request.push( session
                    .createRequest(
                            Integer.parseInt( ATTR.COMMAND_CODE.getAttribute( attributes ) ),
                            applicationId,
                            destinationRealm ) );


            if ( ATTR.TEMPLATE_URI.hasAttribute( attributes ) )
            {
                String requestXml = jstl()
                        .expandUri(
                                ATTR.TEMPLATE_URI.getAttribute( attributes ),
                                model
                        );

                buildRequest( request.peek(), requestXml );
            }

            diameterWriter.push( new DiameterWriter() );
            diameterWriter.peek().setRequest( request.peek() );
        }

        private void newApplicationEvent( Attributes attributes ) throws SAXException
        {
            if ( nonNull( applicationId ) )
            {
                throw new SAXException( "new applicationId event but applicationId is not null" );
            }

            for ( ATTR ia : EnumSet.of( ATTR.VENDOR_ID, ATTR.AUTH_APP_ID ) )
            {
                if ( ! ia.hasAttribute( attributes ) )
                {
                    throw new SAXException( format( "Missing applicationId attribute: %s", ia.getAttribute() ) );
                }
            }

            applicationId = ApplicationId
                    .createByAuthAppId(
                            Integer.parseInt( ATTR.VENDOR_ID.getAttribute( attributes ) ),
                            Integer.parseInt( ATTR.AUTH_APP_ID.getAttribute( attributes ) ) );
        }


        private void newDestinationEvent( Attributes attributes ) throws SAXException
        {
            if ( nonNull( destinationRealm ) )
            {
                throw new SAXException( "new destination event but destinationRealm is not null" );
            }

            for ( ATTR ia : EnumSet.of( ATTR.REALM ) )
            {
                if ( ! ia.hasAttribute( attributes ) )
                {
                    throw new SAXException( format( "Missing destination attribute: %s", ia.getAttribute() ) );
                }
            }

            destinationRealm = ATTR.REALM.getAttribute( attributes );
        }

        private void newClientEvent( Attributes attributes ) throws SAXException
        {
            if ( nonNull( client ) )
            {
                throw new SAXException( "new client event but client is not null" );
            }

            if ( ! Items.ATTR.DIAMETER_CONFIG_URI.hasAttribute( attributes ) )
            {
                throw new SAXException( format( "Missing client attribute: %s", Items.ATTR.DIAMETER_CONFIG_URI.getAttribute() ) );
            }

            try
            {
                if ( Items.ATTR.DICTIONARY_URI.hasAttribute( attributes ) )
                {
                    DictionarySingleton
                            .getDictionary(
                                    getLocalFileURL( SimpleServer.class, Items.ATTR.DICTIONARY_URI.getAttribute( attributes ) )
                                            .openStream() );
                }

                client = new SimpleClient( new XMLConfiguration(
                        getLocalFileURL(
                                SimpleServer.class,
                                Items.ATTR.DIAMETER_CONFIG_URI.getAttribute( attributes ) )
                                .openStream() ) );
            }
            catch ( Exception e )
            {
                throw new SAXException( e );
            }
        }

        private void newStopEvent( Attributes attributes ) throws SAXException
        {
            if ( isNull( client ) )
            {
                throw new SAXException( "new stop event but client is null" );
            }

            if ( ! ATTR.DISCONNECT_CAUSE.hasAttribute( attributes ) )
            {
                throw new SAXException( format( "Missing start attribute: %s", ATTR.DISCONNECT_CAUSE.getAttribute() ) );
            }

            client.stop( Integer.parseInt( ATTR.DISCONNECT_CAUSE.getAttribute( attributes ) ) );
        }

        private void newStartEvent( Attributes attributes ) throws SAXException
        {
            if ( isNull( client ) )
            {
                throw new SAXException( "new start event but client is null" );
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
                client.start(
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

        private void newSessionEvent( Attributes attributes ) throws SAXException
        {
            if ( isNull( client ) )
            {
                throw new SAXException( "new session event but client is null" );
            }
            if ( nonNull( session ) )
            {
                throw new SAXException( "new session event but session is not null" );
            }

            for ( ATTR ia : EnumSet.of( ATTR.SESSION_ID ) )
            {
                if ( ! ia.hasAttribute( attributes ) )
                {
                    throw new SAXException( format( "Missing session attribute: %s", ia.getAttribute() ) );
                }
            }

            try
            {
                session = client.getFactory().getNewSession( ATTR.SESSION_ID.getAttribute( attributes ) );
            }
            catch ( InternalException e )
            {
                throw new SAXException( e );
            }
        }
    }
}

package com.brentcroft.diameter.simple;

import com.brentcroft.diameter.DiameterModel;
import com.brentcroft.diameter.sax.DiameterWriter;
import com.brentcroft.tools.jstl.MapBindings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.client.impl.DictionarySingleton;
import org.jdiameter.client.impl.StackImpl;
import org.jdiameter.client.impl.helpers.XMLConfiguration;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.StringReader;
import java.util.function.Supplier;

import static com.brentcroft.diameter.DiameterRequestProcessor.serializeAnswer;
import static com.brentcroft.diameter.DiameterRequestProcessor.serializeRequest;
import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;
import static com.brentcroft.tools.jstl.MapBindings.jstl;
import static java.lang.String.format;

@Getter
@Log4j2
public class SimpleClient extends StackImpl implements Stack
{
    private final SessionFactory factory;

    @Setter
    private String templateUri;

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

    public void sendRequest( Request request, EventListener< Request, Answer > listener )
    {
        try
        {
            Session session = factory.getNewSession( request.getSessionId() );
            session.send( request, listener );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void main( String[] args )
    {
        String diameterConfig = "diameter/client.xml";
        String dictionaryUri = "diameter/dictionary.xml";
        String templateUri = "requests/basic-request.jstl";

        try
        {
            installDictionary( dictionaryUri );

            XMLConfiguration configuration = new XMLConfiguration(
                    getLocalFileURL( SimpleClient.class, diameterConfig )
                            .openStream() );

            SimpleClient client = new SimpleClient( configuration );

            client.setTemplateUri( templateUri );

            client.start();

            client.sendMessages();

            client.stop( 0 );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    public static void installDictionary( String dictionaryUri ) throws IOException
    {
        DictionarySingleton
                .getDictionary(
                        getLocalFileURL( SimpleClient.class, dictionaryUri )
                                .openStream() );
    }

    private void sendMessages() throws InternalException
    {
        String sessionId = "" + System.currentTimeMillis();
        int code = 272;
        String destRealm = "server.brentcroft.com";

        long vendorId = 0;
        long authApplicationId = 4;

        ApplicationId applicationId = ApplicationId.createByAuthAppId( vendorId, authApplicationId );


        Session session = factory.getNewSession( sessionId );

        {
            Request request = session.createRequest( code, applicationId, destRealm );

            MapBindings model = new MapBindings();

            String requestXml = getRequestXml( model );

            buildRequest( request, requestXml );

            log.info( () -> format( "\n\nrequest: \n%s\n", serializeRequest( request ) ) );

            Answer answer = sendRequest( request );

            log.info( () -> format( "\n\nanswer: \n%s\n", serializeAnswer( answer ) ) );

            log.info( () -> String.format( "\n\nmap: \n%s\n", DiameterModel.toString( DiameterModel.getModel( answer ), "\t" ) ) );
        }

        {
            Request request = session.createRequest( code, applicationId, destRealm );

            MapBindings model = new MapBindings();

            String requestXml = getRequestXml( model );

            buildRequest( request, requestXml );

            log.info( () -> format( "\n\nrequest: \n%s\n", serializeRequest( request ) ) );

            Answer answer = sendRequest( request );

            log.info( () -> format( "\n\nanswer: \n%s\n", serializeAnswer( answer ) ) );

            log.info( () -> format( "\n\nmap: \n%s\n", DiameterModel.toString( DiameterModel.getModel( answer ), "\t" ) ) );
        }
    }

    public String getRequestXml( MapBindings model )
    {
        return jstl()
                .expandUri(
                        templateUri,
                        model
                );
    }

    public void buildRequest( Request request, String requestXmlText )
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


    public Answer sendRequest( Request request )
    {
        try
        {
            final Answer[] answers = new Answer[ 1 ];
            final boolean[] timedout = new boolean[]{false};

            sendRequest( request, new EventListener< Request, Answer >()
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
        catch ( Exception var5 )
        {
            throw new RuntimeException( var5 );
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
}

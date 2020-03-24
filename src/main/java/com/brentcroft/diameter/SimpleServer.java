package com.brentcroft.diameter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.client.impl.DictionarySingleton;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.XMLConfiguration;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;
import static java.lang.String.format;

@Getter
@Setter
@Log4j2
public class SimpleServer extends StackImpl implements Stack
{
    private static final long VENDOR_ID = 0L;
    private static final long AUTH_APP_ID = 4L;
    private static final int DEFAULT_STACK_CREATION_TIMEOUT_MS = 30000;


    public SimpleServer( Configuration config, NetworkReqListener networkReqListener )
    {
        try
        {
            this.init( config );

            // Let it stabilize...
            Thread.sleep( 500 );

            Network network = unwrap( Network.class );

            Set< ApplicationId > appIds = getMetaData().getLocalPeer().getCommonApplications();

            for ( ApplicationId appId : appIds )
            {
                log.info( () -> format( "Adding Listener for appId: %s", appId ) );

                network.addNetworkReqListener( networkReqListener, appId );
            }
        }
        catch ( ApplicationAlreadyUseException | IllegalDiameterStateException | InternalException | InterruptedException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void main( String[] args )
    {
        String diameterConfig = "diameter/server.xml";
        String dictionaryUri = "diameter/dictionary.xml";
        String templateUri = "responses/basic-answer.jstl";

        SimpleProcessor processor = new SimpleProcessor();

        processor.setTemplateUri( templateUri );

        try
        {
            installDictionary( dictionaryUri );

            SimpleStackListener stackListener = new SimpleStackListener();

            stackListener.setDiameterRequestProcessor( processor );

            SimpleServer server = new SimpleServer(
                    new XMLConfiguration(
                            getLocalFileURL( SimpleServer.class, diameterConfig )
                                    .openStream() ),
                    stackListener );


            Network network = server.unwrap( Network.class );

            network.addNetworkReqListener( stackListener, ApplicationId.createByAuthAppId( VENDOR_ID, AUTH_APP_ID ) );

            server.start( Mode.ALL_PEERS, DEFAULT_STACK_CREATION_TIMEOUT_MS, TimeUnit.MILLISECONDS );
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
                        getLocalFileURL( SimpleServer.class, dictionaryUri )
                                .openStream() );
    }
}

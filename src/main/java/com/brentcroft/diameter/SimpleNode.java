package com.brentcroft.diameter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.client.impl.DictionarySingleton;
import org.jdiameter.client.impl.helpers.XMLConfiguration;
import org.jdiameter.server.impl.StackImpl;

import java.io.IOException;
import java.util.Set;

import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;
import static java.lang.String.format;

@Getter
@Setter
@Log4j2
public class SimpleNode extends StackImpl implements Stack
{
    public SimpleNode( Configuration config, NetworkReqListener networkReqListener )
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

            SimpleStack stack = new SimpleStack();

            stack.setStack(
                    new SimpleNode(
                            new XMLConfiguration(
                                    getLocalFileURL( SimpleNode.class, diameterConfig )
                                            .openStream() ),
                            stack ) );

            stack.setDiameterRequestProcessor( processor );

            stack.start();
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
                        getLocalFileURL( SimpleNode.class, dictionaryUri )
                                .openStream() );
    }
}

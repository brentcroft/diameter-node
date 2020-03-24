package com.brentcroft.diameter;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.server.impl.StackImpl;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.Objects.isNull;

/**
 * KCOM Diameter OCS Stub
 * <p>
 * Based on example code delivered with the Mobicents Diameter Charging Server Simulator.
 *
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author Steve Dwyer, KCOM
 */
@Log4j2
public class SimpleStack implements NetworkReqListener, EventListener< Request, Answer >
{
    private static final int DEFAULT_STACK_CREATION_TIMEOUT_MS = 30000;
    private static final long VENDOR_ID = 0L;
    private static final long AUTH_APP_ID = 4L;

    @Setter
    private StackImpl stack;

    @Setter
    private DiameterRequestProcessor diameterRequestProcessor;


    public void start() throws InternalException, ApplicationAlreadyUseException, IllegalDiameterStateException
    {
        Network network = stack.unwrap( Network.class );

        network.addNetworkReqListener( this, ApplicationId.createByAuthAppId( VENDOR_ID, AUTH_APP_ID ) );

        stack.start( Mode.ALL_PEERS, DEFAULT_STACK_CREATION_TIMEOUT_MS, TimeUnit.MILLISECONDS );
    }

    public void stop()
    {
        stack.stop( 0 );
    }


    @Override
    public Answer processRequest( Request request )
    {
        log.info( () -> format( "<< Received Request [%s]", request ) );

        try
        {
            if ( isNull( diameterRequestProcessor ) )
            {
                throw new RuntimeException( "No DiameterRequestProcessor" );
            }

            return diameterRequestProcessor.processRequest( request );
        }
        catch ( Exception e )
        {
            log.error( format( ">< Failure handling received request [%s]", request ), e );
        }

        return null;
    }


    @Override
    public void receivedSuccessMessage( Request request, Answer answer )
    {
        log.info( () -> format( "<< Received Success Message for Request [%s] and Answer [%s]", request, answer ) );
    }

    @Override
    public void timeoutExpired( Request request )
    {
        log.info( () -> format( "<< Received Timeout for Request [%s]", request ) );
    }

}

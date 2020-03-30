package com.brentcroft.diameter.simple;

import com.brentcroft.diameter.DiameterRequestProcessor;
import com.brentcroft.diameter.JstlProcessor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.Answer;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.Request;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@Log4j2
public class SimpleStackListener implements NetworkReqListener, EventListener< Request, Answer >
{
    @Setter
    private JstlProcessor diameterRequestProcessor;

    @Setter
    private String name;


    @Override
    public Answer processRequest( Request request )
    {
        log.info( () -> format( "[%s] Received: [%s]", name, request ) );

        log.debug( () -> format( "[%s] Request:\n%s", name, DiameterRequestProcessor.serializeRequest( request ) ) );

        try
        {
            if ( isNull( diameterRequestProcessor ) )
            {
                throw new RuntimeException( format( "[%s] No DiameterRequestProcessor: ", name ) );
            }

            Answer answer = diameterRequestProcessor.processRequest( request );

            log.debug( () -> format( "[%s] Answer:\n%s", name, DiameterRequestProcessor.serializeAnswer( answer ) ) );

            return answer;
        }
        catch ( Exception e )
        {
            log.error( format( "[%s] Error processing request [%s]", name, request ), e );
        }

        return null;
    }


    @Override
    public void receivedSuccessMessage( Request request, Answer answer )
    {
        log.info( () -> format( "[%s] Success response: [%s] -> [%s]", name, request, answer ) );
    }

    @Override
    public void timeoutExpired( Request request )
    {
        log.info( () -> format( "[%s] Timeout: [%s]", name, request ) );
    }
}

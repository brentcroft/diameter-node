package com.brentcroft.diameter.simple;

import com.brentcroft.diameter.DiameterModel;
import com.brentcroft.diameter.DiameterRequestProcessor;
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
    private DiameterRequestProcessor diameterRequestProcessor;

    @Override
    public Answer processRequest( Request request )
    {
        log.info( () -> format( "Received: [%s]", request ) );

        log.debug( () -> format( "\nrequest: \n%s\n", DiameterRequestProcessor.serializeRequest( request ) ) );
        log.debug( () -> format( "\nmap: \n%s\n", DiameterModel.toString( DiameterModel.getModel( request ) ) ) );

        try
        {
            if ( isNull( diameterRequestProcessor ) )
            {
                throw new RuntimeException( "No DiameterRequestProcessor" );
            }

            Answer answer = diameterRequestProcessor.processRequest( request );

            log.debug( () -> format( "\nanswer: \n%s\n", DiameterRequestProcessor.serializeAnswer( answer ) ) );
            log.debug( () -> format( "\nmap: \n%s\n", DiameterModel.toString( DiameterModel.getModel( answer ) ) ) );

            return answer;
        }
        catch ( Exception e )
        {
            log.error( format( "Error processing request [%s]", request ), e );
        }

        return null;
    }


    @Override
    public void receivedSuccessMessage( Request request, Answer answer )
    {
        log.info( () -> format( "Success response: [%s] -> [%s]", request, answer ) );
    }

    @Override
    public void timeoutExpired( Request request )
    {
        log.info( () -> format( "Timeout: [%s]", request ) );
    }
}
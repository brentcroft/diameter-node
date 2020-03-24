package com.brentcroft.diameter;

import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;

public interface DiameterRequestProcessor
{
    Answer processRequest( Request request );
}

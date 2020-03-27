package com.brentcroft.diameter;

import com.brentcroft.diameter.sax.AnswerInputSource;
import com.brentcroft.diameter.sax.DiameterReader;
import com.brentcroft.diameter.sax.RequestInputSource;
import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public interface DiameterRequestProcessor
{
    Answer processRequest( Request request );


    static String serializeRequest( Request request )
    {

        RequestInputSource rsi = new RequestInputSource();
        rsi.setRequest( request );

        SAXSource saxSource = new SAXSource();
        saxSource.setInputSource( rsi );
        saxSource.setXMLReader( new DiameterReader() );

        StringWriter stringWriter = new StringWriter();
        StreamResult transformResult = new StreamResult();
        transformResult.setWriter( stringWriter );

        try
        {
            configuredTransformer()
                    .transform( saxSource, transformResult );
        }
        catch ( TransformerException e )
        {
            throw new RuntimeException( format( "Error serializing request: %s", request ), e );
        }

        return stringWriter.toString();
    }

    static String serializeAnswer( Answer answer )
    {
        if ( isNull( answer ) )
        {
            return "null";
        }

        AnswerInputSource asi = new AnswerInputSource();
        asi.setAnswer( answer );

        SAXSource saxSource = new SAXSource();
        saxSource.setInputSource( asi );
        saxSource.setXMLReader( new DiameterReader() );

        StringWriter stringWriter = new StringWriter();
        StreamResult transformResult = new StreamResult();
        transformResult.setWriter( stringWriter );

        try
        {
            configuredTransformer()
                    .transform( saxSource, transformResult );
        }
        catch ( TransformerException e )
        {
            throw new RuntimeException( format( "Error serializing answer: %s", answer ), e );
        }

        return stringWriter.toString();
    }

    static Transformer configuredTransformer() throws TransformerConfigurationException
    {
        Transformer transformer = TransformerFactory
                .newInstance()
                .newTransformer();

        transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );

        transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );

        final int indent = 4;

        transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", Integer.toString( indent ) );


        return transformer;
    }
}

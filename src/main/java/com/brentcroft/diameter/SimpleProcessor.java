package com.brentcroft.diameter;

import com.brentcroft.diameter.sax.DiameterWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

import static com.brentcroft.tools.jstl.MapBindings.jstl;
import static java.lang.String.format;

@Getter
@Setter
@Log4j2
public class SimpleProcessor implements DiameterRequestProcessor
{
    private String templateUri;


    public String getAnswer( Request request )
    {
        return jstl()
                .expandUri(
                        templateUri,
                        DiameterModel.getModel( request )
                );
    }


    @Override
    public Answer processRequest( Request request )
    {
        final String answerXmlText = getAnswer( request );


        log.info( () -> format( "\n\nrequest: \n%s\n", DiameterRequestProcessor.serializeRequest( request ) ) );

        log.info( () -> format( "\n\nmap: \n%s\n", DiameterModel.toString( DiameterModel.getModel( request ), "" ) ) );

        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            SAXSource saxSource = new SAXSource();
            saxSource.setInputSource( new InputSource( new StringReader( answerXmlText ) ) );

            DiameterWriter diameterWriter = new DiameterWriter();

            diameterWriter.setRequest( request );

            SAXResult transformResult = new SAXResult();
            transformResult.setHandler( diameterWriter );

            transformer.transform( saxSource, transformResult );

            log.info( () -> format( "\n\nanswer: \n%s\n", DiameterRequestProcessor.serializeAnswer( diameterWriter.getAnswer() ) ) );

            return diameterWriter.getAnswer();
        }
        catch ( TransformerException e )
        {
            e.printStackTrace();
        }

        return null;
    }
}

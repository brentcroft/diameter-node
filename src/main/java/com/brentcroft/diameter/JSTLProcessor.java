package com.brentcroft.diameter;

import com.brentcroft.diameter.sax.DiameterWriter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

import static com.brentcroft.tools.jstl.MapBindings.jstl;

@Getter
@Setter
@Log4j2
public class JSTLProcessor implements DiameterRequestProcessor
{
    private String templateUri;

    @Override
    public Answer processRequest( Request request )
    {
        try
        {
            DiameterWriter diameterWriter = new DiameterWriter();

            diameterWriter.setRequest( request );

            TransformerFactory
                    .newInstance()
                    .newTransformer()
                    .transform(
                            new SAXSource( new InputSource( new StringReader( getAnswerText( request ) ) ) ),
                            new SAXResult( diameterWriter ) );

            return diameterWriter.getAnswer();
        }
        catch ( TransformerException e )
        {
            e.printStackTrace();
        }

        return null;
    }

    public String getAnswerText( Request request )
    {
        return jstl()
                .expandUri(
                        templateUri,
                        DiameterModel.getModel( request )
                );
    }
}

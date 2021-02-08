package com.brentcroft.diameter;

import com.brentcroft.diameter.sax.DiameterWriter;
import com.brentcroft.tools.jstl.JstlTemplateManager;
import com.brentcroft.tools.jstl.MapBindings;
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

@Getter
@Setter
@Log4j2
public class JstlProcessor implements DiameterRequestProcessor
{
    private String templateUri;
    private final static JstlTemplateManager jstl = new JstlTemplateManager();

    private final MapBindings model = new MapBindings();

    @Override
    public Answer processRequest( Request request )
    {
        model.putAll( DiameterModel.getModel( request ) );

        try
        {
//            if (isNull(jstlDocument.getDocument()))
//            {
//                jstlDocument.setDocument(
//                        DocumentBuilderFactory
//                                .newInstance()
//                                .newDocumentBuilder()
//                                .parse( new InputSource( getLocalFileURL( JstlProcessor.class, templateUri ).openStream() ) ) );
//            }

            DiameterWriter diameterWriter = new DiameterWriter();

            diameterWriter.setRequest( request );

            TransformerFactory
                    .newInstance()
                    .newTransformer()
                    .transform(
                            new SAXSource( new InputSource( new StringReader( getAnswerText() ) ) ),
                            new SAXResult( diameterWriter ) );

            return diameterWriter.getAnswer();
        }
        catch ( TransformerException e )
        {
            e.printStackTrace();
        }

        return null;
    }

    public String getAnswerText()
    {
        return jstl
                .expandUri(
                        templateUri,
                        model
                );
    }
}

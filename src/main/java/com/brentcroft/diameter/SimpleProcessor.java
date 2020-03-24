package com.brentcroft.diameter;

import com.brentcroft.diameter.sax.AnswerInputSource;
import com.brentcroft.diameter.sax.DiameterReader;
import com.brentcroft.diameter.sax.DiameterWriter;
import com.brentcroft.diameter.sax.RequestInputSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.xml.sax.InputSource;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static com.brentcroft.tools.jstl.MapBindings.jstl;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

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
                        getModel( request )
                );
    }


    @Override
    public Answer processRequest( Request request )
    {
        final String answerXmlText = getAnswer( request );


        log.info( () -> format( "\n\nrequest: \n%s\n", serializeRequest( request ) ) );


        log.info( () -> format( "\n\nmap: \n%s\n", getModel( request ) ) );

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

            log.info( () -> format( "\n\nanswer: \n%s\n", serializeAnswer( diameterWriter.getAnswer() ) ) );

            return diameterWriter.getAnswer();
        }
        catch ( TransformerException e )
        {
            e.printStackTrace();
        }

        return null;
    }


    public static Map< String, Object > getModel( Request request )
    {
        final Map< String, Object > model = new HashMap<>();

        try
        {
            Map< String, Object > req = new HashMap<>();
            req.put( "commandCode", request.getCommandCode() );
            req.put( "applicationId", request.getApplicationId() );
            req.put( "version", request.getVersion() );

            List< Map< String, Object > > applicationIds = new ArrayList<>();

            ofNullable( request.getApplicationIdAvps() )
                    .orElse( Collections.emptyList() )
                    .stream()
                    .filter( Objects::nonNull )
                    .forEach( appId -> {
                        Map< String, Object > applicationId = new HashMap<>();
                        applicationId.put( "vendorId", appId.getVendorId() );
                        applicationId.put( "authAppId", appId.getAuthAppId() );
                        applicationId.put( "acctAppId", appId.getAcctAppId() );
                        applicationIds.add( applicationId );
                    } );

            req.put( "applicationIds", applicationIds );


            Map< String, Object > avps = new HashMap<>();
            parseAvps( request.getAvps(), avps );
            req.put( "avps", avps );


            model.put( "request", req );
        }
        catch ( AvpDataException e )
        {
            e.printStackTrace();
        }

        return model;
    }

    private static void parseAvps( AvpSet avps, Map< String, Object > bindings ) throws AvpDataException
    {
        if ( isNull( avps ) )
        {
            return;
        }

        for ( Avp avp : avps )
        {
            switch ( avp.getCode() )
            {

                // Transaction-Id
                case 7000:
                    bindings.put( "transactionId", avp.getUTF8String() );
                    break;

                // Call-Type
                case 7050:
                    bindings.put( "callType", avp.getInteger32() );
                    break;

                // Event-Sub-Type
                case 7051:
                    bindings.put( "eventSubType", avp.getInteger32() );
                    break;

                // Destination-Type
                case 7053:
                    bindings.put( "destinationType", avp.getInteger32() );
                    break;

                // Destination-Value
                case 7054:
                    bindings.put( "destinationValue", avp.getUTF8String() );
                    break;

                // Destination-Format
                case 7055:
                    bindings.put( "destinationFormat", avp.getInteger32() );
                    break;

                // Money-Integer
                case 7026:
                    bindings.put( "moneyInteger", avp.getInteger32() );
                    break;

                //Money-Fractional
                case 7027:
                    bindings.put( "moneyFractional", avp.getUTF8String() );
                    break;


                // Retailer-Name
                case 7043:
                    bindings.put( "retailerName", avp.getUTF8String() );
                    break;

                // Information (sector/xxx)
                case 7044:
                    bindings.put( "information", avp.getUTF8String() );
                    break;

                // Reversal-Allowed
                case 7048:
                    bindings.put( "information", avp.getInteger32() );
                    break;

                // Block-Notification
                case 7047:
                    bindings.put( "blockNotification", avp.getInteger32() );
                    break;


                default:
                    try
                    {
                        if ( nonNull( avp.getRaw() ) && avp.getRaw().length > 0 )
                        {
                            parseAvps( avp.getGrouped(), bindings );
                        }
                    }
                    catch ( AvpDataException e )
                    {
                        log.trace( () -> format( "Bad group: %s", avp ), e );
                    }
            }
        }
    }


    public String serializeRequest( Request request )
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
            log.warn( () -> format( "Error serializing request: %s", request ), e );
        }

        return stringWriter.toString();
    }

    public String serializeAnswer( Answer answer )
    {

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
            log.warn( () -> format( "Error serializing answer: %s", answer ), e );
        }

        return stringWriter.toString();
    }

    private Transformer configuredTransformer() throws TransformerConfigurationException
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

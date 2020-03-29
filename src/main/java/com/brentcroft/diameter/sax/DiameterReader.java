package com.brentcroft.diameter.sax;

import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.api.validation.AvpRepresentation;
import org.jdiameter.common.impl.validation.DictionaryImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Reads a JDiameter Request or Answer object
 * generating a sequence of SAX events.
 */
@Log4j2
public class DiameterReader extends AbstractXMLReader implements Items
{
    @Override
    public void parse( InputSource input ) throws SAXException
    {
        if ( input instanceof AnswerInputSource )
        {
            parse( ( ( AnswerInputSource ) input ).getAnswer() );
        }
        else if ( input instanceof RequestInputSource )
        {
            parse( ( ( RequestInputSource ) input ).getRequest() );
        }
    }

    public void parse( Request request ) throws SAXException
    {
        ContentHandler ch = getContentHandler();

        ch.startDocument();

        AttributesImpl atts = getCommonAttributes( request );

        ATTR.NETWORK_REQUEST.setAttribute( atts, NAMESPACE_URI, String.valueOf( request.isNetworkRequest() ) );

        String tag = TAG.REQUEST.getTag();
        ch.startElement( NAMESPACE_URI, tag, tag, atts );

        parse( request.getApplicationIdAvps() );
        parse( request.getAvps() );

        ch.endElement( NAMESPACE_URI, tag, tag );
        ch.endDocument();
    }


    public void parse( Answer answer ) throws SAXException
    {
        ContentHandler ch = getContentHandler();

        ch.startDocument();

        AttributesImpl atts = getCommonAttributes( answer );

        String tag = TAG.ANSWER.getTag();
        ch.startElement( NAMESPACE_URI, tag, tag, atts );

        parse( answer.getApplicationIdAvps() );
        parse( answer.getAvps() );

        ch.endElement( NAMESPACE_URI, tag, tag );
        ch.endDocument();
    }

    public AttributesImpl getCommonAttributes( Message message )
    {
        AttributesImpl atts = new AttributesImpl();

        ATTR.APPLICATION_ID.setAttribute( atts, NAMESPACE_URI, String.valueOf( message.getApplicationId() ) );
        ATTR.COMMAND_CODE.setAttribute( atts, NAMESPACE_URI, String.valueOf( message.getCommandCode() ) );

        if ( message.getEndToEndIdentifier() != 0 )
        {
            ATTR.END_TO_END_IDENTIFIER.setAttribute( atts, NAMESPACE_URI, String.valueOf( message.getEndToEndIdentifier() ) );
        }

        if ( message.getHopByHopIdentifier() != 0 )
        {
            ATTR.HOP_BY_HOP_IDENTIFIER.setAttribute( atts, NAMESPACE_URI, String.valueOf( message.getHopByHopIdentifier() ) );
        }

        if ( nonNull( message.getSessionId() ) )
        {
            ATTR.SESSION_ID.setAttribute( atts, NAMESPACE_URI, String.valueOf( message.getSessionId() ) );
        }

        if ( message.getVersion() != 0 )
        {
            ATTR.VERSION.setAttribute( atts, NAMESPACE_URI, String.valueOf( message.getVersion() ) );
        }

        return atts;
    }


    public void parse( List< ApplicationId > applicationIds ) throws SAXException
    {
        if ( nonNull( applicationIds ) )
        {
            for ( ApplicationId applicationId : applicationIds )
            {
                parse( applicationId );
            }
        }
    }

    public void parse( ApplicationId applicationId ) throws SAXException
    {
        if ( isNull( applicationId ) )
        {
            return;
        }

        ContentHandler ch = getContentHandler();

        AttributesImpl atts = new AttributesImpl();

        ATTR.VENDOR_ID.setAttribute( atts, NAMESPACE_URI, String.valueOf( applicationId.getVendorId() ) );
        ATTR.ACCT_APP_ID.setAttribute( atts, NAMESPACE_URI, String.valueOf( applicationId.getAcctAppId() ) );
        ATTR.AUTH_APP_ID.setAttribute( atts, NAMESPACE_URI, String.valueOf( applicationId.getAuthAppId() ) );

        final String tag = TAG.APPLICATION_ID.getTag();
        ch.startElement( NAMESPACE_URI, tag, tag, atts );
        ch.endElement( NAMESPACE_URI, tag, tag );
    }

    public void parse( AvpSet avpSet ) throws SAXException
    {
        if ( nonNull( avpSet ) )
        {
            for ( Avp avp : avpSet.asArray() )
            {
                parse( avp );
            }
        }
    }

    private void parse( Avp avp ) throws SAXException
    {
        if ( isNull( avp ) )
        {
            return;
        }

        ContentHandler ch = getContentHandler();


        AttributesImpl atts = new AttributesImpl();

        ATTR.CODE.setAttribute( atts, NAMESPACE_URI, String.valueOf( avp.getCode() ) );

        if ( avp.getVendorId() != 0 )
        {
            ATTR.VENDOR_ID.setAttribute( atts, NAMESPACE_URI, String.valueOf( avp.getVendorId() ) );
        }

        AvpRepresentation avpRep = DictionaryImpl.INSTANCE.getAvp( avp.getCode(), avp.getVendorId() );

        if ( nonNull( avpRep ) )
        {
            ATTR.TYPE.setAttribute( atts, NAMESPACE_URI, avpRep.getType() );
            ATTR.NAME.setAttribute( atts, NAMESPACE_URI, avpRep.getName() );
        }

        final String value = getAvpValue( avp, avpRep );

        if ( nonNull( value ) )
        {
            ATTR.VALUE.setAttribute( atts, NAMESPACE_URI, value );
        }


        ch.startElement( NAMESPACE_URI, "avp", "avp", atts );

        try
        {
            parse( avp.getGrouped() );
        }
        catch ( AvpDataException e )
        {
//            if ( nonNull( value ) )
//            {
//                char[] chars = value.toCharArray();
//                ch.characters( chars, 0, chars.length );
//            }
        }

        ch.endElement( NAMESPACE_URI, "avp", "avp" );
    }


    public String getAvpValue( Avp avp, AvpRepresentation avpRep )
    {
        String avpValue = null;

        try
        {
            if ( isNull( avpRep ) )
            {
                return avp.getUTF8String();
            }

            String avpType = avpRep.getType();

            switch ( avpType )
            {
                case "AppId":
                case "Integer32":
                    avpValue = String.valueOf( avp.getInteger32() );
                    break;

                case "VendorId":
                case "Unsigned32":
                    avpValue = String.valueOf( avp.getUnsigned32() );
                    break;

                case "Float64":
                    avpValue = String.valueOf( avp.getFloat64() );
                    break;

                case "Integer64":
                    avpValue = String.valueOf( avp.getInteger64() );
                    break;

                case "Unsigned64":
                    avpValue = String.valueOf( avp.getUnsigned64() );
                    break;

                case "Time":
                    avpValue = String.valueOf( avp.getTime() );
                    break;

                case "UTF8String":
                    avpValue = avp.getUTF8String();
                    break;

                case "OctetString":
                    avpValue = ELEMENT_PARSER.bytesToOctetString( avp.getOctetString() );
                    break;

                case "Grouped":
                    avpValue = null;
                    break;

                default:
                    log.debug( () -> format( "Un-expected AVP type: %s", avpType ) );
            }

        }
        catch ( Exception e )
        {
            log.warn( () -> format( "Error processing avpRep: %s", avpRep ), e );
        }

        return avpValue;
    }
}

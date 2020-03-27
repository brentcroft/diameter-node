package com.brentcroft.diameter.sax;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.Answer;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Request;
import org.jdiameter.api.validation.AvpRepresentation;
import org.jdiameter.client.api.parser.ParseException;
import org.jdiameter.common.impl.validation.DictionaryImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Stack;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@Log4j2
public class DiameterWriter extends DefaultHandler implements Items
{
    @Setter
    @Getter
    private Request request;

    @Getter
    private Answer answer;

    private final Stack< AvpSet > avpStack = new Stack<>();


    private void maybeConsumeAttribute( ATTR attr, Attributes attributes, Consumer< String > setter )
    {
        if ( attr.hasAttribute( attributes ) )
        {
            setter.accept( attr.getAttribute( attributes ) );
        }
    }

    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
    {
        if ( TAG.REQUEST.isTag( qName ) )
        {
            if ( isNull( request ) )
            {
                throw new SAXException( "request is null" );
            }

            maybeConsumeAttribute(
                    ATTR.PROXIABLE, attributes,
                    v -> request.setProxiable( Boolean.parseBoolean( v ) ) );


            avpStack.push( request.getAvps() );
        }
        else if ( TAG.ANSWER.isTag( qName ) )
        {
            if ( isNull( request ) )
            {
                throw new SAXException( "request is null" );
            }

            answer = request.createAnswer();
            avpStack.push( answer.getAvps() );
        }
        else if ( TAG.AVP.isTag( qName ) )
        {
            int code = ATTR.CODE.hasAttribute( attributes )
                       ? Integer.parseInt( ATTR.CODE.getAttribute( attributes ) )
                       : 0;

            // if not present then assumed to be 0
            long vendorId = ATTR.VENDOR_ID.hasAttribute( attributes )
                            ? Integer.parseInt( ATTR.VENDOR_ID.getAttribute( attributes ) )
                            : 0;

            String type = ATTR.TYPE.getAttribute( attributes );


            String value = ATTR.VALUE.getAttribute( attributes );

            AvpRepresentation avpRep = DictionaryImpl.INSTANCE.getAvp( code, vendorId );

            if ( isNull( avpRep ) )
            {
                log.info( () -> format( "No AvpRepresentation for coordinates: code=%s, vendorId=%s, value=%s; guessing data ...", code, vendorId, value ) );

                byte[] rawData = guessData( code, vendorId, value );

                avpStack.peek().addAvp( code, rawData, vendorId, true, false );
                avpStack.push( null );
            }
            else
            {
                // attribute may override dictionary
                if ( isNull( type ) )
                {
                    type = avpRep.getType();
                }

                if ( avpStack.isEmpty() )
                {
                    throw new SAXException( "AvpSet stack is empty" );
                }

                if ( avpRep.isGrouped() )
                {
                    avpStack.push( avpStack.peek().addGroupedAvp( code, vendorId, true, false ) );
                }
                else
                {
                    byte[] rawData = ATTR.VALUE.hasAttribute( attributes )
                                     ? getAvpData( type, value )
                                     : null;

                    avpStack.peek().addAvp( code, rawData, vendorId, true, false );
                    avpStack.push( null );
                }
            }
        }
    }

    public void endElement( String uri, String localName, String qName )
    {
        if ( TAG.AVP.isTag( qName ) )
        {
            avpStack.pop();
        }
    }


    public byte[] getAvpData( String avpType, String value )
    {
        try
        {
            switch ( avpType )
            {
                case "AppId":
                case "Integer32":
                    return ELEMENT_PARSER.int32ToBytes( Integer.parseInt( value ) );

                case "VendorId":
                case "Unsigned32":
                    return ELEMENT_PARSER.intU32ToBytes( Long.parseLong( value ) );

                case "Float64":
                    return ELEMENT_PARSER.float64ToBytes( Double.parseDouble( value ) );

                case "Integer64":
                    return ELEMENT_PARSER.int64ToBytes( Long.parseLong( value ) );

                case "Unsigned64":
                    return ELEMENT_PARSER.int64ToBytes( Long.parseLong( value ) );

                case "Time":
                    return ELEMENT_PARSER.dateToBytes( new Date() );

                case "Grouped":
                    return null;

                case "UTF8String":
                    return ELEMENT_PARSER.utf8StringToBytes( value );

                case "OctetString":
                    return ELEMENT_PARSER.octetStringToBytes( value );
            }
        }
        catch ( ParseException e )
        {
            log.warn( () -> format( "Error processing avpType: %s", avpType ), e );
        }
        return null;
    }

    public static byte[] guessData( int code, long vendorId, String value )
    {
        if ( isNull( value ) )
        {
            return null;
        }

        try
        {
            log.warn( () -> format( "No AvpRepresentation for coordinates: code=%s, vendorId=%s, value=%s; trying integer.", code, vendorId, value ) );

            return ELEMENT_PARSER.int32ToBytes( Integer.parseInt( value ) );
        }
        catch ( Exception e )
        {
            try
            {
                log.warn( () -> format( "No AvpRepresentation for coordinates: code=%s, vendorId=%s, value=%s; trying double.", code, vendorId, value ) );

                return ELEMENT_PARSER.float64ToBytes( Double.parseDouble( value ) );
            }
            catch ( Exception e2 )
            {
                log.warn( () -> format( "No AvpRepresentation for coordinates: code=%s, vendorId=%s, value=%s; assuming string.", code, vendorId, value ) );

                return value.getBytes( StandardCharsets.UTF_8 );
            }
        }
    }
}

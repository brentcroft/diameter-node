package com.brentcroft.diameter;

import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.*;
import org.jdiameter.api.validation.AvpRepresentation;
import org.jdiameter.common.impl.validation.DictionaryImpl;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Log4j2
public class DiameterModel
{
    public static Map< String, Object > getModel( Request request )
    {
        final Map< String, Object > model = new HashMap<>();

        try
        {
            Map< String, Object > req = new HashMap<>();

            req.put( "networkRequest", request.isNetworkRequest() );

            parseMessage( request, req );

            model.put( "request", req );
        }
        catch ( AvpDataException e )
        {
            e.printStackTrace();
        }

        return model;
    }

    public static Map< String, Object > getModel( Answer answer )
    {
        final Map< String, Object > model = new HashMap<>();

        if ( nonNull( answer ) )
        {
            try
            {
                Map< String, Object > req = new HashMap<>();

                req.put( "resultCode", answer.getResultCode() );

                parseMessage( answer, req );

                model.put( "answer", req );
            }
            catch ( AvpDataException e )
            {
                e.printStackTrace();
            }
        }
        return model;
    }

    public static String toString( Map< ?, ? > model )
    {
        return toString( model, "\t" );
    }

    public static String toString( Map< ?, ? > model, String indent )
    {
        return "{\n" + model.entrySet().stream()
                .map( e -> indent + "\"" + e.getKey() + "\"" + ":" + valueOrMap( e.getValue(), indent ) )
                .collect( Collectors.joining( ", \n" ) ) + "}";
    }

    public static Object valueOrMap( Object value, String indent )
    {
        if ( value instanceof Map )
        {
            return toString( ( Map< ?, ? > ) value, indent + "\t" );
        }
        else if ( nonNull( value ) )
        {
            return "\"" + value.toString().replaceAll( "\"", "\\\"" ) + "\"";
        }
        else
        {
            return "\"null\"";
        }
    }

    private static void parseMessage( Message message, Map< String, Object > model ) throws AvpDataException
    {
        model.put( "commandCode", message.getCommandCode() );
        model.put( "applicationId", message.getApplicationId() );
        model.put( "version", message.getVersion() );

        List< Map< String, Object > > applicationIds = new ArrayList<>();

        ofNullable( message.getApplicationIdAvps() )
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

        model.put( "applicationIds", applicationIds );

        Map< String, Object > avps = new HashMap<>();
        parseAvps( message.getAvps(), avps );
        model.put( "avps", avps );
    }


    private static void parseAvps( AvpSet avps, Map< String, Object > model ) throws AvpDataException
    {
        if ( isNull( avps ) )
        {
            return;
        }

        for ( Avp avp : avps )
        {
            AvpRepresentation avpRep = DictionaryImpl.INSTANCE.getAvp( avp.getCode(), avp.getVendorId() );

            if ( isNull( avpRep ) )
            {
                log.warn( () -> format( "Avp not found in dictionary: code=%s, vendorId=%s", avp.getCode(), avp.getVendorId() ) );

                continue;
            }

            switch ( avpRep.getType() )
            {
                case "AppId":
                case "Integer32":
                    model.put( toMemberName( avpRep.getName() ), avp.getInteger32() );
                    break;

                case "VendorId":
                case "Unsigned32":
                    model.put( toMemberName( avpRep.getName() ), avp.getUnsigned32() );
                    break;

                case "Float64":
                    model.put( toMemberName( avpRep.getName() ), avp.getFloat64() );
                    break;

                case "Integer64":
                    model.put( toMemberName( avpRep.getName() ), avp.getInteger64() );
                    break;

                case "Unsigned64":
                    model.put( toMemberName( avpRep.getName() ), avp.getUnsigned64() );
                    break;

                case "Time":
                    model.put( toMemberName( avpRep.getName() ), avp.getTime() );
                    break;

                case "UTF8String":
                    model.put( toMemberName( avpRep.getName() ), avp.getUTF8String() );
                    break;

                case "OctetString":
                    model.put( toMemberName( avpRep.getName() ), avp.getOctetString() );
                    break;

                case "Grouped":
                    Map< String, Object > childAvps = new HashMap<>();
                    parseAvps( avp.getGrouped(), childAvps );
                    model.put( toMemberName( avpRep.getName() ), childAvps );
                    break;

                default:
                    log.debug( () -> format( "Un-expected AVP type: %s", avpRep.getType() ) );
            }
        }
    }

    private static String toMemberName( String name )
    {
        return name.substring( 0, 1 ).toLowerCase() + name.substring( 1 ).replaceAll( "-", "" );
    }
}

package com.brentcroft.diameter.sax;

import org.jdiameter.client.impl.parser.ElementParser;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public interface Items
{
    String NAMESPACE_URI = "";

    ElementParser ELEMENT_PARSER = new ElementParser();

    enum ATTR
    {
        NETWORK_REQUEST( "network-request" ),
        APPLICATION_ID( "application-id" ),
        COMMAND_CODE( "command-code" ),
        COMMAND_NAME( "command-name" ),
        END_TO_END_IDENTIFIER( "end-to-end-identifier" ),
        HOP_BY_HOP_IDENTIFIER( "hop-by-hop-identifier" ),
        SESSION_ID( "session-id" ),
        VERSION( "version" ),
        CODE( "code" ),
        VENDOR_ID( "vendor-id" ),
        VALUE( "value" ),
        RESULT_CODE( "result-code" ),
        ERROR( "error" ),
        REQUEST( "request" ),
        PROXIABLE( "proxiable" ),
        RE_TRANSMITTED( "re-transmitted" ),
        ACCT_APP_ID( "acct-app-id" ),
        AUTH_APP_ID( "auth-app-id" ),
        TYPE( "type" ),
        NAME( "name" ),
        DESCRIPTION( "description" ),
        DICTIONARY_URI( "dictionary-uri" ),
        DIAMETER_CONFIG_URI( "diameter-config-uri" ),
        TEMPLATE_URI( "template-uri" ),
        MODE( "mode" ),
        STACK_CREATION_TIMEOUT( "stack-creation-timeout" ),
        TIME_UNIT( "time-unit" ),
        DISCONNECT_CAUSE( "disconnect-cause" ),
        REALM( "realm" );

        private final String attr;


        ATTR( String attr )
        {
            this.attr = attr;
        }

        public boolean isAttribute( String name )
        {
            return attr.equals( name );
        }

        public boolean hasAttribute( Attributes atts )
        {
            return atts.getIndex( attr ) > - 1;
        }

        public String getAttribute( Attributes atts )
        {
            return atts.getValue( attr );
        }

        public String getAttribute()
        {
            return attr;
        }

        public void setAttribute( AttributesImpl atts, String namespaceUri, String value )
        {
            atts.addAttribute( namespaceUri, attr, attr, "", value == null ? "" : value );
        }

        public boolean hasAttribute( Element element )
        {
            return element.hasAttribute( attr );
        }

        public String getAttribute( Element element )
        {
            return hasAttribute( element ) ? element.getAttribute( attr ) : null;
        }

        public void setAttribute( Element element, String value )
        {
            element.setAttribute( getAttribute(), value );
        }
    }


    enum TAG
    {
        ANSWER( "answer" ),
        REQUEST( "request" ),
        APPLICATION_ID( "application-id" ),
        AVP( "avp" );

        private final String tag;

        TAG( String tag )
        {
            this.tag = tag;
        }

        public String getTag()
        {
            return tag;
        }

        public boolean isTag( String tagName )
        {
            return tag.equals( tagName );
        }
    }
}

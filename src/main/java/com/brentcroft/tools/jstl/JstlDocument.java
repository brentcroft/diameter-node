package com.brentcroft.tools.jstl;

import com.brentcroft.tools.jstl.tag.JstlElement;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.brentcroft.tools.jstl.MapBindings.jstl;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@Getter
@Setter
public class JstlDocument
{
    private final Map< String, Object > bindings = new MapBindings();
    private Document document;
    private ContentHandler contentHandler;

    public void renderEvents() throws SAXException
    {
        emitChildren( document.getChildNodes(), bindings );
    }

    public interface NodeListEmitter
    {
        void emitChildren( NodeList parent, Map< String, Object > bindings ) throws SAXException;
    }

    private static List< Node > getChildNodes( NodeList parent )
    {
        return isNull( parent )
               ? Collections.emptyList()
               : IntStream
                       .range( 0, parent.getLength() )
                       .mapToObj( parent::item )
                       .collect( Collectors.toList() );
    }

    public void emitChildren( NodeList parent, Map< String, Object > bindings ) throws SAXException
    {
        for ( Node node : getChildNodes( parent ) )
        {
            switch ( node.getNodeType() )
            {
                case Node.TEXT_NODE:
                    final Text text = ( Text ) node;
                    char[] chars = jstl().expandText( text.getWholeText(), bindings ).toCharArray();
                    contentHandler.characters( chars, 0, chars.length );
                    break;

                case Node.ELEMENT_NODE:
                    final Element element = ( Element ) node;

                    if ( element.getTagName().startsWith( "c:" ) )
                    {
                        Map< String, String > ai = new HashMap<>();

                        ofNullable( element.getAttributes() )
                                .ifPresent( attrs -> IntStream
                                        .range( 0, attrs.getLength() )
                                        .mapToObj( i -> ( Attr ) attrs.item( i ) )
                                        .forEach( attr -> ai.put( attr.getName(), attr.getValue() ) ) );

                        String tag = element.getTagName().substring( 2 );

                        final JstlTag jstlType = JstlTag.valueOf( tag.toUpperCase() );

                        JstlElement jstlElement = ( JstlElement ) element.getUserData( jstlType.name() );

                        if ( isNull( jstlElement ) )
                        {
                            jstlElement = jstlType.newJstlElement( null, ai );
                            jstlElement.normalize();
                            element.setUserData( jstlType.name(), jstlElement, null );
                        }

                        jstlElement.emitNodeEvents( element, bindings, ( l, b ) -> emitChildren( l, b ) );
                    }
                    else
                    {
                        AttributesImpl ai = new AttributesImpl();

                        ofNullable( element.getAttributes() )
                                .ifPresent( attrs -> IntStream
                                        .range( 0, attrs.getLength() )
                                        .mapToObj( i -> ( Attr ) attrs.item( i ) )
                                        .forEach( attr -> ai.addAttribute(
                                                attr.getNamespaceURI(),
                                                attr.getLocalName(),
                                                attr.getNodeName(),
                                                "",
                                                jstl().expandText( attr.getValue(), bindings ) ) ) );

                        contentHandler.startElement(
                                element.getNamespaceURI(),
                                element.getLocalName(),
                                element.getTagName(),
                                ai
                        );

                        emitChildren( element.getChildNodes(), bindings );

                        contentHandler.endElement(
                                element.getNamespaceURI(),
                                element.getLocalName(),
                                element.getTagName()
                        );
                    }

                    break;
            }
        }
    }
}

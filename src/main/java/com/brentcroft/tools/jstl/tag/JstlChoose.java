package com.brentcroft.tools.jstl.tag;

import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.JstlTag;
import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.Renderable;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

public class JstlChoose extends AbstractJstlElement
{
    private final static String TAG = "c:choose";

    public JstlChoose()
    {
        innerRenderable = new JstlTemplate( this );
    }

    @Override
    public void normalize()
    {
        final List< Renderable > elements = innerRenderable.getElements();

        if ( elements != null )
        {
            List< Renderable > elementsToRemove = null;

            for ( Renderable r : elements )
            {
                if ( r instanceof JstlWhen || r instanceof JstlOtherwise )
                {
                    continue;
                }

                if ( elementsToRemove == null )
                {
                    elementsToRemove = new ArrayList<>();
                }

                // remove from elements
                elementsToRemove.add( r );
            }

            if ( elementsToRemove != null )
            {
                elements.removeAll( elementsToRemove );
            }
        }
    }


    public String render( Map< String, Object > rootObjects )
    {
        if ( isDeferred() )
        {
            return toText();
        }

        final List< Renderable > elements = innerRenderable.getElements();

        if ( elements != null )
        {
            for ( Renderable r : elements )
            {
                if ( r instanceof JstlWhen && ( ( JstlWhen ) r ).test( rootObjects ) )
                {
                    return r.render( rootObjects );
                }
                else if ( r instanceof JstlOtherwise )
                {
                    return r.render( rootObjects );
                }
            }
        }

        return "";
    }

    public String toText()
    {
        return String.format( "<%s>%s</%s>", TAG, innerRenderable, TAG );
    }


    @Override
    public void emitNodeEvents( Element element, Map< String, Object > bindings, JstlDocument.NodeListEmitter emitter ) throws SAXException
    {
        if ( element.hasChildNodes() )
        {
            List< Element > items = ( List< Element > ) element.getUserData( "ITEMS" );

            if ( isNull( items ) )
            {
                NodeList nodes = element.getChildNodes();

                items = IntStream
                        .range( 0, nodes.getLength() )
                        .mapToObj( nodes::item )
                        .filter( node -> node.getNodeType() == Node.ELEMENT_NODE )
                        .map( node -> ( Element ) node )
                        .filter( e -> JstlWhen.TAG.equals( e.getTagName() ) || JstlOtherwise.TAG.equals( e.getTagName() ) )
                        .collect( Collectors.toList() );

                element.setUserData( "ITEMS", items, null );
            }

            for ( Element item : items )
            {
                switch ( item.getTagName() )
                {
                    case JstlWhen.TAG:
                    {
                        Map< String, String > ai = new HashMap<>();

                        ofNullable( item.getAttributes() )
                                .ifPresent( attrs -> IntStream
                                        .range( 0, attrs.getLength() )
                                        .mapToObj( i -> ( Attr ) attrs.item( i ) )
                                        .forEach( attr -> ai.put( attr.getName(), attr.getValue() ) ) );

                        final JstlTag jstlType = JstlTag.WHEN;

                        JstlWhen jstlWhen = ( JstlWhen ) item.getUserData( JstlTag.WHEN.name() );

                        if ( isNull( jstlWhen ) )
                        {
                            jstlWhen = ( JstlWhen ) JstlTag.WHEN.newJstlElement( null, ai );
                            jstlWhen.normalize();
                            item.setUserData( jstlType.name(), jstlWhen, null );
                        }

                        if ( jstlWhen.test( bindings ) )
                        {
                            jstlWhen.emitNodeEvents( item, bindings, emitter );

                            return;
                        }
                    }
                    break;

                    case JstlOtherwise.TAG:
                    {
                        final JstlTag jstlType = JstlTag.OTHERWISE;

                        JstlOtherwise jstlOtherwise = ( JstlOtherwise ) item.getUserData( jstlType.name() );

                        if ( isNull( jstlOtherwise ) )
                        {
                            jstlOtherwise = ( JstlOtherwise ) jstlType.newJstlElement( null, null );
                            jstlOtherwise.normalize();
                            item.setUserData( jstlType.name(), jstlOtherwise, null );
                        }

                        jstlOtherwise.emitNodeEvents( item, bindings, emitter );

                        return;
                    }
                }
            }
        }
    }
}
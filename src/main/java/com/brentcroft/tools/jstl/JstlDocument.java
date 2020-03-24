package com.brentcroft.tools.jstl;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.*;

import java.util.Map;

@Getter
@Setter
public class JstlDocument
{
    private Document document;
    private JstlTemplateManager jstl = new JstlTemplateManager();


    interface NodeVisitor
    {
        void visit( Node node );
    }

    private void visitNodes( Node node, NodeVisitor visitor )
    {
        NamedNodeMap attrs = node.getAttributes();

        if ( attrs != null )
        {
            for ( int i = 0, n = attrs.getLength(); i < n; i++ )
            {
                visitNodes( attrs.item( i ), visitor );
            }
        }

        for ( Node child = node.getFirstChild(); child != null; child = child.getNextSibling() )
        {
            visitNodes( child, visitor );
        }

        visitor.visit( node );
    }

    public void expandNodes( Node node, final Map< String, Object > bindings )
    {
        visitNodes( node, new NodeVisitor()
        {
            @Override
            public void visit( Node node )
            {
                if ( node.getNodeType() == Node.ATTRIBUTE_NODE )
                {
                    final Attr attr = ( Attr ) node;
                    attr.setValue( jstl.expandText( attr.getValue(), bindings ) );
                }
                else if ( node.getNodeType() == Node.TEXT_NODE )
                {
                    final Text text = ( Text ) node;
                    text.replaceWholeText( jstl.expandText( text.getWholeText(), bindings ) );
                }
            }
        } );
    }
}

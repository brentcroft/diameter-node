package com.brentcroft.tools.jstl.tag;


import com.brentcroft.tools.jstl.JstlDocument;
import com.brentcroft.tools.jstl.JstlTemplate;
import com.brentcroft.tools.jstl.Renderable;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Map;

public interface JstlElement extends Renderable
{
    /**
     * Every JstlElement may have an inner JstlTemplate (which actually does the
     * rendering).
     * <p/>
     * Note that for some types of element it may be null.
     *
     * @return the inner JstlTemplate
     */
    JstlTemplate getInnerJstlTemplate();


    /**
     * Introspect and tidy up (after being built) ready for action.
     * <p/>
     * This is called when the parser is closing an element, so after all its
     * content has been parsed.
     */
    default void normalize()
    {
    }


    /**
     * Reconstruct text that could be used to recreate this element.
     *
     * @return A string that could be parsed to reproduce this element.
     */
    String toText();


    /**
     * If true, then the element will be re-constructed as an undeferred element.
     *
     * @return true if the element should re-constructed as undeferred.
     */
    boolean isDeferred();

    void setDeferred( boolean deferred );

    void emitNodeEvents( Element element, Map< String, Object > bindings, JstlDocument.NodeListEmitter emitter ) throws SAXException;
}

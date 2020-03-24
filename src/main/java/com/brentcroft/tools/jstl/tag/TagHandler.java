package com.brentcroft.tools.jstl.tag;

import java.util.Map;

/**
 * Very minimal handler supports SAX type parsing model.
 *
 * @author ADobson
 */
public interface TagHandler
{
    /**
     * Called when an opening tag occurs during parsing.
     *
     * @param tag        the tag being opened
     * @param attributes a Map of attributes associated with the tag
     */
    void open( String tag, Map< String, String > attributes );

    /**
     * Called when a non-tag text sequence occurs.
     *
     * @param text a non-tag text sequence
     */
    void text( String text );

    /**
     * Called when an closing tag occurs during parsing.
     *
     * @param tag
     */
    void close( String tag );
}
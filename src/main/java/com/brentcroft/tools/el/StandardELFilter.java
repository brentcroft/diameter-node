package com.brentcroft.tools.el;

//import org.apache.commons.lang.StringEscapeUtils;

/**
 * Standard EL Filters use the facilities from
 * org.apache.commons.lang.StringEscapeUtils to provide escaping filters.
 * <p>
 * NB: UNESCAPE_ESCAPE filters always un-escape before escaping to preclude double
 * escaping.
 */
public enum StandardELFilter implements ELFilter
{
    ;

    @Override
    public Object filter( Object value )
    {
        return null;
    }
//    SQL_ESCAPE_FILTER()
//            {
//                public Object filter( Object value )
//                {
//                    // there is no unescapeSql!!!
//                    return isNull( value )
//                           ? null
//                           : StringEscapeUtils.escapeSql( value.toString() );
//                }
//            },
//
//    XML_ESCAPE_FILTER()
//            {
//                public Object filter( Object value )
//                {
//                    return isNull( value )
//                           ? null
//                           : StringEscapeUtils.escapeXml( value.toString() );
//                }
//            },

//    XML_UNESCAPE_ESCAPE_FILTER()
//            {
//                public Object filter( Object value )
//                {
//                    return isNull( value )
//                           ? null
//                           : StringEscapeUtils.escapeXml(
//                                   StringEscapeUtils.unescapeXml( value.toString() ) );
//                }
//            },
//
//    CSV_ESCAPE_FILTER()
//            {
//                public Object filter( Object value )
//                {
//                    return isNull( value )
//                           ? null
//                           : StringEscapeUtils.escapeCsv( value.toString() );
//                }
//            },
//
//    CSV_UNESCAPE_ESCAPE_FILTER()
//            {
//                public Object filter( Object value )
//                {
//                    return isNull( value )
//                           ? null
//                           : StringEscapeUtils.escapeCsv(
//                                   StringEscapeUtils.unescapeCsv( value.toString() ) );
//                }
//            },
//
//
//    HTML_ESCAPE_FILTER()
//            {
//                public Object filter( Object value )
//                {
//                    return isNull( value )
//                           ? null
//                           : StringEscapeUtils.escapeHtml( value.toString() );
//                }
//            },
//
//    HTML_UNESCAPE_ESCAPE_FILTER()
//            {
//                public Object filter( Object value )
//                {
//                    return isNull( value )
//                           ? null
//                           : StringEscapeUtils.escapeHtml(
//                                   StringEscapeUtils.unescapeHtml(
//                                           value.toString() ) );
//                }
//            },
}

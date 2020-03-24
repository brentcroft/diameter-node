package com.brentcroft.tools.jstl.tag;

public class TagMessages
{

    public static final String REQ_ATTR_MISSING = "Required attribute is missing: [%s]";

    public static final String REQ_ATTR_EMPTY = "Required attribute is empty: [%s]";

    public static final String OPT_ATTR_EMPTY = "Optional attribute is empty: [%s]";

    public static final String INCLUDE_CIRCULARITY = "Detected circularity including uri [%s]";

    public static final String ENGINE_NAME_NOT_FOUND = "No ScriptEngine found for name: [%s]";

    public static final String INCONSISTENT_FOREACH = "Inconsistent [foreach] attributes: either [items], or both [begin] and [end] must be present";

    public static final String PARSER_ERROR_UNEXPECTED_TEXT = "Parsing Error: Unexpected text inside [%s]";

    public static final String PARSER_ERROR_UNEXPECTED_ELEMENT = "Parsing Error: Unexpected element [%s] inside [%s]";

    public static final String PARSER_ERROR_EMPTY_STACK = "Parsing Error: Expected closing tag [%s] but stack is empty";

    public static final String PARSER_ERROR_SEQUENCE_ERROR = "Parsing Error: Expected closing tag [%s] but stack has: [%s]";

    public static final String PARSER_ERROR_SEQUENCE_ERROR2 = "Parsing Error: The current item [tag: %s] in the stack [%s] is not the root! [%s]";
}

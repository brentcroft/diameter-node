package com.brentcroft.tools.jstl;

import lombok.Getter;
import lombok.Setter;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

@Getter
@Setter
public class JstlInputSource extends InputSource
{
    private MapBindings bindings;

    private JstlTemplate jstlTemplate;

    private DefaultHandler defaultHandler;
}

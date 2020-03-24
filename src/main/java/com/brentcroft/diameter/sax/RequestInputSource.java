package com.brentcroft.diameter.sax;

import lombok.Getter;
import lombok.Setter;
import org.jdiameter.api.Request;
import org.xml.sax.InputSource;

@Getter
@Setter
public class RequestInputSource extends InputSource
{
    private Request request;
}

package com.brentcroft.tools.jstl;

import java.util.Map;

public interface Renderable
{
    String render( Map< String, Object > rootObjects );
}

package com.brentcroft.tools.el;

import javax.el.ELContext;
import java.util.Map;

public interface ELContextFactory
{
    ELContext getELContext( Map< ?, ? > rootObjects );

    ELContext getELConfigContext();
}

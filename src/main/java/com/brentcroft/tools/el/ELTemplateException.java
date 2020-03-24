package com.brentcroft.tools.el;

public class ELTemplateException extends RuntimeException
{
    private static final long serialVersionUID = - 1219191469571053279L;

    public ELTemplateException()
    {
    }

    public ELTemplateException( String msg )
    {
        super( msg );
    }

    public ELTemplateException( String msg, Throwable t )
    {
        super( msg, t );
    }

    public ELTemplateException( Throwable t )
    {
        super( t );
    }

}

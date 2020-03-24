package com.brentcroft.diameter.sax;

import lombok.Getter;
import lombok.Setter;
import org.xml.sax.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Getter
@Setter
public abstract class AbstractXMLReader implements XMLReader
{
    private String systemId;
    private ContentHandler contentHandler;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;
    private DTDHandler dTDHandler;

    public boolean getFeature( String name )
    {
        return false;
    }

    public void setFeature( String name, boolean value )
    {
    }

    public Object getProperty( String name )
    {
        return null;
    }

    public void setProperty( String name, Object value )
    {
    }

    public void parse( String uri ) throws SAXException
    {
        systemId = uri;

        try
        {
            parse( new InputSource( new URL( uri ).openStream() ) );

            return;
        }
        catch ( MalformedURLException e )
        {
            // ok - try file
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }

        try
        {
            parse( new InputSource( new FileInputStream( uri ) ) );
        }
        catch ( FileNotFoundException e )
        {
            throw new SAXException( e );
        }
        catch ( IOException e )
        {
            throw new SAXException( e );
        }
    }
}

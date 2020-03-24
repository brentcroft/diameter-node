package com.brentcroft.diameter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Stack;
import org.jdiameter.client.impl.DictionarySingleton;
import org.jdiameter.client.impl.StackImpl;
import org.jdiameter.client.impl.helpers.XMLConfiguration;
import org.jdiameter.client.impl.parser.MessageParser;

import java.io.IOException;

import static com.brentcroft.tools.el.ELTemplateManager.getLocalFileURL;

@Getter
@Setter
@Log4j2
public class SimpleClient extends StackImpl implements Stack
{
    public SimpleClient( Configuration config )
    {
        try
        {
            this.init( config );

            // Let it stabilize...
            Thread.sleep( 500 );
        }
        catch ( InterruptedException | IllegalDiameterStateException | InternalException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void main( String[] args )
    {
        String diameterConfig = "diameter/client.xml";
        String dictionaryUri = "diameter/dictionary.xml";
        String templateUri = "responses/basic-answer.jstl";

        SimpleProcessor processor = new SimpleProcessor();

        processor.setTemplateUri( templateUri );

        try
        {
            installDictionary( dictionaryUri );

            SimpleClient client = new SimpleClient(
                    new XMLConfiguration(
                            getLocalFileURL( SimpleClient.class, diameterConfig )
                                    .openStream() ) );

            client.start();

            client.sendMessage( new MessageParser().createEmptyMessage( 2, 4 ) );

            client.stop( 0 );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }


    public static void installDictionary( String dictionaryUri ) throws IOException
    {
        DictionarySingleton
                .getDictionary(
                        getLocalFileURL( SimpleClient.class, dictionaryUri )
                                .openStream() );
    }
}

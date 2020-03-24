package com.brentcroft.diameter.fixtures;

import com.brentcroft.diameter.SimpleProcessor;
import com.brentcroft.diameter.SimpleServer;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;

import java.io.IOException;

public class WhenStubState extends Stage< WhenStubState >
{
    @ExpectedScenarioState
    String diameterConfig;

    @ExpectedScenarioState
    String dictionaryUri;

    @ExpectedScenarioState
    String templateUri;

    @ExpectedScenarioState
    Request request;

    @ProvidedScenarioState
    String answerText;

    @ProvidedScenarioState
    Answer answer;

    @ExpectedScenarioState
    SimpleProcessor processor;


    public WhenStubState install_avp_dictionary() throws IOException
    {
        SimpleServer.installDictionary( dictionaryUri );

        return self();
    }


    public WhenStubState get_answer_text()
    {
        answerText = processor.getAnswer( request );

        return self();
    }

    public WhenStubState process_request()
    {
        answer = processor.processRequest( request );

        return self();
    }
}

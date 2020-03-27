package com.brentcroft.diameter.fixtures;

import com.brentcroft.diameter.JSTLProcessor;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jdiameter.api.Answer;
import org.jdiameter.api.Request;

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
    JSTLProcessor processor;



    public WhenStubState get_answer_text()
    {
        answerText = processor.getAnswerText( request );

        return self();
    }

    public WhenStubState process_request()
    {
        answer = processor.processRequest( request );

        return self();
    }
}

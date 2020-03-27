package com.brentcroft.diameter.fixtures;

import com.brentcroft.diameter.JSTLProcessor;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import org.jdiameter.api.Answer;
import org.jdiameter.api.AvpDataException;

import static org.junit.Assert.assertEquals;

public class ThenStubState extends Stage< ThenStubState >
{
    @ExpectedScenarioState
    String diameterConfig;

    @ExpectedScenarioState
    String dictionaryUri;

    @ExpectedScenarioState
    String templateUri;

    @ExpectedScenarioState
    JSTLProcessor processor;

    @ExpectedScenarioState
    Answer answer;

    @ExpectedScenarioState
    String answerText;

    public ThenStubState answer_result_code_is( int resultCode ) throws AvpDataException
    {
        assertEquals( resultCode, answer.getResultCode().getInteger32() );

        return self();
    }

    public ThenStubState answer_text_is( String text )
    {
        assertEquals(
                canonicaliseText( text ),
                canonicaliseText( answerText )
        );

        return self();
    }

    private String canonicaliseText( String text )
    {
        return text
                .replaceAll( "\r", "" )
                .replaceAll( "\"", "'" );
    }
}

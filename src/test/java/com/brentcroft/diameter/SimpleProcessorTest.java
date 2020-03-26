package com.brentcroft.diameter;

import com.brentcroft.diameter.fixtures.GivenStubState;
import com.brentcroft.diameter.fixtures.ThenStubState;
import com.brentcroft.diameter.fixtures.WhenStubState;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.AvpDataException;
import org.junit.Test;


public class SimpleProcessorTest extends ScenarioTest< GivenStubState, WhenStubState, ThenStubState >
{
    @Test
    public void gets_answer_text() throws AvpDataException
    {
        given()
                .simple_request( 272, 123456 )
                .with_application_ids(
                        ApplicationId.createByAuthAppId( 0, 0 ),
                        ApplicationId.createByAuthAppId( 12429, 27 )
                )
                .simple_processor( "answers/basic-answer.jstl" );

        when()
                .get_answer_text();

        then()
                .answer_text_is( "" +
                        "<answer>\n" +
                        "    <avp code='268' name='Result-Code' value='2001'/>\n" +
                        "</answer>" );

        when()
                .process_request();

        then()
                .answer_result_code_is( 2001 );

    }
}
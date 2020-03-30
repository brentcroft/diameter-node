package com.brentcroft.diameter.fixtures;

import com.brentcroft.diameter.JstlProcessor;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Request;
import org.jdiameter.client.impl.parser.MessageParser;

import static java.util.Arrays.asList;

public class GivenStubState extends Stage< GivenStubState >
{
    @ProvidedScenarioState
    String diameterConfig = "diameter/server-stack-config.xml";

    @ProvidedScenarioState
    String dictionaryUri = "dictionary.xml";

    @ProvidedScenarioState
    String templateUri = "answers/basic-answer.jstl";

    @ProvidedScenarioState
    Request request;

    @ProvidedScenarioState
    JstlProcessor processor;


    public GivenStubState avp_dictionary_path( String dictionaryUri )
    {
        this.dictionaryUri = dictionaryUri;
        return self();
    }


    public GivenStubState diameter_config_path( String diameterConfig )
    {
        this.diameterConfig = diameterConfig;
        return self();
    }


    public GivenStubState answer_template_path( String templateUri )
    {
        this.templateUri = templateUri;
        return self();
    }

    public GivenStubState simple_request( int commandCode, long headerAppId )
    {
        this.request = new MessageParser().createEmptyMessage( commandCode, headerAppId );
        return self();
    }

    public GivenStubState simple_processor( String templateUri )
    {
        this.processor = new JstlProcessor();
        this.processor.setTemplateUri( templateUri );
        return self();
    }


    public GivenStubState with_application_ids( ApplicationId... applicationIds )
    {
        request.getApplicationIdAvps().addAll( asList( applicationIds ) );

        return self();
    }
}

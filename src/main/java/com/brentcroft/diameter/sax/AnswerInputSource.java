package com.brentcroft.diameter.sax;

import lombok.Getter;
import lombok.Setter;
import org.jdiameter.api.Answer;
import org.xml.sax.InputSource;

@Getter
@Setter
public class AnswerInputSource extends InputSource
{
    private Answer answer;
}

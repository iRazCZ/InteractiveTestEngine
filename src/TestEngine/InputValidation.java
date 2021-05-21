package TestEngine;

import java.util.regex.*;

class InputValidation
{

    String expression;
    Pattern p;
    Matcher m;

    InputValidation()
    {
        this.expression="([^a-zA-Z\\s])";
    }

    void setExpression(String expression)
    {
        this.expression=expression;
    }

    boolean isValid(String text)
    {
        boolean b=false;
        p=Pattern.compile(this.expression);
        m=p.matcher(text);

        while(b=m.find())
        {
            return false;
        }

        return true;
    }
    
}

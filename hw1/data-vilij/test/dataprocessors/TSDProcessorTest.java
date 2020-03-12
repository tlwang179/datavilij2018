package dataprocessors;

import org.junit.Test;


import static org.junit.Assert.*;

public class TSDProcessorTest {
    @Test
    public void processString() throws Exception {
        TSDProcessorTestClass tsdProcessor=new TSDProcessorTestClass();
        //This test is to test if the name is valid. If the name has an error, the name will change to " ". No exception is needed to handle it
        String x="@adsa\tlabel\t2,2";
        tsdProcessor.processString(x);
        assertEquals("@adsa",tsdProcessor.name);
        x="adsa\tlabel\t2,2";
        tsdProcessor.processString(x);
        assertEquals(" ",tsdProcessor.name);

        //This test is to test if the data point is a number
        x="@adsa\tlabel\t2,2";
        tsdProcessor.processString(x);
        assertFalse(tsdProcessor.hadAnError);

        x="adsa\tlabel\tN,2";
        tsdProcessor.processString(x);
        assertTrue(tsdProcessor.hadAnError);
    }

}
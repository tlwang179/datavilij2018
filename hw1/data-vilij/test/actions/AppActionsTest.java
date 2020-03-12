package actions;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.Assert.*;


public class AppActionsTest {

    @Test (expected = FileNotFoundException.class)  //TO TEST A TRUE/CORRECT PATH, REMOVE THE (expected = FileNotFoundException.class)

    public void handleSaveRequest() throws FileNotFoundException {
        AppActionsTestClass appActionsTestClass = new AppActionsTestClass();
        String textarea = "testing2";

            File selected = new File("/skdfjskdf/kdsfjasl.tsd");//false/impossible path:no error
            appActionsTestClass.setPath(selected.toPath());
            appActionsTestClass.saveText(textarea);
            appActionsTestClass.handleSaveRequest();

            Scanner x = new Scanner(selected);
            String y = x.nextLine();

            assertTrue(appActionsTestClass.checkSame(textarea, y));

    }
}
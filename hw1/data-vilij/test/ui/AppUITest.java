package ui;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppUITest {

    @Test
    public void configDialog() {
        AppUITestClass appUI=new AppUITestClass();

        //valid test boundary. Although 0 is valid in the textfields, it will not enable the run button in the UI
        appUI.setData("0","0");
        assertEquals(0,appUI.getMax());
        assertEquals(0,appUI.getUp());

        appUI.setDataClust("0","0","0");
        assertEquals(0,appUI.getMax());
        assertEquals(0,appUI.getUp());
        assertEquals(0,appUI.getClust());

        //invalid max
        appUI.setData("N","1");
        assertEquals(0,appUI.getMax());
        assertEquals(1,appUI.getUp());

        //invalid update increment  & invalid run configuration for classification (run will not show)
        appUI.setData("1","N");
        assertEquals(0,appUI.getMax());
        assertEquals(0,appUI.getUp());

        //invalid cluster    &invalid run configuration for cluster algorithm (run will not show)
        appUI.setDataClust("1","1","N");
        assertEquals(0,appUI.getMax());
        assertEquals(0,appUI.getUp());
        assertEquals(0,appUI.getClust());

    }
}
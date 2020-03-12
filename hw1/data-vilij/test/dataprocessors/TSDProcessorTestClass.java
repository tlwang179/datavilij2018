package dataprocessors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TSDProcessorTestClass {
    boolean hadAnError=false;
    String name;

    public void processString(String tsdString) throws Exception {

        StringBuilder errorMessage = new StringBuilder();
        hadAnError=false;
        List<String> list=Arrays.asList(tsdString.split("\t"));
                    try {
                        name = checkedname(list.get(0));
                        if (name.equals(" "))
                            throw new Exception();
                        String label = list.get(1);
                        String[] pair = list.get(2).split(",");
                        Double x = Double.parseDouble(pair[0]);
                        Double y = Double.parseDouble(pair[1]);
                        if (x.isNaN() || y.isNaN()) {
                            throw new Exception();
                        }

                    } catch (Exception e) {
                        errorMessage.setLength(1);//this prevents saving/loading/displaying invalid data
                        hadAnError=true;
                        //}
                    }


    }

    private String checkedname(String name) {
        if (!name.startsWith("@")) {
            return " ";
        }
        return name;
    }
}

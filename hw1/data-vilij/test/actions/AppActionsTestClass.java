package actions;



import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;


public class AppActionsTestClass {
    String x;//text from textarea
    Path dataFilePath;
    public void setPath(Path x){
        dataFilePath=x;
    }
    public void saveText(String y){
        x=y;
    }
    public void handleSaveRequest() {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(x.toString());
            writer.close();
        } catch (Exception e) {

        }
    }
    public boolean checkSame(String x,String y){
        return x.equals(y);
    }
}
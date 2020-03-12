package dataprocessors;


import actions.AppActions;
import javafx.scene.text.Text;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;

//import java.io.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;

import java.nio.file.Path;
import java.util.Scanner;


/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    private int counter;
    private Path data=null;
    private boolean error=false;
    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    public boolean getError(){
        return error;
    }
    @Override
    public void loadData(Path dataFilePath) {
        error=false;
        data=dataFilePath;
        counter=0;
      //  PropertyManager manager = applicationTemplate.manager;
        File file=dataFilePath.toFile();
        StringBuilder string=new StringBuilder();
        StringBuilder update=new StringBuilder();
        try {
            Scanner scanner=new Scanner(file);
            while(scanner.hasNextLine() &&counter<10){
                counter++;                                      //max here can be 10
                String x=scanner.nextLine();
                string.append(x+"\n");
                update.append(x+"\n");
            }
            while(scanner.hasNextLine() &&counter>=10){
                counter++;
                String x=scanner.nextLine();
                update.append(x+"\n");
            }
            processor.processString(update.toString());         //process everything so that avg is correct
            if(((AppUI)applicationTemplate.getUIComponent()).getStartup()) {
                ((AppUI) applicationTemplate.getUIComponent()).insertText(string);
                ((AppActions)applicationTemplate.getActionComponent()).setIsUnsavedProperty(false);
               // if (!((AppUI) applicationTemplate.getUIComponent()).doneSelected)
                    displayData();
/*                if (counter > 10) {
                    ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    dialog.show(manager.getPropertyValue(AppPropertyTypes.NOTIFICATIONS_TITLE.name()), manager.getPropertyValue(AppPropertyTypes.NOTIFICATIONS_MESSAGE.name()) + counter + manager.getPropertyValue(AppPropertyTypes.NOTIFICATIONS_LINES.name()));
                    ((AppUI) applicationTemplate.getUIComponent()).Loaded(update.toString());
                }*/
                ((AppUI) applicationTemplate.getUIComponent()).disableSave(true);
            }
        }catch(Exception i){ //if processor detects wrong data
            error=true;
        }
        // TODO: NOT A PART OF HW 1
    }

    public void loadData(String dataString) {
        error=false;
        try {
            processor.processString(dataString);
            displayData();
            //if (((AppUI)applicationTemplate.getUIComponent()).doneSelected)
                //displayData();
        } catch (Exception e) {//delete this and change the other one so that it can show correct dialog.
            error=true;
            return;
        }
    }

    @Override
    public void saveData(Path dataFilePath) {

        try{
            processor.processString(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        }catch (Exception e){
            return;
        }
        try(PrintWriter writer=new PrintWriter(Files.newOutputStream(dataFilePath))){
            //processor.processString(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());

        } catch (Exception e) {//IO
            //System.err.println(e.getMessage());
            return;
        }
    }

    @Override
    public void clear() {
        processor.clear();
        data=null;
    }

    public void displayData() {
        (((AppUI) applicationTemplate.getUIComponent()).getChart()).getData().clear();
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
        ((AppUI)applicationTemplate.getUIComponent()).disableScreenShot(false);
        //make screenshot show

    }
    public boolean fromFile(){
        return data != null;
    }
    public Text getSource(){
        String fileName;
        if (fromFile()){
            fileName=data.getFileName().toString();
        } else fileName="TextArea";
        Text t=new Text();
        t.setText(" Number of instances: "+processor.getInstanceCount()+"\n Number of labels: "+processor.getLabelCount()+ "\n Labels: " +processor.getLabels() +"\n Source: "+fileName);
        return t;

    }
    public Path getPath(){
        return data;
    }
    public int getArrSize(){
        return processor.getLabelCount();
    }
}

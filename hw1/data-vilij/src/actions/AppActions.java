package actions;

import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import ui.AppUI;

import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;

import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;

//import java.io.*;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

import java.net.URL;
import java.nio.file.Path;

import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;


    /** Path to the data file currently active. */
    Path dataFilePath;

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;


    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    @Override
    public void handleNewRequest() {
        AppUI x=(AppUI)(applicationTemplate.getUIComponent());
        (applicationTemplate.getDataComponent()).clear();
        try {
            if(!(x).getStartup()) {
                x.setStartup(true);
            } else if (!isUnsaved.get() || promptToSave()) {
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                isUnsaved.set(false);           //because new file
                dataFilePath = null;            //because it saved previous file not new one
            }
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handleSaveRequest() {//disable save button, check if data is correct
        try {
            if (dataFilePath==null) { //if is not saved(no path), prompt to save
                AppUI appUI=(AppUI)(applicationTemplate.getUIComponent());
                AppData appData=(AppData)(applicationTemplate.getDataComponent());
                appData.loadData(appUI.getCurrentText());//might be unnecessary?
                promptToSave();
                isUnsaved.set(true);//saved
            }else {
                save();
            }

        }catch (IOException e){ errorHandlingHelper(); }
    }

    @Override
    public void handleLoadRequest() {//does not check if loaded data is correct &fix exception, put in appdata?
        PropertyManager manager  = applicationTemplate.manager;
        FileChooser filechooser=new FileChooser();
        String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),String.format("*.%s", extension));
        filechooser.getExtensionFilters().add(extFilter);
        File selected=filechooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        AppUI x=(AppUI)(applicationTemplate.getUIComponent());
        if (selected!=null){
            dataFilePath=selected.toPath(); //so it can be saved in the same file
            boolean start=x.getStartup();
            try {
                x.clear();
                if (!start) {//And no error in data
                    applicationTemplate.getDataComponent().loadData(dataFilePath);
                    if (!((AppData)(applicationTemplate.getDataComponent())).getError()) {
                        x.clear();
                        x.setStartup(true);
                    }
                }
                applicationTemplate.getDataComponent().loadData(dataFilePath);
                x.textAreaDisable();
                x.getDone().setSelected(true);
                x.displayWhenValid();

            }catch (Exception e){
                if(start!=x.getStartup())
                    x.setStartup(false);
                return;
            }
        }
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        try {
            if(((AppUI)applicationTemplate.getUIComponent()).getAlgRunning()){
                algorithmRunningDialog();
            }
            if (!isUnsaved.get() || promptToSave())
                System.exit(0);
            System.out.println(((AppUI)applicationTemplate.getUIComponent()).getAlgRunning());

        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser=new FileChooser();
        if(!((AppUI)applicationTemplate.getUIComponent()).getChart().getData().isEmpty()){
            WritableImage screenshot=((AppUI) applicationTemplate.getUIComponent()).getChart().snapshot(null,null);
                File file=fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (file!=null)
                    ImageIO.write(SwingFXUtils.fromFXImage(screenshot,null),manager.getPropertyValue(AppPropertyTypes.PNG.name()),file);
        }

    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false; // if user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
                String      dataDirPath = "/" + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                URL         dataDirURL  = getClass().getResource(dataDirPath);

                if (dataDirURL == null)
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),String.format("*.%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);

                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
        }

        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);

    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }
    private void algorithmRunningDialog(){
        Stage config = new Stage();
        config.setTitle("Warning: Algorithm is Running");
        VBox x = new VBox();
        x.setPadding(new Insets(20));
        Scene scene = new Scene(x, 400, 200);
        config.initModality(Modality.APPLICATION_MODAL);
        //config.initOwner(primaryStage);
        Text t=new Text("The algorithm is currently running. Would you like to close?");
        Button yes=new Button ("Yes");
        Button no=new Button("No");
        HBox h=new HBox();
        h.getChildren().addAll(yes,no);
        x.getChildren().addAll(t,h);
        yes.setOnAction(event -> {
            config.close();
            System.exit(0);
        });
        no.setOnAction(event -> {
            config.close();
        });

        config.setScene(scene);
        config.showAndWait();


    }

}

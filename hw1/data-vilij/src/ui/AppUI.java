package ui;

import actions.AppActions;
import algorithms.Clusterer;
import classification.RandomClassifier;
import cluster.KMeansClusterer;
import cluster.RandomClusterer;
import data.DataSet;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    private ApplicationTemplate applicationTemplate;
    private static final String CSS_RESOURCE_PATH = "/cssSheet.css";

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton; // toolbar button to take a screenshot of the data
    private Button displayButton;  // workspace button to display data on the chart
    private TextArea textArea;       // text area for new data input
    private boolean hasNewText;     // whether or not the text area has any new data since last display
    private CheckBox checkBox;
    private LineChart<Number, Number> chart;
    private Boolean Startup = false;
    private RadioButton done;    //use get methods for this
    private RadioButton edit;
    private HBox disp;
    private VBox algorithms;
    private RadioButton classi;    //use get methods for this
    private RadioButton clust;
    // private boolean doneSelected;
    private VBox algChoices;
    private ToggleButton gear;
    private ToggleButton gear1;
    private Button run;
    private RadioButton clusterOp;
    private RadioButton classificationOp;
    private RadioButton continuous;

    private HashMap<RadioButton, Integer> max;
    private HashMap<RadioButton, Integer> up;
    private HashMap<RadioButton, Boolean> Cont;
    private HashMap<RadioButton, Integer> clustNum;
    private int maxIt = 0;
    private int upInt = 0;
    private int clustN = 0;
    private RadioButton chosen;
    private TextField maxNumText;
    private TextField updateIntervalText;
    private TextField clusterText;
    private int count = 0;
    private boolean algRunning;
    private HBox hBox;
    private Thread t;
    private ToggleButton gear2;
    private RadioButton clusterOp1;
    private RadioButton algCategory;


    public RadioButton getDone() {
        return done;
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(SEPARATOR,
                iconsPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                true);
        toolBar.getItems().add(scrnshotButton);
        scrnshotButton.setOnAction(event -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e) {
                e.getMessage();
            }
        });
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e ->
                applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> {
            saveButton.setDisable(true);
            applicationTemplate.getActionComponent().handleSaveRequest();
        });
        loadButton.setOnAction(e -> {
            disp.getChildren().clear();
            applicationTemplate.getActionComponent().handleLoadRequest();
            clust.setSelected(false);
            classi.setSelected(false);

        });

        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        disp.setVisible(false);
        algorithms.setVisible(false);
        algChoices.setVisible(false);
        textArea.setDisable(false);
        textArea.clear();                   //needs to clear not just the textarea but the rest of the string as well
        textArea.clear();
        chart.getData().clear();
        edit.setSelected(true);
        checkBox.setSelected(false);
    }

    public String getCurrentText() {
        return textArea.getText();
    }

    public boolean getStartup() {
        return Startup;
    }

    public void setStartup(boolean x) {
        Startup = x;
        if (x) {
            appPane.getChildren().remove(workspace);
            initialize();
        }
    }

    private void layout() {
        primaryScene.getStylesheets().add(getClass().getResource(CSS_RESOURCE_PATH).toExternalForm());

        PropertyManager manager = applicationTemplate.manager;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));

        chart.verticalGridLinesVisibleProperty().setValue(false);
        chart.horizontalGridLinesVisibleProperty().setValue(false);
        chart.verticalZeroLineVisibleProperty().setValue(false);
        chart.horizontalZeroLineVisibleProperty().set(false);

        VBox leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.3);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3);

        Text leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));
        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setMinHeight(150);

        //edit/done toggle buttons
        edit = new RadioButton("edit");
        done = new RadioButton("done");
        ToggleGroup group = new ToggleGroup();
        edit.setToggleGroup(group);
        done.setToggleGroup(group);
        HBox buttons = new HBox(edit, done);
        edit.setSelected(true);

        disp = new HBox();
        disp.setMaxWidth(windowWidth * .29);

        clust = new RadioButton("Clustering");
        classi = new RadioButton("Classification");
        ToggleGroup AlgGroup = new ToggleGroup();
        clust.setToggleGroup(AlgGroup);
        classi.setToggleGroup(AlgGroup);

        algorithms = new VBox(clust, classi);
        algorithms.setMaxWidth(windowWidth * .29);
        algorithms.setVisible(false);

        algChoices = new VBox();
        gear = new ToggleButton("settings");
        gear1 = new ToggleButton("settings");
        gear2 = new ToggleButton("settings");
        run = new Button("run");
        run.setDisable(true);
        clusterOp = new RadioButton("KMeans Clusterer");
        clusterOp1 = new RadioButton("Random Clusterer");
        classificationOp = new RadioButton("Random Classifier");

        max = new HashMap<>();
        up = new HashMap<>();
        Cont = new HashMap<>();
        clustNum = new HashMap<>();

        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.getChildren().add(displayButton);
        checkBox = new CheckBox();

        Text text = new Text("disable");
        processButtonsBox.getChildren().add(text);
        processButtonsBox.getChildren().add(checkBox);
        VBox rightPanel;
        if (!Startup) {
            newButton.setDisable(false);
            Text rightPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
            String Fontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
            Double Fontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
            rightPanelTitle.setFont(Font.font(Fontname, Fontsize));
            rightPanel = new VBox(rightPanelTitle);
            rightPanel.setAlignment(Pos.TOP_CENTER);
        } else {
            leftPanel.getChildren().addAll(leftPanelTitle, textArea, processButtonsBox, buttons, disp, algorithms, algChoices);
            rightPanel = new VBox(chart);
            rightPanel.setAlignment(Pos.TOP_CENTER);
        }
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);


        Separator sep = new Separator();
        sep.setOrientation(Orientation.VERTICAL);
        VBox mid = new VBox(sep);
        sep.setPrefHeight(windowHeight);

        workspace = new HBox(leftPanel, mid, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setCheckBoxActions();
        setToggleActions();
        setAlgActions();
        setGearAction();
        setIndivActions();
        setRunActions();


    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n') {
                            hasNewText = true;
                            newButton.setDisable(false);
                            disableSave(false);
                        }
                    } else {
                        hasNewText = true;
                        newButton.setDisable(true);
                        disableSave(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }

    private void setCheckBoxActions() {
        checkBox.setOnAction(event -> {
            if (checkBox.isSelected()) {
                textArea.setDisable(true);
            } else {
                textArea.setDisable(false);
            }
        });
    }

    public void textAreaDisable() {
        textArea.setDisable(true);
        checkBox.setSelected(true);
        done.setSelected(true);
        clearStorage();
    }

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            if (hasNewText) {
                if (textArea.getText().isEmpty()) {
                    chart.getData().clear();
                    scrnshotButton.setDisable(true);
                } else {
                    try {
                        chart.getData().clear();
                        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                        dataComponent.clear();
                        dataComponent.loadData(textArea.getText());

                        //if there is no error do these
                        dataComponent.displayData();
                        if (chart.getData().isEmpty())
                            scrnshotButton.setDisable(true);
                        else disableScreenShot(false);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        return;
                    }
                }
            }
        });
    }

    public void disableScreenShot(boolean x) {
        scrnshotButton.setDisable(x);
    }

    public void disableSave(boolean x) {
        saveButton.setDisable(x);
    }

    public void insertText(StringBuilder s) {
        textArea.appendText(s.toString());
    }

/*    public void Loaded(String x) {
        AtomicInteger maxLineShown = new AtomicInteger(10);
        List<String> list = new ArrayList<>(Arrays.asList(x.split("\n")));
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
                    int k = textArea.getText().split("\n").length;
                    if (newValue.compareTo(oldValue) != 0) {
                        if (k < 10 && maxLineShown.get() < list.size()) {
                            String a = list.get(maxLineShown.get());
                            maxLineShown.addAndGet(1);
                            textArea.appendText(a + "\n");
                        }
                    }
                }
        );
    }*/

    public void displayWhenValid() {
        Text stats = ((AppData) applicationTemplate.getDataComponent()).getSource();
        disp.getChildren().add(stats);
        disp.setVisible(true);
        algorithms.setVisible(true);
        algChoices.setVisible(false);
        if (((AppData) applicationTemplate.getDataComponent()).getArrSize() != 2)
            classi.setDisable(true);
    }

    private void setToggleActions() {
        done.setOnAction(event -> {

                    clust.setSelected(false);
                    classi.setSelected(false);
                    chart.getData().clear();
                    AppData x = ((AppData) applicationTemplate.getDataComponent());
                    if (x.getPath() != null)
                        x.loadData(x.getPath());
                    else x.loadData(getCurrentText());
                    Text y = x.getSource();
                    if (!x.getError()) {
                        disp.getChildren().clear();
                        disp.getChildren().add(y);
                        textAreaDisable();
                        DoneChosen(true);
                        if (((AppData) applicationTemplate.getDataComponent()).getArrSize() != 2)
                            classi.setDisable(true);
                    } else edit.setSelected(true);
                }
        );
        edit.setOnAction(event -> {
            DoneChosen(false);
            clearStorage();
        });
    }

    private void DoneChosen(boolean done) {
        disp.setVisible(done);
        //doneSelected = done;
        algorithms.setVisible(done);
        textArea.setDisable(done);
        checkBox.setSelected(done);
        algChoices.setVisible(false);
        clust.setSelected(false);
        classi.setSelected(false);
        clusterOp.setSelected(false);
        clusterOp1.setSelected(false);
        classificationOp.setSelected(false);
    }

    private void setAlgActions() { //problem with multiple loads
        PropertyManager manager = applicationTemplate.manager;
        VBox x = new VBox();
        clust.setOnAction(event -> {
            algCategory = clust;
            ToggleGroup tg = new ToggleGroup();
            clusterOp.setToggleGroup(tg);
            clusterOp1.setToggleGroup(tg);
            algChoices.setVisible(true);
            clusterOp.setSelected(false);
            clusterOp1.setSelected(false);
            run.setDisable(true);
            gear.setDisable(true);
            gear2.setDisable(true);
            x.getChildren().clear();
            HBox y = new HBox(clusterOp, gear);
            HBox z = new HBox(clusterOp1, gear2);
            x.getChildren().add(y);
            x.getChildren().add(z);
            algChoices.getChildren().clear();
            algChoices.getChildren().add(x);
            hBox = new HBox();
            Text t = new Text("complete");
            hBox.getChildren().add(t);
            hBox.setVisible(false);
            algChoices.getChildren().addAll(run, hBox);

            try {
                //Class.forName("cluster.KMeansClusterer");
                Class.forName(manager.getPropertyValue(AppPropertyTypes.KMEANSCLUSTERER.name()));
            } catch (Exception e) {
                y.getChildren().clear();
            }
            try {
                //Class.forName("cluster.RandomClusterer");
                Class.forName(manager.getPropertyValue(AppPropertyTypes.RANDOMCLUSTERER.name()));
            } catch (Exception e) {
                z.getChildren().clear();
            }
        });
        classi.setOnAction(event -> {
            algCategory = classi;
            algChoices.setVisible(true);
            classificationOp.setSelected(false);
            run.setDisable(true);
            gear1.setDisable(true);
            x.getChildren().clear();
            HBox y = new HBox(classificationOp, gear1);
            x.getChildren().add(y);
            algChoices.getChildren().clear();
            algChoices.getChildren().add(x);
            hBox = new HBox();
            Text t = new Text("complete");
            hBox.getChildren().add(t);
            hBox.setVisible(false);
            algChoices.getChildren().addAll(run, hBox);


        });
    }

    private void setGearAction() {
        gear.setOnAction(event -> {
            configDialog();
            setCompleteVisable(false);
            if (max.containsKey(chosen) && max.get(chosen) >= 1 && up.get(chosen) >= 1 && clustNum.get(chosen) >= 1) {
                run.setDisable(false);
            } else run.setDisable(true);
        });
        gear1.setOnAction(event -> {
            configDialog();
            setCompleteVisable(false);
            if (max.containsKey(chosen) && max.get(chosen) >= 1 && up.get(chosen) >= 1) {
                run.setDisable(false);
            } else run.setDisable(true);
        });
        gear2.setOnAction(event -> {
            configDialog();
            setCompleteVisable(false);
            if (max.containsKey(chosen) && max.get(chosen) >= 1 && up.get(chosen) >= 1 && clustNum.get(chosen) >= 1) {
                run.setDisable(false);
            } else run.setDisable(true);
        });

    }

    private void setIndivActions() {
        clusterOp.setOnAction(event -> {
            setChosen(clusterOp);
            if (clusterOp.isSelected()) {
                gear.setDisable(false);
                gear2.setDisable(true);
                if (max.containsKey(chosen) && max.get(chosen) > 0 && up.get(chosen) > 0 && clustNum.get(chosen) > 0) {
                    run.setDisable(false);
                } else run.setDisable(true);
            } else {
                gear.setDisable(true);
                run.setDisable(true);
            }
        });
        clusterOp1.setOnAction(event -> {
            setChosen(clusterOp1);
            if (clusterOp1.isSelected()) {
                gear2.setDisable(false);
                gear.setDisable(true);
                if (max.containsKey(chosen) && max.get(chosen) > 0 && up.get(chosen) > 0 && clustNum.get(chosen) > 0) {
                    run.setDisable(false);
                } else run.setDisable(true);
            } else {
                gear2.setDisable(true);
                run.setDisable(true);
            }
        });


        classificationOp.setOnAction(event -> {
            setChosen(classificationOp);
            run.setDisable(true);
            if (classificationOp.isSelected()) {
                gear1.setDisable(false);
                if (max.containsKey(chosen) && max.get(chosen) > 0 && up.get(chosen) > 0) {
                    run.setDisable(false);
                } else run.setDisable(true);
            } else {
                gear1.setDisable(true);
                run.setDisable(true);
            }
        });
    }

    private void configDialog() {

        Stage config = new Stage();
        config.setTitle("Running Configuration");
        VBox x = new VBox();
        x.setPadding(new Insets(20));
        Scene scene = new Scene(x, 300, 200);
        config.initModality(Modality.APPLICATION_MODAL);
        config.initOwner(primaryStage);

        maxNumText = new TextField();
        Text maxNum = new Text("Max number of iterations: ");
        HBox top = new HBox(maxNum, maxNumText);

        updateIntervalText = new TextField();
        Text updateInterval = new Text("Update Interval ");
        HBox middle = new HBox(updateInterval, updateIntervalText);

        continuous = new RadioButton("Continuous");
        Button complete = new Button("return");

        HBox addition = new HBox();
        clusterText = new TextField();
        Text cluster = new Text("Cluster Number: ");
        if (clust.isSelected())
            addition.getChildren().addAll(cluster, clusterText);

        x.getChildren().addAll(top, middle, continuous, addition, complete);
        if (max.containsKey(clusterOp) && chosen.equals(clusterOp)) {
            maxNumText.insertText(0, max.get(clusterOp).toString());
            updateIntervalText.insertText(0, up.get(clusterOp).toString());
            continuous.setSelected(Cont.get(clusterOp));
            clusterText.insertText(0, clustNum.get(clusterOp).toString());
        }
        if (max.containsKey(classificationOp) && chosen.equals(classificationOp)) {
            maxNumText.insertText(0, max.get(classificationOp).toString());
            updateIntervalText.insertText(0, up.get(classificationOp).toString());
            continuous.setSelected(Cont.get(classificationOp));
        }
        if (max.containsKey(clusterOp1) && chosen.equals(clusterOp1)) {
            maxNumText.insertText(0, max.get(clusterOp1).toString());
            updateIntervalText.insertText(0, up.get(clusterOp1).toString());
            continuous.setSelected(Cont.get(clusterOp1));
            clusterText.insertText(0, clustNum.get(clusterOp1).toString());
        }

        complete.setOnAction(event -> {
            config.close();
            count = 0;
            try {
                storeMax(Integer.parseInt(maxNumText.getText()));
            } catch (Exception e) {
                setZero(chosen);
                int temp = maxNumText.getCharacters().length() - 1;
                maxNumText.deleteText(0, temp + 1);
                maxNumText.insertText(0, "0");
            }
            try {
                storeUpInt(Integer.parseInt(updateIntervalText.getText()));

            } catch (Exception e) {
                setZero(chosen);
                int temp = updateIntervalText.getCharacters().length() - 1;
                updateIntervalText.deleteText(0, temp + 1);
                updateIntervalText.insertText(0, "0");
            }

            if (chosen.equals(clusterOp) || chosen.equals(clusterOp1)) {
                try {
                    storeClustN(Integer.parseInt(clusterText.getText()));
                } catch (Exception e) {
                    setZero(chosen);
                    int temp = clusterText.getCharacters().length() - 1;
                    clusterText.deleteText(0, temp + 1);
                    clusterText.insertText(0, "0");
                }
            }
            Cont.put(chosen, continuous.isSelected());

        });

        config.setScene(scene);
        config.showAndWait();

        max.put(chosen, maxIt);
        up.put(chosen, upInt);
        Cont.put(chosen, continuous.isSelected());
        if (chosen.equals(clusterOp) || chosen.equals(clusterOp1))
            clustNum.put(chosen, clustN);
    }

    private void storeMax(int x) {
        maxIt = x;
    }

    private void storeUpInt(int x) {
        upInt = x;
    }

    private void storeClustN(int x) {
        clustN = x;
    }

    private void setChosen(RadioButton x) {
        chosen = x;
    }

    private void setZero(RadioButton chosen) {
        storeMax(0);
        storeUpInt(0);
        Cont.put(chosen, false);
        if (chosen.equals(clusterOp) || chosen.equals(clusterOp1))
            storeClustN(0);

    }

    private void setRunActions() {
        run.setOnAction(event -> {
            setCompleteVisable(false);
            disableRun(true);
            chart.getData().clear();

            try {
                DataSet d;
                if (((AppData) applicationTemplate.getDataComponent()).fromFile()) {
                    d = DataSet.fromTSDFile(((AppData) applicationTemplate.getDataComponent()).getPath());
                } else {
                    d = DataSet.fromTextArea(getCurrentText());
                }

                if (algCategory == classi) {
                    if (((AppData) applicationTemplate.getDataComponent()).fromFile()) {
                        (applicationTemplate.getDataComponent()).loadData(((AppData) applicationTemplate.getDataComponent()).getPath());
                    } else
                        ((AppData) applicationTemplate.getDataComponent()).loadData(getCurrentText());
                    disableScreenShot(true);
                    try {
                        if (!Cont.get(chosen)) {
                            disableRun(true);
                            count = count + up.get(chosen);
                        }
                        Class randomClassifer = Class.forName(PropertyManager.getManager().getPropertyValue(AppPropertyTypes.RANDOMCLASSI.name()));
                        Constructor<RandomClassifier> randomClassifierConstructor = (Constructor<RandomClassifier>) randomClassifer.getConstructor(DataSet.class, int.class, int.class, boolean.class, LineChart.class, ApplicationTemplate.class);
                        Object o = randomClassifierConstructor.newInstance(d, max.get(chosen), up.get(chosen), Cont.get(chosen), getChart(), applicationTemplate);
                        t = new Thread((Runnable) o);
                        setAlgRunning(true);
                        t.start();
                    } catch (Exception e) {
                        return;
                    }
                } else {
                    // if (chosen.equals(clusterOp)) {
                    disableScreenShot(true);
                    if (!Cont.get(chosen) && count + up.get(chosen) <= max.get(chosen))
                        count = count + up.get(chosen);
                    if (chosen.equals(clusterOp)) {
                        try {
                            Class kMeansClusterer = Class.forName(PropertyManager.getManager().getPropertyValue(AppPropertyTypes.KMEANSCLUSTERER.name()));
                            Constructor<KMeansClusterer> kMeansClustererConstructor = (Constructor<KMeansClusterer>) kMeansClusterer.getConstructor(DataSet.class, int.class, int.class, boolean.class, int.class, ApplicationTemplate.class);
                            Object o = kMeansClustererConstructor.newInstance(d, max.get(chosen), up.get(chosen), Cont.get(chosen), clustNum.get(chosen), applicationTemplate);
                            t = new Thread((Runnable) o);
                            setAlgRunning(true);
                            t.start();

                        } catch (Exception e) {
                            return;
                        }
                    } else if (chosen.equals(clusterOp1)) {
                        try {
                            Class RandomClusterer = Class.forName(PropertyManager.getManager().getPropertyValue(AppPropertyTypes.RANDOMCLUSTERER.name()));
                            Constructor<RandomClusterer> randomClustererConstructor = (Constructor<RandomClusterer>) RandomClusterer.getConstructor(DataSet.class, int.class, int.class, boolean.class,int.class, ApplicationTemplate.class);
                            Object o = randomClustererConstructor.newInstance(d, max.get(chosen), up.get(chosen), Cont.get(chosen), clustNum.get(chosen), applicationTemplate);
                            t = new Thread((Runnable) o);
                            setAlgRunning(true);
                            t.start();
                        } catch (Exception e) {
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                return;
            }
        });

    }

    public void disableRun(boolean x) {
        run.setDisable(x);
    }

    public int getCounter() {
        return count;
    }

    public void setAlgRunning(boolean x) {
        algRunning = x;
    }

    public boolean getAlgRunning() {
        return algRunning;
    }

    public void setCompleteVisable(boolean x) {
        hBox.setVisible(x);
        if (x) {
            count = 0;
        }
    }

    public boolean compareCounterUpdate() {
        return (count == max.get(chosen) || count + up.get(chosen) > max.get(chosen));
    }

    private void clearStorage() {
        max.clear();
        up.clear();
        Cont.clear();
        clustNum.clear();
    }

}

package dataprocessors;


import com.sun.prism.Graphics;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;

//import java.util.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    private Map<String, String> dataLabels;
    private Map<String, Point2D> dataPoints;
    private Map<Point2D, String> pointName;
    private double ySum;
    private double xMin;
    private double xMax;
    private String dupeName;
    private ArrayList<String> names = new ArrayList<>();
    public ArrayList<String> LabelName;
    ApplicationTemplate applicationTemplate;


    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        pointName = new HashMap<>();
        applicationTemplate = new ApplicationTemplate();

    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        PropertyManager manager = applicationTemplate.manager;
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        applicationTemplate = new ApplicationTemplate();
        LabelName = new ArrayList<>();

        names.clear();
        ySum = 0;
        dupeName = "";
        hadAnError.set(false);
        Stream.of(tsdString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    if (hadAnError.get()) {
                        return;//this prevents you from continuing this stream if error is found
                    }
                    try {
                        String name = checkedname(list.get(0));
                        if (name.equals(" ")) {
                            names.add(name);
                            throw new Exception();
                        }
                        dupeName = checkDupe(name);
                        if (!dupeName.equals(""))
                            throw new Exception();
                        names.add(name);

                        String label = list.get(1);

                        String[] pair = list.get(2).split(",");
                        Double x = Double.parseDouble(pair[0]);
                        Double y = Double.parseDouble(pair[1]);
                        if (x.isNaN() || y.isNaN()) {
                            throw new Exception();
                        }
                        Point2D point = new Point2D(x, y);
                        if (!LabelName.contains(label) && !label.matches("null"))
                            LabelName.add(label);
                        dataLabels.put(name, label);
                        dataPoints.put(name, point);
                        pointName.put(point, name);


                        if (names.size() == 1) {
                            xMin = Double.parseDouble(pair[0]);
                            xMax = Double.parseDouble(pair[0]);
                        } else {
                            if (xMin > Double.parseDouble(pair[0]))
                                xMin = Double.parseDouble(pair[0]);
                            else if (xMax < Double.parseDouble(pair[0]))
                                xMax = Double.parseDouble(pair[0]);
                        }
                        ySum = ySum + Double.parseDouble(pair[1]);


                    } catch (Exception e) {
                        ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                        String ErrTitle = manager.getPropertyValue((AppPropertyTypes.ERROR_TITLE.name()));
                        //if(!((AppUI)(applicationTemplate.getUIComponent())).getStartup()) {
                        if (!dupeName.equals(""))
                            dialog.show(ErrTitle, manager.getPropertyValue(AppPropertyTypes.ERROR_NAME.name()) + dupeName + manager.getPropertyValue(AppPropertyTypes.ERROR_NAME2.name()) + (names.size() + 1));
                        else if (names.contains(" "))
                            dialog.show(ErrTitle, manager.getPropertyValue(AppPropertyTypes.ERROR_AT.name()) + names.size());
                        else {
                            dialog.show(ErrTitle, manager.getPropertyValue(AppPropertyTypes.ERROR_COORDINATES.name()) + (names.size()));
                        }
                        errorMessage.setLength(1);//this prevents saving/loading/displaying invalid data
                        hadAnError.set(true);
                        names.clear();
                        //}
                    }
                });
        if (errorMessage.length() > 0)
            throw new Exception();
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        PropertyManager manager = applicationTemplate.manager;

        if (names.isEmpty()) return;
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));

            });
            chart.getData().add(series);
        }
        for (XYChart.Series<Number, Number> s : chart.getData()) {
            for (XYChart.Data<Number, Number> d : s.getData()) {
                Point2D point = new Point2D((double) d.getXValue(), (double) d.getYValue());
                Tooltip toolTip = new Tooltip(pointName.get(point));
                Tooltip.install(d.getNode(), toolTip);
                d.getNode().setOnMouseEntered(event -> d.getNode());
                d.getNode().setOnMouseExited(event -> d.getNode());
                d.getNode().setOnMouseEntered(event -> chart.setCursor(Cursor.HAND));
                d.getNode().setOnMouseExited(event -> chart.setCursor(Cursor.DEFAULT));
            }

        }
/*        double average = ySum / names.size();
        Point2D point1;
        Point2D point2;
        if (xMin == xMax) {
            point1 = new Point2D(xMin - 1, average);
            point2 = new Point2D(xMax + 1, average);
        } else {
            point1 = new Point2D(xMin, average);
            point2 = new Point2D(xMax, average);
        }

        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.setName(manager.getPropertyValue(AppPropertyTypes.AVERAGE.name()));
        series1.getData().add(new XYChart.Data<>(point1.getX(), point1.getY()));
        series1.getData().add(new XYChart.Data<>(point2.getX(), point2.getY()));
        chart.getData().add(series1);
        series1.nodeProperty().get().setStyle(manager.getPropertyValue(AppPropertyTypes.AVERAGE_LINE.name()));
        Node node1 = series1.getData().get(0).getNode();
        Node node2 = series1.getData().get(1).getNode();
        node1.setStyle(manager.getPropertyValue(AppPropertyTypes.AVERAGE_REMOVE_NODE.name()));
        node2.setStyle(manager.getPropertyValue(AppPropertyTypes.AVERAGE_REMOVE_NODE.name()));*/
    }

    private String checkDupe(String x) {
        for (int i = 0; i < names.size(); i++) {
            if (x.compareTo(names.get(i)) == 0)
                return x;
        }
        return "";
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) {
        //System.out.println(name);
        if (!name.startsWith("@")) {
            return " ";
        }
        return name;
    }

    public int getLabelCount() {
        return LabelName.toArray().length;
    }

    public String getLabels() {
        StringBuilder x=new StringBuilder();
        for(int i=0;i<LabelName.toArray().length;i++) {
            x.append(LabelName.get(i)+"\n");
        }

        return x.toString();

    }

    public int getInstanceCount() {
        return names.size();
    }
}


package data;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class specifies how an algorithm will expect the dataset to be. It is
 * provided as a rudimentary structure only, and does not include many of the
 * sanity checks and other requirements of the use cases. As such, you can
 * completely write your own class to represent a set of data instances as long
 * as the algorithm can read from and write into two {@link java.util.Map}
 * objects representing the name-to-label map and the name-to-location (i.e.,
 * the x,y values) map. These two are the {@link DataSet#labels} and
 * {@link DataSet#locations} maps in this class.
 *
 * @author Ritwik Banerjee
 */
public class DataSet {
    private XYChart.Series<Number, Number> series2;
    private double xMin;
    private double xMax;
  //  private double yMin;
  //  private double yMax;
    private boolean first=false;

    private static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        private InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private static String nameFormatCheck(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }

    private static Point2D locationOf(String locationString) {
        String[] coordinateStrings = locationString.trim().split(",");
        return new Point2D(Double.parseDouble(coordinateStrings[0]), Double.parseDouble(coordinateStrings[1]));
    }

    private Map<String, String>  labels;
    private Map<String, Point2D> locations;

    /** Creates an empty dataset. */
    private DataSet() {
        labels = new HashMap<>();
        locations = new HashMap<>();
        series2= new XYChart.Series<>();
        series2.setName("series2");
    }

    public Map<String, String> getLabels()     { return labels; }

    public Map<String, Point2D> getLocations() { return locations; }

    public void updateLabel(String instanceName, String newlabel) {
        if (labels.get(instanceName) == null)
            throw new NoSuchElementException();
        labels.put(instanceName, newlabel);
    }

    private void addInstance(String tsdLine) throws InvalidDataNameException {
        String[] arr = tsdLine.split("\t");
        labels.put(nameFormatCheck(arr[0]), arr[1]);
        locations.put(arr[0], locationOf(arr[2]));//coordinates
        if(locations.get(arr[0]).getX()<xMin||!first)
            xMin=locations.get(arr[0]).getX();
        if(locations.get(arr[0]).getX()>xMax||!first)
            xMax=locations.get(arr[0]).getX();
       /* if(locations.get(arr[0]).getY()<yMin||!first)
            yMin=locations.get(arr[0]).getY();
        if(locations.get(arr[0]).getY()>yMax||!first)
            yMax=locations.get(arr[0]).getY();*/
        first=true;
    }
    /*public double getYMin(){
        return yMin;
    }
    public double getYMax(){
        return yMax;
    }*/

    public static DataSet fromTSDFile(Path tsdFilePath) throws IOException {
        DataSet dataset = new DataSet();
        Files.lines(tsdFilePath).forEach(line -> {
            try {
                dataset.addInstance(line);
            } catch (InvalidDataNameException e) {
                e.printStackTrace();
            }
        });
        return dataset;
    }
    public static DataSet fromTextArea(String text){
        DataSet dataset=new DataSet();
        Scanner s=new Scanner(text);
        while( s.hasNextLine()){
            try {
                dataset.addInstance(s.nextLine());
            }catch (InvalidDataNameException e) {
                e.printStackTrace();
            }
        }
        return dataset;
    }
    public List<Point2D> setLine(java.util.List<Integer> arr) {
       // PropertyManager manager=applicationTemplate.manager;
        //ax+by+c=0
        //        y=(-ax-c)/b;
        if(xMin==xMax){
            xMin=xMin-1;
            xMax=xMax+1;
        }
        double yMin = (arr.get(0) * xMin +arr.get(2)) / arr.get(1);
        double yMax = (arr.get(0) * xMax + arr.get(2)) / arr.get(1);
       // System.out.println(xMin+" "+xMax+" "+yMin+" "+yMax);
        Point2D point;
        Point2D point2;
        if (yMax < yMin) {
            point = new Point2D(xMin, yMax);
            point2 = new Point2D(xMax, yMin);
        } else {
            point = new Point2D(xMin, yMin);
            point2 = new Point2D(xMax, yMax);
        }
        List<Point2D> list=new ArrayList();
        list.add(point);
        list.add(point2);
        return list;
    }
    public void displayLine(List<Point2D> list,LineChart chart){
        if (!series2.getData().isEmpty()) {
            if(chart.getData().contains(series2)) {
                chart.getData().remove(series2.getData());
                //chart.getData().remove(chart.getData().size()-1,0);
            }
            series2.getData().clear();
        }
        try{
            Thread.sleep(500);
        }catch (Exception e){
            //e.printStackTrace();
            return;
        }
        if (series2.getData().isEmpty()&&!chart.getData().contains(series2)) {
            series2.getData().add(new XYChart.Data<>((list.get(0)).getX(), (list.get(0)).getY()));
            series2.getData().add(new XYChart.Data<>((list.get(1)).getX(), (list.get(1)).getY()));
            chart.getData().add(series2);
        }else{
            series2.getData().add(new XYChart.Data<>((list.get(0)).getX(), (list.get(0)).getY()));
            series2.getData().add(new XYChart.Data<>((list.get(1)).getX(), (list.get(1)).getY()));
        }

        Node node1 = series2.getData().get(0).getNode();
        Node node2 = series2.getData().get(1).getNode();
        series2.nodeProperty().get().setStyle("-fx-stroke:blue");
        node1.setStyle("-fx-padding:0.0px;");
        node2.setStyle("-fx-padding:0.0px;");

    }
    public void displayPoints(int numclust,LineChart chart,int val) {
        chart.getData().clear();
        if (val==0){
        for (int i = 0; i < numclust; i++) {
            AtomicInteger x = new AtomicInteger(i);
            XYChart.Series<Number, Number> series = new XYChart.Series();
            series.setName(x.toString());
            getLocations().forEach((instanceName, location) -> {
                if (labels.get(instanceName).equals(x.toString())) {
                    series.getData().add(new XYChart.Data<>(location.getX(), location.getY()));     //add all with the label
                }
            });
            chart.getData().add(series);

        }}
        else if (val==1){
            for (int i = 1; i <= numclust; i++) {
                AtomicInteger x = new AtomicInteger(i);
                XYChart.Series<Number, Number> series = new XYChart.Series();
                series.setName(x.toString());
                getLocations().forEach((instanceName, location) -> {
                    if (labels.get(instanceName).equals(x.toString())) {
                        series.getData().add(new XYChart.Data<>(location.getX(), location.getY()));     //add all with the label
                    }
                });
                chart.getData().add(series);

            }
        }
    }

}
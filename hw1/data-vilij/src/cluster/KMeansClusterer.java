package cluster;

import algorithms.Clusterer;
import data.DataSet;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int maxIterations;
    private final int updateInterval;
    private final AtomicBoolean tocontinue;
    private ApplicationTemplate applicationTemplate;
    private LineChart chart;
    boolean cont;


    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, boolean cont, int numberOfClusters, ApplicationTemplate applicationTemplate) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(false);
        this.applicationTemplate = applicationTemplate;
        this.chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();
        this.cont=cont;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    @Override
    public void run() {
        initializeCentroids();
        int iteration = 0;
        while (iteration++ < maxIterations & tocontinue.get()&&cont) {
            assignLabels();
            recomputeCentroids();
            if (iteration % updateInterval == 0) {
                Platform.runLater(() -> {
                    dataset.displayPoints(getNumberOfClusters(), chart,0);
                });
            }
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                return;
            }
        }
        if (!cont&&tocontinue()) {
            assignLabels();
            recomputeCentroids();
            Platform.runLater(() -> {
                dataset.displayPoints(getNumberOfClusters(), chart,0);
            });
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        Platform.runLater(() -> {
            ((AppUI) applicationTemplate.getUIComponent()).disableRun(false);
            ((AppUI) applicationTemplate.getUIComponent()).disableScreenShot(false);
            ((AppUI) applicationTemplate.getUIComponent()).setAlgRunning(false);
            if (((AppUI) applicationTemplate.getUIComponent()).compareCounterUpdate() || cont||!tocontinue()) {
                ((AppUI) applicationTemplate.getUIComponent()).setCompleteVisable(true);
            }
        });
    }

    private void initializeCentroids() {
        Set<String> chosen = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random r = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                ++i;
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance = Double.MAX_VALUE;
            int minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

}
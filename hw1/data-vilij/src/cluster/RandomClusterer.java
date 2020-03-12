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

public class RandomClusterer extends Clusterer{
        private DataSet dataset;

        private final int maxIterations;
        private final int updateInterval;
        private AtomicBoolean tocontinue;
        private ApplicationTemplate applicationTemplate;
        private LineChart chart;

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
        public RandomClusterer(DataSet dataset, int maxIterations, int updateInterval, boolean cont, int numberOfClusters, ApplicationTemplate applicationTemplate) {
            super(numberOfClusters);
            this.dataset = dataset;
            this.maxIterations = maxIterations;
            this.updateInterval = updateInterval;
            this.tocontinue = new AtomicBoolean(false);
            this.applicationTemplate = applicationTemplate;
            this.chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();
            this.tocontinue.set(cont);
        }

        @Override
        public void run() {
            for(int i=1;i<= maxIterations && tocontinue();i++) {
                assignLabels();
                if (i % updateInterval == 0) {
                    Platform.runLater(() -> dataset.displayPoints(numberOfClusters, chart,1));
                    try {
                        Thread.sleep(1500);
                    } catch (java.lang.InterruptedException e) {
                        return;
                    }
                }
            }
            if (!tocontinue()) {
                assignLabels();
                Platform.runLater(() -> dataset.displayPoints(getNumberOfClusters(), chart,1));
                try {
                    Thread.sleep(1500);
                } catch (java.lang.InterruptedException e) {
                    return;
                }
            }
            Platform.runLater(() -> {
                ((AppUI) applicationTemplate.getUIComponent()).disableRun(false);
                ((AppUI) applicationTemplate.getUIComponent()).disableScreenShot(false);
                ((AppUI) applicationTemplate.getUIComponent()).setAlgRunning(false);
                if (((AppUI) applicationTemplate.getUIComponent()).compareCounterUpdate() || tocontinue()) {
                    ((AppUI) applicationTemplate.getUIComponent()).setCompleteVisable(true);
                }
            });
        }

        private void assignLabels() {
            dataset.getLocations().forEach((instanceName, location) -> {
                int x=generateRandom();
               dataset.updateLabel(instanceName,Integer.toString(x));
            });
        }
        private int generateRandom(){
            int randomValue=0;
            while(randomValue<1||randomValue>numberOfClusters) {
                randomValue=(int)( Math.random() * 100);
                randomValue=randomValue%10;                     //look at last digit
            }
           return randomValue;
        }

}

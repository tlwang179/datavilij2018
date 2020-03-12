package classification;


import algorithms.Classifier;
import data.DataSet;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;
    private ApplicationTemplate applicationTemplate;
    private LineChart<Number,Number> chart;


    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

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

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue,
                            LineChart chart,
                            ApplicationTemplate applicationTemplate) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.chart=chart;
        this.applicationTemplate=applicationTemplate;
    }

    @Override
    public void run() {
        ((AppUI)applicationTemplate.getUIComponent()).disableScreenShot(true);
        for (int i = 1; i <= maxIterations && tocontinue(); i++) {
            int xCoefficient=0;
            int yCoefficient=0;
            int constant=0;
            while(yCoefficient==0) {                //prevents negative denominator
                xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                constant = new Double(RAND.nextDouble() * 100).intValue();
            }

            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            List<Point2D> points=dataset.setLine(output);

            if (i % updateInterval == 0) {
                Platform.runLater(() -> dataset.displayLine(points,chart)
                );
                System.out.printf("Iteration number %d: ", i); //
                flush();
            }
            else if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                Platform.runLater(() -> dataset.displayLine(points,chart));
                System.out.printf("Iteration number %d: ", i);
                flush();
                break;
            }
            try{
                Thread.sleep(1500);}catch(Exception e){return;}
        }

        if(!tocontinue()){
            int xCoefficient=0;
            int yCoefficient=0;
            int constant=0;
            while(yCoefficient==0) {                //prevents negative denominator
                xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
                constant = new Double(RAND.nextDouble() * 100).intValue();
            }
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            List<Point2D> points=dataset.setLine(output);
                Platform.runLater(() -> dataset.displayLine(points,chart));
                System.out.printf("Iteration number %d: ",((AppUI)applicationTemplate.getUIComponent()).getCounter()); //
                flush();
            try{
                Thread.sleep(1500);
            }catch(Exception e){
                //e.printStackTrace();
                return;
            }
        }

        Platform.runLater(() -> {
            ((AppUI)applicationTemplate.getUIComponent()).disableRun(false);
            ((AppUI)applicationTemplate.getUIComponent()).disableScreenShot(false);
            ((AppUI)applicationTemplate.getUIComponent()).setAlgRunning(false);
            if (((AppUI)applicationTemplate.getUIComponent()).compareCounterUpdate()||tocontinue()){
                ((AppUI)applicationTemplate.getUIComponent()).setCompleteVisable(true);
            }
        });
    }
    // for internal viewing only
    private void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }
}
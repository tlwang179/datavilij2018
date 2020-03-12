package ui;

import javafx.scene.control.TextField;


public class AppUITestClass {

    String maxIt;
    String upInt;
    String clustN;
    int max;
    int up;
    int clust;

    public void configDialog() {
        try {
            storeMax(Integer.parseInt(maxIt));
        } catch (Exception e) {
            setZero();
        }
        try {
            storeUpInt(Integer.parseInt(upInt));
        } catch (Exception e) {
            setZero();
        }

        try {
            storeClustN(Integer.parseInt(clustN));
        } catch (Exception e) {
            setZero();
        }
    }

    public void setData(String a,String b){
        maxIt=a;
        upInt=b;
        configDialog();
    }
    public void setDataClust(String a,String b,String c){
        maxIt=a;
        upInt=b;
        clustN=c;
        configDialog();
    }

    private void storeMax(int x) {
        max = x;
    }
    private void storeUpInt(int x) {
        up = x;
    }
    private void storeClustN(int x) {
        clust = x;
    }

    private void setZero() {
        storeMax(0);
        storeUpInt(0);
        storeClustN(0);
    }
    public int getMax() {
        return max;
    }
    public int getUp(){
        return up;
    }
    public int getClust() {
        return clust;
    }
}
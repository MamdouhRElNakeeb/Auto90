package com.be4em.auto90.object;

/**
 * Created by mamdouhelnakeeb on 6/29/17.
 */

public class FareItem {

    public double value;
    public long timeInMillis;
    public String status;

    public FareItem (double value, long timeInMillis, String status){
        this.value = value;
        this.timeInMillis = timeInMillis;
        this.status = status;
    }
}

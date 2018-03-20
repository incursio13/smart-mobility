package id.ac.its.intravtasuser.model;

import org.parceler.Parcel;

@Parcel
public class Halte {

    private String name;
    private int waiting;
    private double lat;
    private double lng;

    public Halte() {
    }

    public Halte(String name, int waiting, double lat, double lng) {
        this.name = name;
        this.waiting = waiting;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public int getWaiting() {
        return waiting;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setWaiting(int waiting) {
        this.waiting = waiting;
    }

    public void setName(String name) {
        this.name = name;
    }
}

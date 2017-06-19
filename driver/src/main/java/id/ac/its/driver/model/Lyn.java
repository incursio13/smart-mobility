package id.ac.its.driver.model;

import org.parceler.Parcel;

/**
 * Created by Ilham Aulia Majid on 19-Apr-17.
 */
@Parcel
public class Lyn {

    private String plate;
    private boolean full, status;
    private double lat;
    private double lng;
    private int price;

    public Lyn() {
    }

    public Lyn(String plate, int price, boolean full, boolean status, double lat, double lng) {
        this.plate = plate;
        this.price = price;
        this.full = full;
        this.status = status;
        this.lat = lat;
        this.lng = lng;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPlate() {
        return plate;
    }

    public boolean isFull() {
        return full;
    }

    public boolean isStatus() {
        return status;
    }
}

package ca.ualberta.ridr;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mackenzie on 12/10/16.
 */
public class Driver {
    private String email;
    private String phoneNumber;
    public String name;
    public Date dateOfBirth;
    public Vehicle vehicle;
    public String bankAccountNo;
    public ArrayList<Ride> rideArrayList;
    public ArrayList<Request> requestArrayList;

    public Driver(String name, Date dateOfBirth, Vehicle vehicle, String bankAccountNo){
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.vehicle = vehicle;
        this.bankAccountNo = bankAccountNo;
        this.rideArrayList = new ArrayList<Ride>();
        this.requestArrayList = new ArrayList<Request>();
    }

    public Driver(String jeff, Date date, Vehicle vehicle, String bankAccountNo, String email, String phoneNumber) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.vehicle = vehicle;
        this.bankAccountNo = bankAccountNo;
        this.rideArrayList = new ArrayList<Ride>();
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public ArrayList<Ride> getRideArrayList() {
        return rideArrayList;
    }

    public void setRideArrayList(ArrayList<Ride> rideArrayList) {
        this.rideArrayList = rideArrayList;
    }

    public ArrayList<Request> getRequestArrayList() {
        return this.requestArrayList;
    }

    public void setRequestArrayList(ArrayList<Request> requestArrayList) {
        this.requestArrayList = requestArrayList;
    }

    public void addRide() {
    }

    public boolean kewordSearch(String james) {
        return false;
    }

    public boolean geoSort(String s, String myLocation) {
        return false;
    }

    public void acceptRide(Ride ride) {
    }

    public boolean completeRide(Ride ride) {
        return false;
    }

    public boolean isPayed() {
        return false;
    }

    public boolean getPendingRides() {
        return false;
    }

    public boolean getCompletedRides() {
        return false;
    }

    public boolean riderAcceptedRide(Ride ride) {
        return false;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public boolean isOffline() {
        return false;
    }

    public void goOffline() {
    }

    public void goOnline() {
    }
}

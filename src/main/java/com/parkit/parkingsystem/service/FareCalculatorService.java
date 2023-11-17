package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, Boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = (double) (outHour - inHour) /(60*60*1000); //in hours

        double discount_multiplier=1;
        if(discount){
            discount_multiplier=0.95;
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if(duration<(0.5)){
                    ticket.setPrice(0);
                }
                else{
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * discount_multiplier);
                }
                break;
            }
            case BIKE: {
                if(duration<(0.5)){
                    ticket.setPrice(0);
                }
                else{
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * discount_multiplier);
                }
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}
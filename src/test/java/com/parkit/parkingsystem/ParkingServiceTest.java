package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private static Ticket ticket;
    private static ParkingSpot parkingSpot;
    @BeforeEach
    private void setUpPerTest() {
            parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }
    @Test
    public void processExitingVehicleTest(){
        //GIVEN
        try {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to get your vehicle registration number");
        }
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);
        when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
        //WHEN
        parkingService.processExitingVehicle();
        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpot);
    }
    @Test
    public void testProcessIncomingVehicle(){
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to get your vehicle registration number");
        }
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);
        //WHEN
        parkingService.processIncomingVehicle();
        //THEN
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }
    @Test
    public void processExitingVehicleTestUnableUpdate(){
        //GIVEN
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to get your vehicle registration number");
        }
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
        when(ticketDAO.updateTicket(ticket)).thenReturn(false);
        //WHEN
        parkingService.processExitingVehicle();
        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(parkingSpot);
    }
    @Test
    public void testGetNextParkingNumberIfAvailable(){
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        //WHEN
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
        //THEN
        ParkingSpot expected = new ParkingSpot(1, ParkingType.CAR, true);
        Assertions.assertEquals(expected, spot);
    }
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
        //WHEN
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
        //THEN
        Assertions.assertEquals(null, spot);
    }
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(3);
        //WHEN
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
        //THEN
        Assertions.assertEquals(null, spot);
    }
}



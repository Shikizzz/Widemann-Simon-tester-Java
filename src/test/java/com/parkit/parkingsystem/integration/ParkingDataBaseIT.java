package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.model.Ticket; //added
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @ AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        //Given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //When
        parkingService.processIncomingVehicle();
        //Then
        Ticket testTicket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(testTicket);
        Assertions.assertFalse(testTicket.getParkingSpot().isAvailable());
        //check that a ticket is actualy saved in DB and Parking table is updated with availability
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        try {
            Thread.sleep(1000); // to have outTime != inTime
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        parkingService.processExitingVehicle();
        Ticket testTicket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(testTicket.getOutTime());
        Assertions.assertNotNull(testTicket.getPrice());
        //check that the fare generated and out time are populated correctly in the database
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Ticket firstTicket = ticketDAO.getTicket("ABCDEF");
        firstTicket.setInTime(new Date(System.currentTimeMillis() - (3600*1000*2)));
        ticketDAO.updateTicket(firstTicket);
        parkingService.processExitingVehicle();
        //User is now recurrent
        parkingService.processIncomingVehicle();
        Ticket secondTicket = ticketDAO.getTicket("ABCDEF");
        secondTicket.setInTime(new Date(System.currentTimeMillis() - (3600*1000)));
        ticketDAO.updateTicket(secondTicket);
        parkingService.processExitingVehicle();
        Ticket testTicket = ticketDAO.getTicket("ABCDEF");

        Assertions.assertEquals(0, (int)(100*(1.5*0.95 - testTicket.getPrice())));
        //This assert means the price is correct approximately (difference < 0.01)
    }

}

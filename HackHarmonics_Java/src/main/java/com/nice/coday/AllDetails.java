package com.nice.coday;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
//Class to load and store all details from csv files in maps
//primary keys of tables in csv files are used as keys to store values in maps

public class AllDetails {

    ResourceInfo resourceInfo;
    SortedMap<Integer, Integer> chargingStationInfo = new TreeMap<>();
    Map<Integer, Integer> entryExitPointInfo = new HashMap<>();
    Map<VCSPair, Integer> timeToChargeVehicleInfo = new HashMap<>();
    Map<Integer, int[]> vehicleTypeInfo = new HashMap<>();
    Map<Integer, int[]> tripDetails = new HashMap<>();

    public AllDetails(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
        try {// avoid csv validation errors due to opencsv
            loadChargingStationInfo();
            loadEntryExitPointInfo();
            loadTimeToChargeVehicleInfo();
            loadVehicleTypeInfo();
            loadTripDetails();
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    private void loadChargingStationInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getChargingStationInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // ignore column titles

            while ((values = csvReader.readNext()) != null) {
                int chargingStation = Integer.parseInt(values[0].substring(1));
                int distanceFromStart = Integer.parseInt(values[1]);
                chargingStationInfo.put(distanceFromStart, chargingStation);
            }
        }
    }

    private void loadEntryExitPointInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getEntryExitPointInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // ignore column titles

            while ((values = csvReader.readNext()) != null) {
                int entryExitPoint = Integer.parseInt(values[0].substring(1)); //convert point id strign to int
                int distanceFromStart = Integer.parseInt(values[1]);
                entryExitPointInfo.put(entryExitPoint, distanceFromStart);
            }
        }
    }

    private void loadTimeToChargeVehicleInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getTimeToChargeVehicleInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // ignore column titles

            while ((values = csvReader.readNext()) != null) {
                int vehicleType = Integer.parseInt(values[0].substring(1)); // convert vehicle type id string to int
                int chargingStation = Integer.parseInt(values[1].substring(1)); // convert charging station id string to int
                int timeToCharge = Integer.parseInt(values[2]);
                VCSPair pair = new VCSPair(vehicleType, chargingStation);
                timeToChargeVehicleInfo.put(pair, timeToCharge);
            }
        }
    }

    private void loadVehicleTypeInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getVehicleTypeInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // ignore column titles

            while ((values = csvReader.readNext()) != null) {
                int vehicleType = Integer.parseInt(values[0].substring(1)); // convert vehicle type id string to int
                int numberOfUnits = Integer.parseInt(values[1]);
                int mileage = Integer.parseInt(values[2]);
                vehicleTypeInfo.put(vehicleType, new int[]{numberOfUnits, mileage});
            }
        }
    }

    private void loadTripDetails() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getTripDetailsPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // Skip header

            while ((values = csvReader.readNext()) != null) {
                int id = Integer.parseInt(values[0]);
                int vehicleType = Integer.parseInt(values[1].substring(1)); // convert vehicle type id string to int
                int remainingBattery = Integer.parseInt(values[2]);
                int entryPoint = Integer.parseInt(values[3].substring(1)); // entry/exit point names to int
                int exitPoint = Integer.parseInt(values[4].substring(1));
                tripDetails.put(id, new int[]{vehicleType, remainingBattery, entryPoint, exitPoint});
            }
        }
    }
}

class VCSPair {// Vehicle Charging Station pair class

    int vehicleType;
    int chargingStation;

    public VCSPair(int vehicleType, int chargingStation) {
        this.vehicleType = vehicleType;
        this.chargingStation = chargingStation;
    }

    // for putting VCSPair objects in a map
    // we need to override equals and hashcode methods so that they can work as hashed keys 
    // and be compared
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VCSPair vcPair = (VCSPair) o;
        return vehicleType == vcPair.vehicleType && chargingStation == vcPair.chargingStation;
    }

    // hashcode method to generate hashcode for the object based on vehicleType and chargingStation. 
    // unlikely to match for upto 32 charging stations
    @Override
    public int hashCode() {
        return 31 * vehicleType + chargingStation;
    }
}

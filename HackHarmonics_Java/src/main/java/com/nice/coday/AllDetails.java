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
    SortedMap<Long, Long> chargingStationInfo = new TreeMap<>();
    Map<Long, Long> entryExitPointInfo = new HashMap<>();
    Map<VCSPair, Long> timeToChargeVehicleInfo = new HashMap<>();
    Map<Long, long[]> vehicleTypeInfo = new HashMap<>();
    Map<Long, long[]> tripDetails = new HashMap<>();

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
                long chargingStation = Long.parseLong(values[0].substring(1));
                long distanceFromStart = Long.parseLong(values[1]);
                chargingStationInfo.put(distanceFromStart, chargingStation);
            }
        }
    }

    private void loadEntryExitPointInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getEntryExitPointInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // ignore column titles

            while ((values = csvReader.readNext()) != null) {
                long entryExitPoint = Long.parseLong(values[0].substring(1)); //convert point id strign to long
                long distanceFromStart = Long.parseLong(values[1]);
                entryExitPointInfo.put(entryExitPoint, distanceFromStart);
            }
        }
    }

    private void loadTimeToChargeVehicleInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getTimeToChargeVehicleInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // ignore column titles

            while ((values = csvReader.readNext()) != null) {
                long vehicleType = Long.parseLong(values[0].substring(1)); // convert vehicle type id string to long
                long chargingStation = Long.parseLong(values[1].substring(1)); // convert charging station id string to long
                long timeToCharge = Long.parseLong(values[2]);
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
                long vehicleType = Long.parseLong(values[0].substring(1)); // convert vehicle type id string to long
                long numberOfUnits = Long.parseLong(values[1]);
                long mileage = Long.parseLong(values[2]);
                vehicleTypeInfo.put(vehicleType, new long[]{numberOfUnits, mileage});
            }
        }
    }

    private void loadTripDetails() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getTripDetailsPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // Skip header

            while ((values = csvReader.readNext()) != null) {
                long id = Long.parseLong(values[0]);
                long vehicleType = Long.parseLong(values[1].substring(1)); // convert vehicle type id string to long
                long remainingBattery = Long.parseLong(values[2]);
                long entryPoint = Long.parseLong(values[3].substring(1)); // entry/exit point names to long
                long exitPoint = Long.parseLong(values[4].substring(1));
                tripDetails.put(id, new long[]{vehicleType, remainingBattery, entryPoint, exitPoint});
            }
        }
    }
}

class VCSPair {// Vehicle Charging Station pair class

    long vehicleType;
    long chargingStation;

    public VCSPair(long vehicleType, long chargingStation) {
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
        return (int) (31 * vehicleType + chargingStation);
    }
}

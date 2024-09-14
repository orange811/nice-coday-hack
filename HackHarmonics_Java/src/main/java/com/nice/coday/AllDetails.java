package com.nice.coday;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class AllDetails {

    ResourceInfo resourceInfo;
    SortedMap<Integer, Integer> chargingStationInfo = new TreeMap<>();
    Map<Integer, Integer> entryExitPointInfo = new HashMap<>();
    Map<VCSPair, Integer> timeToChargeVehicleInfo = new HashMap<>();
    Map<Integer, int[]> vehicleTypeInfo = new HashMap<>();
    Map<Integer, int[]> tripDetails = new HashMap<>();

    public AllDetails(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
        try {
            // Initialize all maps using methods to read CSV files
            loadChargingStationInfo();
            loadEntryExitPointInfo();
            loadTimeToChargeVehicleInfo();
            loadVehicleTypeInfo();
            loadTripDetails();
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public void displayAllDetails() {
        System.out.println("Charging Station Info:");
        for (Map.Entry<Integer, Integer> entry : chargingStationInfo.entrySet()) {
            System.out.println("ChargingStation: C" + entry.getValue() + ", DistanceFromStart: " + entry.getKey());
        }

        System.out.println("\nEntry Exit Point Info:");
        for (Map.Entry<Integer, Integer> entry : entryExitPointInfo.entrySet()) {
            System.out.println("EntryExitPoint: A" + entry.getKey() + ", DistanceFromStart: " + entry.getValue());
        }

        System.out.println("\nTime to Charge Vehicle Info:");
        for (Map.Entry<VCSPair, Integer> entry : timeToChargeVehicleInfo.entrySet()) {
            VCSPair pair = entry.getKey();
            System.out.println("VehicleType: V" + pair.vehicleType + ", ChargingStation: C" + pair.chargingStation + ", TimeToChargePerUnit: " + entry.getValue());
        }

        System.out.println("\nVehicle Type Info:");
        for (Map.Entry<Integer, int[]> entry : vehicleTypeInfo.entrySet()) {
            int[] details = entry.getValue();
            System.out.println("VehicleType: V" + entry.getKey() + ", NumberOfUnitsForFullyCharge: " + details[0] + ", Mileage: " + details[1]);
        }

        System.out.println("\nTrip Details:");
        for (Map.Entry<Integer, int[]> entry : tripDetails.entrySet()) {
            int[] details = entry.getValue();
            System.out.println("ID: " + entry.getKey() + ", VehicleType: V" + details[0] + ", RemainingBatteryPercentage: " + details[1] + ", EntryPoint: A" + details[2] + ", ExitPoint: A" + details[3]);
        }
    }

    private void loadChargingStationInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getChargingStationInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // Skip header

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
            csvReader.readNext(); // Skip header

            while ((values = csvReader.readNext()) != null) {
                int entryExitPoint = Integer.parseInt(values[0].substring(1)); // Remove "A" from "A1", "A2", etc.
                int distanceFromStart = Integer.parseInt(values[1]);
                entryExitPointInfo.put(entryExitPoint, distanceFromStart);
            }
        }
    }

    private void loadTimeToChargeVehicleInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getTimeToChargeVehicleInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // Skip header

            while ((values = csvReader.readNext()) != null) {
                int vehicleType = Integer.parseInt(values[0].substring(1)); // Remove "V" from "V1", "V2", etc.
                int chargingStation = Integer.parseInt(values[1].substring(1)); // Remove "C" from "C1", "C2", etc.
                int timeToCharge = Integer.parseInt(values[2]);
                VCSPair pair = new VCSPair(vehicleType, chargingStation);
                timeToChargeVehicleInfo.put(pair, timeToCharge);
            }
        }
    }

    private void loadVehicleTypeInfo() throws IOException, CsvValidationException {
        try (CSVReader csvReader = new CSVReader(new FileReader(resourceInfo.getVehicleTypeInfoPath().toFile()))) {
            String[] values;
            csvReader.readNext(); // Skip header

            while ((values = csvReader.readNext()) != null) {
                int vehicleType = Integer.parseInt(values[0].substring(1)); // Remove "V" from "V1", "V2", etc.
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
                int vehicleType = Integer.parseInt(values[1].substring(1)); // Remove "V" from "V1", "V2", etc.
                int remainingBattery = Integer.parseInt(values[2]);
                int entryPoint = Integer.parseInt(values[3].substring(1)); // Remove "A" from "A1", "A2", etc.
                int exitPoint = Integer.parseInt(values[4].substring(1));  // Remove "A" from "A1", "A2", etc.
                tripDetails.put(id, new int[]{vehicleType, remainingBattery, entryPoint, exitPoint});
            }
        }
    }
}

class VCSPair {

    int vehicleType;
    int chargingStation;

    public VCSPair(int vehicleType, int chargingStation) {
        this.vehicleType = vehicleType;
        this.chargingStation = chargingStation;
    }

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

    @Override
    public int hashCode() {
        return 31 * vehicleType + chargingStation;
    }
}

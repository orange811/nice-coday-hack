package com.nice.coday;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.formula.eval.NotImplementedException;

public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {

    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {
        // Your implementation will go here
        ConsumptionResult consumptionResult = new ConsumptionResult();
        AllDetails allDetails = new AllDetails(resourceInfo);
        allDetails.displayAllDetails();
        Map<Integer, AggregatedResults> vehicleDetails = new HashMap<>();

        for (int tripId : allDetails.tripDetails.keySet()) {
            TripDetails tripDetails = calculateTripDetails(allDetails, tripId);
            //printTripDetails(tripId, tripDetails);

            if (tripDetails.finished) {
                // Get or create the AggregatedResults for the current vehicle type
                AggregatedResults results = vehicleDetails.computeIfAbsent(tripDetails.vehicleType, k -> new AggregatedResults());

                // Update aggregated results
                results.totalUnitsConsumed += tripDetails.unitConsumed;
                results.totalTimeRequired += tripDetails.timeRequired;
                results.numberOfTripsFinished++;
            }
        }
        printAggregatedResults(vehicleDetails);
        throw new NotImplementedException("Not implemented yet.");
    }

    private void printAggregatedResults(Map<Integer, AggregatedResults> vehicleTypeAggregates) {
        System.out.println("Aggregated Results by Vehicle Type:");
        for (Map.Entry<Integer, AggregatedResults> entry : vehicleTypeAggregates.entrySet()) {
            int vehicleType = entry.getKey();
            AggregatedResults results = entry.getValue();
            System.out.println("Vehicle Type: V" + vehicleType);
            System.out.println("Total Units Consumed: " + results.totalUnitsConsumed);
            System.out.println("Total Time Required: " + results.totalTimeRequired);
            System.out.println("Number of Trips Finished: " + results.numberOfTripsFinished);
            System.out.println(); // Print a blank line for better readability
        }
    }

    private void printTripDetails(int tripId, TripDetails tripDetails) {
        System.out.println("Trip ID: " + tripId);
        System.out.println("Vehicle Type: V" + tripDetails.vehicleType);
        System.out.println("Units Consumed: " + tripDetails.unitConsumed);
        System.out.println("Time Required: " + tripDetails.timeRequired);
        System.out.println("Finished: " + (tripDetails.finished ? "Yes" : "No"));
        System.out.println(); // Print a blank line for better readability
    }

    TripDetails calculateTripDetails(AllDetails allDetails, int tripId) {
        TripDetails tripDetails = new TripDetails();
        int vehicleType = allDetails.tripDetails.get(tripId)[0];
        int fullChargeDist = allDetails.vehicleTypeInfo.get(vehicleType)[1];
        int fullChargeUnit = allDetails.vehicleTypeInfo.get(vehicleType)[0];
        double initDistRemaining = allDetails.tripDetails.get(tripId)[1] * fullChargeDist / 100.0;
        int entryPointDist = allDetails.entryExitPointInfo.get(allDetails.tripDetails.get(tripId)[2]);
        int exitPointDist = allDetails.entryExitPointInfo.get(allDetails.tripDetails.get(tripId)[3]);
        int distRequired = exitPointDist - entryPointDist;

        //no charging needed
        if (initDistRemaining >= distRequired) {
            tripDetails.vehicleType = vehicleType;
            tripDetails.finished = true;
            return tripDetails;
        }

        int latestStationDist = -1;
        for (Map.Entry<Integer, Integer> entry : allDetails.chargingStationInfo.entrySet()) {

            if (entry.getKey() >= entryPointDist) {
                if (entry.getKey() < exitPointDist && entry.getKey() <= (double) entryPointDist + initDistRemaining) {
                    latestStationDist = entry.getKey();
                } else {
                    break;
                }
            }
        }

        //no charging station available - ded
        if (latestStationDist == -1) {
            return tripDetails;
        }

        int distTravelled = latestStationDist - entryPointDist;

        tripDetails.unitConsumed = ((100.0 - allDetails.tripDetails.get(tripId)[1]) / 100.0 + 1.0 * distTravelled / fullChargeDist) * fullChargeUnit;
        tripDetails.timeRequired = (long) (tripDetails.unitConsumed * allDetails.timeToChargeVehicleInfo.get(new VCSPair(vehicleType, allDetails.chargingStationInfo.get(latestStationDist))));

        while (distTravelled < distRequired) {
            int currentStationDist = -1;
            if (latestStationDist + fullChargeDist >= exitPointDist) {
                break;
            }
            for (Map.Entry<Integer, Integer> entry : allDetails.chargingStationInfo.tailMap(latestStationDist).entrySet()) {
                if (entry.getKey() < exitPointDist && entry.getKey() <= latestStationDist + fullChargeDist) {
                    currentStationDist = entry.getKey();
                } else {
                    break;
                }
            }
            if (currentStationDist == -1) {
                return tripDetails;
            }
            int distTravelledThisIteration = currentStationDist - latestStationDist;
            double chargeUnitsConsumedThisIteration = 1.0 * distTravelledThisIteration / fullChargeDist * fullChargeUnit;
            tripDetails.unitConsumed += chargeUnitsConsumedThisIteration;
            tripDetails.timeRequired += chargeUnitsConsumedThisIteration * allDetails.timeToChargeVehicleInfo.get(new VCSPair(vehicleType, allDetails.chargingStationInfo.get(currentStationDist)));
            distTravelled += distTravelledThisIteration;
            latestStationDist = currentStationDist;
        }
        tripDetails.vehicleType = vehicleType;
        tripDetails.finished = true;
        return tripDetails;
    }

}

class TripDetails {

    public int vehicleType = 0;
    public double unitConsumed = 0;
    public long timeRequired = 0;
    public boolean finished = false;
}

class AggregatedResults {

    double totalUnitsConsumed = 0;
    long totalTimeRequired = 0;
    long numberOfTripsFinished = 0;
}

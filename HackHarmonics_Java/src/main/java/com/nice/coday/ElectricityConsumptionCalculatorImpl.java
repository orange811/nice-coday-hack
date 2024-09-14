package com.nice.coday;

import java.io.IOException;
import java.util.Map;

public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {

    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {
        // Your implementation will go here
        AllDetails allDetails = new AllDetails(resourceInfo);
        allDetails.displayAllDetails();

<<<<<<< Updated upstream
        throw new NotImplementedException("Not implemented yet.");
    }

    TripDetails calculateTripDetails(AllDetails allDetails, int tripId) {
=======
        for (int tripId : allDetails.tripDetails.keySet()) {
            TripDetails tripDetails = calculateTripDetails(allDetails, tripId, consumptionResult);
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
        return consumptionResult;
        // throw new NotImplementedException("Not implemented yet.");
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

    TripDetails calculateTripDetails(AllDetails allDetails, int tripId, ConsumptionResult consumptionResult) {

>>>>>>> Stashed changes
        TripDetails tripDetails = new TripDetails();
        int vehicleType = allDetails.tripDetails.get(tripId)[1];
        int fullChargeDist = allDetails.vehicleTypeInfo.get(vehicleType)[2];
        int fullChargeUnit = allDetails.vehicleTypeInfo.get(vehicleType)[1];
        double initDistRemaining = allDetails.tripDetails.get(tripId)[2] * fullChargeDist / 100.0;
        int entryPointDist = allDetails.entryExitPointInfo.get(allDetails.tripDetails.get(tripId)[3]);
        int exitPointDist = allDetails.entryExitPointInfo.get(allDetails.tripDetails.get(tripId)[4]);
        int distRequired = exitPointDist - entryPointDist;
        
        //no charging needed
        if (initDistRemaining <= distRequired) {
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
        while (distTravelled < distRequired) {
            int currentStationDist = -1;
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
            long timeRequiredThisIteration = (long) (chargeUnitsConsumedThisIteration * allDetails.timeToChargeVehicleInfo.get(new VCSPair(vehicleType, allDetails.chargingStationInfo.get(currentStationDist))));
            tripDetails.unitConsumed += chargeUnitsConsumedThisIteration;
<<<<<<< Updated upstream
            tripDetails.timeRequired += allDetails.timeToChargeVehicleInfo.get(new VCSPair(vehicleType, allDetails.chargingStationInfo.get(currentStationDist))) * chargeUnitsConsumedThisIteration;
=======
            tripDetails.timeRequired += timeRequiredThisIteration;
>>>>>>> Stashed changes
            distTravelled += distTravelledThisIteration;
            String chargingStation= "C"+allDetails.chargingStationInfo.get(currentStationDist);
            consumptionResult.totalChargingStationTime.put(chargingStation,consumptionResult.totalChargingStationTime.getOrDefault(chargingStation,0l)+timeRequiredThisIteration);
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
    public double timeRequired = 0;
    public boolean finished = false;
}

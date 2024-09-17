package com.nice.coday;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
//Class to calculate electricity consumption and time required for each trip and 
// aggregate results by vehicle type and charging station

public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {

    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {
        ConsumptionResult consumptionResult = new ConsumptionResult();
        AllDetails allDetails = new AllDetails(resourceInfo);// loads all details from csv files
        Map<Long, AggregatedResults> vehicleDetails = new HashMap<>(); // to store aggregated results by vehicle type

        for (long tripId : allDetails.tripDetails.keySet()) {//iterate over all trips in all detials class
            TripDetails tripDetails = calculateTripDetails(allDetails, tripId, consumptionResult);

            // if (tripDetails.finished) {
            // create a new AggregatedResults object if given vehicle type is not already in the map
            AggregatedResults results = vehicleDetails.computeIfAbsent(tripDetails.vehicleType, k -> new AggregatedResults());

            // Update aggregated results
            results.totalUnitsConsumed = round(results.totalUnitsConsumed + tripDetails.unitConsumed);
            results.totalTimeRequired += tripDetails.timeRequired;
            if (tripDetails.finished) {
                results.numberOfTripsFinished++;
            }
            // printTripDetails(tripId, tripDetails);
            // }
        }
        // printAggregatedResults(vehicleDetails);

        // iterate over aggregated results by vehicle type 
        for (Map.Entry<Long, AggregatedResults> entry : vehicleDetails.entrySet()) {
            long vehicleType = entry.getKey();
            AggregatedResults results = entry.getValue();

            //create ConsumptionDetails instance for vehicle type and add it to consumptionResult object
            ConsumptionDetails consumptionDetails = new ConsumptionDetails(
                    "V" + vehicleType,
                    round(results.totalUnitsConsumed),
                    results.totalTimeRequired,
                    results.numberOfTripsFinished
            );

            consumptionResult.getConsumptionDetails().add(consumptionDetails);
        }
        return consumptionResult;
    }

    //calculate details for a single trip
    TripDetails calculateTripDetails(AllDetails allDetails, long tripId, ConsumptionResult consumptionResult) {
        TripDetails tripDetails = new TripDetails();
        // get all required details from allDetails object
        long vehicleType = allDetails.tripDetails.get(tripId)[0];
        long fullChargeDist = allDetails.vehicleTypeInfo.get(vehicleType)[1];
        long fullChargeUnit = allDetails.vehicleTypeInfo.get(vehicleType)[0];
        double initDistRemaining = round(allDetails.tripDetails.get(tripId)[1] * fullChargeDist / 100.0);
        long entryPointDist = allDetails.entryExitPointInfo.get(allDetails.tripDetails.get(tripId)[2]);
        long exitPointDist = allDetails.entryExitPointInfo.get(allDetails.tripDetails.get(tripId)[3]);
        long distRequired = exitPointDist - entryPointDist;

        tripDetails.vehicleType = vehicleType;
        //check if trip can be completed without charging
        if (initDistRemaining >= distRequired) {
            tripDetails.finished = true;
            return tripDetails;
        }

        //find the first required charging station
        // we take the last charging station we can before we run out of battery
        long latestStationDist = -1;
        for (Map.Entry<Long, Long> entry : allDetails.chargingStationInfo.entrySet()) {
            if (entry.getKey() >= entryPointDist) {
                if (entry.getKey() < exitPointDist && entry.getKey() <= (double) entryPointDist + initDistRemaining) {
                    latestStationDist = entry.getKey();
                } else {
                    break;
                }
            }
        }

        // if no charging station is found we can't finish the trip
        if (latestStationDist == -1) {
            return tripDetails;
        }

        // distance travelled to first station
        long distTravelled = latestStationDist - entryPointDist;

        // calculate units consumed and time required to charge at first station
        tripDetails.unitConsumed = round(((100.0 - allDetails.tripDetails.get(tripId)[1]) / 100.0 + 1.0 * distTravelled / fullChargeDist) * fullChargeUnit);
        tripDetails.timeRequired = Math.round(tripDetails.unitConsumed * allDetails.timeToChargeVehicleInfo.get(new VCSPair(vehicleType, allDetails.chargingStationInfo.get(latestStationDist))));

        // update total units consumed and total time required for charging station
        String chargingStation = "C" + allDetails.chargingStationInfo.get(latestStationDist);
        consumptionResult.totalChargingStationTime.put(chargingStation, consumptionResult.totalChargingStationTime.getOrDefault(chargingStation, 0l) + tripDetails.timeRequired);

        // find the subsequent stations till trip is over or we run out of battery
        while (distTravelled < distRequired) {
            long currentStationDist = -1;//initialised as -1 in case we die before reaching next station
            if (latestStationDist + fullChargeDist >= exitPointDist) {//if we can reach exit point without charging
                tripDetails.finished = true;
                break;
            }
            //iterate to the latest possible station
            for (Map.Entry<Long, Long> entry : allDetails.chargingStationInfo.tailMap(latestStationDist).entrySet()) {
                if (entry.getKey() < exitPointDist && entry.getKey() <= latestStationDist + fullChargeDist) {
                    currentStationDist = entry.getKey();
                } else {
                    break;
                }
            }
            //no station found
            if (currentStationDist == -1 || currentStationDist == latestStationDist) {
                break;
            }

            //calculate distance, charging units and time taken to charge in this iteration
            long distTravelledThisIteration = currentStationDist - latestStationDist;
            double chargeUnitsConsumedThisIteration = round(1.0 * distTravelledThisIteration / fullChargeDist * fullChargeUnit);
            long timeRequiredThisIteration = (long) (chargeUnitsConsumedThisIteration * allDetails.timeToChargeVehicleInfo.get(new VCSPair(vehicleType, allDetails.chargingStationInfo.get(currentStationDist))));
            //update totals
            tripDetails.unitConsumed += chargeUnitsConsumedThisIteration;
            tripDetails.timeRequired += timeRequiredThisIteration;
            distTravelled += distTravelledThisIteration;
            //update total time required at this iteration's charging station in consumptionResult
            chargingStation = "C" + allDetails.chargingStationInfo.get(currentStationDist);
            consumptionResult.totalChargingStationTime.put(chargingStation, consumptionResult.totalChargingStationTime.getOrDefault(chargingStation, 0l) + timeRequiredThisIteration);
            latestStationDist = currentStationDist;
        }
        //remaining tripDetails
        return tripDetails;
    }

    //round function to round to 2 decimal places
    private double round(double value) {
        return Math.ceil(value * 100.0) / 100.0;
    }

}

//utility classes for trip details and aggregated vehicle details
class TripDetails {

    public long vehicleType = 0;
    public double unitConsumed = 0;
    public long timeRequired = 0;
    public boolean finished = false;
}

class AggregatedResults {

    double totalUnitsConsumed = 0;
    long totalTimeRequired = 0;
    long numberOfTripsFinished = 0;
}

package com.nice.coday;

import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.formula.eval.NotImplementedException;

public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {

    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {
        // Your implementation will go here
        AllDetails allDetails = new AllDetails(resourceInfo);
        allDetails.displayAllDetails();

        throw new NotImplementedException("Not implemented yet.");
    }

    TripDetails calculateTripDetails(AllDetails allDetails, int tripId) {
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
            tripDetails.unitConsumed += chargeUnitsConsumedThisIteration;
            tripDetails.timeRequired += allDetails.timeToChargeVehicleInfo.get(new VCSPair(vehicleType, allDetails.chargingStationInfo.get(currentStationDist))) * chargeUnitsConsumedThisIteration;
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
    public double timeRequired = 0;
    public boolean finished = false;
}

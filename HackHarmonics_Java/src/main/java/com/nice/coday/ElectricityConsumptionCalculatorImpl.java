package com.nice.coday;

import java.io.IOException;

import org.apache.poi.ss.formula.eval.NotImplementedException;

public class ElectricityConsumptionCalculatorImpl implements ElectricityConsumptionCalculator {

    @Override
    public ConsumptionResult calculateElectricityAndTimeConsumption(ResourceInfo resourceInfo) throws IOException {
        // Your implementation will go here
        AllDetails allDetails = new AllDetails(resourceInfo);
        allDetails.displayAllDetails();
        System.out.println("HELLLLLLLOOOOOOOOOOOOOOO");

        throw new NotImplementedException("Not implemented yet.");
    }
}

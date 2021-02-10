package uk.tw.energy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.exception.JoiEnergyException;
import uk.tw.energy.utils.Messages;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountService {

    private final Map<String, String> smartMeterToPricePlanAccounts;

    @Autowired
    private List<PricePlan> pricePlans;

    @Autowired
    private MeterReadingService meterReadingService;

    @Autowired
    private PricePlanService pricePlanService;

    public AccountService(Map<String, String> smartMeterToPricePlanAccounts) {
        this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
    }

    public String getPricePlanIdForSmartMeterId(String smartMeterId) {
        return smartMeterToPricePlanAccounts.get(smartMeterId);
    }

    /**
     * @param smartMeterID - Smart MeterId
     * @return @{@link Optional<Map<String, BigDecimal>>}
     * @throws JoiEnergyException
     * @implSpec Get the cost for last week for the meter Id passed. If the meter id provided does not have any
     * pricePlan or Readings then it will send the appropriate message.
     */
    public Optional<Map<String, BigDecimal>> getCostForLastWeek(String smartMeterID) throws JoiEnergyException {
        Optional<List<ElectricityReading>> meterReadingServiceReadings = meterReadingService.getReadings(smartMeterID);
        if (!meterReadingServiceReadings.isPresent()) {
            throw new JoiEnergyException(Messages.METER_ID_NOT_FOUND);
        }
        List<ElectricityReading> lastWeekMeterReadings = getLastWeekMeterReadings(meterReadingServiceReadings);
        PricePlan pricePlan = getPricePlanForSmartMeterId(smartMeterID);
        return pricePlanService.getCostForElectricityReading(pricePlan, Optional.ofNullable(lastWeekMeterReadings));
    }

    /**
     * @param electricityReadings - Electricity Readings for the the particular account
     * @return @{@link List<ElectricityReading>}
     * @implSpec Fetch last week readings from all reading
     */
    private List<ElectricityReading> getLastWeekMeterReadings(Optional<List<ElectricityReading>> electricityReadings) {
        List<ElectricityReading> lastWeekMeterReadings = new ArrayList<>();
        Instant lastWeekTime = LocalDateTime.now().minusDays(7).atZone(ZoneId.systemDefault()).toInstant();
        for (ElectricityReading electricityReading : electricityReadings.get()) {
            if (electricityReading.getTime().isAfter(lastWeekTime))
                lastWeekMeterReadings.add(electricityReading);
        }
        return lastWeekMeterReadings;
    }

    /**
     * @param smartMeterId - smart meter id
     * @return @{@link PricePlan}
     * @throws JoiEnergyException
     * @implSpec fetch price plan for the smart meter id
     */
    private PricePlan getPricePlanForSmartMeterId(String smartMeterId) throws JoiEnergyException {
        String planId = getPricePlanIdForSmartMeterId(smartMeterId);
        for (PricePlan pricePlan : pricePlans) {
            if (pricePlan.getPlanName().equals(planId)) return pricePlan;
        }
        throw new JoiEnergyException(Messages.PRICE_PLAN_NOT_FOUND);
    }
}

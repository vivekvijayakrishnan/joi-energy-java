package uk.tw.energy.utils;

/**
 * Interface provides all the Error messages for the application
 */
public interface Messages {

    String METER_ID_NOT_FOUND = "The provided meter id is not found in our records";
    String PRICE_PLAN_NOT_FOUND = "Price Plan is not found for the meter id passed";
    String INTERNAL_SERVER_ERROR = "Internal Server Error, please contact Administrator";
    String WORK_IN_PROGRESS = "The API filter is work in progress";
}

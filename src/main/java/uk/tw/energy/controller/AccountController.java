package uk.tw.energy.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.tw.energy.exception.JoiEnergyException;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.utils.Messages;
import uk.tw.energy.utils.TimeEnum;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/account")
public class AccountController {

    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    /**
     * @param smartMeterID - smart meter Id
     * @param time         - Enum to filter based on last WEEK/DAY/MONTH
     * @return @{@link ResponseEntity} with result
     * @implSpec API to fetch cost for last day/week/month. By default the API will provide last Week cost.
     */
    @GetMapping("/usage/{smartMeterID}")
    public ResponseEntity getUsageBasedOnTime(@PathVariable String smartMeterID, @RequestParam(value = "time", required = false) TimeEnum time) {
        time = null != time ? time : TimeEnum.WEEK;
        if (null != smartMeterID) {
            switch (time) {
                case DAY:
                case MONTH:
                    return ResponseEntity.ok().body(Messages.WORK_IN_PROGRESS);
                case WEEK: {
                    try {
                        Optional<Map<String, BigDecimal>> result = accountService.getCostForLastWeek(smartMeterID);
                        if (result.isPresent()) {
                            return ResponseEntity.ok().body(result.get());
                        } else
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Messages.INTERNAL_SERVER_ERROR);
                    } catch (JoiEnergyException exception) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

}

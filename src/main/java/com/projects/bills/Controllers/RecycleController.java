package com.projects.bills.Controllers;

import com.projects.bills.DTOs.RecycleDTOList;
import com.projects.bills.Services.RecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecycleController {
    private static final Logger logger = LoggerFactory.getLogger(RecycleController.class);
    private final RecycleService recycleService;

    public RecycleController(RecycleService recycleService) {
        this.recycleService = recycleService;
    }

    @GetMapping("/api/v1/recycle")
    public ResponseEntity<RecycleDTOList> getRecycleBin(@AuthenticationPrincipal UserDetails user) {
        logger.info("User '{}' requested recycle bin.", user.getUsername());
        RecycleDTOList recycleDTOS = recycleService.getRecycleBin(user.getUsername());
        return new ResponseEntity<>(recycleDTOS, HttpStatus.OK);
    }
}

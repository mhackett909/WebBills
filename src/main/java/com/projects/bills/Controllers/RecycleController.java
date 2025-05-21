package com.projects.bills.Controllers;

import com.projects.bills.DTOs.RecycleDTO;

import com.projects.bills.Services.RecycleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecycleController {
    private final RecycleService recycleService;


    public RecycleController(RecycleService recycleService) {
        this.recycleService = recycleService;
    }

    @GetMapping("/api/v1/recycle")
    public ResponseEntity<List<RecycleDTO>> getRecycleBin() {
        List<RecycleDTO> recycleDTOS = recycleService.getRecycleBin();
        return new ResponseEntity<>(recycleDTOS, HttpStatus.OK);
    }
}

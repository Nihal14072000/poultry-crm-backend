package com.example.demo.dto;

import com.example.demo.entity.Employee;
import com.example.demo.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripVehicleDriversResponse {
    private Integer tripId;
    private Vehicle vehicle;
    private Employee driver;
}

package com.example.demo.dto;

import com.example.demo.entity.Order;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyingPartydto {
    private Integer id;

    private String name;

    private String contact;

    private String location;

    private List<Order> order;

    private Boolean isActive = true;

    private Boolean isDeleted = false;

    private LocalDateTime createDate;

    private LocalDateTime updatedDate;

    private Integer createdBy;

    private Integer updatedBy;
}

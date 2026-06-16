package com.example.sportswms.domain.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AssignOrderRequestDTO(
    @NotNull(message = "{order.warehouse.selected}")
    Long warehouseId,
    
    @NotEmpty(message = "{order.detail.selected}")
    List<Long> orderDetailIds
) {
}
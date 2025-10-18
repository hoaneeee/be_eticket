package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.InventoryConfigDto;
import com.example.E_Ticket.entity.InventoryConfig;

public class InventoryMapper {
    public static InventoryConfigDto toDto(InventoryConfig c){
        return new InventoryConfigDto(
                c.getId(),
                c.getEvent().getId(),
                c.getHoldTimeoutSec(),
                c.getAllowOverbook(),
                c.getMaxRenewPerHold()
        );
    }
}


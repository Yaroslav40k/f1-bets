package com.sporty.bet.settler.worker.adapter.in.messaging;

import com.sporty.bet.settler.worker.usecase.SettleBetsCommand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper (componentModel = MappingConstants.ComponentModel.SPRING)
public interface SettleBetsCommandMapper {

    SettleBetsCommand toCommand(BetWorkUnitMessage message);

}

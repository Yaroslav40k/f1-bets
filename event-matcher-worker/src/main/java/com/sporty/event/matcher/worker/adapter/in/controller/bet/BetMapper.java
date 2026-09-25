package com.sporty.event.matcher.worker.adapter.in.controller.bet;

import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Maps the bet registration request onto the application's bet model.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BetMapper {

    // Both are owned by the platform, not the caller: the identifier and the initial PENDING
    // status are assigned in the persistence adapter.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Bet toBet(RegisterBetRequest request);

}

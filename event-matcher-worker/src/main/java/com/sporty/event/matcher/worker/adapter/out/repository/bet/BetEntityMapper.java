package com.sporty.event.matcher.worker.adapter.out.repository.bet;

import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Maps between the matcher domain {@code Bet} record and the JDBC {@code BetEntity}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface BetEntityMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BetEntity toEntity(Bet bet);

    Bet toModel(BetEntity entity);
}

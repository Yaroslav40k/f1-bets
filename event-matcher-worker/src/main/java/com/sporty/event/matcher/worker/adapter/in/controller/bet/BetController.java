package com.sporty.event.matcher.worker.adapter.in.controller.bet;

import com.sporty.event.matcher.worker.application.BetManager;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the HTTP endpoint used to register bets in the matcher service.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bet")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BetController {

    BetManager betManager;
    BetMapper betMapper;

    /**
     * Registers a bet so it can later be matched against an event outcome.
     *
     * @param request bet payload to persist in the matcher database.
     * @return {@code 201 Created} once the bet has been stored.
     */
    @PutMapping()
    public ResponseEntity<Void> create(@Valid @RequestBody RegisterBetRequest request) {
        var bet = betMapper.toBet(request);
        betManager.create(bet);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}

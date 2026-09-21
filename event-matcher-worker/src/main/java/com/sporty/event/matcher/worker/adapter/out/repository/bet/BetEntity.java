package com.sporty.event.matcher.worker.adapter.out.repository.bet;

import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Represents a row in the {@code bets} table managed by Spring Data JDBC.
 * The entity implements {@code Persistable<UUID>} so inserts with preassigned identifiers are not
 * mistaken for updates when new bets are stored.
 */
@Table("bets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BetEntity implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("event_id")
    private UUID eventId;

    @Column("event_market_id")
    private UUID eventMarketId;

    @Column("event_winner_id")
    private UUID eventWinnerId;

    @Column("stake")
    private BigDecimal amount;

    @Column("status")
    private BetStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    // save() in this codebase is only ever used to insert a brand-new bet (with a
    // client-generated UUID assigned beforehand) - Spring Data JDBC's default "is new" check
    // would otherwise treat a non-null id as an existing row and issue an UPDATE that matches
    // zero rows. All later mutations (claim()) go through a dedicated native UPDATE query,
    // never through save(), so it is safe to always report "new" here.
    /**
     * Forces Spring Data JDBC to insert this entity even when the identifier is already set.
     *
     * @return always {@code true} because new bets are created with client-side UUIDs.
     */
    @Override
    public boolean isNew() {
        return true;
    }

}

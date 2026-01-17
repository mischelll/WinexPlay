package org.winex.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "EUR";

    @Column(name = "bonus_balance", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal bonusBalance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Get total available balance (real + bonus).
     */
    public BigDecimal getTotalBalance() {
        return balance.add(bonusBalance);
    }

    /**
     * Check if user has sufficient balance for a bet.
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return getTotalBalance().compareTo(amount) >= 0;
    }
}

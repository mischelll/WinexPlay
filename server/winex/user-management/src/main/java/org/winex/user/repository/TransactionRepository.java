package org.winex.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.winex.user.domain.Transaction;
import org.winex.user.domain.TransactionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<Transaction> findByUserIdAndType(UUID userId, TransactionType type);

    List<Transaction> findByUserIdAndCreatedAtBetween(UUID userId, Instant from, Instant to);

    List<Transaction> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);
}

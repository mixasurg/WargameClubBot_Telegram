package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для LoyaltyAccount.
 */
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
}


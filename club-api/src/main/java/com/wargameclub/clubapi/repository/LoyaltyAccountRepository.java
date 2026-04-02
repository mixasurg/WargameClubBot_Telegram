package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.LoyaltyAccount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA-репозиторий для счетов лояльности.
 */
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {

    /**
     * Возвращает и блокирует счет лояльности пользователя.
     *
     * @param userId идентификатор пользователя
     * @return счет лояльности
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select la from LoyaltyAccount la where la.userId = :userId")
    Optional<LoyaltyAccount> findByIdForUpdate(@Param("userId") Long userId);
}

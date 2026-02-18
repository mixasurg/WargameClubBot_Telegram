package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);

    List<User> findByNameContainingIgnoreCase(String name);
}


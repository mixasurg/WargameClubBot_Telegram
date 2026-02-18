package com.wargameclub.clubapi.repository;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.entity.ClubEvent;
import com.wargameclub.clubapi.enums.EventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubEventRepository extends JpaRepository<ClubEvent, Long> {
    @Query("select e from ClubEvent e join fetch e.organizer o " +
            "where (:type is null or e.type = :type) and e.startAt < :to and e.endAt > :from")
    List<ClubEvent> findOverlappingWithOrganizer(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("type") EventType type
    );

    @Query("select distinct e.title from ClubEvent e " +
            "where e.title is not null and trim(e.title) <> '' " +
            "order by e.title")
    List<String> findDistinctTitles(Pageable pageable);
}


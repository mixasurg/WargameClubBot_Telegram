package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.wargameclub.clubapi.entity.Army;
import com.wargameclub.clubapi.entity.ArmyUsage;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.ArmyRepository;
import com.wargameclub.clubapi.repository.ArmyUsageRepository;
import com.wargameclub.clubapi.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArmyService {
    private static final DateTimeFormatter MESSAGE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm XXX");

    private final ArmyRepository armyRepository;
    private final ArmyUsageRepository usageRepository;
    private final UserRepository userRepository;
    private final LoyaltyService loyaltyService;
    private final EventPublisher eventPublisher;

    public ArmyService(
            ArmyRepository armyRepository,
            ArmyUsageRepository usageRepository,
            UserRepository userRepository,
            LoyaltyService loyaltyService,
            EventPublisher eventPublisher
    ) {
        this.armyRepository = armyRepository;
        this.usageRepository = usageRepository;
        this.userRepository = userRepository;
        this.loyaltyService = loyaltyService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Army create(Long ownerUserId, String game, String faction, boolean isClubShared) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerUserId));
        Army army = new Army(owner, game, faction, isClubShared);
        Army saved = armyRepository.save(army);
        if (isClubShared) {
            loyaltyService.addPointsForSharedArmy(ownerUserId);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Army> find(String game, String faction, Boolean clubShared, Boolean active) {
        Specification<Army> spec = Specification.where(null);
        if (game != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("game"), game));
        }
        if (faction != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("faction"), faction));
        }
        if (clubShared != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isClubShared"), clubShared));
        }
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), active));
        }
        return armyRepository.findAll(spec);
    }

    @Transactional
    public Army deactivate(Long armyId) {
        Army army = armyRepository.findById(armyId)
                .orElseThrow(() -> new NotFoundException("Армия не найдена: " + armyId));
        army.setActive(false);
        return army;
    }

    @Transactional
    public ArmyUsage useArmy(Long armyId, Long usedByUserId, OffsetDateTime usedAt, String notes) {
        if (usedAt == null) {
            throw new BadRequestException("Поле usedAt обязательно");
        }
        Army army = armyRepository.findById(armyId)
                .orElseThrow(() -> new NotFoundException("Армия не найдена: " + armyId));
        if (!army.isActive()) {
            throw new BadRequestException("Армия неактивна");
        }
        User usedBy = userRepository.findById(usedByUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + usedByUserId));
        ArmyUsage usage = new ArmyUsage(army, usedBy, usedAt, notes);
        usageRepository.save(usage);
        loyaltyService.addPoints(army.getOwner().getId());
        publishArmyUsageNotification(army, usedBy, usedAt);
        return usage;
    }

    private void publishArmyUsageNotification(Army army, User usedBy, OffsetDateTime usedAt) {
        String message = "Армия использована: " + army.getGame() + " / " + army.getFaction()
                + "\nВладелец: " + army.getOwner().getName()
                + "\nИспользовал: " + usedBy.getName()
                + "\nКогда: " + usedAt.format(MESSAGE_FORMAT);
        eventPublisher.publishEventNotification(message);
    }
}


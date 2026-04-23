package com.example.ara.repository;

import com.example.ara.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUserId(Long userId);

    @Modifying
    @Transactional
    void deleteByEndpoint(String endpoint);

    @Modifying
    @Transactional
    void deleteAllByUserId(Long userId);
}

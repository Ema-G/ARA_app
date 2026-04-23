package com.example.ara.repository;

import com.example.ara.model.ScopeResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScopeResultRepository extends JpaRepository<ScopeResult, Long> {
    Optional<ScopeResult> findByAssessmentId(Long assessmentId);
}

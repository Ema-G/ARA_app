package com.example.ara.controller;

import com.example.ara.model.Assessment;
import com.example.ara.model.ScopeResult;
import com.example.ara.model.User;
import com.example.ara.repository.AssessmentRepository;
import com.example.ara.repository.ScopeResultRepository;
import com.example.ara.service.EmailService;
import com.example.ara.service.PushNotificationService;
import com.example.ara.service.ScopeEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentRepository assessmentRepository;
    private final ScopeResultRepository scopeResultRepository;
    private final ScopeEvaluationService scopeEvaluationService;
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;

    public AssessmentController(AssessmentRepository assessmentRepository,
                                ScopeResultRepository scopeResultRepository,
                                ScopeEvaluationService scopeEvaluationService,
                                EmailService emailService,
                                PushNotificationService pushNotificationService) {
        this.assessmentRepository = assessmentRepository;
        this.scopeResultRepository = scopeResultRepository;
        this.scopeEvaluationService = scopeEvaluationService;
        this.emailService = emailService;
        this.pushNotificationService = pushNotificationService;
    }

    /** Returns all assessments submitted by the currently authenticated user. */
    @GetMapping
    public List<Assessment> getMyAssessments(Authentication auth) {
        String email = auth.getName();
        return assessmentRepository.findBySubmittedByEmailOrderByCreatedAtDesc(email);
    }

    @GetMapping("/{id}")
    public Assessment getAssessment(@PathVariable Long id, Authentication auth) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        if (!assessment.getSubmittedByEmail().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return assessment;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitAssessment(
            @Valid @RequestBody Assessment assessment,
            Authentication auth) {
        // Stamp the submitter's email from the JWT
        assessment.setSubmittedByEmail(auth.getName());

        Assessment saved = assessmentRepository.save(assessment);
        ScopeResult result = scopeEvaluationService.evaluate(saved);
        ScopeResult savedResult = scopeResultRepository.save(result);
        saved.setStatus(Assessment.AssessmentStatus.COMPLETED);
        assessmentRepository.save(saved);

        // Email results to the ARA team — async, so user doesn't wait
        emailService.sendScopeResultEmail(saved, savedResult);

        // Push notification to the submitting user — async
        User submitter = (User) auth.getPrincipal();
        pushNotificationService.sendToUser(
            submitter,
            "Assessment Submitted",
            "Your assessment for '" + saved.getAssetName() + "' has been received. The security team has been notified.",
            "/pages/thankyou.html"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Your assessment has been submitted. The security team will be in touch.",
            "assessmentId", saved.getId()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssessment(@PathVariable Long id, Authentication auth) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assessment not found"));
        if (!assessment.getSubmittedByEmail().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        scopeResultRepository.findByAssessmentId(id).ifPresent(scopeResultRepository::delete);
        assessmentRepository.delete(assessment);
    }

    @GetMapping("/enums")
    public Map<String, Object> getEnums() {
        return Map.of(
                "assetTypes", Assessment.AssetType.values(),
                "criticalityLevels", Assessment.CriticalityLevel.values(),
                "networkExposures", Assessment.NetworkExposure.values(),
                "dataClassifications", Assessment.DataClassification.values(),
                "complianceFrameworks", Assessment.ComplianceFramework.values(),
                "systemTypes", Assessment.SystemType.values(),
                "businessImpacts", Assessment.BusinessImpact.values(),
                "availabilityRequirements", Assessment.AvailabilityRequirement.values()
        );
    }
}

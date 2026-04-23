package com.example.ara.service;

import com.example.ara.model.Assessment;
import com.example.ara.model.Assessment.*;
import com.example.ara.model.ScopeResult;
import com.example.ara.model.ScopeResult.ScopeItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScopeEvaluationService {

    public ScopeResult evaluate(Assessment assessment) {
        ScopeResult result = new ScopeResult();
        result.setAssessment(assessment);

        List<ScopeItem> items = new ArrayList<>();

        applyExternalPenTestRules(assessment, items);
        applyInternalPenTestRules(assessment, items);
        applyWebAppTestingRules(assessment, items);
        applyApiSecurityRules(assessment, items);
        applyDatabaseSecurityRules(assessment, items);
        applyCodeReviewRules(assessment, items);
        applyConfigurationReviewRules(assessment, items);
        applyComplianceAuditRules(assessment, items);
        applyDataPrivacyRules(assessment, items);
        applyThirdPartyRiskRules(assessment, items);
        applyPhysicalSecurityRules(assessment, items);
        applyBcdrRules(assessment, items);
        applyCloudSecurityRules(assessment, items);
        applySocialEngineeringRules(assessment, items);

        result.setScopeItems(items);
        result.setRiskRating(computeOverallRisk(assessment));
        result.setSummary(buildSummary(assessment, items));

        return result;
    }

    private void applyExternalPenTestRules(Assessment a, List<ScopeItem> items) {
        if (a.isInternetFacing() &&
                (a.getCriticality() == CriticalityLevel.CRITICAL || a.getCriticality() == CriticalityLevel.HIGH)) {
            items.add(new ScopeItem(
                    "External Penetration Testing",
                    "CRITICAL",
                    "Asset is internet-facing with " + a.getCriticality() + " criticality — external attack surface must be validated.",
                    true
            ));
        } else if (a.isInternetFacing()) {
            items.add(new ScopeItem(
                    "External Penetration Testing",
                    "HIGH",
                    "Asset is internet-facing and exposed to external threats.",
                    true
            ));
        }
    }

    private void applyInternalPenTestRules(Assessment a, List<ScopeItem> items) {
        boolean internalExposure = a.getNetworkExposure() == NetworkExposure.INTRANET_ONLY
                || a.getNetworkExposure() == NetworkExposure.HYBRID;
        if (internalExposure &&
                (a.getCriticality() == CriticalityLevel.CRITICAL
                        || a.getCriticality() == CriticalityLevel.HIGH
                        || a.getCriticality() == CriticalityLevel.MEDIUM)) {
            items.add(new ScopeItem(
                    "Internal Penetration Testing",
                    a.getCriticality() == CriticalityLevel.CRITICAL ? "CRITICAL" : "HIGH",
                    "Asset is on internal network; insider threat and lateral movement risks apply.",
                    a.getCriticality() == CriticalityLevel.CRITICAL
            ));
        }
    }

    private void applyWebAppTestingRules(Assessment a, List<ScopeItem> items) {
        boolean isWebApp = a.getAssetType() == AssetType.WEB_APPLICATION
                || (a.getSystemTypes() != null && (a.getSystemTypes().contains(SystemType.WEB_APPLICATION)
                || a.getSystemTypes().contains(SystemType.MOBILE_APPLICATION)));
        if (isWebApp) {
            items.add(new ScopeItem(
                    "Web Application Security Testing (OWASP Top 10)",
                    criticalityToPriority(a.getCriticality()),
                    "Web/mobile application requires OWASP Top 10 assessment including injection, auth, XSS, and CSRF testing.",
                    a.getCriticality() == CriticalityLevel.CRITICAL || a.getCriticality() == CriticalityLevel.HIGH
            ));
        }
    }

    private void applyApiSecurityRules(Assessment a, List<ScopeItem> items) {
        boolean isApi = a.getAssetType() == AssetType.API_SERVICE
                || (a.getSystemTypes() != null && (a.getSystemTypes().contains(SystemType.REST_API)
                || a.getSystemTypes().contains(SystemType.GRAPHQL_API)
                || a.getSystemTypes().contains(SystemType.MICROSERVICES)));
        if (isApi) {
            items.add(new ScopeItem(
                    "API Security Testing (OWASP API Top 10)",
                    criticalityToPriority(a.getCriticality()),
                    "API services require testing for broken object level authorization, authentication, and data exposure.",
                    a.getCriticality() == CriticalityLevel.CRITICAL || a.getCriticality() == CriticalityLevel.HIGH
            ));
        }
    }

    private void applyDatabaseSecurityRules(Assessment a, List<ScopeItem> items) {
        boolean sensitiveData = a.getDataTypes() != null && (
                a.getDataTypes().contains(DataClassification.PII)
                        || a.getDataTypes().contains(DataClassification.FINANCIAL)
                        || a.getDataTypes().contains(DataClassification.HEALTH)
                        || a.getDataTypes().contains(DataClassification.CLASSIFIED)
                        || a.getDataTypes().contains(DataClassification.CREDENTIALS));
        boolean isDatabase = a.getAssetType() == AssetType.DATABASE
                || (a.getSystemTypes() != null && a.getSystemTypes().contains(SystemType.DATABASE));
        if (sensitiveData || isDatabase) {
            items.add(new ScopeItem(
                    "Database Security Review",
                    sensitiveData && a.getCriticality() == CriticalityLevel.CRITICAL ? "CRITICAL" : "HIGH",
                    "Asset stores sensitive data — database access controls, encryption at rest, and injection protections must be reviewed.",
                    sensitiveData
            ));
        }
    }

    private void applyCodeReviewRules(Assessment a, List<ScopeItem> items) {
        if (a.isCustomDeveloped()) {
            items.add(new ScopeItem(
                    "Secure Code Review / SAST",
                    criticalityToPriority(a.getCriticality()),
                    "Custom-developed asset requires static analysis and manual code review for security vulnerabilities.",
                    a.getCriticality() == CriticalityLevel.CRITICAL || a.getCriticality() == CriticalityLevel.HIGH
            ));
        }
        if (a.isCustomDeveloped() && a.isRecentChanges()) {
            items.add(new ScopeItem(
                    "Change-Driven Security Review",
                    "HIGH",
                    "Recent changes to custom code increase the risk of newly introduced vulnerabilities.",
                    false
            ));
        }
    }

    private void applyConfigurationReviewRules(Assessment a, List<ScopeItem> items) {
        boolean isInfra = a.getAssetType() == AssetType.IT_SYSTEM
                || a.getAssetType() == AssetType.CLOUD_INFRASTRUCTURE
                || (a.getSystemTypes() != null && a.getSystemTypes().contains(SystemType.CLOUD_NATIVE));
        if (isInfra) {
            items.add(new ScopeItem(
                    "Infrastructure & Configuration Review (CIS Benchmarks)",
                    criticalityToPriority(a.getCriticality()),
                    "Infrastructure assets require hardening verification against CIS benchmarks and security baselines.",
                    false
            ));
        }
    }

    private void applyComplianceAuditRules(Assessment a, List<ScopeItem> items) {
        if (a.getComplianceFrameworks() != null && !a.getComplianceFrameworks().isEmpty()) {
            String frameworks = String.join(", ", a.getComplianceFrameworks().stream()
                    .map(Enum::name).toList());
            items.add(new ScopeItem(
                    "Regulatory Compliance Audit",
                    "HIGH",
                    "Asset is subject to: " + frameworks + " — compliance controls must be audited.",
                    true
            ));
        }
    }

    private void applyDataPrivacyRules(Assessment a, List<ScopeItem> items) {
        boolean hasPii = a.getDataTypes() != null && a.getDataTypes().contains(DataClassification.PII);
        boolean gdprApplies = a.getComplianceFrameworks() != null && a.getComplianceFrameworks().contains(ComplianceFramework.GDPR);
        if (hasPii || gdprApplies) {
            items.add(new ScopeItem(
                    "Data Privacy Assessment (DPIA)",
                    "HIGH",
                    "PII or GDPR-regulated data requires a Data Protection Impact Assessment.",
                    gdprApplies
            ));
        }
    }

    private void applyThirdPartyRiskRules(Assessment a, List<ScopeItem> items) {
        if (a.isThirdPartyIntegrations()) {
            items.add(new ScopeItem(
                    "Third-Party / Supply Chain Risk Assessment",
                    a.getCriticality() == CriticalityLevel.CRITICAL ? "HIGH" : "MEDIUM",
                    "Third-party integrations introduce supply chain risk — vendor security posture and data-sharing agreements must be reviewed.",
                    false
            ));
        }
        if (a.getAssetType() == AssetType.THIRD_PARTY_SERVICE) {
            items.add(new ScopeItem(
                    "Vendor Security Assessment",
                    criticalityToPriority(a.getCriticality()),
                    "Third-party service — vendor SOC 2/ISO 27001 reports and contractual security obligations must be reviewed.",
                    true
            ));
        }
    }

    private void applyPhysicalSecurityRules(Assessment a, List<ScopeItem> items) {
        if (a.getAssetType() == AssetType.PHYSICAL_ASSET || a.getAssetType() == AssetType.IOT_DEVICE) {
            items.add(new ScopeItem(
                    "Physical Security Review",
                    criticalityToPriority(a.getCriticality()),
                    "Physical/IoT asset requires review of physical access controls, tamper protection, and environment security.",
                    a.getCriticality() == CriticalityLevel.CRITICAL
            ));
        }
    }

    private void applyBcdrRules(Assessment a, List<ScopeItem> items) {
        if (a.getAvailabilityRequirement() == AvailabilityRequirement.ALWAYS_ON_24_7
                || (a.getCriticality() == CriticalityLevel.CRITICAL && a.getBusinessImpact() == BusinessImpact.CATASTROPHIC)) {
            items.add(new ScopeItem(
                    "Business Continuity & Disaster Recovery Testing",
                    "HIGH",
                    "High availability requirement and/or catastrophic business impact mandates BC/DR validation.",
                    a.getCriticality() == CriticalityLevel.CRITICAL
            ));
        }
    }

    private void applyCloudSecurityRules(Assessment a, List<ScopeItem> items) {
        boolean isCloud = a.getAssetType() == AssetType.CLOUD_INFRASTRUCTURE
                || (a.getSystemTypes() != null && a.getSystemTypes().contains(SystemType.CLOUD_NATIVE));
        if (isCloud) {
            items.add(new ScopeItem(
                    "Cloud Security Assessment (CSPM / CIS Cloud Benchmarks)",
                    criticalityToPriority(a.getCriticality()),
                    "Cloud-hosted asset requires review of IAM policies, storage permissions, network security groups, and logging.",
                    a.getCriticality() == CriticalityLevel.CRITICAL || a.getCriticality() == CriticalityLevel.HIGH
            ));
        }
    }

    private void applySocialEngineeringRules(Assessment a, List<ScopeItem> items) {
        if (a.getAssetType() == AssetType.PERSONNEL
                || (a.getCriticality() == CriticalityLevel.CRITICAL && a.getBusinessImpact() == BusinessImpact.CATASTROPHIC)) {
            items.add(new ScopeItem(
                    "Social Engineering / Phishing Assessment",
                    "MEDIUM",
                    "Personnel assets or high-value targets require evaluation of human-factor security awareness.",
                    false
            ));
        }
    }

    private String criticalityToPriority(CriticalityLevel level) {
        return switch (level) {
            case CRITICAL -> "CRITICAL";
            case HIGH -> "HIGH";
            case MEDIUM -> "MEDIUM";
            case LOW -> "LOW";
        };
    }

    private String computeOverallRisk(Assessment a) {
        int score = 0;
        score += switch (a.getCriticality()) {
            case CRITICAL -> 40;
            case HIGH -> 30;
            case MEDIUM -> 20;
            case LOW -> 10;
        };
        score += switch (a.getBusinessImpact()) {
            case CATASTROPHIC -> 30;
            case HIGH -> 20;
            case MEDIUM -> 10;
            case LOW -> 5;
        };
        if (a.isInternetFacing()) score += 15;
        if (a.getDataTypes() != null) {
            if (a.getDataTypes().contains(DataClassification.PII)) score += 5;
            if (a.getDataTypes().contains(DataClassification.FINANCIAL)) score += 5;
            if (a.getDataTypes().contains(DataClassification.HEALTH)) score += 5;
            if (a.getDataTypes().contains(DataClassification.CLASSIFIED)) score += 10;
        }
        if (!a.isExistingSecurityControls()) score += 10;

        if (score >= 80) return "CRITICAL";
        if (score >= 60) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }

    private String buildSummary(Assessment a, List<ScopeItem> items) {
        long mandatoryCount = items.stream().filter(ScopeItem::isMandatory).count();
        return String.format(
                "Assessment for '%s' (%s) has identified %d testing area(s) in scope, of which %d are mandatory. " +
                        "Overall risk rating: %s.",
                a.getAssetName(),
                a.getAssetType().name().replace("_", " "),
                items.size(),
                mandatoryCount,
                computeOverallRisk(a)
        );
    }
}

package com.example.ara;

import com.example.ara.model.Assessment;
import com.example.ara.model.ScopeResult;
import com.example.ara.service.ScopeEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AraApplicationTests {

    @Autowired
    private ScopeEvaluationService scopeEvaluationService;

    @Test
    void contextLoads() {
    }

    @Test
    void criticalInternetFacingAssetProducesCriticalScope() {
        Assessment a = new Assessment();
        a.setAssetName("Test Asset");
        a.setAssetOwner("Owner");
        a.setDepartment("IT");
        a.setAssetType(Assessment.AssetType.WEB_APPLICATION);
        a.setCriticality(Assessment.CriticalityLevel.CRITICAL);
        a.setNetworkExposure(Assessment.NetworkExposure.INTERNET_FACING);
        a.setInternetFacing(true);
        a.setBusinessImpact(Assessment.BusinessImpact.CATASTROPHIC);
        a.setAvailabilityRequirement(Assessment.AvailabilityRequirement.ALWAYS_ON_24_7);
        a.setDataTypes(List.of(Assessment.DataClassification.PII));
        a.setComplianceFrameworks(List.of(Assessment.ComplianceFramework.GDPR));
        a.setSystemTypes(List.of(Assessment.SystemType.WEB_APPLICATION));

        ScopeResult result = scopeEvaluationService.evaluate(a);

        assertThat(result.getRiskRating()).isEqualTo("CRITICAL");
        assertThat(result.getScopeItems()).isNotEmpty();
        assertThat(result.getScopeItems().stream()
                .anyMatch(i -> i.getTestArea().contains("External Penetration Testing"))).isTrue();
        assertThat(result.getScopeItems().stream()
                .anyMatch(i -> i.getTestArea().contains("Web Application Security Testing"))).isTrue();
    }

    @Test
    void lowRiskInternalAssetProducesLimitedScope() {
        Assessment a = new Assessment();
        a.setAssetName("Internal Tool");
        a.setAssetOwner("Owner");
        a.setDepartment("HR");
        a.setAssetType(Assessment.AssetType.IT_SYSTEM);
        a.setCriticality(Assessment.CriticalityLevel.LOW);
        a.setNetworkExposure(Assessment.NetworkExposure.INTRANET_ONLY);
        a.setInternetFacing(false);
        a.setBusinessImpact(Assessment.BusinessImpact.LOW);
        a.setAvailabilityRequirement(Assessment.AvailabilityRequirement.BEST_EFFORT);
        a.setDataTypes(List.of(Assessment.DataClassification.PUBLIC));
        a.setExistingSecurityControls(true);

        ScopeResult result = scopeEvaluationService.evaluate(a);

        assertThat(result.getRiskRating()).isIn("LOW", "MEDIUM");
        assertThat(result.getScopeItems().stream()
                .noneMatch(i -> i.getTestArea().contains("External Penetration Testing"))).isTrue();
    }
}

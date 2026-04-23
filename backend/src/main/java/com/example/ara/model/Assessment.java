package com.example.ara.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assessments")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String assetName;

    @NotBlank
    private String assetOwner;

    @NotBlank
    private String department;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CriticalityLevel criticality;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NetworkExposure networkExposure;

    @ElementCollection
    @CollectionTable(name = "assessment_data_types", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "data_type")
    @Enumerated(EnumType.STRING)
    private List<DataClassification> dataTypes;

    @ElementCollection
    @CollectionTable(name = "assessment_frameworks", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "framework")
    @Enumerated(EnumType.STRING)
    private List<ComplianceFramework> complianceFrameworks;

    @ElementCollection
    @CollectionTable(name = "assessment_system_types", joinColumns = @JoinColumn(name = "assessment_id"))
    @Column(name = "system_type")
    @Enumerated(EnumType.STRING)
    private List<SystemType> systemTypes;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BusinessImpact businessImpact;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AvailabilityRequirement availabilityRequirement;

    private boolean internetFacing;
    private boolean thirdPartyIntegrations;
    private boolean customDeveloped;
    private boolean recentChanges;
    private boolean previouslyAssessed;
    private boolean existingSecurityControls;

    private String additionalContext;

    private String submittedByEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private AssessmentStatus status;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = AssessmentStatus.PENDING;
    }

    // Enums

    public enum AssetType {
        IT_SYSTEM, WEB_APPLICATION, API_SERVICE, DATABASE,
        CLOUD_INFRASTRUCTURE, PHYSICAL_ASSET, DATA_ASSET,
        FINANCIAL_SYSTEM, IOT_DEVICE, THIRD_PARTY_SERVICE, PERSONNEL
    }

    public enum CriticalityLevel {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public enum NetworkExposure {
        INTERNET_FACING, INTRANET_ONLY, AIR_GAPPED, HYBRID
    }

    public enum DataClassification {
        PII, FINANCIAL, HEALTH, CLASSIFIED, INTELLECTUAL_PROPERTY,
        CREDENTIALS, OPERATIONAL, PUBLIC
    }

    public enum ComplianceFramework {
        GDPR, ISO_27001, SOC2, PCI_DSS, HIPAA, NIST, CIS, DORA, NIS2, INTERNAL_POLICY
    }

    public enum SystemType {
        WEB_APPLICATION, MOBILE_APPLICATION, REST_API, GRAPHQL_API,
        MICROSERVICES, MONOLITH, DATABASE, MESSAGE_QUEUE,
        CLOUD_NATIVE, LEGACY_SYSTEM, EMBEDDED_SYSTEM
    }

    public enum BusinessImpact {
        CATASTROPHIC, HIGH, MEDIUM, LOW
    }

    public enum AvailabilityRequirement {
        ALWAYS_ON_24_7, BUSINESS_HOURS, BEST_EFFORT, MINIMAL
    }

    public enum AssessmentStatus {
        PENDING, COMPLETED, ARCHIVED
    }

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getAssetOwner() { return assetOwner; }
    public void setAssetOwner(String assetOwner) { this.assetOwner = assetOwner; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public CriticalityLevel getCriticality() { return criticality; }
    public void setCriticality(CriticalityLevel criticality) { this.criticality = criticality; }

    public NetworkExposure getNetworkExposure() { return networkExposure; }
    public void setNetworkExposure(NetworkExposure networkExposure) { this.networkExposure = networkExposure; }

    public List<DataClassification> getDataTypes() { return dataTypes; }
    public void setDataTypes(List<DataClassification> dataTypes) { this.dataTypes = dataTypes; }

    public List<ComplianceFramework> getComplianceFrameworks() { return complianceFrameworks; }
    public void setComplianceFrameworks(List<ComplianceFramework> complianceFrameworks) { this.complianceFrameworks = complianceFrameworks; }

    public List<SystemType> getSystemTypes() { return systemTypes; }
    public void setSystemTypes(List<SystemType> systemTypes) { this.systemTypes = systemTypes; }

    public BusinessImpact getBusinessImpact() { return businessImpact; }
    public void setBusinessImpact(BusinessImpact businessImpact) { this.businessImpact = businessImpact; }

    public AvailabilityRequirement getAvailabilityRequirement() { return availabilityRequirement; }
    public void setAvailabilityRequirement(AvailabilityRequirement availabilityRequirement) { this.availabilityRequirement = availabilityRequirement; }

    public boolean isInternetFacing() { return internetFacing; }
    public void setInternetFacing(boolean internetFacing) { this.internetFacing = internetFacing; }

    public boolean isThirdPartyIntegrations() { return thirdPartyIntegrations; }
    public void setThirdPartyIntegrations(boolean thirdPartyIntegrations) { this.thirdPartyIntegrations = thirdPartyIntegrations; }

    public boolean isCustomDeveloped() { return customDeveloped; }
    public void setCustomDeveloped(boolean customDeveloped) { this.customDeveloped = customDeveloped; }

    public boolean isRecentChanges() { return recentChanges; }
    public void setRecentChanges(boolean recentChanges) { this.recentChanges = recentChanges; }

    public boolean isPreviouslyAssessed() { return previouslyAssessed; }
    public void setPreviouslyAssessed(boolean previouslyAssessed) { this.previouslyAssessed = previouslyAssessed; }

    public boolean isExistingSecurityControls() { return existingSecurityControls; }
    public void setExistingSecurityControls(boolean existingSecurityControls) { this.existingSecurityControls = existingSecurityControls; }

    public String getAdditionalContext() { return additionalContext; }
    public void setAdditionalContext(String additionalContext) { this.additionalContext = additionalContext; }

    public String getSubmittedByEmail() { return submittedByEmail; }
    public void setSubmittedByEmail(String submittedByEmail) { this.submittedByEmail = submittedByEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public AssessmentStatus getStatus() { return status; }
    public void setStatus(AssessmentStatus status) { this.status = status; }
}

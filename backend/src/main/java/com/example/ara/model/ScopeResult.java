package com.example.ara.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "scope_results")
public class ScopeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ElementCollection
    @CollectionTable(name = "scope_items", joinColumns = @JoinColumn(name = "scope_result_id"))
    private List<ScopeItem> scopeItems;

    private String riskRating;
    private String summary;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    @Embeddable
    public static class ScopeItem {
        private String testArea;
        private String priority;
        private String rationale;
        private boolean mandatory;

        public ScopeItem() {}

        public ScopeItem(String testArea, String priority, String rationale, boolean mandatory) {
            this.testArea = testArea;
            this.priority = priority;
            this.rationale = rationale;
            this.mandatory = mandatory;
        }

        public String getTestArea() { return testArea; }
        public void setTestArea(String testArea) { this.testArea = testArea; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }

        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public List<ScopeItem> getScopeItems() { return scopeItems; }
    public void setScopeItems(List<ScopeItem> scopeItems) { this.scopeItems = scopeItems; }

    public String getRiskRating() { return riskRating; }
    public void setRiskRating(String riskRating) { this.riskRating = riskRating; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}

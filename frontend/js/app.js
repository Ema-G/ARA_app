// ─── Application State ───────────────────────────────────────
const state = {
  currentStep: 0,
  totalSteps: 5,
  formData: {},
  view: 'questionnaire', // 'questionnaire' | 'history'
  historyItems: []
};

// ─── Questionnaire Definition ─────────────────────────────────
const STEPS = [
  {
    id: 'asset-info',
    title: 'Asset Information',
    description: 'Basic details about the asset being assessed',
    badge: 'Step 1 of 5',
    fields: () => `
      <div class="form-group">
        <label>Asset Name <span class="required">*</span></label>
        <input type="text" id="assetName" placeholder="e.g. Customer Portal, ERP System" value="${val('assetName')}" required>
      </div>
      <div class="form-group">
        <label>Asset Owner <span class="required">*</span></label>
        <input type="text" id="assetOwner" placeholder="Full name of the asset owner" value="${val('assetOwner')}" required>
      </div>
      <div class="form-group">
        <label>Department / Business Unit <span class="required">*</span></label>
        <input type="text" id="department" placeholder="e.g. IT, Finance, Operations" value="${val('department')}" required>
      </div>
      <div class="form-group">
        <label>Asset Type <span class="required">*</span>
          <span class="hint">Select the primary classification of this asset</span>
        </label>
        <select id="assetType">
          <option value="">-- Select asset type --</option>
          ${option('IT_SYSTEM','IT System / Server')}
          ${option('WEB_APPLICATION','Web Application')}
          ${option('API_SERVICE','API / Web Service')}
          ${option('DATABASE','Database')}
          ${option('CLOUD_INFRASTRUCTURE','Cloud Infrastructure')}
          ${option('PHYSICAL_ASSET','Physical Asset')}
          ${option('DATA_ASSET','Data / Information Asset')}
          ${option('FINANCIAL_SYSTEM','Financial System')}
          ${option('IOT_DEVICE','IoT / Embedded Device')}
          ${option('THIRD_PARTY_SERVICE','Third-Party / SaaS Service')}
          ${option('PERSONNEL','Personnel / People')}
        </select>
      </div>
    `
  },
  {
    id: 'criticality',
    title: 'Criticality & Impact',
    description: 'How important is this asset to the business?',
    badge: 'Step 2 of 5',
    fields: () => `
      <div class="form-group">
        <label>Asset Criticality <span class="required">*</span>
          <span class="hint">Based on the asset's importance to core business operations</span>
        </label>
        <select id="criticality">
          <option value="">-- Select criticality --</option>
          ${option('CRITICAL','Critical — essential to core operations, failure causes immediate business stoppage')}
          ${option('HIGH','High — significant impact on operations if unavailable')}
          ${option('MEDIUM','Medium — moderate impact, workarounds exist')}
          ${option('LOW','Low — minimal business impact')}
        </select>
      </div>
      <div class="form-group">
        <label>Business Impact if Compromised <span class="required">*</span></label>
        <select id="businessImpact">
          <option value="">-- Select impact level --</option>
          ${option('CATASTROPHIC','Catastrophic — existential threat, regulatory penalties, major data breach')}
          ${option('HIGH','High — significant financial loss, reputation damage')}
          ${option('MEDIUM','Medium — moderate disruption, recoverable')}
          ${option('LOW','Low — minimal impact')}
        </select>
      </div>
      <div class="form-group">
        <label>Availability Requirement <span class="required">*</span></label>
        <select id="availabilityRequirement">
          <option value="">-- Select availability requirement --</option>
          ${option('ALWAYS_ON_24_7','24/7 — must be always available')}
          ${option('BUSINESS_HOURS','Business hours only')}
          ${option('BEST_EFFORT','Best effort — some downtime acceptable')}
          ${option('MINIMAL','Minimal — non-critical availability')}
        </select>
      </div>
      <div class="form-group">
        <label>Has this asset been previously assessed?</label>
        ${boolToggle('previouslyAssessed')}
      </div>
    `
  },
  {
    id: 'technical',
    title: 'Technical Environment',
    description: 'Technical characteristics and deployment details',
    badge: 'Step 3 of 5',
    fields: () => `
      <div class="form-group">
        <label>Network Exposure <span class="required">*</span></label>
        <select id="networkExposure">
          <option value="">-- Select network exposure --</option>
          ${option('INTERNET_FACING','Internet-facing — directly accessible from the internet')}
          ${option('INTRANET_ONLY','Intranet only — internal network access only')}
          ${option('AIR_GAPPED','Air-gapped — isolated from all networks')}
          ${option('HYBRID','Hybrid — both internal and external access')}
        </select>
      </div>
      <div class="form-group">
        <label>Is the asset directly internet-facing?</label>
        ${boolToggle('internetFacing')}
      </div>
      <div class="form-group">
        <label>System / Technology Types
          <span class="hint">Select all that apply</span>
        </label>
        <div class="checkbox-grid" id="systemTypes">
          ${checkbox('systemTypes', 'WEB_APPLICATION', 'Web Application')}
          ${checkbox('systemTypes', 'MOBILE_APPLICATION', 'Mobile Application')}
          ${checkbox('systemTypes', 'REST_API', 'REST API')}
          ${checkbox('systemTypes', 'GRAPHQL_API', 'GraphQL API')}
          ${checkbox('systemTypes', 'MICROSERVICES', 'Microservices')}
          ${checkbox('systemTypes', 'MONOLITH', 'Monolithic Application')}
          ${checkbox('systemTypes', 'DATABASE', 'Database')}
          ${checkbox('systemTypes', 'MESSAGE_QUEUE', 'Message Queue / Broker')}
          ${checkbox('systemTypes', 'CLOUD_NATIVE', 'Cloud Native')}
          ${checkbox('systemTypes', 'LEGACY_SYSTEM', 'Legacy System')}
          ${checkbox('systemTypes', 'EMBEDDED_SYSTEM', 'Embedded / Firmware')}
        </div>
      </div>
      <div class="form-group">
        <label>Is the asset custom-developed (in-house code)?</label>
        ${boolToggle('customDeveloped')}
      </div>
      <div class="form-group">
        <label>Does the asset integrate with third-party systems?</label>
        ${boolToggle('thirdPartyIntegrations')}
      </div>
      <div class="form-group">
        <label>Have there been significant changes to the asset recently?</label>
        ${boolToggle('recentChanges')}
      </div>
    `
  },
  {
    id: 'data-compliance',
    title: 'Data & Compliance',
    description: 'Data classification and regulatory requirements',
    badge: 'Step 4 of 5',
    fields: () => `
      <div class="form-group">
        <label>Data Types Processed or Stored
          <span class="hint">Select all that apply</span>
        </label>
        <div class="checkbox-grid" id="dataTypes">
          ${checkbox('dataTypes', 'PII', 'Personally Identifiable Information (PII)')}
          ${checkbox('dataTypes', 'FINANCIAL', 'Financial / Payment Data')}
          ${checkbox('dataTypes', 'HEALTH', 'Health / Medical Data')}
          ${checkbox('dataTypes', 'CLASSIFIED', 'Classified / Sensitive Business Data')}
          ${checkbox('dataTypes', 'INTELLECTUAL_PROPERTY', 'Intellectual Property')}
          ${checkbox('dataTypes', 'CREDENTIALS', 'Credentials / Authentication Data')}
          ${checkbox('dataTypes', 'OPERATIONAL', 'Operational / Process Data')}
          ${checkbox('dataTypes', 'PUBLIC', 'Public / Non-sensitive Data')}
        </div>
      </div>
      <div class="form-group">
        <label>Applicable Regulatory / Compliance Frameworks
          <span class="hint">Select all that apply</span>
        </label>
        <div class="checkbox-grid" id="complianceFrameworks">
          ${checkbox('complianceFrameworks', 'GDPR', 'GDPR')}
          ${checkbox('complianceFrameworks', 'ISO_27001', 'ISO 27001')}
          ${checkbox('complianceFrameworks', 'SOC2', 'SOC 2')}
          ${checkbox('complianceFrameworks', 'PCI_DSS', 'PCI DSS')}
          ${checkbox('complianceFrameworks', 'HIPAA', 'HIPAA')}
          ${checkbox('complianceFrameworks', 'NIST', 'NIST CSF')}
          ${checkbox('complianceFrameworks', 'CIS', 'CIS Controls')}
          ${checkbox('complianceFrameworks', 'DORA', 'DORA')}
          ${checkbox('complianceFrameworks', 'NIS2', 'NIS2')}
          ${checkbox('complianceFrameworks', 'INTERNAL_POLICY', 'Internal Policy Only')}
        </div>
      </div>
    `
  },
  {
    id: 'controls',
    title: 'Existing Controls',
    description: 'Current security posture and additional context',
    badge: 'Step 5 of 5',
    fields: () => `
      <div class="form-group">
        <label>Are existing security controls in place for this asset?</label>
        ${boolToggle('existingSecurityControls')}
      </div>
      <div class="form-group">
        <label>Additional Context
          <span class="hint">Any other relevant information for the assessment scope</span>
        </label>
        <textarea id="additionalContext" placeholder="e.g. Recent incidents, known vulnerabilities, upcoming changes...">${val('additionalContext')}</textarea>
      </div>
    `
  }
];

// ─── Helpers ──────────────────────────────────────────────────
function val(key) { return state.formData[key] || ''; }

function option(value, label) {
  const selected = [
    state.formData.assetType,
    state.formData.criticality,
    state.formData.businessImpact,
    state.formData.availabilityRequirement,
    state.formData.networkExposure
  ].includes(value) ? 'selected' : '';
  return `<option value="${value}" ${selected}>${label}</option>`;
}

function checkbox(group, value, label) {
  const arr = state.formData[group] || [];
  const checked = arr.includes(value) ? 'checked' : '';
  const checkedClass = arr.includes(value) ? 'checked' : '';
  return `
    <label class="checkbox-item ${checkedClass}">
      <input type="checkbox" name="${group}" value="${value}" ${checked}>
      <span>${label}</span>
    </label>
  `;
}

function boolToggle(key) {
  const current = state.formData[key];
  const yesClass = current === true  ? 'selected-yes' : '';
  const noClass  = current === false ? 'selected-no'  : '';
  return `
    <div class="toggle-group">
      <button type="button" class="toggle-btn ${yesClass}" data-key="${key}" data-value="true">Yes</button>
      <button type="button" class="toggle-btn ${noClass}"  data-key="${key}" data-value="false">No</button>
    </div>
  `;
}

function showToast(message, type = 'info') {
  document.querySelectorAll('.toast').forEach(t => t.remove());
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

function showLoading(show) {
  document.getElementById('loading-overlay').classList.toggle('hidden', !show);
}

// ─── Rendering ────────────────────────────────────────────────
function render() {
  if (state.view === 'questionnaire') renderQuestionnaire();
  else if (state.view === 'history')  renderHistory();

  document.querySelectorAll('.nav-btn[data-view]').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.view === state.view);
  });
}

function renderQuestionnaire() {
  const step = STEPS[state.currentStep];
  const pct  = Math.round((state.currentStep / state.totalSteps) * 100);

  document.getElementById('app').innerHTML = `
    <div class="progress-container">
      <div class="progress-info">
        <span>${step.badge}</span>
        <span>${pct}% complete</span>
      </div>
      <div class="progress-track">
        <div class="progress-fill" style="width: ${pct}%"></div>
      </div>
      <div class="step-dots">
        ${STEPS.map((_, i) => `
          <div class="step-dot ${i < state.currentStep ? 'done' : i === state.currentStep ? 'active' : ''}"></div>
        `).join('')}
      </div>
    </div>

    <div class="card">
      <div class="section-header">
        <div>
          <div class="section-badge">${step.badge}</div>
          <h2>${step.title}</h2>
          <p>${step.description}</p>
        </div>
      </div>
      <form id="step-form" novalidate>
        ${step.fields()}
      </form>
    </div>

    <div class="form-nav">
      <button class="btn btn-secondary" id="btn-back" ${state.currentStep === 0 ? 'disabled' : ''}>
        &larr; Back
      </button>
      <button class="btn btn-primary" id="btn-next">
        ${state.currentStep === STEPS.length - 1 ? 'Submit Assessment' : 'Next &rarr;'}
      </button>
    </div>
  `;

  bindStepEvents();
}

function renderHistory() {
  document.getElementById('app').innerHTML = `
    <div class="card">
      <div class="section-header">
        <div>
          <div class="section-badge">History</div>
          <h2>My Submitted Assessments</h2>
          <p>Assessments you have submitted — the security team has been notified for each</p>
        </div>
      </div>
      ${state.historyItems.length === 0 ? `
        <div class="empty-state">
          <svg width="64" height="64" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
          <h3>No assessments yet</h3>
          <p>Complete the questionnaire to submit your first assessment.</p>
        </div>
      ` : `
        <div class="history-list">
          ${state.historyItems.map(item => `
            <div class="history-item">
              <div class="history-item-name">${item.assetName}</div>
              <div class="history-item-meta">
                ${item.assetType.replace(/_/g,' ')} &middot;
                ${item.department} &middot;
                ${new Date(item.createdAt).toLocaleDateString()}
              </div>
              <div class="history-item-badge">
                <span style="font-size:.72rem;font-weight:600;color:var(--text-muted);background:var(--surface-2);padding:.25rem .75rem;border-radius:99px">
                  ${item.criticality}
                </span>
              </div>
            </div>
          `).join('')}
        </div>
      `}
    </div>
  `;
}

// ─── Event Binding ────────────────────────────────────────────
function bindStepEvents() {
  document.getElementById('btn-back')?.addEventListener('click', () => {
    if (state.currentStep > 0) { state.currentStep--; render(); }
  });

  document.getElementById('btn-next')?.addEventListener('click', () => {
    if (!collectStep()) return;
    if (state.currentStep < STEPS.length - 1) {
      state.currentStep++;
      render();
      window.scrollTo(0, 0);
    } else {
      submitAssessment();
    }
  });

  document.querySelectorAll('.toggle-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      state.formData[btn.dataset.key] = btn.dataset.value === 'true';
      render();
    });
  });

  document.querySelectorAll('.checkbox-item input[type="checkbox"]').forEach(cb => {
    cb.addEventListener('change', () => {
      cb.closest('.checkbox-item').classList.toggle('checked', cb.checked);
    });
  });

  document.querySelectorAll('select').forEach(sel => {
    sel.addEventListener('change', () => { state.formData[sel.id] = sel.value; });
  });
}

function collectStep() {
  const step = STEPS[state.currentStep];

  document.querySelectorAll('#step-form input[type="text"]').forEach(i => {
    state.formData[i.id] = i.value.trim();
  });
  document.querySelectorAll('#step-form select').forEach(s => {
    state.formData[s.id] = s.value;
  });
  document.querySelectorAll('#step-form textarea').forEach(t => {
    state.formData[t.id] = t.value.trim();
  });

  const groups = {};
  document.querySelectorAll('#step-form input[type="checkbox"]').forEach(cb => {
    if (!groups[cb.name]) groups[cb.name] = [];
    if (cb.checked) groups[cb.name].push(cb.value);
  });
  Object.assign(state.formData, groups);

  if (step.id === 'asset-info') {
    if (!state.formData.assetName)  { showToast('Asset name is required.', 'error'); return false; }
    if (!state.formData.assetOwner) { showToast('Asset owner is required.', 'error'); return false; }
    if (!state.formData.department) { showToast('Department is required.', 'error'); return false; }
    if (!state.formData.assetType)  { showToast('Please select an asset type.', 'error'); return false; }
  }
  if (step.id === 'criticality') {
    if (!state.formData.criticality)             { showToast('Please select asset criticality.', 'error'); return false; }
    if (!state.formData.businessImpact)          { showToast('Please select business impact.', 'error'); return false; }
    if (!state.formData.availabilityRequirement) { showToast('Please select availability requirement.', 'error'); return false; }
  }
  if (step.id === 'technical') {
    if (!state.formData.networkExposure) { showToast('Please select network exposure.', 'error'); return false; }
  }
  return true;
}

// ─── API Calls ────────────────────────────────────────────────
async function submitAssessment() {
  showLoading(true);
  try {
    const res = await auth.fetchWithAuth(`${API_BASE}/assessments`, {
      method: 'POST',
      body: JSON.stringify(buildPayload())
    });
    if (!res.ok) throw new Error(await res.text());
    // Results are sent to the security team — redirect user to thank-you page
    window.location.replace('/pages/thankyou.html');
  } catch (err) {
    showToast('Failed to submit: ' + err.message, 'error');
  } finally {
    showLoading(false);
  }
}

function buildPayload() {
  const f = state.formData;
  return {
    assetName:                f.assetName,
    assetOwner:               f.assetOwner,
    department:               f.department,
    assetType:                f.assetType,
    criticality:              f.criticality,
    networkExposure:          f.networkExposure,
    dataTypes:                f.dataTypes || [],
    complianceFrameworks:     f.complianceFrameworks || [],
    systemTypes:              f.systemTypes || [],
    businessImpact:           f.businessImpact,
    availabilityRequirement:  f.availabilityRequirement,
    internetFacing:           f.internetFacing === true,
    thirdPartyIntegrations:   f.thirdPartyIntegrations === true,
    customDeveloped:          f.customDeveloped === true,
    recentChanges:            f.recentChanges === true,
    previouslyAssessed:       f.previouslyAssessed === true,
    existingSecurityControls: f.existingSecurityControls === true,
    additionalContext:        f.additionalContext || ''
  };
}

async function loadHistory() {
  try {
    const res = await auth.fetchWithAuth(`${API_BASE}/assessments`);
    if (!res.ok) return;
    state.historyItems = await res.json();
  } catch { /* offline */ }
}

function resetForm() {
  state.formData = {};
  state.currentStep = 0;
  state.view = 'questionnaire';
  render();
}

// ─── Navigation ───────────────────────────────────────────────
function bindNav() {
  document.querySelectorAll('.nav-btn[data-view]').forEach(btn => {
    btn.addEventListener('click', async () => {
      const view = btn.dataset.view;
      if (view === 'history') {
        showLoading(true);
        await loadHistory();
        showLoading(false);
      }
      state.view = view;
      render();
      window.scrollTo(0, 0);
    });
  });

  document.getElementById('btn-logout')?.addEventListener('click', () => auth.logout());

  // Show logged-in user's name
  const user = auth.getUser();
  const greeting = document.getElementById('user-greeting');
  if (user && greeting) greeting.textContent = user.fullName || user.email;
}

// ─── Push Notifications ───────────────────────────────────────
function urlBase64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - base64String.length % 4) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const raw = atob(base64);
  return Uint8Array.from([...raw].map(c => c.charCodeAt(0)));
}

async function initPush() {
  if (!('serviceWorker' in navigator) || !('PushManager' in window)) return;

  // Check if the server has push configured
  let vapidPublicKey;
  try {
    const res = await auth.fetchWithAuth(`${API_BASE}/push/vapid-key`);
    if (!res.ok) return; // push not configured on server
    const data = await res.json();
    vapidPublicKey = data.publicKey;
  } catch { return; }

  const btn = document.getElementById('btn-notify');
  if (!btn) return;
  btn.style.display = '';

  const reg = await navigator.serviceWorker.ready;
  const existing = await reg.pushManager.getSubscription();

  if (existing) {
    updateNotifyBtn(btn, true);
  } else {
    updateNotifyBtn(btn, false);
  }

  btn.addEventListener('click', async () => {
    const current = await reg.pushManager.getSubscription();
    if (current) {
      // Unsubscribe
      await current.unsubscribe();
      await auth.fetchWithAuth(`${API_BASE}/push/unsubscribe`, {
        method: 'POST',
        body: JSON.stringify({ endpoint: current.endpoint })
      });
      updateNotifyBtn(btn, false);
      showToast('Push notifications disabled.', 'info');
    } else {
      // Subscribe
      const permission = await Notification.requestPermission();
      if (permission !== 'granted') {
        showToast('Notification permission denied.', 'error');
        return;
      }
      try {
        const sub = await reg.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: urlBase64ToUint8Array(vapidPublicKey)
        });
        await auth.fetchWithAuth(`${API_BASE}/push/subscribe`, {
          method: 'POST',
          body: JSON.stringify(sub)
        });
        updateNotifyBtn(btn, true);
        showToast('Push notifications enabled!', 'success');
      } catch (err) {
        showToast('Could not enable notifications: ' + err.message, 'error');
      }
    }
  });
}

function updateNotifyBtn(btn, subscribed) {
  const label = document.getElementById('notify-label');
  if (subscribed) {
    btn.title = 'Disable push notifications';
    btn.style.color = 'var(--primary)';
    if (label) label.textContent = ' On';
  } else {
    btn.title = 'Enable push notifications';
    btn.style.color = '';
    if (label) label.textContent = ' Notify';
  }
}

// ─── Service Worker ───────────────────────────────────────────
function registerSW() {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js').catch(() => {});
  }
}

// ─── Init ─────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  bindNav();
  render();
  registerSW();
  initPush();
});

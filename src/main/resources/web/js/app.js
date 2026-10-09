/**
 * AI ResearchHub - Main Application Logic
 * Single Page Application Controller
 */

const App = {
  currentUser: null,
  currentView: 'dashboard',
  selectedProject: null,
  activeTrainingInterval: null,

  async init() {
    this.setupListeners();
    try {
      const resp = await API.auth.me();
      this.currentUser = resp.data;
    } catch (e) {
      // Auto login as default researcher
      try {
        const loginResp = await API.auth.login('vishal', 'Vishal@123');
        this.currentUser = loginResp.data;
      } catch (err) {
        console.error('Failed auto-login:', err);
      }
    }
    this.updateUserUI();
    this.navigate('dashboard');
  },

  setupListeners() {
    // Sidebar nav items
    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', () => {
        const view = item.getAttribute('data-view');
        if (view) this.navigate(view);
      });
    });

    // Logout button
    document.getElementById('btn-logout').addEventListener('click', async () => {
      await API.auth.logout();
      this.showToast('Logged out successfully', 'success');
      setTimeout(() => location.reload(), 600);
    });

    // Global search
    document.getElementById('global-search').addEventListener('input', (e) => {
      const q = e.target.value.toLowerCase();
      const rows = document.querySelectorAll('table.data-table tbody tr');
      rows.forEach(r => {
        r.style.display = r.textContent.toLowerCase().includes(q) ? '' : 'none';
      });
    });
  },

  updateUserUI() {
    if (!this.currentUser) return;
    const nameEl = document.getElementById('sidebar-user-name');
    const roleEl = document.getElementById('sidebar-user-role');
    const avatarEl = document.getElementById('sidebar-user-avatar');

    nameEl.textContent = this.currentUser.fullName || this.currentUser.username;
    avatarEl.textContent = (this.currentUser.fullName || this.currentUser.username).charAt(0).toUpperCase();

    if (this.currentUser.role === 'ADMIN') {
      roleEl.textContent = 'Administrator';
      roleEl.className = 'user-role-badge role-admin';
      document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'flex');
      document.getElementById('switch-admin').classList.add('active');
      document.getElementById('switch-researcher').classList.remove('active');
    } else {
      roleEl.textContent = 'Researcher';
      roleEl.className = 'user-role-badge role-researcher';
      document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'none');
      document.getElementById('switch-researcher').classList.add('active');
      document.getElementById('switch-admin').classList.remove('active');
    }
  },

  async loginDemo(username) {
    try {
      const pass = username === 'admin' ? 'Admin@123' : 'Vishal@123';
      const resp = await API.auth.login(username, pass);
      this.currentUser = resp.data;
      this.updateUserUI();
      this.showToast(`Switched account to ${this.currentUser.fullName}`, 'success');
      this.navigate(this.currentUser.role === 'ADMIN' ? 'admin-dashboard' : 'dashboard');
    } catch (e) {
      this.showToast('Switch error: ' + e.message, 'error');
    }
  },

  navigate(viewName, params = {}) {
    this.currentView = viewName;
    document.querySelectorAll('.nav-item').forEach(item => {
      if (item.getAttribute('data-view') === viewName) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });

    const content = document.getElementById('content-area');
    content.innerHTML = '<div style="text-align: center; padding: 60px;"><div class="stat-value">⏳ Loading...</div></div>';

    switch (viewName) {
      case 'dashboard':
        this.renderDashboard();
        break;
      case 'admin-dashboard':
        this.renderAdminDashboard();
        break;
      case 'datasets':
        this.renderDatasets();
        break;
      case 'model-lab':
        this.renderModelLab(params);
        break;
      case 'experiments':
        this.renderExperiments();
        break;
      case 'assistant':
        this.renderAssistant();
        break;
      case 'projects':
        this.renderProjects();
        break;
      case 'users':
        this.renderUsers();
        break;
      case 'resources':
        this.renderResources();
        break;
      case 'audit-logs':
        this.renderAuditLogs();
        break;
      case 'settings':
        this.renderSettings();
        break;
      default:
        this.renderDashboard();
    }
  },

  // --- 1. RESEARCHER DASHBOARD ---
  async renderDashboard() {
    try {
      const [datasetsResp, jobsResp, expsResp, projectsResp, metricsResp] = await Promise.all([
        API.datasets.list(),
        API.training.list(),
        API.experiments.list(),
        API.projects.list(),
        API.usage.getMetrics()
      ]);

      const datasets = datasetsResp.data || [];
      const jobs = jobsResp.data || [];
      const experiments = expsResp.data || [];
      const projects = projectsResp.data || [];
      const metrics = metricsResp.data || {};

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <h2>Researcher Intelligence Workspace</h2>
            <p>Welcome back, ${this.currentUser.fullName}! Manage machine learning pipelines, experiments, and datasets.</p>
          </div>
          <div style="display: flex; gap: 10px;">
            <button class="btn btn-primary" onclick="App.navigate('model-lab')">
              <span>🚀</span> Model Lab
            </button>
            <button class="btn btn-secondary" onclick="App.openUploadDatasetModal()">
              <span>📁</span> Upload Data
            </button>
          </div>
        </div>

        <!-- Metric Stat Cards -->
        <div class="grid-cols-4">
          <div class="stat-card">
            <div class="stat-content">
              <h4>Active Datasets</h4>
              <div class="stat-value" style="color: var(--accent-cyan);">${datasets.length}</div>
              <div class="stat-subtitle">Available for ML training</div>
            </div>
            <div class="stat-icon" style="background: rgba(6, 182, 212, 0.15); color: var(--accent-cyan);">📁</div>
          </div>

          <div class="stat-card">
            <div class="stat-content">
              <h4>Completed Experiments</h4>
              <div class="stat-value" style="color: var(--accent-indigo);">${experiments.length}</div>
              <div class="stat-subtitle">Evaluated runs & models</div>
            </div>
            <div class="stat-icon" style="background: rgba(99, 102, 241, 0.15); color: var(--accent-indigo);">🧪</div>
          </div>

          <div class="stat-card">
            <div class="stat-content">
              <h4>Active Projects</h4>
              <div class="stat-value" style="color: var(--accent-emerald);">${projects.length}</div>
              <div class="stat-subtitle">Research collaborations</div>
            </div>
            <div class="stat-icon" style="background: rgba(16, 185, 129, 0.15); color: var(--accent-emerald);">🔬</div>
          </div>

          <div class="stat-card">
            <div class="stat-content">
              <h4>Server CPU Load</h4>
              <div class="stat-value" style="color: var(--accent-purple);">${metrics.availableProcessors || 8} Cores</div>
              <div class="stat-subtitle">JVM Heap: ${metrics.heapMemoryUsedMb || 45} MB</div>
            </div>
            <div class="stat-icon" style="background: rgba(139, 92, 246, 0.15); color: var(--accent-purple);">⚡</div>
          </div>
        </div>

        <!-- Quick Launch Actions & Recent Experiments -->
        <div class="grid-cols-2">
          <!-- Quick Action Card -->
          <div class="card">
            <div class="card-header">
              <div class="card-title"><span>⚡</span> Quick Research Actions</div>
            </div>
            <div class="card-body" style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px;">
              <button class="btn btn-secondary" style="padding: 16px; justify-content: flex-start; text-align: left;" onclick="App.navigate('model-lab')">
                <span style="font-size: 22px;">⚙️</span>
                <div>
                  <div style="font-weight: 700;">Train ML Model</div>
                  <div style="font-size: 11.5px; color: var(--text-muted);">Regression, CART, Ensembles</div>
                </div>
              </button>

              <button class="btn btn-secondary" style="padding: 16px; justify-content: flex-start; text-align: left;" onclick="App.openUploadDatasetModal()">
                <span style="font-size: 22px;">📤</span>
                <div>
                  <div style="font-weight: 700;">Import CSV Dataset</div>
                  <div style="font-size: 11.5px; color: var(--text-muted);">Summary stats & heatmap</div>
                </div>
              </button>

              <button class="btn btn-secondary" style="padding: 16px; justify-content: flex-start; text-align: left;" onclick="App.navigate('assistant')">
                <span style="font-size: 22px;">🤖</span>
                <div>
                  <div style="font-weight: 700;">AI Research Chatbot</div>
                  <div style="font-size: 11.5px; color: var(--text-muted);">Algorithm tips & code generator</div>
                </div>
              </button>

              <button class="btn btn-secondary" style="padding: 16px; justify-content: flex-start; text-align: left;" onclick="App.openCreateProjectModal()">
                <span style="font-size: 22px;">👥</span>
                <div>
                  <div style="font-weight: 700;">New Research Project</div>
                  <div style="font-size: 11.5px; color: var(--text-muted);">Assign members & tasks</div>
                </div>
              </button>
            </div>
          </div>

          <!-- Recent Experiments -->
          <div class="card">
            <div class="card-header">
              <div class="card-title"><span>🧪</span> Recent Experiments</div>
              <button class="btn btn-secondary btn-sm" onclick="App.navigate('experiments')">View All</button>
            </div>
            <div class="card-body" style="padding: 0;">
              <div class="table-responsive">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Experiment</th>
                      <th>Algorithm</th>
                      <th>Metric</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${experiments.length === 0 ? `
                      <tr><td colspan="4" style="text-align: center; padding: 24px;">No experiments yet. Run a model in the Model Lab!</td></tr>
                    ` : experiments.slice(0, 4).map(e => `
                      <tr>
                        <td style="font-weight: 600; color: #fff;">${e.experimentName}</td>
                        <td><span class="badge badge-info">${e.algorithm.displayName || e.algorithm}</span></td>
                        <td><span class="badge badge-success">${e.primaryMetricName}: ${e.primaryMetricValue}</span></td>
                        <td>
                          <button class="btn btn-secondary btn-sm" onclick="App.viewExperimentDetails(${e.id})">Inspect</button>
                        </td>
                      </tr>
                    `).join('')}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- Active Datasets Overview -->
        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>📁</span> Available Research Datasets</div>
            <button class="btn btn-secondary btn-sm" onclick="App.navigate('datasets')">Manage Datasets</button>
          </div>
          <div class="card-body" style="padding: 0;">
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Dataset Name</th>
                    <th>Rows</th>
                    <th>Columns</th>
                    <th>Size</th>
                    <th>Uploader</th>
                    <th>Quick Actions</th>
                  </tr>
                </thead>
                <tbody>
                  ${datasets.map(d => `
                    <tr>
                      <td style="font-weight: 600; color: #fff;">${d.name}</td>
                      <td>${d.rowCount}</td>
                      <td>${d.columnCount}</td>
                      <td>${(d.fileSize / 1024).toFixed(1)} KB</td>
                      <td>${d.uploaderName}</td>
                      <td>
                        <button class="btn btn-secondary btn-sm" onclick="App.analyzeDataset(${d.id})">🔍 Inspect Stats</button>
                        <button class="btn btn-primary btn-sm" onclick="App.navigate('model-lab', { datasetId: ${d.id} })">⚙️ Train Model</button>
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;
    } catch (err) {
      this.showToast('Error loading dashboard: ' + err.message, 'error');
    }
  },

  // --- 2. ADMIN DASHBOARD ---
  async renderAdminDashboard() {
    try {
      const [statsResp, usersResp, resourcesResp, logsResp] = await Promise.all([
        API.admin.getStats(),
        API.admin.getUsers(),
        API.admin.getResources(),
        API.admin.getAuditLogs()
      ]);

      const stats = statsResp.data || {};
      const users = usersResp.data || [];
      const resources = resourcesResp.data || [];
      const logs = logsResp.data || [];

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <h2>Platform Administration & Governance</h2>
            <p>System-wide governance, user account management, resource allocations, and audit logs.</p>
          </div>
          <button class="btn btn-primary" onclick="App.openCreateUserModal()">
            <span>➕</span> Provision User
          </button>
        </div>

        <!-- Metric Stat Cards -->
        <div class="grid-cols-4">
          <div class="stat-card">
            <div class="stat-content">
              <h4>Total Users</h4>
              <div class="stat-value" style="color: var(--accent-indigo);">${stats.totalUsers || 0}</div>
              <div class="stat-subtitle">${stats.activeResearchers || 0} Active Researchers</div>
            </div>
            <div class="stat-icon" style="background: rgba(99, 102, 241, 0.15); color: var(--accent-indigo);">👥</div>
          </div>

          <div class="stat-card">
            <div class="stat-content">
              <h4>Total Projects</h4>
              <div class="stat-value" style="color: var(--accent-emerald);">${stats.totalProjects || 0}</div>
              <div class="stat-subtitle">Research projects active</div>
            </div>
            <div class="stat-icon" style="background: rgba(16, 185, 129, 0.15); color: var(--accent-emerald);">🔬</div>
          </div>

          <div class="stat-card">
            <div class="stat-content">
              <h4>Total Datasets</h4>
              <div class="stat-value" style="color: var(--accent-cyan);">${stats.totalDatasets || 0}</div>
              <div class="stat-subtitle">${stats.storageUsageMb || 0} MB Storage Consumed</div>
            </div>
            <div class="stat-icon" style="background: rgba(6, 182, 212, 0.15); color: var(--accent-cyan);">💾</div>
          </div>

          <div class="stat-card">
            <div class="stat-content">
              <h4>Training Jobs</h4>
              <div class="stat-value" style="color: var(--accent-purple);">${stats.totalTrainingJobs || 0}</div>
              <div class="stat-subtitle">${stats.totalExperiments || 0} Saved Experiments</div>
            </div>
            <div class="stat-icon" style="background: rgba(139, 92, 246, 0.15); color: var(--accent-purple);">⚡</div>
          </div>
        </div>

        <!-- User Accounts Table -->
        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>👥</span> User Accounts Directory</div>
          </div>
          <div class="card-body" style="padding: 0;">
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>User</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Compute Quota</th>
                    <th>Storage Limit</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  ${users.map(u => `
                    <tr>
                      <td>#${u.id}</td>
                      <td style="font-weight: 600; color: #fff;">${u.fullName} (${u.username})</td>
                      <td>${u.email}</td>
                      <td>
                        <span class="user-role-badge ${u.role === 'ADMIN' ? 'role-admin' : 'role-researcher'}">${u.role}</span>
                      </td>
                      <td>
                        <span class="badge ${u.active ? 'badge-success' : 'badge-danger'}">
                          ${u.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td>${u.computeHoursQuota} hrs</td>
                      <td>${u.storageLimitMb} MB</td>
                      <td>
                        <button class="btn btn-secondary btn-sm" onclick="App.toggleUserStatus(${u.id}, ${!u.active})">
                          ${u.active ? 'Deactivate' : 'Activate'}
                        </button>
                        <button class="btn btn-secondary btn-sm" onclick="App.changeUserRole(${u.id}, '${u.role === 'ADMIN' ? 'RESEARCHER' : 'ADMIN'}')">
                          Toggle Role
                        </button>
                        ${u.id !== this.currentUser.id ? `
                          <button class="btn btn-danger btn-sm" onclick="App.deleteUser(${u.id})">Delete</button>
                        ` : ''}
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Audit Logs -->
        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>📜</span> System Audit Logs</div>
          </div>
          <div class="card-body" style="padding: 0;">
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>User</th>
                    <th>Action</th>
                    <th>Details</th>
                    <th>IP Address</th>
                  </tr>
                </thead>
                <tbody>
                  ${logs.slice(0, 8).map(l => `
                    <tr>
                      <td style="font-family: 'JetBrains Mono', monospace; font-size: 12px;">${l.createdAt}</td>
                      <td style="font-weight: 600;">${l.username}</td>
                      <td><span class="badge badge-purple">${l.actionType}</span></td>
                      <td>${l.details}</td>
                      <td style="color: var(--text-muted); font-size: 12px;">${l.ipAddress}</td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;
    } catch (err) {
      this.showToast('Error loading admin dashboard: ' + err.message, 'error');
    }
  },

  // --- 3. DATASET MANAGEMENT ---
  async renderDatasets() {
    try {
      const resp = await API.datasets.list();
      const datasets = resp.data || [];

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <h2>Dataset Management Module</h2>
            <p>Upload, validate, inspect summary statistics, and generate correlation heatmaps for machine learning.</p>
          </div>
          <button class="btn btn-primary" onclick="App.openUploadDatasetModal()">
            <span>➕</span> Upload New CSV
          </button>
        </div>

        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>📁</span> Managed Research Datasets (${datasets.length})</div>
          </div>
          <div class="card-body" style="padding: 0;">
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Dataset Name & Description</th>
                    <th>Filename</th>
                    <th>Rows</th>
                    <th>Columns</th>
                    <th>Size</th>
                    <th>Uploaded By</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  ${datasets.map(d => `
                    <tr>
                      <td>
                        <div style="font-weight: 600; color: #fff; font-size: 14px;">${d.name}</div>
                        <div style="font-size: 12px; color: var(--text-secondary); max-width: 320px;">${d.description || 'No description provided.'}</div>
                      </td>
                      <td style="font-family: 'JetBrains Mono', monospace; font-size: 12px;">${d.filename}</td>
                      <td><span class="badge badge-info">${d.rowCount} rows</span></td>
                      <td><span class="badge badge-purple">${d.columnCount} cols</span></td>
                      <td>${(d.fileSize / 1024).toFixed(1)} KB</td>
                      <td>${d.uploaderName}</td>
                      <td>
                        <button class="btn btn-secondary btn-sm" onclick="App.analyzeDataset(${d.id})">🔍 Inspect & Stats</button>
                        <button class="btn btn-primary btn-sm" onclick="App.navigate('model-lab', { datasetId: ${d.id} })">⚙️ Train Model</button>
                        <button class="btn btn-danger btn-sm" onclick="App.deleteDataset(${d.id})">🗑️</button>
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;
    } catch (err) {
      this.showToast('Error loading datasets: ' + err.message, 'error');
    }
  },

  openUploadDatasetModal() {
    const body = `
      <form id="upload-dataset-form">
        <div class="form-group">
          <label class="form-label">Dataset Display Name</label>
          <input type="text" id="upload-name" class="form-control" placeholder="e.g. Credit Card Risk Assessment" required>
        </div>
        <div class="form-group">
          <label class="form-label">Description / Research Hypothesis</label>
          <textarea id="upload-desc" class="form-control" rows="2" placeholder="Explain the features and target variable..."></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Select CSV File</label>
          <input type="file" id="upload-file" class="form-control" accept=".csv" required>
        </div>
        <div style="font-size: 12px; color: var(--text-muted); margin-top: 6px;">
          Note: CSV structure is validated server-side by Java DataFrame parser.
        </div>
      </form>
    `;

    const footer = `
      <button class="btn btn-secondary" onclick="App.closeModal()">Cancel</button>
      <button class="btn btn-primary" onclick="App.submitDatasetUpload()">Upload & Parse</button>
    `;

    this.openModal('Upload Dataset (CSV)', body, footer);
  },

  async submitDatasetUpload() {
    const name = document.getElementById('upload-name').value;
    const desc = document.getElementById('upload-desc').value;
    const fileInput = document.getElementById('upload-file');

    if (!fileInput.files || fileInput.files.length === 0) {
      this.showToast('Please select a CSV file.', 'error');
      return;
    }

    const file = fileInput.files[0];
    const reader = new FileReader();

    reader.onload = async (e) => {
      try {
        const content = e.target.result;
        await API.datasets.upload(name, desc, file.name, content);
        this.closeModal();
        this.showToast('Dataset uploaded and analyzed successfully!', 'success');
        this.renderDatasets();
      } catch (err) {
        this.showToast('Upload error: ' + err.message, 'error');
      }
    };

    reader.readAsText(file);
  },

  async analyzeDataset(datasetId) {
    try {
      this.openModal('Analyzing Dataset...', '<div style="text-align: center; padding: 40px;">⏳ Calculating summary statistics and Pearson correlation matrix...</div>', '');
      const resp = await API.datasets.analyze(datasetId);
      const summary = resp.data;

      let body = `
        <div style="display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap;">
          <div class="badge badge-info">Rows: ${summary.rowCount}</div>
          <div class="badge badge-purple">Columns: ${summary.columnCount}</div>
          <div class="badge badge-warning">Duplicates: ${summary.duplicateRows}</div>
        </div>

        <h4 style="margin: 16px 0 8px; font-size: 14px; text-transform: uppercase; color: var(--text-muted);">First 15 Rows Preview</h4>
        <div class="table-responsive" style="max-height: 240px; margin-bottom: 24px; border: 1px solid var(--border-color); border-radius: 8px;">
          <table class="data-table">
            <thead>
              <tr>${summary.columnNames.map(c => `<th>${c}</th>`).join('')}</tr>
            </thead>
            <tbody>
              ${summary.previewRows.map(row => `
                <tr>${summary.columnNames.map(c => `<td>${row[c] !== undefined ? row[c] : ''}</td>`).join('')}</tr>
              `).join('')}
            </tbody>
          </table>
        </div>

        <h4 style="margin: 16px 0 8px; font-size: 14px; text-transform: uppercase; color: var(--text-muted);">Descriptive Statistics</h4>
        <div class="table-responsive" style="max-height: 240px; margin-bottom: 24px; border: 1px solid var(--border-color); border-radius: 8px;">
          <table class="data-table">
            <thead>
              <tr>
                <th>Feature</th>
                <th>Mean</th>
                <th>Std Dev</th>
                <th>Min</th>
                <th>25%</th>
                <th>Median</th>
                <th>75%</th>
                <th>Max</th>
              </tr>
            </thead>
            <tbody>
              ${Object.entries(summary.descriptiveStats || {}).map(([col, s]) => `
                <tr>
                  <td style="font-weight: 600; color: #fff;">${col}</td>
                  <td>${s.mean}</td>
                  <td>${s.std}</td>
                  <td>${s.min}</td>
                  <td>${s['25%']}</td>
                  <td>${s['50%']}</td>
                  <td>${s['75%']}</td>
                  <td>${s.max}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>

        <h4 style="margin: 16px 0 8px; font-size: 14px; text-transform: uppercase; color: var(--text-muted);">Pearson Correlation Matrix</h4>
        <div id="corr-heatmap-container" style="margin-bottom: 20px;"></div>
      `;

      const footer = `
        <button class="btn btn-secondary" onclick="App.closeModal()">Close</button>
        <button class="btn btn-primary" onclick="App.closeModal(); App.navigate('model-lab', { datasetId: ${datasetId} })">⚙️ Proceed to Model Lab</button>
      `;

      this.openModal('Dataset Statistical Audit & Correlation', body, footer);
      setTimeout(() => {
        ChartUI.renderCorrelationMatrix('corr-heatmap-container', summary.correlationMatrix);
      }, 100);

    } catch (err) {
      this.showToast('Analysis error: ' + err.message, 'error');
    }
  },

  async deleteDataset(id) {
    if (!confirm('Are you sure you want to delete this dataset?')) return;
    try {
      await API.datasets.delete(id);
      this.showToast('Dataset deleted.', 'success');
      this.renderDatasets();
    } catch (err) {
      this.showToast('Delete error: ' + err.message, 'error');
    }
  },

  // --- 4. MODEL LAB (ML MODEL TRAINING) ---
  async renderModelLab(params = {}) {
    try {
      const [datasetsResp, projectsResp] = await Promise.all([
        API.datasets.list(),
        API.projects.list()
      ]);

      const datasets = datasetsResp.data || [];
      const projects = projectsResp.data || [];
      const preSelectedDs = params.datasetId || (datasets.length > 0 ? datasets[0].id : null);

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <h2>Model Lab – Interactive Machine Learning Trainer</h2>
            <p>Configure model architectures, train/test split, hyperparameter tuning, and dispatch multithreaded training jobs.</p>
          </div>
        </div>

        <div class="grid-cols-2">
          <!-- Configuration Card -->
          <div class="card">
            <div class="card-header">
              <div class="card-title"><span>⚙️</span> Training Configuration</div>
            </div>
            <div class="card-body">
              <form id="train-model-form" onsubmit="event.preventDefault(); App.startTrainingJob();">
                <div class="form-group">
                  <label class="form-label">Training Run Name</label>
                  <input type="text" id="train-job-name" class="form-control" placeholder="e.g. Housing Multi-Regression Alpha" required>
                </div>

                <div class="form-group">
                  <label class="form-label">Research Project Association</label>
                  <select id="train-project-id" class="form-control">
                    <option value="">-- Standalone Experiment --</option>
                    ${projects.map(p => `<option value="${p.id}">${p.title}</option>`).join('')}
                  </select>
                </div>

                <div class="form-group">
                  <label class="form-label">Select Dataset</label>
                  <select id="train-dataset-id" class="form-control" onchange="App.onDatasetSelectedForTraining(this.value)" required>
                    ${datasets.map(d => `<option value="${d.id}" ${d.id == preSelectedDs ? 'selected' : ''}>${d.name} (${d.rowCount} rows)</option>`).join('')}
                  </select>
                </div>

                <div class="form-group">
                  <label class="form-label">Machine Learning Algorithm</label>
                  <select id="train-algorithm" class="form-control" onchange="App.onAlgorithmChanged(this.value)">
                    <option value="LINEAR_REGRESSION">Linear Regression (Supervised Regression)</option>
                    <option value="LOGISTIC_REGRESSION">Logistic Regression (Supervised Classification)</option>
                    <option value="DECISION_TREE">Decision Tree (CART Classifier)</option>
                    <option value="RANDOM_FOREST">Random Forest (Ensemble Bagging)</option>
                    <option value="KNN">K-Nearest Neighbors (Instance-based)</option>
                    <option value="KMEANS">K-Means Clustering (Unsupervised)</option>
                  </select>
                </div>

                <div class="form-group" id="target-col-group">
                  <label class="form-label">Target Column (y)</label>
                  <select id="train-target-col" class="form-control"></select>
                </div>

                <div class="form-group">
                  <label class="form-label">Feature Columns (X)</label>
                  <div id="train-features-container" style="display: flex; flex-wrap: wrap; gap: 8px; max-height: 120px; overflow-y: auto; padding: 10px; background: var(--bg-input); border: 1px solid var(--border-color); border-radius: 8px;"></div>
                </div>

                <div class="form-group">
                  <label class="form-label">Train-Test Split Ratio: <span id="split-val-display" style="color: var(--accent-indigo); font-weight: 700;">80% / 20%</span></label>
                  <input type="range" id="train-split" min="50" max="90" value="80" class="form-control" oninput="document.getElementById('split-val-display').textContent = this.value + '% / ' + (100 - this.value) + '%'">
                </div>

                <!-- Dynamic Hyperparameters -->
                <div id="hyperparams-container" style="background: rgba(0, 0, 0, 0.2); padding: 14px; border-radius: 8px; margin-bottom: 20px;">
                  <h5 style="font-size: 12.5px; text-transform: uppercase; color: var(--text-muted); margin-bottom: 10px;">Hyperparameters Tuning</h5>
                  <div id="hyperparams-fields" class="grid-cols-2" style="margin-bottom: 0;"></div>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center; padding: 12px;">
                  <span>🚀</span> Launch Multithreaded Training Job
                </button>
              </form>
            </div>
          </div>

          <!-- Live Progress / Output Card -->
          <div class="card">
            <div class="card-header">
              <div class="card-title"><span>📊</span> Live Evaluation Results</div>
              <span id="job-status-badge" class="badge badge-info" style="display: none;">Idle</span>
            </div>
            <div class="card-body" id="training-output-panel">
              <div style="text-align: center; padding: 60px; color: var(--text-muted);">
                <div style="font-size: 40px; margin-bottom: 12px;">🧪</div>
                <h4>Configure and dispatch a model to see real evaluation metrics, confusion matrix, and feature importances.</h4>
              </div>
            </div>
          </div>
        </div>
      `;

      if (preSelectedDs) {
        this.onDatasetSelectedForTraining(preSelectedDs);
      }
      this.onAlgorithmChanged('LINEAR_REGRESSION');

    } catch (err) {
      this.showToast('Error loading Model Lab: ' + err.message, 'error');
    }
  },

  async onDatasetSelectedForTraining(datasetId) {
    try {
      const resp = await API.datasets.analyze(datasetId);
      const summary = resp.data;
      const targetSelect = document.getElementById('train-target-col');
      const featContainer = document.getElementById('train-features-container');

      targetSelect.innerHTML = summary.columnNames.map(c => `<option value="${c}">${c}</option>`).join('');

      // Auto-select last numeric column as target
      if (summary.columnNames.length > 0) {
        targetSelect.value = summary.columnNames[summary.columnNames.length - 1];
      }

      featContainer.innerHTML = summary.columnNames.map((c, i) => `
        <label style="display: flex; align-items: center; gap: 6px; font-size: 12.5px; cursor: pointer; background: rgba(255,255,255,0.05); padding: 4px 8px; border-radius: 4px;">
          <input type="checkbox" name="feat-col" value="${c}" ${i < summary.columnNames.length - 1 ? 'checked' : ''}>
          ${c}
        </label>
      `).join('');

    } catch (err) {
      console.error(err);
    }
  },

  onAlgorithmChanged(algo) {
    const targetGroup = document.getElementById('target-col-group');
    const fields = document.getElementById('hyperparams-fields');

    if (algo === 'KMEANS') {
      targetGroup.style.display = 'none';
      fields.innerHTML = `
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Clusters (k)</label>
          <input type="number" id="param-k" class="form-control" value="3" min="2" max="10">
        </div>
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Max Iterations</label>
          <input type="number" id="param-maxIter" class="form-control" value="100" min="10" max="500">
        </div>
      `;
    } else if (algo === 'RANDOM_FOREST') {
      targetGroup.style.display = 'block';
      fields.innerHTML = `
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Trees (n_estimators)</label>
          <input type="number" id="param-nEstimators" class="form-control" value="15" min="1" max="100">
        </div>
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Max Tree Depth</label>
          <input type="number" id="param-maxDepth" class="form-control" value="5" min="1" max="20">
        </div>
      `;
    } else if (algo === 'DECISION_TREE') {
      targetGroup.style.display = 'block';
      fields.innerHTML = `
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Max Tree Depth</label>
          <input type="number" id="param-maxDepth" class="form-control" value="5" min="1" max="20">
        </div>
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Min Samples Split</label>
          <input type="number" id="param-minSamplesSplit" class="form-control" value="2" min="2" max="10">
        </div>
      `;
    } else if (algo === 'KNN') {
      targetGroup.style.display = 'block';
      fields.innerHTML = `
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Neighbors (k)</label>
          <input type="number" id="param-k" class="form-control" value="3" min="1" max="15">
        </div>
      `;
    } else {
      // Linear or Logistic
      targetGroup.style.display = 'block';
      fields.innerHTML = `
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Epochs</label>
          <input type="number" id="param-epochs" class="form-control" value="500" min="50" max="2000">
        </div>
        <div class="form-group">
          <label class="form-label" style="font-size: 11.5px;">Learning Rate</label>
          <input type="number" id="param-learningRate" class="form-control" value="0.05" step="0.01" min="0.001" max="1.0">
        </div>
      `;
    }
  },

  async startTrainingJob() {
    const jobName = document.getElementById('train-job-name').value;
    const projectId = document.getElementById('train-project-id').value;
    const datasetId = document.getElementById('train-dataset-id').value;
    const algorithm = document.getElementById('train-algorithm').value;
    const targetCol = document.getElementById('train-target-col').value;
    const splitRatio = parseFloat(document.getElementById('train-split').value) / 100.0;

    const checkedFeatures = [];
    document.querySelectorAll('input[name="feat-col"]:checked').forEach(c => checkedFeatures.push(c.value));

    if (checkedFeatures.length === 0) {
      this.showToast('Select at least one feature column.', 'error');
      return;
    }

    const hyperparams = {};
    if (document.getElementById('param-epochs')) hyperparams.epochs = parseInt(document.getElementById('param-epochs').value);
    if (document.getElementById('param-learningRate')) hyperparams.learningRate = parseFloat(document.getElementById('param-learningRate').value);
    if (document.getElementById('param-maxDepth')) hyperparams.maxDepth = parseInt(document.getElementById('param-maxDepth').value);
    if (document.getElementById('param-nEstimators')) hyperparams.nEstimators = parseInt(document.getElementById('param-nEstimators').value);
    if (document.getElementById('param-k')) hyperparams.k = parseInt(document.getElementById('param-k').value);

    const req = {
      jobName,
      projectId: projectId ? parseInt(projectId) : null,
      datasetId: parseInt(datasetId),
      algorithm,
      targetColumn: algorithm === 'KMEANS' ? null : targetCol,
      featureColumns: checkedFeatures,
      trainSplitRatio: splitRatio,
      hyperparameters: hyperparams
    };

    const outPanel = document.getElementById('training-output-panel');
    outPanel.innerHTML = `
      <div style="padding: 30px; text-align: center;">
        <h4 style="font-size: 16px; margin-bottom: 12px;">⚙️ Multithreaded Worker Running</h4>
        <div style="background: rgba(255,255,255,0.08); border-radius: 10px; height: 12px; width: 100%; overflow: hidden; margin: 16px 0;">
          <div id="live-progress-bar" style="background: linear-gradient(90deg, var(--accent-indigo), var(--accent-cyan)); height: 100%; width: 10%; transition: width 0.3s ease;"></div>
        </div>
        <p id="live-progress-text" style="font-size: 13px; color: var(--text-secondary);">Worker thread allocated. Training dataset partition...</p>
      </div>
    `;

    const badge = document.getElementById('job-status-badge');
    badge.style.display = 'inline-block';
    badge.className = 'badge badge-warning';
    badge.textContent = 'RUNNING';

    try {
      const resp = await API.training.start(req);
      const job = resp.data;
      this.pollTrainingProgress(job.id);
    } catch (err) {
      this.showToast('Training dispatch error: ' + err.message, 'error');
      badge.className = 'badge badge-danger';
      badge.textContent = 'FAILED';
    }
  },

  pollTrainingProgress(jobId) {
    if (this.activeTrainingInterval) clearInterval(this.activeTrainingInterval);

    this.activeTrainingInterval = setInterval(async () => {
      try {
        const resp = await API.training.get(jobId);
        const job = resp.data;

        const pBar = document.getElementById('live-progress-bar');
        const pText = document.getElementById('live-progress-text');
        const badge = document.getElementById('job-status-badge');

        if (pBar) pBar.style.width = Math.max(10, job.progressPercentage) + '%';
        if (pText) pText.textContent = `Job Status: ${job.status} (${job.progressPercentage}%)`;

        if (job.status === 'COMPLETED') {
          clearInterval(this.activeTrainingInterval);
          badge.className = 'badge badge-success';
          badge.textContent = 'COMPLETED';
          this.showToast('Model training finished successfully!', 'success');
          // Load latest experiment
          const exps = await API.experiments.list();
          const targetExp = exps.data.find(e => e.trainingJobId === jobId) || exps.data[0];
          if (targetExp) this.displayTrainingResults(targetExp.id);

        } else if (job.status === 'FAILED') {
          clearInterval(this.activeTrainingInterval);
          badge.className = 'badge badge-danger';
          badge.textContent = 'FAILED';
          this.showToast('Training failed: ' + job.errorDetails, 'error');
        }
      } catch (e) {
        clearInterval(this.activeTrainingInterval);
      }
    }, 400);
  },

  async displayTrainingResults(expId) {
    try {
      const resp = await API.experiments.get(expId);
      const exp = resp.data;
      const metrics = JSON.parse(exp.metricsJson || '{}');
      const cm = JSON.parse(exp.confusionMatrixJson || '[]');
      const featureImp = JSON.parse(exp.featureImportanceJson || '{}');
      const predictions = JSON.parse(exp.predictionsSummaryJson || '[]');

      const outPanel = document.getElementById('training-output-panel');
      outPanel.innerHTML = `
        <div style="padding: 10px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div>
              <h4 style="font-size: 16px; font-weight: 700; color: #fff;">${exp.experimentName}</h4>
              <span style="font-size: 12px; color: var(--text-muted);">${exp.algorithm} | ${exp.datasetName}</span>
            </div>
            <span class="badge badge-success" style="font-size: 14px; padding: 6px 12px;">
              ${exp.primaryMetricName}: ${exp.primaryMetricValue}
            </span>
          </div>

          <!-- Evaluation Metric Badges -->
          <div style="display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap;">
            ${Object.entries(metrics).map(([k, v]) => `
              <div class="stat-card" style="padding: 10px 14px; flex: 1; min-width: 110px;">
                <div class="stat-content">
                  <h4 style="font-size: 10.5px;">${k.toUpperCase()}</h4>
                  <div style="font-size: 18px; font-weight: 800; color: #fff;">${v}</div>
                </div>
              </div>
            `).join('')}
          </div>

          <!-- Feature Importance Chart -->
          ${Object.keys(featureImp).length > 0 ? `
            <h5 style="font-size: 13px; text-transform: uppercase; color: var(--text-muted); margin: 16px 0 8px;">Feature Importance Vector</h5>
            <canvas id="train-fi-chart" style="width: 100%; max-height: 200px; margin-bottom: 16px;"></canvas>
          ` : ''}

          <!-- Confusion Matrix (for classification) -->
          ${cm.length > 0 ? `
            <h5 style="font-size: 13px; text-transform: uppercase; color: var(--text-muted); margin: 16px 0 8px;">Confusion Matrix Validation</h5>
            <div id="train-cm-container"></div>
          ` : ''}

          <!-- Predictions Table Sample -->
          <h5 style="font-size: 13px; text-transform: uppercase; color: var(--text-muted); margin: 16px 0 8px;">Validation Predictions (Hold-out Test Sample)</h5>
          <div class="table-responsive" style="max-height: 180px; border: 1px solid var(--border-color); border-radius: 8px;">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Sample</th>
                  <th>Actual Target</th>
                  <th>Model Prediction</th>
                </tr>
              </thead>
              <tbody>
                ${predictions.map((p, i) => `
                  <tr>
                    <td>#${i + 1}</td>
                    <td style="font-weight: 600;">${p.actual}</td>
                    <td style="color: var(--accent-cyan); font-family: 'JetBrains Mono', monospace;">${p.predicted}</td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `;

      if (Object.keys(featureImp).length > 0) {
        ChartUI.renderBarChart('train-fi-chart', featureImp);
      }
      if (cm.length > 0) {
        ChartUI.renderConfusionMatrix('train-cm-container', cm);
      }

    } catch (err) {
      console.error(err);
    }
  },

  // --- 5. EXPERIMENTS & COMPARATOR ---
  async renderExperiments() {
    try {
      const resp = await API.experiments.list();
      const exps = resp.data || [];

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <h2>Experiment Tracking & Multi-Model Comparator</h2>
            <p>Reproducible experiment tracking, hyperparameter records, and side-by-side metric comparison.</p>
          </div>
          <button class="btn btn-primary" onclick="App.compareSelectedExperiments()">
            <span>⚖️</span> Compare Selected (2+)
          </button>
        </div>

        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>🧪</span> Logged Research Experiments (${exps.length})</div>
          </div>
          <div class="card-body" style="padding: 0;">
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th style="width: 40px;"><input type="checkbox" id="check-all-exp" onclick="App.toggleAllExpChecks(this.checked)"></th>
                    <th>Experiment Name</th>
                    <th>Dataset</th>
                    <th>Algorithm</th>
                    <th>Primary Metric</th>
                    <th>Created By</th>
                    <th>Timestamp</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  ${exps.map(e => `
                    <tr>
                      <td><input type="checkbox" class="exp-check" value="${e.id}"></td>
                      <td style="font-weight: 600; color: #fff;">${e.experimentName}</td>
                      <td>${e.datasetName}</td>
                      <td><span class="badge badge-info">${e.algorithm}</span></td>
                      <td><span class="badge badge-success">${e.primaryMetricName}: ${e.primaryMetricValue}</span></td>
                      <td>${e.createdByName}</td>
                      <td style="font-size: 12px; font-family: 'JetBrains Mono', monospace;">${e.createdAt}</td>
                      <td>
                        <button class="btn btn-secondary btn-sm" onclick="App.viewExperimentDetails(${e.id})">Inspect</button>
                        <button class="btn btn-danger btn-sm" onclick="App.deleteExperiment(${e.id})">🗑️</button>
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;
    } catch (err) {
      this.showToast('Error loading experiments: ' + err.message, 'error');
    }
  },

  toggleAllExpChecks(checked) {
    document.querySelectorAll('.exp-check').forEach(cb => cb.checked = checked);
  },

  async compareSelectedExperiments() {
    const selected = [];
    document.querySelectorAll('.exp-check:checked').forEach(cb => selected.push(parseInt(cb.value)));

    if (selected.length < 2) {
      this.showToast('Select at least 2 experiments to compare side by side.', 'error');
      return;
    }

    try {
      const resp = await API.experiments.compare(selected);
      const exps = resp.data.experiments || [];

      let body = `
        <h4 style="margin-bottom: 12px; font-size: 14px; text-transform: uppercase; color: var(--text-muted);">Primary Metric Performance Comparison</h4>
        <canvas id="comp-chart" style="width: 100%; height: 260px; margin-bottom: 24px;"></canvas>

        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Attribute</th>
                ${exps.map(e => `<th style="color: #fff; font-weight: 700;">${e.name}</th>`).join('')}
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style="font-weight: 600;">Algorithm</td>
                ${exps.map(e => `<td><span class="badge badge-info">${e.algorithm}</span></td>`).join('')}
              </tr>
              <tr>
                <td style="font-weight: 600;">Dataset</td>
                ${exps.map(e => `<td>${e.datasetName}</td>`).join('')}
              </tr>
              <tr>
                <td style="font-weight: 600;">Primary Score</td>
                ${exps.map(e => `<td><span class="badge badge-success">${e.primaryMetricName}: ${e.primaryMetricValue}</span></td>`).join('')}
              </tr>
              <tr>
                <td style="font-weight: 600;">Evaluation Metrics</td>
                ${exps.map(e => `<td><pre style="font-size: 11px;">${JSON.stringify(e.metrics, null, 2)}</pre></td>`).join('')}
              </tr>
              <tr>
                <td style="font-weight: 600;">Hyperparameters</td>
                ${exps.map(e => `<td><pre style="font-size: 11px;">${JSON.stringify(e.hyperparameters, null, 2)}</pre></td>`).join('')}
              </tr>
            </tbody>
          </table>
        </div>
      `;

      this.openModal('Side-by-Side Model Experiment Comparison', body, '<button class="btn btn-secondary" onclick="App.closeModal()">Close</button>');
      setTimeout(() => ChartUI.renderComparisonChart('comp-chart', exps), 100);

    } catch (err) {
      this.showToast('Comparison error: ' + err.message, 'error');
    }
  },

  async viewExperimentDetails(id) {
    try {
      const resp = await API.experiments.get(id);
      const exp = resp.data;
      const metrics = JSON.parse(exp.metricsJson || '{}');
      const cm = JSON.parse(exp.confusionMatrixJson || '[]');
      const featureImp = JSON.parse(exp.featureImportanceJson || '{}');

      let body = `
        <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
          <div>
            <div style="font-size: 16px; font-weight: 700; color: #fff;">${exp.experimentName}</div>
            <div style="font-size: 12.5px; color: var(--text-muted);">${exp.algorithm} | ${exp.datasetName}</div>
          </div>
          <span class="badge badge-success" style="font-size: 14px; padding: 6px 12px;">
            ${exp.primaryMetricName}: ${exp.primaryMetricValue}
          </span>
        </div>

        <div style="display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap;">
          ${Object.entries(metrics).map(([k, v]) => `
            <div class="stat-card" style="padding: 10px 14px; flex: 1; min-width: 100px;">
              <div class="stat-content">
                <h4 style="font-size: 10px;">${k.toUpperCase()}</h4>
                <div style="font-size: 17px; font-weight: 800; color: #fff;">${v}</div>
              </div>
            </div>
          `).join('')}
        </div>

        ${Object.keys(featureImp).length > 0 ? `
          <h5 style="font-size: 13px; text-transform: uppercase; color: var(--text-muted); margin: 16px 0 8px;">Feature Importance Vector</h5>
          <canvas id="exp-modal-fi-chart" style="width: 100%; max-height: 200px; margin-bottom: 16px;"></canvas>
        ` : ''}

        ${cm.length > 0 ? `
          <h5 style="font-size: 13px; text-transform: uppercase; color: var(--text-muted); margin: 16px 0 8px;">Confusion Matrix</h5>
          <div id="exp-modal-cm-container"></div>
        ` : ''}

        <h5 style="font-size: 13px; text-transform: uppercase; color: var(--text-muted); margin: 16px 0 8px;">Hyperparameters Configured</h5>
        <pre style="background: rgba(0,0,0,0.3); padding: 12px; border-radius: 6px; font-size: 12px;">${exp.hyperparametersJson || 'None'}</pre>
      `;

      this.openModal('Experiment Run Details', body, '<button class="btn btn-secondary" onclick="App.closeModal()">Close</button>');
      if (Object.keys(featureImp).length > 0) {
        setTimeout(() => ChartUI.renderBarChart('exp-modal-fi-chart', featureImp), 100);
      }
      if (cm.length > 0) {
        setTimeout(() => ChartUI.renderConfusionMatrix('exp-modal-cm-container', cm), 100);
      }

    } catch (err) {
      this.showToast('Error loading experiment: ' + err.message, 'error');
    }
  },

  async deleteExperiment(id) {
    if (!confirm('Are you sure you want to delete this experiment record?')) return;
    try {
      await API.experiments.delete(id);
      this.showToast('Experiment deleted', 'success');
      this.renderExperiments();
    } catch (err) {
      this.showToast('Delete error: ' + err.message, 'error');
    }
  },

  // --- 6. AI RESEARCH ASSISTANT ---
  async renderAssistant() {
    const convId = 'conv_' + (this.currentUser ? this.currentUser.id : 'guest');
    let history = [];
    try {
      const resp = await API.assistant.getHistory(convId);
      history = resp.data || [];
    } catch (ignored) {}

    const content = document.getElementById('content-area');
    content.innerHTML = `
      <div class="page-header">
        <div class="page-title">
          <h2>AI Research Assistant & Machine Learning Copilot</h2>
          <p>Consult with your research assistant on dataset algorithms, starter code, hypothesis formulation, and metric evaluation.</p>
        </div>
      </div>

      <div class="card chat-container">
        <!-- Suggestion Pills -->
        <div class="chat-pills" style="padding: 12px 18px; border-bottom: 1px solid var(--border-color);">
          <div class="chat-pill" onclick="App.sendAssistantPrompt('Generate AI & ML project ideas for B.Tech thesis', 'CHAT')">💡 Project Ideas</div>
          <div class="chat-pill" onclick="App.sendAssistantPrompt('Explain Random Forest and how it avoids overfitting', 'EXPLAIN_ALGORITHM')">🌲 Explain Random Forest</div>
          <div class="chat-pill" onclick="App.sendAssistantPrompt('Generate starter Python and Java training pipeline code', 'CODE_STARTER')">💻 Starter ML Code</div>
          <div class="chat-pill" onclick="App.sendAssistantPrompt('Explain accuracy, precision, recall, and F1-score', 'EXPLAIN_EXPERIMENT')">📊 Explain Metrics</div>
          <div class="chat-pill" onclick="App.sendAssistantPrompt('Summarize key findings of the research paper methodology', 'SUMMARIZE_DOC')">📄 Summarize Research Paper</div>
        </div>

        <!-- Chat Message Log -->
        <div class="chat-messages" id="chat-messages-container">
          <div class="chat-bubble assistant">
            Hello ${this.currentUser.fullName}! I am your **AI Research Assistant**. How can I support your model training and research today?
          </div>
          ${history.map(m => `
            <div class="chat-bubble ${m.senderRole}">
              ${this.formatMarkdown(m.content)}
            </div>
          `).join('')}
        </div>

        <!-- Input Bar -->
        <div class="chat-input-bar" style="padding: 14px 18px;">
          <input type="text" id="chat-input" placeholder="Ask questions about your datasets, ML algorithms, or code..." onkeydown="if(event.key === 'Enter') App.sendAssistantMessage()">
          <button class="btn btn-primary" onclick="App.sendAssistantMessage()">
            <span>➤</span> Send
          </button>
        </div>
      </div>
    `;

    setTimeout(() => {
      const el = document.getElementById('chat-messages-container');
      if (el) el.scrollTop = el.scrollHeight;
    }, 100);
  },

  async sendAssistantPrompt(text, actionType = 'CHAT') {
    const input = document.getElementById('chat-input');
    if (input) input.value = text;
    this.sendAssistantMessage(actionType);
  },

  async sendAssistantMessage(actionType = 'CHAT') {
    const input = document.getElementById('chat-input');
    const msg = input.value.trim();
    if (!msg) return;

    input.value = '';
    const container = document.getElementById('chat-messages-container');
    const convId = 'conv_' + (this.currentUser ? this.currentUser.id : 'guest');

    // Add user bubble
    const userBubble = document.createElement('div');
    userBubble.className = 'chat-bubble user';
    userBubble.textContent = msg;
    container.appendChild(userBubble);

    // Add loading bubble
    const loadingBubble = document.createElement('div');
    loadingBubble.className = 'chat-bubble assistant';
    loadingBubble.textContent = 'Thinking and analyzing...';
    container.appendChild(loadingBubble);
    container.scrollTop = container.scrollHeight;

    try {
      const resp = await API.assistant.chat({
        conversationId: convId,
        message: msg,
        actionType
      });
      loadingBubble.innerHTML = this.formatMarkdown(resp.data.content);
      container.scrollTop = container.scrollHeight;
    } catch (err) {
      loadingBubble.textContent = 'Assistant Error: ' + err.message;
    }
  },

  formatMarkdown(text) {
    if (!text) return '';
    let html = text
      .replace(/### (.*?)\n/g, '<h4 style="color: #fff; margin: 10px 0 6px;">$1</h4>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/```python([\s\S]*?)```/g, '<pre><code>$1</code><button class="btn btn-secondary btn-sm" style="margin-top: 6px;" onclick="navigator.clipboard.writeText(this.previousElementSibling.textContent); App.showToast(\'Code copied!\', \'success\')">Copy Code</button></pre>')
      .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
      .replace(/\n\n/g, '<br><br>')
      .replace(/\n/g, '<br>');
    return html;
  },

  // --- 7. PROJECTS & COLLABORATION ---
  async renderProjects() {
    try {
      const resp = await API.projects.list();
      const projects = resp.data || [];

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <h2>Research Projects & Collaboration Workspace</h2>
            <p>Collaborative project hubs, team member permissions, and research milestone Kanban task boards.</p>
          </div>
          <button class="btn btn-primary" onclick="App.openCreateProjectModal()">
            <span>➕</span> New Research Project
          </button>
        </div>

        <div class="grid-cols-2" style="margin-bottom: 32px;">
          ${projects.map(p => `
            <div class="card" style="margin-bottom: 0;">
              <div class="card-header">
                <div>
                  <h3 style="font-size: 16px; font-weight: 700; color: #fff;">${p.title}</h3>
                  <span style="font-size: 12px; color: var(--text-muted);">Lead: ${p.ownerName}</span>
                </div>
                <span class="badge ${p.status === 'ACTIVE' ? 'badge-success' : 'badge-warning'}">${p.status}</span>
              </div>
              <div class="card-body">
                <p style="font-size: 13.5px; color: var(--text-secondary); margin-bottom: 16px;">${p.description || 'No description provided.'}</p>
                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 14px;">
                  <span style="font-size: 12.5px; color: var(--text-muted);">👥 ${p.memberCount} Team Members</span>
                  <div>
                    <button class="btn btn-secondary btn-sm" onclick="App.openProjectWorkspace(${p.id})">Open Workspace</button>
                    ${(p.ownerId === this.currentUser.id || this.currentUser.role === 'ADMIN') ? `
                      <button class="btn btn-danger btn-sm" onclick="App.deleteProject(${p.id})">🗑️</button>
                    ` : ''}
                  </div>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      `;
    } catch (err) {
      this.showToast('Error loading projects: ' + err.message, 'error');
    }
  },

  openCreateProjectModal() {
    const body = `
      <form id="create-project-form">
        <div class="form-group">
          <label class="form-label">Project Title</label>
          <input type="text" id="proj-title" class="form-control" placeholder="e.g. Deep Neural NLP Sentiment Model" required>
        </div>
        <div class="form-group">
          <label class="form-label">Research Hypothesis & Scope</label>
          <textarea id="proj-desc" class="form-control" rows="3" placeholder="Outline the research objectives and deliverables..."></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Target Completion Deadline</label>
          <input type="date" id="proj-deadline" class="form-control">
        </div>
      </form>
    `;

    const footer = `
      <button class="btn btn-secondary" onclick="App.closeModal()">Cancel</button>
      <button class="btn btn-primary" onclick="App.submitCreateProject()">Create Project</button>
    `;

    this.openModal('Create New Research Project', body, footer);
  },

  async submitCreateProject() {
    const title = document.getElementById('proj-title').value;
    const description = document.getElementById('proj-desc').value;
    const deadline = document.getElementById('proj-deadline').value || null;

    if (!title.trim()) {
      this.showToast('Title is required.', 'error');
      return;
    }

    try {
      await API.projects.create({ title, description, deadline });
      this.closeModal();
      this.showToast('Project created successfully!', 'success');
      this.renderProjects();
    } catch (err) {
      this.showToast('Creation error: ' + err.message, 'error');
    }
  },

  async openProjectWorkspace(projectId) {
    try {
      const [projResp, membersResp, tasksResp] = await Promise.all([
        API.projects.get(projectId),
        API.projects.getMembers(projectId),
        API.projects.getTasks(projectId)
      ]);

      const proj = projResp.data;
      const members = membersResp.data || [];
      const tasks = tasksResp.data || [];

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <div style="display: flex; align-items: center; gap: 10px;">
              <button class="btn btn-secondary btn-sm" onclick="App.navigate('projects')">← Back to Projects</button>
              <h2>${proj.title}</h2>
            </div>
            <p>${proj.description || ''}</p>
          </div>
          <div style="display: flex; gap: 10px;">
            <button class="btn btn-secondary" onclick="App.openAddMemberModal(${proj.id})">➕ Add Member</button>
            <button class="btn btn-primary" onclick="App.openCreateTaskModal(${proj.id})">➕ Add Task</button>
          </div>
        </div>

        <!-- Members Bar -->
        <div class="card" style="margin-bottom: 24px;">
          <div class="card-header">
            <div class="card-title"><span>👥</span> Research Team Members (${members.length})</div>
          </div>
          <div class="card-body" style="display: flex; gap: 12px; flex-wrap: wrap;">
            ${members.map(m => `
              <div style="background: rgba(255,255,255,0.05); padding: 8px 14px; border-radius: 8px; border: 1px solid var(--border-color); display: flex; align-items: center; gap: 10px;">
                <div class="user-avatar" style="width: 28px; height: 28px; font-size: 12px;">${m.userName.charAt(0)}</div>
                <div>
                  <div style="font-size: 13px; font-weight: 600; color: #fff;">${m.userName}</div>
                  <div style="font-size: 11px; color: var(--accent-indigo);">${m.projectRole}</div>
                </div>
              </div>
            `).join('')}
          </div>
        </div>

        <!-- Kanban Board -->
        <div class="kanban-board">
          ${['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE'].map(status => `
            <div class="kanban-col">
              <div class="kanban-col-header">
                <span>${status.replace('_', ' ')}</span>
                <span class="badge badge-info">${tasks.filter(t => t.status === status).length}</span>
              </div>
              <div>
                ${tasks.filter(t => t.status === status).map(t => `
                  <div class="kanban-card">
                    <h5>${t.title}</h5>
                    <p>${t.description || ''}</p>
                    <div style="display: flex; justify-content: space-between; align-items: center; font-size: 11px;">
                      <span class="badge ${t.priority === 'HIGH' ? 'badge-danger' : 'badge-warning'}">${t.priority}</span>
                      <select class="form-control" style="width: 105px; padding: 3px 6px; font-size: 11px;" onchange="App.moveTaskStatus(${t.id}, this.value, ${proj.id})">
                        <option value="TODO" ${t.status === 'TODO' ? 'selected' : ''}>TODO</option>
                        <option value="IN_PROGRESS" ${t.status === 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                        <option value="REVIEW" ${t.status === 'REVIEW' ? 'selected' : ''}>REVIEW</option>
                        <option value="DONE" ${t.status === 'DONE' ? 'selected' : ''}>DONE</option>
                      </select>
                    </div>
                  </div>
                `).join('')}
              </div>
            </div>
          `).join('')}
        </div>
      `;
    } catch (err) {
      this.showToast('Workspace error: ' + err.message, 'error');
    }
  },

  async moveTaskStatus(taskId, newStatus, projectId) {
    try {
      await API.projects.updateTask({ id: taskId, status: newStatus });
      this.showToast('Task updated', 'success');
      this.openProjectWorkspace(projectId);
    } catch (err) {
      this.showToast('Task update error: ' + err.message, 'error');
    }
  },

  openAddMemberModal(projectId) {
    API.admin.getUsers().then(resp => {
      const users = resp.data || [];
      const body = `
        <div class="form-group">
          <label class="form-label">Select Registered User</label>
          <select id="member-user-id" class="form-control">
            ${users.map(u => `<option value="${u.id}">${u.fullName} (${u.email})</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Project Role</label>
          <select id="member-role" class="form-control">
            <option value="RESEARCHER">Researcher</option>
            <option value="CONTRIBUTOR">Contributor</option>
            <option value="SUPERVISOR">Supervisor</option>
          </select>
        </div>
      `;

      const footer = `
        <button class="btn btn-secondary" onclick="App.closeModal()">Cancel</button>
        <button class="btn btn-primary" onclick="App.submitAddMember(${projectId})">Add Member</button>
      `;

      App.openModal('Assign Team Member', body, footer);
    }).catch(e => App.showToast('Error: ' + e.message, 'error'));
  },

  async submitAddMember(projectId) {
    const userId = document.getElementById('member-user-id').value;
    const role = document.getElementById('member-role').value;
    try {
      await API.projects.addMember(projectId, parseInt(userId), role);
      this.closeModal();
      this.showToast('Member assigned!', 'success');
      this.openProjectWorkspace(projectId);
    } catch (err) {
      this.showToast('Assignment error: ' + err.message, 'error');
    }
  },

  openCreateTaskModal(projectId) {
    const body = `
      <div class="form-group">
        <label class="form-label">Task Title</label>
        <input type="text" id="task-title" class="form-control" placeholder="e.g. Hyperparameter grid search" required>
      </div>
      <div class="form-group">
        <label class="form-label">Details</label>
        <textarea id="task-desc" class="form-control" rows="2"></textarea>
      </div>
      <div class="form-group">
        <label class="form-label">Priority</label>
        <select id="task-priority" class="form-control">
          <option value="LOW">Low</option>
          <option value="MEDIUM" selected>Medium</option>
          <option value="HIGH">High</option>
        </select>
      </div>
    `;

    const footer = `
      <button class="btn btn-secondary" onclick="App.closeModal()">Cancel</button>
      <button class="btn btn-primary" onclick="App.submitCreateTask(${projectId})">Create Task</button>
    `;

    this.openModal('Add Project Milestone Task', body, footer);
  },

  async submitCreateTask(projectId) {
    const title = document.getElementById('task-title').value;
    const description = document.getElementById('task-desc').value;
    const priority = document.getElementById('task-priority').value;

    if (!title.trim()) return;

    try {
      await API.projects.createTask(projectId, { title, description, priority, status: 'TODO' });
      this.closeModal();
      this.showToast('Task added!', 'success');
      this.openProjectWorkspace(projectId);
    } catch (err) {
      this.showToast('Error: ' + err.message, 'error');
    }
  },

  async deleteProject(id) {
    if (!confirm('Are you sure you want to delete this project?')) return;
    try {
      await API.projects.delete(id);
      this.showToast('Project deleted', 'success');
      this.renderProjects();
    } catch (err) {
      this.showToast('Error: ' + err.message, 'error');
    }
  },

  // --- 8. ADMIN GOVERNANCE: USERS & RESOURCES ---
  async renderUsers() {
    this.renderAdminDashboard();
  },

  async toggleUserStatus(userId, active) {
    try {
      await API.admin.updateUserStatus(userId, active);
      this.showToast('User status updated.', 'success');
      this.renderAdminDashboard();
    } catch (err) {
      this.showToast('Update error: ' + err.message, 'error');
    }
  },

  async changeUserRole(userId, newRole) {
    try {
      await API.admin.updateUserRole(userId, newRole);
      this.showToast(`User role updated to ${newRole}`, 'success');
      this.renderAdminDashboard();
    } catch (err) {
      this.showToast('Role update error: ' + err.message, 'error');
    }
  },

  async deleteUser(userId) {
    if (!confirm('Permanently delete this user account?')) return;
    try {
      await API.admin.deleteUser(userId);
      this.showToast('User account deleted.', 'success');
      this.renderAdminDashboard();
    } catch (err) {
      this.showToast('Delete error: ' + err.message, 'error');
    }
  },

  async renderResources() {
    try {
      const resp = await API.admin.getResources();
      const allocations = resp.data || [];

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <h2>Resource Management & Compute Allocation</h2>
            <p>Configure storage quotas and compute hours allocated per researcher.</p>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>⚡</span> Quota Allocations</div>
          </div>
          <div class="card-body" style="padding: 0;">
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Compute Quota (Hours)</th>
                    <th>Compute Used</th>
                    <th>Storage Quota (MB)</th>
                    <th>Storage Used</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  ${allocations.map(a => `
                    <tr>
                      <td style="font-weight: 600; color: #fff;">${a.username}</td>
                      <td>${a.allocatedComputeHours} hrs</td>
                      <td>${a.usedComputeHours} hrs</td>
                      <td>${a.allocatedStorageMb} MB</td>
                      <td>${a.usedStorageMb} MB</td>
                      <td>
                        <button class="btn btn-secondary btn-sm" onclick="App.openEditResourceModal(${a.id}, ${a.allocatedComputeHours}, ${a.allocatedStorageMb})">
                          Edit Quota
                        </button>
                      </td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;
    } catch (err) {
      this.showToast('Error: ' + err.message, 'error');
    }
  },

  openEditResourceModal(id, currentCompute, currentStorage) {
    const body = `
      <div class="form-group">
        <label class="form-label">Allocated Compute Hours</label>
        <input type="number" id="alloc-compute" class="form-control" value="${currentCompute}" step="10">
      </div>
      <div class="form-group">
        <label class="form-label">Allocated Storage Limit (MB)</label>
        <input type="number" id="alloc-storage" class="form-control" value="${currentStorage}" step="512">
      </div>
    `;

    const footer = `
      <button class="btn btn-secondary" onclick="App.closeModal()">Cancel</button>
      <button class="btn btn-primary" onclick="App.submitEditResource(${id})">Save Quotas</button>
    `;

    this.openModal('Adjust Resource Quotas', body, footer);
  },

  async submitEditResource(id) {
    const comp = parseFloat(document.getElementById('alloc-compute').value);
    const stor = parseFloat(document.getElementById('alloc-storage').value);
    try {
      await API.admin.updateResource({ id, allocatedComputeHours: comp, allocatedStorageMb: stor });
      this.closeModal();
      this.showToast('Resource quotas saved!', 'success');
      this.renderResources();
    } catch (err) {
      this.showToast('Error: ' + err.message, 'error');
    }
  },

  async renderAuditLogs() {
    try {
      const resp = await API.admin.getAuditLogs();
      const logs = resp.data || [];

      const content = document.getElementById('content-area');
      content.innerHTML = `
        <div class="page-header">
          <div class="page-title">
            <h2>Platform Audit & Security Event Logs</h2>
            <p>Immutable traceability records for system events, logins, and project mutations.</p>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>📜</span> System Event Log</div>
          </div>
          <div class="card-body" style="padding: 0;">
            <div class="table-responsive">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>User</th>
                    <th>Event Type</th>
                    <th>Description</th>
                    <th>Client IP</th>
                  </tr>
                </thead>
                <tbody>
                  ${logs.map(l => `
                    <tr>
                      <td style="font-family: 'JetBrains Mono', monospace; font-size: 12px;">${l.createdAt}</td>
                      <td style="font-weight: 600;">${l.username}</td>
                      <td><span class="badge badge-purple">${l.actionType}</span></td>
                      <td>${l.details}</td>
                      <td style="font-size: 12px; color: var(--text-muted);">${l.ipAddress}</td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;
    } catch (err) {
      this.showToast('Error: ' + err.message, 'error');
    }
  },

  // --- 9. SETTINGS & PROFILE ---
  renderSettings() {
    const content = document.getElementById('content-area');
    content.innerHTML = `
      <div class="page-header">
        <div class="page-title">
          <h2>User Profile & Security Settings</h2>
          <p>Manage account security credentials and research workspace preferences.</p>
        </div>
      </div>

      <div class="grid-cols-2">
        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>👤</span> Account Profile</div>
          </div>
          <div class="card-body">
            <div class="form-group">
              <label class="form-label">Full Name</label>
              <input type="text" class="form-control" value="${this.currentUser.fullName}" readonly>
            </div>
            <div class="form-group">
              <label class="form-label">Username</label>
              <input type="text" class="form-control" value="${this.currentUser.username}" readonly>
            </div>
            <div class="form-group">
              <label class="form-label">Email Address</label>
              <input type="email" class="form-control" value="${this.currentUser.email}" readonly>
            </div>
            <div class="form-group">
              <label class="form-label">Role</label>
              <input type="text" class="form-control" value="${this.currentUser.role}" readonly>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <div class="card-title"><span>🔒</span> Change Password</div>
          </div>
          <div class="card-body">
            <form onsubmit="event.preventDefault(); App.submitChangePassword();">
              <div class="form-group">
                <label class="form-label">Current Password</label>
                <input type="password" id="old-pass" class="form-control" required>
              </div>
              <div class="form-group">
                <label class="form-label">New Password (min 6 characters)</label>
                <input type="password" id="new-pass" class="form-control" required minlength="6">
              </div>
              <button type="submit" class="btn btn-primary">Update Password</button>
            </form>
          </div>
        </div>
      </div>
    `;
  },

  async submitChangePassword() {
    const oldPass = document.getElementById('old-pass').value;
    const newPass = document.getElementById('new-pass').value;
    try {
      await API.auth.changePassword(oldPass, newPass);
      this.showToast('Password changed successfully!', 'success');
      document.getElementById('old-pass').value = '';
      document.getElementById('new-pass').value = '';
    } catch (err) {
      this.showToast('Password error: ' + err.message, 'error');
    }
  },

  // --- MODAL & TOAST HELPERS ---
  openModal(title, bodyHtml, footerHtml = '') {
    const backdrop = document.getElementById('app-modal');
    const content = document.getElementById('modal-content');
    content.innerHTML = `
      <div class="modal-header">
        <h3 class="modal-title">${title}</h3>
        <button onclick="App.closeModal()" style="background: transparent; border: none; color: #94a3b8; font-size: 20px; cursor: pointer;">✕</button>
      </div>
      <div class="modal-body">${bodyHtml}</div>
      ${footerHtml ? `<div class="modal-footer">${footerHtml}</div>` : ''}
    `;
    backdrop.classList.add('active');
  },

  closeModal() {
    const backdrop = document.getElementById('app-modal');
    backdrop.classList.remove('active');
  },

  showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  }
};

window.addEventListener('DOMContentLoaded', () => App.init());

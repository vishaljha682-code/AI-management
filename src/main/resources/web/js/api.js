/**
 * AI ResearchHub API Client
 * Connects frontend UI to Java HttpServlets
 */
const API = {
  baseUrl: window.location.origin,

  async request(endpoint, options = {}) {
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    };

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers
      }
    };

    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, config);
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || `Request failed with status ${response.status}`);
      }
      return data;
    } catch (err) {
      console.error(`API Error on ${endpoint}:`, err);
      throw err;
    }
  },

  // Auth Endpoints (AuthServlet)
  auth: {
    login: (usernameOrEmail, password) => 
      API.request('/api/auth/login', { method: 'POST', body: JSON.stringify({ usernameOrEmail, password }) }),
    register: (username, fullName, email, password, role) => 
      API.request('/api/auth/register', { method: 'POST', body: JSON.stringify({ username, fullName, email, password, role }) }),
    logout: () => 
      API.request('/api/auth/logout', { method: 'POST' }),
    me: () => 
      API.request('/api/auth/me'),
    changePassword: (oldPassword, newPassword) => 
      API.request('/api/auth/change-password', { method: 'POST', body: JSON.stringify({ oldPassword, newPassword }) })
  },

  // Admin Endpoints (AdminServlet)
  admin: {
    getStats: () => API.request('/api/admin/dashboard'),
    getUsers: () => API.request('/api/admin/users'),
    updateUserStatus: (userId, active) => 
      API.request('/api/admin/users/status', { method: 'POST', body: JSON.stringify({ userId, active }) }),
    updateUserRole: (userId, role) => 
      API.request('/api/admin/users/role', { method: 'POST', body: JSON.stringify({ userId, role }) }),
    deleteUser: (userId) => 
      API.request(`/api/admin/users/${userId}`, { method: 'DELETE' }),
    getResources: () => API.request('/api/admin/resources'),
    updateResource: (resource) => 
      API.request('/api/admin/resources', { method: 'POST', body: JSON.stringify(resource) }),
    getAuditLogs: () => API.request('/api/admin/audit-logs')
  },

  // Project Endpoints (ProjectServlet)
  projects: {
    list: () => API.request('/api/projects/list'),
    get: (id) => API.request(`/api/projects/${id}`),
    create: (project) => 
      API.request('/api/projects/create', { method: 'POST', body: JSON.stringify(project) }),
    update: (project) => 
      API.request('/api/projects/update', { method: 'PUT', body: JSON.stringify(project) }),
    delete: (id) => 
      API.request(`/api/projects/${id}`, { method: 'DELETE' }),
    getMembers: (projectId) => 
      API.request(`/api/projects/${projectId}/members`),
    addMember: (projectId, userId, projectRole) => 
      API.request(`/api/projects/${projectId}/members`, { method: 'POST', body: JSON.stringify({ userId, projectRole }) }),
    removeMember: (projectId, userId) => 
      API.request(`/api/projects/${projectId}/members/${userId}`, { method: 'DELETE' }),
    getTasks: (projectId) => 
      API.request(`/api/projects/${projectId}/tasks`),
    createTask: (projectId, task) => 
      API.request(`/api/projects/${projectId}/tasks`, { method: 'POST', body: JSON.stringify(task) }),
    updateTask: (task) => 
      API.request(`/api/projects/tasks/${task.id}`, { method: 'PUT', body: JSON.stringify(task) }),
    deleteTask: (taskId) => 
      API.request(`/api/projects/tasks/${taskId}`, { method: 'DELETE' })
  },

  // Dataset Endpoints (DatasetServlet)
  datasets: {
    list: () => API.request('/api/datasets/list'),
    get: (id) => API.request(`/api/datasets/${id}`),
    analyze: (id) => API.request(`/api/datasets/${id}/analysis`),
    upload: (name, description, filename, content) => 
      API.request('/api/datasets/upload', { method: 'POST', body: JSON.stringify({ name, description, filename, content }) }),
    delete: (id) => 
      API.request(`/api/datasets/${id}`, { method: 'DELETE' })
  },

  // Training Jobs Endpoints (TrainingJobServlet)
  training: {
    list: (projectId = null) => 
      API.request(projectId ? `/api/training-jobs/list?projectId=${projectId}` : '/api/training-jobs/list'),
    get: (id) => API.request(`/api/training-jobs/${id}`),
    start: (req) => 
      API.request('/api/training-jobs/start', { method: 'POST', body: JSON.stringify(req) }),
    cancel: (id) => 
      API.request(`/api/training-jobs/${id}/cancel`, { method: 'POST' })
  },

  // Experiments Endpoints (ExperimentServlet)
  experiments: {
    list: (projectId = null) => 
      API.request(projectId ? `/api/experiments/list?projectId=${projectId}` : '/api/experiments/list'),
    get: (id) => API.request(`/api/experiments/${id}`),
    compare: (experimentIds) => 
      API.request('/api/experiments/compare', { method: 'POST', body: JSON.stringify({ experimentIds }) }),
    delete: (id) => 
      API.request(`/api/experiments/${id}`, { method: 'DELETE' })
  },

  // Assistant Endpoints (AssistantServlet)
  assistant: {
    chat: (req) => 
      API.request('/api/assistant/chat', { method: 'POST', body: JSON.stringify(req) }),
    getHistory: (convId) => 
      API.request(`/api/assistant/history/${convId}`)
  },

  // Usage Endpoints (UsageServlet)
  usage: {
    getMetrics: () => API.request('/api/usage/reports')
  }
};

import { API_BASE_URL } from './apiConfig.js';

const TASKS_ENDPOINT = `${API_BASE_URL}/api/tasks`;

/**
 * Custom error class for API errors with backend status and details
 */
export class ApiError extends Error {
  constructor(message, status, validationErrors = null, data = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.validationErrors = validationErrors;
    this.data = data;
  }
}

/**
 * Helper to process fetch responses and handle JSON parsing and errors
 */
async function handleResponse(response) {
  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : null;

  if (!response.ok) {
    const message = data?.message || data?.error || `HTTP error ${response.status}`;
    const validationErrors = data?.validationErrors || null;
    throw new ApiError(message, response.status, validationErrors, data);
  }

  return data;
}

/**
 * Fetch a paginated and filtered list of tasks
 * @param {Object} [params] - Optional query parameters
 * @param {string} [params.status] - PENDING, IN_PROGRESS, COMPLETED
 * @param {string} [params.priority] - LOW, MEDIUM, HIGH
 * @param {string} [params.dueDateFrom] - ISO date-time string
 * @param {string} [params.dueDateTo] - ISO date-time string
 * @param {number} [params.page=0] - Zero-based page index
 * @param {number} [params.size=10] - Page size (1-100)
 * @param {string} [params.sortBy='createdAt'] - Field to sort by
 * @param {string} [params.direction='desc'] - asc or desc
 * @returns {Promise<Object>} TaskPageResponseDto
 */
export async function getTasks(params = {}) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, value);
    }
  });

  const queryString = query.toString();
  const url = queryString ? `${TASKS_ENDPOINT}?${queryString}` : TASKS_ENDPOINT;

  const response = await fetch(url, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
    },
  });

  return handleResponse(response);
}

/**
 * Fetch a single task by ID
 * @param {number|string} id - Task ID
 * @returns {Promise<Object>} TaskResponseDto
 */
export async function getTaskById(id) {
  const response = await fetch(`${TASKS_ENDPOINT}/${id}`, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
    },
  });

  return handleResponse(response);
}

/**
 * Create a new task
 * @param {Object} taskData - TaskRequestDto { title, description, status, priority, dueDate }
 * @returns {Promise<Object>} TaskResponseDto
 */
export async function createTask(taskData) {
  const response = await fetch(TASKS_ENDPOINT, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(taskData),
  });

  return handleResponse(response);
}

/**
 * Update an existing task by ID
 * @param {number|string} id - Task ID
 * @param {Object} taskData - TaskRequestDto { title, description, status, priority, dueDate }
 * @returns {Promise<Object>} TaskResponseDto
 */
export async function updateTask(id, taskData) {
  const response = await fetch(`${TASKS_ENDPOINT}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(taskData),
  });

  return handleResponse(response);
}

/**
 * Delete a task by ID
 * @param {number|string} id - Task ID
 * @returns {Promise<null>}
 */
export async function deleteTask(id) {
  const response = await fetch(`${TASKS_ENDPOINT}/${id}`, {
    method: 'DELETE',
    headers: {
      Accept: 'application/json',
    },
  });

  return handleResponse(response);
}

export default {
  getTasks,
  getTaskById,
  createTask,
  updateTask,
  deleteTask,
};

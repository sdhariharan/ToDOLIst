import React, { useState } from 'react';
import { createTask, updateTask } from '../../services/taskService.js';

function getCurrentDateTimeLocal() {
  const now = new Date();
  now.setSeconds(0, 0);
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function formatForDateTimeLocal(isoString) {
  if (!isoString) return '';
  try {
    const d = new Date(isoString);
    if (isNaN(d.getTime())) return '';
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  } catch {
    return '';
  }
}

const INITIAL_FORM_STATE = {
  title: '',
  description: '',
  status: 'PENDING',
  priority: 'MEDIUM',
  dueDate: '',
};

export function TaskForm({ task, onSuccess, onCancel }) {
  const isEditMode = Boolean(task && task.id);

  const [formData, setFormData] = useState(() => {
    if (task) {
      return {
        title: task.title || '',
        description: task.description || '',
        status: task.status || 'PENDING',
        priority: task.priority || 'MEDIUM',
        dueDate: formatForDateTimeLocal(task.dueDate),
      };
    }
    return INITIAL_FORM_STATE;
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [prevTaskId, setPrevTaskId] = useState(task?.id);
  if (task?.id !== prevTaskId) {
    setPrevTaskId(task?.id);
    setFormData(
      task
        ? {
            title: task.title || '',
            description: task.description || '',
            status: task.status || 'PENDING',
            priority: task.priority || 'MEDIUM',
            dueDate: formatForDateTimeLocal(task.dueDate),
          }
        : INITIAL_FORM_STATE
    );
    setFieldErrors({});
    setGeneralError(null);
  }

  const minDateTime = getCurrentDateTimeLocal();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    // Clear field-level error as user types
    if (fieldErrors[name]) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        delete next[name];
        return next;
      });
    }
  };

  const validate = () => {
    const errors = {};
    const trimmedTitle = formData.title.trim();

    if (!trimmedTitle) {
      errors.title = 'Title is required.';
    } else if (trimmedTitle.length > 150) {
      errors.title = 'Title cannot exceed 150 characters.';
    }

    if (formData.description && formData.description.length > 2000) {
      errors.description = 'Description cannot exceed 2000 characters.';
    }

    if (formData.dueDate) {
      const selected = new Date(formData.dueDate);
      const now = new Date();
      // Allow 60-second grace window to prevent false positives while editing/filling
      if (selected.getTime() < now.getTime() - 60000) {
        errors.dueDate = 'Due date must be in the present or future.';
      }
    }

    return errors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setGeneralError(null);

    const clientErrors = validate();
    if (Object.keys(clientErrors).length > 0) {
      setFieldErrors(clientErrors);
      return;
    }

    setFieldErrors({});
    setSubmitting(true);

    try {
      const payload = {
        title: formData.title.trim(),
        description: formData.description.trim() ? formData.description.trim() : null,
        status: formData.status || 'PENDING',
        priority: formData.priority || 'MEDIUM',
        dueDate: formData.dueDate ? formData.dueDate : null,
      };

      let result;
      if (isEditMode) {
        result = await updateTask(task.id, payload);
      } else {
        result = await createTask(payload);
        setFormData(INITIAL_FORM_STATE);
      }

      if (onSuccess) {
        onSuccess(result);
      }
    } catch (err) {
      if (err.validationErrors && typeof err.validationErrors === 'object') {
        setFieldErrors(err.validationErrors);
      }
      setGeneralError(
        err.message || (isEditMode ? 'Failed to update task.' : 'Failed to create task.')
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = () => {
    if (!isEditMode) {
      setFormData(INITIAL_FORM_STATE);
    }
    setFieldErrors({});
    setGeneralError(null);
    if (onCancel) {
      onCancel();
    }
  };

  return (
    <div className="task-form-card" aria-labelledby="form-heading">
      <div className="task-form-header">
        <h3 id="form-heading" className="task-form-title">
          {isEditMode ? `Edit Task #${task.id}` : 'Create New Task'}
        </h3>
        <p className="task-form-subtitle">
          {isEditMode
            ? 'Update task details and schedule'
            : 'Add a new item to your task list'}
        </p>
      </div>

      {generalError && (
        <div className="form-alert-error" role="alert">
          <span className="alert-icon" aria-hidden="true">&#9888;</span>
          <span>{generalError}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate className="task-form">
        <div className="form-group">
          <div className="form-label-row">
            <label htmlFor="task-title" className="form-label">
              Title <span className="required-indicator">*</span>
            </label>
            <span className="char-count">{formData.title.length}/150</span>
          </div>
          <input
            id="task-title"
            name="title"
            type="text"
            className={`form-input ${fieldErrors.title ? 'input-error' : ''}`}
            placeholder="e.g. Finalize quarterly sprint backlog"
            value={formData.title}
            onChange={handleChange}
            maxLength={150}
            disabled={submitting}
            required
          />
          {fieldErrors.title && (
            <p className="field-error" role="alert">{fieldErrors.title}</p>
          )}
        </div>

        <div className="form-group">
          <div className="form-label-row">
            <label htmlFor="task-description" className="form-label">
              Description <span className="optional-indicator">(optional)</span>
            </label>
            <span className="char-count">{formData.description.length}/2000</span>
          </div>
          <textarea
            id="task-description"
            name="description"
            rows={3}
            className={`form-textarea ${fieldErrors.description ? 'input-error' : ''}`}
            placeholder="Add any helpful details, requirements, or links..."
            value={formData.description}
            onChange={handleChange}
            maxLength={2000}
            disabled={submitting}
          />
          {fieldErrors.description && (
            <p className="field-error" role="alert">{fieldErrors.description}</p>
          )}
        </div>

        <div className="form-row form-row-three">
          <div className="form-group">
            <label htmlFor="task-status" className="form-label">Status</label>
            <select
              id="task-status"
              name="status"
              className={`form-select ${fieldErrors.status ? 'input-error' : ''}`}
              value={formData.status}
              onChange={handleChange}
              disabled={submitting}
            >
              <option value="PENDING">PENDING</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="COMPLETED">COMPLETED</option>
            </select>
            {fieldErrors.status && (
              <p className="field-error" role="alert">{fieldErrors.status}</p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="task-priority" className="form-label">Priority</label>
            <select
              id="task-priority"
              name="priority"
              className={`form-select ${fieldErrors.priority ? 'input-error' : ''}`}
              value={formData.priority}
              onChange={handleChange}
              disabled={submitting}
            >
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
            </select>
            {fieldErrors.priority && (
              <p className="field-error" role="alert">{fieldErrors.priority}</p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="task-dueDate" className="form-label">
              Due Date <span className="optional-indicator">(optional)</span>
            </label>
            <input
              id="task-dueDate"
              name="dueDate"
              type="datetime-local"
              min={minDateTime}
              className={`form-input ${fieldErrors.dueDate ? 'input-error' : ''}`}
              value={formData.dueDate}
              onChange={handleChange}
              disabled={submitting}
            />
            {fieldErrors.dueDate && (
              <p className="field-error" role="alert">{fieldErrors.dueDate}</p>
            )}
          </div>
        </div>

        <div className="form-actions">
          {onCancel && (
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleReset}
              disabled={submitting}
            >
              Cancel
            </button>
          )}
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting}
          >
            {submitting ? (
              <>
                <span className="btn-spinner" aria-hidden="true" />
                <span>{isEditMode ? 'Updating Task...' : 'Creating Task...'}</span>
              </>
            ) : (
              isEditMode ? 'Update Task' : 'Create Task'
            )}
          </button>
        </div>
      </form>
    </div>
  );
}

export default TaskForm;

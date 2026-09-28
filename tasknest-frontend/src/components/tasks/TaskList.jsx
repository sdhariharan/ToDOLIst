import React, { useState, useEffect, useCallback } from 'react';
import TaskItem from './TaskItem.jsx';
import TaskFilters from './TaskFilters.jsx';
import Pagination from './Pagination.jsx';
import taskService, { getTasks, ApiError } from '../../services/taskService.js';

function formatErrorMessage(error) {
  if (!error) return 'An unexpected error occurred.';
  if (typeof error === 'string') return error;

  if (error.validationErrors) {
    if (Array.isArray(error.validationErrors)) {
      return error.validationErrors.join(', ');
    }
    if (typeof error.validationErrors === 'object') {
      return Object.entries(error.validationErrors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(', ');
    }
  }

  return error.message || 'Failed to load tasks.';
}

export function TaskList({
  tasks: propTasks,
  loading: propLoading,
  error: propError,
  onRetry: propOnRetry,
}) {
  const isControlled = propTasks !== undefined;

  // Internal state when TaskList is used standalone
  const [internalTasks, setInternalTasks] = useState([]);
  const [internalLoading, setInternalLoading] = useState(false);
  const [internalError, setInternalError] = useState(null);
  const [internalFilters, setInternalFilters] = useState({
    status: '',
    priority: '',
    dueDateFrom: '',
    dueDateTo: '',
    sortBy: 'createdAt',
    direction: 'desc',
  });
  const [internalPage, setInternalPage] = useState(0);
  const [internalSize, setInternalSize] = useState(10);
  const [internalPageInfo, setInternalPageInfo] = useState({
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const fetchInternalTasks = useCallback(async () => {
    if (isControlled) return;

    setInternalLoading(true);
    setInternalError(null);

    try {
      const data = await getTasks({
        ...internalFilters,
        page: internalPage,
        size: internalSize,
        sortBy: internalFilters.sortBy,
        direction: internalFilters.direction,
      });

      setInternalTasks(data?.content || []);
      setInternalPageInfo({
        page: data?.page ?? internalPage,
        size: data?.size ?? internalSize,
        totalElements: data?.totalElements ?? 0,
        totalPages: data?.totalPages ?? 0,
        first: data?.first ?? true,
        last: data?.last ?? true,
      });
    } catch (err) {
      setInternalError(err);
      setInternalTasks([]);
    } finally {
      setInternalLoading(false);
    }
  }, [isControlled, internalFilters, internalPage, internalSize]);

  useEffect(() => {
    if (!isControlled) {
      fetchInternalTasks();
    }
  }, [fetchInternalTasks, isControlled]);

  const tasks = isControlled ? propTasks : internalTasks;
  const loading = isControlled ? propLoading : internalLoading;
  const error = isControlled ? propError : internalError;
  const onRetry = isControlled ? propOnRetry : fetchInternalTasks;

  const handleFilterChange = (updatedFilter) => {
    setInternalFilters((prev) => ({ ...prev, ...updatedFilter }));
    setInternalPage(0);
  };

  const handleResetFilters = () => {
    setInternalFilters({
      status: '',
      priority: '',
      dueDateFrom: '',
      dueDateTo: '',
      sortBy: 'createdAt',
      direction: 'desc',
    });
    setInternalPage(0);
  };

  const handlePageChange = (newPage) => {
    setInternalPage(newPage);
  };

  const handleSizeChange = (newSize) => {
    setInternalSize(newSize);
    setInternalPage(0);
  };

  return (
    <div className="task-list-wrapper">
      {!isControlled && (
        <TaskFilters
          filters={internalFilters}
          onFilterChange={handleFilterChange}
          onReset={handleResetFilters}
        />
      )}

      {loading && (
        <div className="tasks-state tasks-loading" aria-live="polite">
          <div className="spinner" />
          <p>Loading tasks...</p>
        </div>
      )}

      {!loading && error && (
        <div className="tasks-state tasks-error" role="alert">
          <div className="error-icon" aria-hidden="true">&#9888;</div>
          <h4 className="error-title">Unable to Load Tasks</h4>
          <p className="error-message">{formatErrorMessage(error)}</p>
          {onRetry && (
            <button type="button" className="btn-retry" onClick={onRetry}>
              Try Again
            </button>
          )}
        </div>
      )}

      {!loading && !error && tasks && tasks.length === 0 && (
        <div className="tasks-state tasks-empty">
          <div className="empty-icon" aria-hidden="true">&#128221;</div>
          <p className="empty-title">No tasks found.</p>
          <p className="empty-subtitle">
            There are no tasks matching your current filters. Try changing or clearing your filters.
          </p>
        </div>
      )}

      {!loading && !error && tasks && tasks.length > 0 && (
        <div className="task-grid" role="feed" aria-label="Task list">
          {tasks.map((task) => (
            <TaskItem key={task.id} task={task} />
          ))}
        </div>
      )}

      {!isControlled && !loading && !error && tasks && tasks.length > 0 && (
        <Pagination
          page={internalPage}
          size={internalSize}
          totalElements={internalPageInfo.totalElements}
          totalPages={internalPageInfo.totalPages}
          first={internalPageInfo.first}
          last={internalPageInfo.last}
          onPageChange={handlePageChange}
          onSizeChange={handleSizeChange}
        />
      )}
    </div>
  );
}

export default TaskList;

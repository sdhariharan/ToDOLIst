import React, { useState, useEffect, useCallback } from 'react';
import TaskFilters from '../components/tasks/TaskFilters.jsx';
import TaskList from '../components/tasks/TaskList.jsx';
import Pagination from '../components/tasks/Pagination.jsx';
import taskService, { getTasks } from '../services/taskService.js';
import '../components/tasks/tasks.css';

const DEFAULT_FILTERS = {
  status: '',
  priority: '',
  dueDateFrom: '',
  dueDateTo: '',
  sortBy: 'createdAt',
  direction: 'desc',
};

export function TasksPage() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [filters, setFilters] = useState(DEFAULT_FILTERS);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [pageInfo, setPageInfo] = useState({
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const loadTasks = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await getTasks({
        status: filters.status || undefined,
        priority: filters.priority || undefined,
        dueDateFrom: filters.dueDateFrom || undefined,
        dueDateTo: filters.dueDateTo || undefined,
        page,
        size,
        sortBy: filters.sortBy || 'createdAt',
        direction: filters.direction || 'desc',
      });

      setTasks(data?.content || []);
      setPageInfo({
        page: data?.page ?? page,
        size: data?.size ?? size,
        totalElements: data?.totalElements ?? 0,
        totalPages: data?.totalPages ?? 0,
        first: data?.first ?? true,
        last: data?.last ?? true,
      });
    } catch (err) {
      setError(err);
      setTasks([]);
    } finally {
      setLoading(false);
    }
  }, [filters, page, size]);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  // When filters change, reset page to 0 and update filter values
  const handleFilterChange = (updatedFields) => {
    setFilters((prev) => ({
      ...prev,
      ...updatedFields,
    }));
    setPage(0);
  };

  // Reset filters to defaults and reset page to 0
  const handleResetFilters = () => {
    setFilters(DEFAULT_FILTERS);
    setPage(0);
  };

  // Page navigation preserves current filters
  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  // Page size changes reset page to 0
  const handleSizeChange = (newSize) => {
    setSize(newSize);
    setPage(0);
  };

  return (
    <div className="tasks-page-container">
      <header className="tasks-page-header">
        <div className="header-text">
          <h2 className="tasks-title">Task Overview</h2>
          <p className="tasks-subtitle">Manage, filter, and track your daily tasks</p>
        </div>
      </header>

      <TaskFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onReset={handleResetFilters}
      />

      <TaskList
        tasks={tasks}
        loading={loading}
        error={error}
        onRetry={loadTasks}
      />

      {!loading && !error && tasks.length > 0 && (
        <Pagination
          page={page}
          size={size}
          totalElements={pageInfo.totalElements}
          totalPages={pageInfo.totalPages}
          first={pageInfo.first}
          last={pageInfo.last}
          onPageChange={handlePageChange}
          onSizeChange={handleSizeChange}
        />
      )}
    </div>
  );
}

export default TasksPage;

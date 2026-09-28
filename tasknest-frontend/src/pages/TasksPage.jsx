import React, { useState, useEffect, useCallback } from 'react';
import TaskFilters from '../components/tasks/TaskFilters.jsx';
import TaskList from '../components/tasks/TaskList.jsx';
import Pagination from '../components/tasks/Pagination.jsx';
import TaskForm from '../components/tasks/TaskForm.jsx';
import DeleteConfirmModal from '../components/tasks/DeleteConfirmModal.jsx';
import { getTasks, deleteTask } from '../services/taskService.js';
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
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [deletingTask, setDeletingTask] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

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

  // Close modals on Escape key press
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        if (editingTask) setEditingTask(null);
        if (deletingTask && !isDeleting) {
          setDeletingTask(null);
          setDeleteError(null);
        }
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [editingTask, deletingTask, isDeleting]);

  // When a task is created, display success message, hide form, and refresh task list
  const handleTaskCreated = (newTask) => {
    setSuccessMessage(`Task "${newTask.title}" created successfully!`);
    setShowForm(false);
    loadTasks();
  };

  // When a task is updated, display success message, close modal, and refresh task list preserving page & filters
  const handleTaskUpdated = (updatedTask) => {
    setSuccessMessage(`Task "${updatedTask.title}" updated successfully!`);
    setEditingTask(null);
    loadTasks();
  };

  // Handle task deletion with confirmation and page boundary adjustment
  const handleDeleteConfirm = async () => {
    if (!deletingTask) return;
    setIsDeleting(true);
    setDeleteError(null);

    try {
      await deleteTask(deletingTask.id);
      const deletedTitle = deletingTask.title;
      const deletedId = deletingTask.id;
      setDeletingTask(null);
      setSuccessMessage(`Task #${deletedId} "${deletedTitle}" deleted successfully!`);

      // If this was the last item on the current page and we're past page 0, go back one page
      if (tasks.length === 1 && page > 0) {
        setPage((prevPage) => Math.max(0, prevPage - 1));
      } else {
        loadTasks();
      }
    } catch (err) {
      setDeleteError(err.message || 'Failed to delete task. Please try again.');
    } finally {
      setIsDeleting(false);
    }
  };

  const handleDeleteCancel = () => {
    if (!isDeleting) {
      setDeletingTask(null);
      setDeleteError(null);
    }
  };

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
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => {
            setShowForm((prev) => !prev);
            setSuccessMessage(null);
          }}
          aria-expanded={showForm}
        >
          {showForm ? 'Cancel' : '+ New Task'}
        </button>
      </header>

      {successMessage && (
        <div className="alert-success" role="status">
          <span>{successMessage}</span>
          <button
            type="button"
            className="btn-alert-close"
            onClick={() => setSuccessMessage(null)}
            aria-label="Dismiss success message"
          >
            &times;
          </button>
        </div>
      )}

      {showForm && (
        <TaskForm
          onSuccess={handleTaskCreated}
          onCancel={() => setShowForm(false)}
        />
      )}

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
        onEditTask={setEditingTask}
        onDeleteTask={(task) => {
          setDeletingTask(task);
          setDeleteError(null);
        }}
      />

      {editingTask && (
        <div
          className="modal-backdrop"
          onClick={() => setEditingTask(null)}
          role="presentation"
        >
          <div
            className="modal-dialog"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="form-heading"
          >
            <button
              type="button"
              className="modal-close-btn"
              onClick={() => setEditingTask(null)}
              aria-label="Close edit dialog"
            >
              &times;
            </button>
            <TaskForm
              task={editingTask}
              onSuccess={handleTaskUpdated}
              onCancel={() => setEditingTask(null)}
            />
          </div>
        </div>
      )}

      {deletingTask && (
        <DeleteConfirmModal
          task={deletingTask}
          onConfirm={handleDeleteConfirm}
          onCancel={handleDeleteCancel}
          deleting={isDeleting}
          error={deleteError}
        />
      )}

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

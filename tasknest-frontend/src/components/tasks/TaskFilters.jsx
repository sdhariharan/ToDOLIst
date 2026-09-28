import React from 'react';

export function TaskFilters({ filters, onFilterChange, onReset }) {
  const handleChange = (e) => {
    const { name, value } = e.target;
    onFilterChange({ [name]: value });
  };

  return (
    <div className="task-filters-card">
      <div className="filters-header">
        <h3 className="filters-title">Filter &amp; Sort Tasks</h3>
        <button type="button" className="btn-reset" onClick={onReset}>
          Reset Filters
        </button>
      </div>

      <div className="filters-grid">
        <div className="filter-group">
          <label htmlFor="filter-status">Status</label>
          <select
            id="filter-status"
            name="status"
            value={filters.status || ''}
            onChange={handleChange}
          >
            <option value="">ALL</option>
            <option value="PENDING">PENDING</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="COMPLETED">COMPLETED</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="filter-priority">Priority</label>
          <select
            id="filter-priority"
            name="priority"
            value={filters.priority || ''}
            onChange={handleChange}
          >
            <option value="">ALL</option>
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="filter-dueDateFrom">Due Date From</label>
          <input
            type="datetime-local"
            id="filter-dueDateFrom"
            name="dueDateFrom"
            value={filters.dueDateFrom || ''}
            onChange={handleChange}
          />
        </div>

        <div className="filter-group">
          <label htmlFor="filter-dueDateTo">Due Date To</label>
          <input
            type="datetime-local"
            id="filter-dueDateTo"
            name="dueDateTo"
            value={filters.dueDateTo || ''}
            onChange={handleChange}
          />
        </div>

        <div className="filter-group">
          <label htmlFor="filter-sortBy">Sort By</label>
          <select
            id="filter-sortBy"
            name="sortBy"
            value={filters.sortBy || 'createdAt'}
            onChange={handleChange}
          >
            <option value="createdAt">Created Date</option>
            <option value="dueDate">Due Date</option>
            <option value="priority">Priority</option>
            <option value="status">Status</option>
            <option value="title">Title</option>
            <option value="id">ID</option>
            <option value="updatedAt">Updated Date</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="filter-direction">Direction</label>
          <select
            id="filter-direction"
            name="direction"
            value={filters.direction || 'desc'}
            onChange={handleChange}
          >
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
        </div>
      </div>
    </div>
  );
}

export default TaskFilters;

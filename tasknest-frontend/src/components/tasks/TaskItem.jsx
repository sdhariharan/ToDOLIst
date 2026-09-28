import React from 'react';

function formatDate(dateString) {
  if (!dateString) return 'Not set';
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return dateString;
  }
}

export function TaskItem({ task }) {
  const statusClass = `badge-status badge-status-${task.status?.toLowerCase() || 'pending'}`;
  const priorityClass = `badge-priority badge-priority-${task.priority?.toLowerCase() || 'medium'}`;

  return (
    <article className="task-card" data-task-id={task.id}>
      <div className="task-header">
        <div className="task-title-group">
          <span className="task-id">#{task.id}</span>
          <h3 className="task-title">{task.title}</h3>
        </div>
        <div className="task-badges">
          <span className={`badge ${statusClass}`}>{task.status || 'PENDING'}</span>
          <span className={`badge ${priorityClass}`}>{task.priority || 'MEDIUM'}</span>
        </div>
      </div>

      {task.description && (
        <p className="task-description">{task.description}</p>
      )}

      <div className="task-meta">
        <div className="meta-item">
          <span className="meta-label">Due:</span>
          <span className="meta-value">{formatDate(task.dueDate)}</span>
        </div>
        <div className="meta-item">
          <span className="meta-label">Created:</span>
          <span className="meta-value">{formatDate(task.createdAt)}</span>
        </div>
        {task.updatedAt && task.updatedAt !== task.createdAt && (
          <div className="meta-item">
            <span className="meta-label">Updated:</span>
            <span className="meta-value">{formatDate(task.updatedAt)}</span>
          </div>
        )}
      </div>
    </article>
  );
}

export default TaskItem;

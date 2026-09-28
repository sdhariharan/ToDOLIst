import React from 'react';

export function DeleteConfirmModal({
  task,
  onConfirm,
  onCancel,
  deleting = false,
  error = null,
}) {
  if (!task) return null;

  return (
    <div
      className="modal-backdrop"
      onClick={() => {
        if (!deleting && onCancel) {
          onCancel();
        }
      }}
      role="presentation"
    >
      <div
        className="modal-dialog modal-dialog-confirm"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="delete-dialog-title"
      >
        <button
          type="button"
          className="modal-close-btn"
          onClick={onCancel}
          disabled={deleting}
          aria-label="Close delete dialog"
        >
          &times;
        </button>

        <div className="confirm-header">
          <div className="confirm-icon" aria-hidden="true">&#9888;</div>
          <h3 id="delete-dialog-title" className="confirm-title">
            Delete Task
          </h3>
        </div>

        <div className="confirm-body">
          <p className="confirm-message">
            Are you sure you want to delete <strong>#{task.id} &ndash; &ldquo;{task.title}&rdquo;</strong>?
          </p>
          <p className="confirm-warning">
            This action cannot be undone and will permanently remove this task.
          </p>

          {error && (
            <div className="form-alert-error" role="alert">
              <span className="alert-icon" aria-hidden="true">&#9888;</span>
              <span>{error}</span>
            </div>
          )}
        </div>

        <div className="confirm-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onCancel}
            disabled={deleting}
          >
            Cancel
          </button>
          <button
            type="button"
            className="btn btn-danger"
            onClick={onConfirm}
            disabled={deleting}
          >
            {deleting ? (
              <>
                <span className="btn-spinner" aria-hidden="true" />
                <span>Deleting...</span>
              </>
            ) : (
              'Delete Task'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

export default DeleteConfirmModal;

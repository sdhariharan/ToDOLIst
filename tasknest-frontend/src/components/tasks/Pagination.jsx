import React from 'react';

export function Pagination({
  page = 0,
  size = 10,
  totalElements = 0,
  totalPages = 0,
  first = true,
  last = true,
  onPageChange,
  onSizeChange,
}) {
  const isFirst = first || page <= 0;
  const isLast = last || totalPages === 0 || page >= totalPages - 1;
  const displayPage = totalPages === 0 ? 0 : page + 1;
  const startItem = totalElements === 0 ? 0 : page * size + 1;
  const endItem = Math.min((page + 1) * size, totalElements);

  const handlePrev = () => {
    if (!isFirst && onPageChange) {
      onPageChange(page - 1);
    }
  };

  const handleNext = () => {
    if (!isLast && onPageChange) {
      onPageChange(page + 1);
    }
  };

  const handleSizeSelect = (e) => {
    const newSize = Number(e.target.value);
    if (onSizeChange) {
      onSizeChange(newSize);
    }
  };

  return (
    <nav className="pagination-container" aria-label="Task pagination">
      <div className="pagination-info">
        <span>
          Showing {startItem}–{endItem} of {totalElements} {totalElements === 1 ? 'task' : 'tasks'}
        </span>
      </div>

      <div className="pagination-controls">
        <button
          type="button"
          className="btn-pagination"
          onClick={handlePrev}
          disabled={isFirst}
          aria-label="Previous page"
        >
          &larr; Previous
        </button>

        <span className="pagination-current" aria-current="page">
          Page {displayPage} of {totalPages || 1}
        </span>

        <button
          type="button"
          className="btn-pagination"
          onClick={handleNext}
          disabled={isLast}
          aria-label="Next page"
        >
          Next &rarr;
        </button>
      </div>

      <div className="pagination-size">
        <label htmlFor="page-size-select">Per page:</label>
        <select
          id="page-size-select"
          value={size}
          onChange={handleSizeSelect}
          className="page-size-select"
        >
          <option value={5}>5</option>
          <option value={10}>10</option>
          <option value={20}>20</option>
          <option value={50}>50</option>
        </select>
      </div>
    </nav>
  );
}

export default Pagination;

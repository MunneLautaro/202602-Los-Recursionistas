import React from "react";
import { paginationStyles } from "./PaginationControls.styles";

interface PaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (newPage: number) => void;
}

export const PaginationControls: React.FC<PaginationProps> = ({
  page,
  totalPages,
  onPageChange,
}) => {
  const handlePrev = () => {
    if (page > 0) onPageChange(page - 1);
  };
  const handleNext = () => {
    if (page + 1 < totalPages) onPageChange(page + 1);
  };
  const goToPage = (p: number) => onPageChange(p);

  const pageNumbers: number[] = [];
  const start = Math.max(0, page - 3);
  const end = Math.min(totalPages - 1, page + 3);
  for (let i = start; i <= end; i++) {
    pageNumbers.push(i);
  }

  return (
    <div className={paginationStyles.container}>
      {start > 0 && (
        <button className={paginationStyles.button} onClick={() => goToPage(0)}>
          Primero
        </button>
      )}
      <div className={paginationStyles.numbers}>
        {pageNumbers.map((p) => (
          <button
            key={p}
            className={`${paginationStyles.pageButton} ${p === page ? paginationStyles.active : ""}`}
            onClick={() => goToPage(p)}
          >
            {p + 1}
          </button>
        ))}
      </div>

      {end < totalPages - 1 && (
        <button
          className={paginationStyles.button}
          onClick={() => goToPage(totalPages - 1)}
        >
          Último
        </button>
      )}
    </div>
  );
};

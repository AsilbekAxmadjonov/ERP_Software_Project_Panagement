import { __ } from '../lib/i18n';
import type { Column } from './ListView';

export type CellValue = string | number | null;

export type CellFormat = 'money' | 'quantity' | 'text';

/** A cell with layout hints; a plain value is a regular cell. */
export interface CellSpec {
  value: CellValue;
  bold?: boolean;
  /** Indent level, for account lines under a section header. */
  indent?: number;
}

export type ExportCell = CellValue | CellSpec;

export interface ExportColumn {
  label: string;
  /** How numbers in this column are shown; money by default. */
  format?: CellFormat;
  /** Width in characters. */
  width?: number;
}

export interface ExportSheet {
  name: string;
  columns: readonly ExportColumn[];
  rows: readonly (readonly ExportCell[])[];
}

/** One downloadable workbook, usually the data currently shown on screen (filters applied). */
export interface ExportSpec {
  fileName: string;
  sheets: readonly ExportSheet[];
}

const NUMBER_FORMAT: Record<CellFormat, string | undefined> = {
  money: '#,##0.00',
  quantity: '#,##0.###',
  text: undefined,
};

const isSpec = (cell: ExportCell): cell is CellSpec => cell !== null && typeof cell === 'object';

/** Plain value of a list column: its export value, its sum, its text, or its sort value, in that order. */
function columnValue<T>(column: Column<T>, row: T): CellValue {
  if (column.exportValue) return column.exportValue(row);
  if (column.sum) return column.sum(row);
  const sortValue = column.sortValue?.(row);
  if (typeof sortValue === 'number' && column.align === 'right') return sortValue;
  const rendered = column.render(row);
  if (typeof rendered === 'string' || typeof rendered === 'number') return rendered;
  return sortValue ?? null;
}

/** Sheet of the given rows with the list's own columns, plus a totals row for summed columns. */
export function listSheet<T>(name: string, columns: readonly Column<T>[], rows: readonly T[]): ExportSheet {
  const exported = columns.filter((c) => c.label !== '' || c.exportValue);
  const body: ExportCell[][] = rows.map((row) => exported.map((c) => columnValue(c, row)));
  if (exported.some((c) => c.sum) && rows.length > 0) {
    body.push(exported.map((c, i) => ({
      value: c.sum ? rows.reduce((total, r) => total + c.sum!(r), 0) : i === 0 ? __('Total') : null,
      bold: true,
    })));
  }
  return {
    name,
    columns: exported.map((c) => ({ label: c.label, format: c.exportFormat ?? (c.align === 'right' ? 'money' : 'text') })),
    rows: body,
  };
}

/** Builds a real .xlsx workbook in the browser and saves it; the writer is loaded on first use. */
export async function downloadXlsx(spec: ExportSpec): Promise<void> {
  const { default: writeXlsxFile } = await import('write-excel-file/browser');
  const sheets = spec.sheets.map((sheet) => ({
    sheet: sheet.name.slice(0, 31).replace(/[\\/?*[\]:]/g, ' '),
    stickyRowsCount: 1,
    columns: sheet.columns.map((c, i) => ({
      width: c.width ?? Math.min(48, Math.max(10, c.label.length + 2, ...sheet.rows.map((r) => String(cellValue(r[i]) ?? '').length + 2))),
    })),
    data: [
      sheet.columns.map((c) => ({ value: c.label, fontWeight: 'bold' as const, bottomBorderStyle: 'thin' as const })),
      ...sheet.rows.map((row) =>
        sheet.columns.map((c, i) => {
          const cell = row[i] ?? null;
          const value = cellValue(cell);
          const style = isSpec(cell) ? { fontWeight: cell.bold ? ('bold' as const) : undefined, indent: cell.indent } : {};
          if (value === null) return { ...style, value: undefined };
          if (typeof value === 'number') return { ...style, value, type: Number, format: NUMBER_FORMAT[c.format ?? 'money'] };
          return { ...style, value, type: String };
        }),
      ),
    ],
  }));
  await writeXlsxFile(sheets).toFile(`${spec.fileName}.xlsx`);
}

const cellValue = (cell: ExportCell | undefined): CellValue => (cell === undefined ? null : isSpec(cell) ? cell.value : cell);

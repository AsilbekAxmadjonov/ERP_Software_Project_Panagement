import { FileSpreadsheet, Printer } from 'lucide-react';
import { useState } from 'react';
import { __ } from '../lib/i18n';
import { downloadXlsx, type ExportSpec } from './export';
import { useToast } from './Toast';

/**
 * "Export" saves what is on screen (with its filters) as an .xlsx workbook; "Print / PDF" opens the browser's print
 * dialog, where "Save as PDF" gives a PDF of the print layout (control panels and filters are hidden by print CSS).
 */
export function ExportActions({ build }: { build?: () => ExportSpec }) {
  const toast = useToast();
  const [busy, setBusy] = useState(false);

  const exportXlsx = async () => {
    if (!build) return;
    setBusy(true);
    await toast.run(() => downloadXlsx(build()));
    setBusy(false);
  };

  return (
    <div className="flex items-center gap-1">
      {build && (
        <button type="button" className="btn-secondary" onClick={() => void exportXlsx()} disabled={busy} title={__('Download as Excel (.xlsx)')}>
          <FileSpreadsheet className="h-4 w-4" />{__('Export')}
        </button>
      )}
      <button type="button" className="btn-secondary" onClick={() => window.print()} title={__('Print or save as PDF')}>
        <Printer className="h-4 w-4" />{__('Print / PDF')}
      </button>
    </div>
  );
}

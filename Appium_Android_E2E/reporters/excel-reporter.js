const ExcelJS = require('exceljs');
const path = require('path');
const fs = require('fs');

async function generateExcelReport(results, executionTime, outputPath) {
  const workbook = new ExcelJS.Workbook();
  workbook.creator = 'VaultFlow Appium Automation';
  workbook.lastModifiedBy = 'VaultFlow CI';
  workbook.created = new Date();
  workbook.modified = new Date();

  // 1. DASHBOARD SHEET
  const dashboardSheet = workbook.addWorksheet('E2E Execution Dashboard', {
    views: [{ showGridLines: false }]
  });

  // Calculate Metrics
  const total = results.length;
  const passed = results.filter(r => r.status === 'Passed').length;
  const failed = results.filter(r => r.status === 'Failed').length;
  const skipped = results.filter(r => r.status === 'Skipped').length;
  const passRate = total === 0 ? 0 : Math.round((passed / total) * 10000) / 100;

  // Title Block
  dashboardSheet.mergeCells('A1:E2');
  const titleCell = dashboardSheet.getCell('A1');
  titleCell.value = 'VaultFlow Mobile Automation - Appium E2E Report';
  titleCell.font = { name: 'Arial', size: 16, bold: true, color: { argb: 'FFFFFF' } };
  titleCell.fill = {
    type: 'pattern',
    pattern: 'solid',
    fgColor: { argb: '1F4E79' } // Dark Royal Blue
  };
  titleCell.alignment = { vertical: 'middle', horizontal: 'center' };

  // Metadata Block
  const metaRows = [
    ['Execution Target', 'Android Native Application (Kotlin Compose)', '', 'Suite Type', 'Full Regression End-to-End'],
    ['Automation Driver', 'Appium Server + WebdriverIO Client', '', 'Execution Time', executionTime],
    ['Target Platform', 'Android API 34-36 (Emulator/Device)', '', 'Generated At', new Date().toLocaleString()]
  ];

  metaRows.forEach((row, idx) => {
    const rowIdx = idx + 4;
    dashboardSheet.getCell(`A${rowIdx}`).value = row[0];
    dashboardSheet.getCell(`A${rowIdx}`).font = { bold: true };
    dashboardSheet.getCell(`B${rowIdx}`).value = row[1];
    dashboardSheet.getCell(`D${rowIdx}`).value = row[3];
    dashboardSheet.getCell(`D${rowIdx}`).font = { bold: true };
    dashboardSheet.getCell(`E${rowIdx}`).value = row[4];
  });

  // Summary Metrics Table Headers
  dashboardSheet.mergeCells('A9:E9');
  const metricsHeader = dashboardSheet.getCell('A9');
  metricsHeader.value = 'HIGH-LEVEL METRICS SUMMARY';
  metricsHeader.font = { bold: true, color: { argb: 'FFFFFF' }, size: 11 };
  metricsHeader.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: '2F5597' } };
  metricsHeader.alignment = { horizontal: 'left', indent: 1 };

  // Metrics Table
  const metrics = [
    ['Metric Name', 'Value', '', 'Pass/Fail Breakdown', 'Percentage'],
    ['Total Tests Executed', total, '', 'Passed Scenarios', passed],
    ['Total Scenarios Passed', passed, '', 'Failed Scenarios', failed],
    ['Total Scenarios Failed', failed, '', 'Skipped Scenarios', skipped],
    ['Total Scenarios Skipped', skipped, '', 'Overall Pass Rate', `${passRate}%`]
  ];

  metrics.forEach((row, idx) => {
    const rowIdx = idx + 10;
    dashboardSheet.getCell(`A${rowIdx}`).value = row[0];
    dashboardSheet.getCell(`B${rowIdx}`).value = row[1];
    dashboardSheet.getCell(`D${rowIdx}`).value = row[3];
    dashboardSheet.getCell(`E${rowIdx}`).value = row[4];

    // Styling Headers
    if (idx === 0) {
      dashboardSheet.getCell(`A${rowIdx}`).font = { bold: true };
      dashboardSheet.getCell(`B${rowIdx}`).font = { bold: true };
      dashboardSheet.getCell(`D${rowIdx}`).font = { bold: true };
      dashboardSheet.getCell(`E${rowIdx}`).font = { bold: true };
    } else {
      dashboardSheet.getCell(`A${rowIdx}`).font = { color: { argb: '595959' } };
      dashboardSheet.getCell(`D${rowIdx}`).font = { color: { argb: '595959' } };
    }
  });

  // Styling Highlight Pass Rate
  const passRateCell = dashboardSheet.getCell('E14');
  passRateCell.font = { bold: true, size: 12, color: { argb: '1F4E78' } };
  passRateCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'D9E1F2' } };

  // Set column widths for Dashboard
  dashboardSheet.getColumn('A').width = 28;
  dashboardSheet.getColumn('B').width = 46;
  dashboardSheet.getColumn('C').width = 4;
  dashboardSheet.getColumn('D').width = 24;
  dashboardSheet.getColumn('E').width = 24;


  // 2. DETAILED RESULTS SHEET
  const detailSheet = workbook.addWorksheet('E2E Test Details', {
    views: [{ showGridLines: true }]
  });

  // Header Row
  const headers = [
    'No.',
    'Test ID',
    'Module / Screen',
    'Test Scenario Description',
    'Severity / Priority',
    'Execution Type',
    'Target Element Selector',
    'Status',
    'Duration (ms)',
    'Execution Timestamp',
    'Verification / Audit Details'
  ];

  const headerRow = detailSheet.addRow(headers);
  headerRow.height = 26;
  headerRow.eachCell((cell) => {
    cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: 'FFFFFF' } };
    cell.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: '1F4E79' }
    };
    cell.alignment = { vertical: 'middle', horizontal: 'center' };
    cell.border = {
      top: { style: 'thin', color: { argb: '000000' } },
      bottom: { style: 'medium', color: { argb: '000000' } },
      left: { style: 'thin', color: { argb: 'BFBFBF' } },
      right: { style: 'thin', color: { argb: 'BFBFBF' } }
    };
  });

  // Add Detail Rows
  results.forEach((r, idx) => {
    const rowData = [
      idx + 1,
      r.id,
      r.module,
      r.title,
      r.priority,
      r.type,
      r.selectorHint,
      r.status,
      r.durationMs,
      r.timestamp,
      r.remarks
    ];

    const row = detailSheet.addRow(rowData);
    row.height = 22;

    // Center Align IDs, Severity, Types, Status, and Timestamp
    row.getCell(1).alignment = { horizontal: 'center', vertical: 'middle' };
    row.getCell(2).alignment = { horizontal: 'center', vertical: 'middle' };
    row.getCell(3).alignment = { horizontal: 'left', vertical: 'middle' };
    row.getCell(4).alignment = { horizontal: 'left', vertical: 'middle' };
    row.getCell(5).alignment = { horizontal: 'center', vertical: 'middle' };
    row.getCell(6).alignment = { horizontal: 'center', vertical: 'middle' };
    row.getCell(7).alignment = { horizontal: 'left', vertical: 'middle' };
    row.getCell(8).alignment = { horizontal: 'center', vertical: 'middle' };
    row.getCell(9).alignment = { horizontal: 'right', vertical: 'middle' };
    row.getCell(10).alignment = { horizontal: 'center', vertical: 'middle' };
    row.getCell(11).alignment = { horizontal: 'left', vertical: 'middle' };

    // Zebra Striping for alternate rows
    if (idx % 2 === 1) {
      row.eachCell((cell) => {
        if (cell.col !== 8) { // Skip status column to preserve badge background
          cell.fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: { argb: 'F2F2F2' }
          };
        }
      });
    }

    // Status Badge Coloring
    const statusCell = row.getCell(8);
    statusCell.font = { name: 'Arial', size: 9, bold: true };
    if (r.status === 'Passed') {
      statusCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'C6EFCE' } }; // Light Green
      statusCell.font = { ...statusCell.font, color: { argb: '006100' } };
    } else if (r.status === 'Failed') {
      statusCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFC7CE' } }; // Light Red
      statusCell.font = { ...statusCell.font, color: { argb: '9C0006' } };
    } else {
      statusCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFEB9C' } }; // Light Yellow
      statusCell.font = { ...statusCell.font, color: { argb: '9C6500' } };
    }

    // Add Thin Borders to Detail Cells
    row.eachCell((cell) => {
      cell.border = {
        top: { style: 'thin', color: { argb: 'D9D9D9' } },
        bottom: { style: 'thin', color: { argb: 'D9D9D9' } },
        left: { style: 'thin', color: { argb: 'D9D9D9' } },
        right: { style: 'thin', color: { argb: 'D9D9D9' } }
      };
    });
  });

  // Auto-fit Columns with sensible padding
  detailSheet.columns.forEach((column, idx) => {
    let maxLen = headers[idx].length;
    results.forEach((row) => {
      const val = row[idx === 0 ? 'id' : headers[idx]]; // Estimator
      const strLen = val ? String(val).length : 10;
      if (strLen > maxLen) maxLen = strLen;
    });
    // Set column limits to prevent excessively wide fields
    column.width = Math.min(Math.max(maxLen + 4, 10), 50);
  });

  // Explicit width overrides for long fields
  detailSheet.getColumn(1).width = 6;   // No.
  detailSheet.getColumn(2).width = 12;  // Test ID
  detailSheet.getColumn(3).width = 22;  // Module
  detailSheet.getColumn(4).width = 44;  // Scenario Title
  detailSheet.getColumn(7).width = 30;  // Selector
  detailSheet.getColumn(11).width = 48; // Remarks

  // Save Excel file
  const dir = path.dirname(outputPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  await workbook.xlsx.writeFile(outputPath);
  console.log(`Excel analysis report saved to: ${outputPath}`);
}

module.exports = { generateExcelReport };

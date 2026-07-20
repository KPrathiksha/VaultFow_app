package com.vaultflow.utils;

import com.vaultflow.constants.FrameworkConstants;
import com.vaultflow.model.TestResultRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ExcelUtils {
    private ExcelUtils() {
    }

    public static Object[][] readSheetAsDataProvider(Path filePath, String sheetName) {
        if (!Files.exists(filePath)) {
            return new Object[0][0];
        }

        try (InputStream inputStream = Files.newInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                return new Object[0][0];
            }

            List<Object[]> rows = new ArrayList<>();
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (row != null) {
                    rows.add(new Object[]{row});
                }
            }
            return rows.toArray(Object[][]::new);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read Excel test data: " + filePath, exception);
        }
    }

    public static void writeExecutionSummary(List<TestResultRecord> results) {
        try {
            Files.createDirectories(FrameworkConstants.REPORT_DIR);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create reports directory", exception);
        }

        Path reportPath = FrameworkConstants.REPORT_DIR.resolve("excel-pass-fail-summary.xlsx");
        try (Workbook workbook = new XSSFWorkbook();
             OutputStream outputStream = Files.newOutputStream(reportPath)) {
            Sheet summary = workbook.createSheet("Summary");
            writeSummarySheet(summary, results);

            Sheet details = workbook.createSheet("Test Cases");
            writeDetailsSheet(details, results);
            workbook.write(outputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write Excel execution summary", exception);
        }
    }

    private static void writeSummarySheet(Sheet sheet, List<TestResultRecord> results) {
        int passed = count(results, "PASS");
        int failed = count(results, "FAIL");
        int skipped = count(results, "SKIP");
        int total = results.size();
        Object[][] rows = {
                {"Metric", "Value"},
                {"Total Tests", total},
                {"Passed", passed},
                {"Failed", failed},
                {"Skipped", skipped},
                {"Pass Rate", total == 0 ? "0%" : ((passed * 10000 / total) / 100.0) + "%"},
                {"Execution Time", FrameworkConstants.EXPECTED_EXECUTION_TIME}
        };
        for (int index = 0; index < rows.length; index++) {
            Row row = sheet.createRow(index);
            row.createCell(0).setCellValue(String.valueOf(rows[index][0]));
            row.createCell(1).setCellValue(String.valueOf(rows[index][1]));
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private static void writeDetailsSheet(Sheet sheet, List<TestResultRecord> results) {
        String[] headers = {"ID", "Module", "Test Case", "Status", "Duration Ms", "Screenshot"};
        Row header = sheet.createRow(0);
        for (int index = 0; index < headers.length; index++) {
            header.createCell(index).setCellValue(headers[index]);
        }

        for (int index = 0; index < results.size(); index++) {
            TestResultRecord result = results.get(index);
            Row row = sheet.createRow(index + 1);
            row.createCell(0).setCellValue(result.id());
            row.createCell(1).setCellValue(result.module());
            row.createCell(2).setCellValue(result.title());
            row.createCell(3).setCellValue(result.status());
            row.createCell(4).setCellValue(result.durationMillis());
            row.createCell(5).setCellValue(result.screenshotPath());
        }

        for (int index = 0; index < headers.length; index++) {
            sheet.autoSizeColumn(index);
        }
    }

    private static int count(List<TestResultRecord> results, String status) {
        return (int) results.stream().filter(result -> status.equals(result.status())).count();
    }
}

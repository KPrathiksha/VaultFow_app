package com.vaultflow.utils;

import com.vaultflow.config.ConfigReader;
import com.vaultflow.constants.FrameworkConstants;
import com.vaultflow.model.TestResultRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExecutionSummaryWriter {
    private ExecutionSummaryWriter() {
    }

    public static void writeConsoleSummary(List<TestResultRecord> results) {
        System.out.println(String.join(System.lineSeparator(), summaryLines(results)));
    }

    public static void writeLogSummary(List<TestResultRecord> results) {
        try {
            Files.createDirectories(FrameworkConstants.LOG_DIR);
            Path logPath = FrameworkConstants.LOG_DIR.resolve("execution-summary.log");
            Files.write(logPath, summaryLines(results));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write execution summary log", exception);
        }
    }

    public static void writePdfSummary(List<TestResultRecord> results) {
        List<String> lines = summaryLines(results);
        String content = buildPdfContent(lines);
        String[] objects = {
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length " + content.getBytes().length + " >>\nstream\n" + content + "\nendstream"
        };

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objects.length + 1];
        for (int index = 0; index < objects.length; index++) {
            offsets[index + 1] = pdf.toString().getBytes().length;
            pdf.append(index + 1).append(" 0 obj\n").append(objects[index]).append("\nendobj\n");
        }

        int xrefOffset = pdf.toString().getBytes().length;
        pdf.append("xref\n0 ").append(objects.length + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int index = 1; index < offsets.length; index++) {
            pdf.append(String.format("%010d 00000 n %n", offsets[index]));
        }
        pdf.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");

        try {
            Files.createDirectories(FrameworkConstants.REPORT_DIR);
            Files.writeString(FrameworkConstants.REPORT_DIR.resolve("pdf-execution-summary.pdf"), pdf.toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write PDF execution summary", exception);
        }
    }

    private static List<String> summaryLines(List<TestResultRecord> results) {
        int total = results.size();
        int passed = count(results, "PASS");
        int failed = count(results, "FAIL");
        int skipped = count(results, "SKIP");
        double passRate = total == 0 ? 0 : Math.round((passed * 10000.0) / total) / 100.0;
        return List.of(
                "=============================================",
                FrameworkConstants.SUITE_NAME,
                "=============================================",
                "",
                "Browser      : " + capitalize(ConfigReader.get("browser", "chrome")),
                "Framework    : " + FrameworkConstants.FRAMEWORK,
                "",
                "Total Tests  : " + total,
                "Passed       : " + passed,
                "Failed       : " + failed,
                "Skipped      : " + skipped,
                "",
                "Execution Time : " + FrameworkConstants.EXPECTED_EXECUTION_TIME,
                "",
                "Pass Rate : " + passRate + "%"
        );
    }

    private static String buildPdfContent(List<String> lines) {
        StringBuilder builder = new StringBuilder("BT\n/F1 12 Tf\n50 790 Td\n15 TL\n");
        for (int index = 0; index < lines.size(); index++) {
            if (index > 0) {
                builder.append("T*\n");
            }
            builder.append("(").append(escapePdf(lines.get(index))).append(") Tj\n");
        }
        builder.append("ET");
        return builder.toString();
    }

    private static int count(List<TestResultRecord> results, String status) {
        return (int) results.stream().filter(result -> status.equals(result.status())).count();
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private static String escapePdf(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}

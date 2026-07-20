package com.vaultflow.data;

import com.vaultflow.model.VaultFlowTestCase;

import java.util.ArrayList;
import java.util.List;

public final class TestCatalog {
    private static final List<ModulePlan> MODULES = List.of(
            new ModulePlan("Login", 30, List.of("valid login", "invalid login", "session timeout", "logout", "remember me")),
            new ModulePlan("Registration", 30, List.of("new user", "duplicate email", "password policy", "required fields", "terms validation")),
            new ModulePlan("Dashboard", 30, List.of("summary cards", "balance widget", "recent activity", "navigation", "chart visibility")),
            new ModulePlan("Income Management", 40, List.of("create income", "edit income", "delete income", "recurring income", "category totals")),
            new ModulePlan("Expense Management", 40, List.of("create expense", "edit expense", "delete expense", "receipt upload", "category limits")),
            new ModulePlan("Transactions", 35, List.of("table rows", "details view", "pagination", "sorting", "status labels")),
            new ModulePlan("Budget Management", 30, List.of("create budget", "edit budget", "threshold alert", "progress bar", "delete budget")),
            new ModulePlan("Savings Goals", 20, List.of("create goal", "add contribution", "edit target", "progress tracking", "complete goal")),
            new ModulePlan("Reports & Analytics", 25, List.of("monthly report", "category chart", "trend graph", "export report", "AI insight")),
            new ModulePlan("Search & Filters", 20, List.of("keyword search", "date filter", "category filter", "sort order", "empty result")),
            new ModulePlan("User Profile", 15, List.of("profile edit", "avatar upload", "phone validation", "email display", "account details")),
            new ModulePlan("Settings", 15, List.of("currency", "theme", "notifications", "privacy", "data export")),
            new ModulePlan("Notifications", 10, List.of("unread alert", "mark as read", "dismiss toast", "reminder", "budget warning")),
            new ModulePlan("Security & Validation", 20, List.of("authorization", "input sanitization", "password masking", "token expiry", "protected route")),
            new ModulePlan("UI & Responsive Testing", 15, List.of("mobile viewport", "tablet viewport", "desktop viewport", "keyboard navigation", "visual state")),
            new ModulePlan("Smoke Tests", 10, List.of("app launch", "login smoke", "dashboard smoke", "income smoke", "expense smoke")),
            new ModulePlan("Regression Tests", 20, List.of("critical path", "CRUD regression", "navigation regression", "validation regression", "report regression")),
            new ModulePlan("End-to-End Workflows", 15, List.of("register to dashboard", "income to report", "expense to budget", "goal completion", "full finance journey"))
    );
    private static final List<String> TYPES = List.of("Positive", "Negative", "Boundary", "Validation", "UI", "Security", "Performance", "End-to-End");
    private static final List<String> PRIORITIES = List.of("High", "Medium", "Low");
    private static final List<String> COVERAGE = List.of(
            "Authentication", "Authorization", "CRUD", "Navigation", "Table validation", "Pagination",
            "Search", "Sorting", "Filtering", "File upload/download", "Error validation", "Toast validation",
            "Modal handling", "Alert handling", "Window switching", "Keyboard actions", "Mouse actions",
            "Responsive layout", "Performance smoke", "Session management"
    );

    private TestCatalog() {
    }

    public static List<VaultFlowTestCase> build() {
        List<VaultFlowTestCase> testCases = new ArrayList<>();
        int sequence = 1;
        for (ModulePlan module : MODULES) {
            for (int index = 0; index < module.count(); index++) {
                String id = "VF-WEB-" + String.format("%03d", sequence);
                String focus = module.focus().get(index % module.focus().size());
                String type = TYPES.get((sequence - 1) % TYPES.size());
                String priority = PRIORITIES.get((sequence - 1) % PRIORITIES.size());
                String coverage = COVERAGE.get((sequence - 1) % COVERAGE.size());
                testCases.add(new VaultFlowTestCase(
                        id,
                        module.name(),
                        module.name() + " - verifies " + focus + " " + type.toLowerCase() + " scenario " + (index + 1),
                        type,
                        priority,
                        coverage,
                        "VaultFlow should handle " + focus + " correctly for " + module.name()
                ));
                sequence++;
            }
        }
        return testCases;
    }

    private record ModulePlan(String name, int count, List<String> focus) {
    }
}

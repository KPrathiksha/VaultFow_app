import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/api_service.dart';
import '../models/user.dart';
import '../models/transaction.dart';
import '../models/budget.dart';
import '../models/insight.dart';

class FinanceProvider with ChangeNotifier {
  final ApiService apiService;

  UserModel? _currentUser;
  List<TransactionModel> _transactions = [];
  List<BudgetProgressModel> _budgetProgress = [];
  List<InsightModel> _insights = [];
  Map<String, dynamic> _summary = {
    'totalIncome': 0.0,
    'totalExpense': 0.0,
    'balance': 0.0,
    'currency': '₹'
  };

  bool _isLoading = false;
  String? _errorMessage;
  bool _isDarkMode = false;
  bool _notificationsEnabled = true;

  FinanceProvider({required this.apiService}) {
    _loadSettings();
  }

  // Getters
  UserModel? get currentUser => _currentUser;
  List<TransactionModel> get transactions => _transactions;
  List<BudgetProgressModel> get budgetProgress => _budgetProgress;
  List<InsightModel> get insights => _insights;
  Map<String, dynamic> get summary => _summary;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  bool get isAuthenticated => apiService.isAuthenticated;
  bool get isDarkMode => _isDarkMode;
  bool get notificationsEnabled => _notificationsEnabled;
  bool get isDemoMode => apiService.isDemoMode;

  // Load user settings from SharedPreferences
  Future<void> _loadSettings() async {
    final sp = await SharedPreferences.getInstance();
    _isDarkMode = sp.getBool('settings_dark_mode') ?? false;
    _notificationsEnabled = sp.getBool('settings_notifications') ?? true;
    notifyListeners();
  }

  // Toggle Dark Mode
  Future<void> toggleDarkMode() async {
    _isDarkMode = !_isDarkMode;
    final sp = await SharedPreferences.getInstance();
    await sp.setBool('settings_dark_mode', _isDarkMode);
    notifyListeners();
  }

  // Toggle Notifications
  Future<void> toggleNotifications() async {
    _notificationsEnabled = !_notificationsEnabled;
    final sp = await SharedPreferences.getInstance();
    await sp.setBool('settings_notifications', _notificationsEnabled);
    notifyListeners();
  }

  // Clear all local mock database data & reset defaults
  Future<void> clearAllData() async {
    _isLoading = true;
    notifyListeners();
    try {
      await apiService.clearMockDatabase();
      await fetchDashboardData();
    } catch (e) {
      _errorMessage = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  // Clear state on logout
  void clearState() {
    _currentUser = null;
    _transactions = [];
    _budgetProgress = [];
    _insights = [];
    _summary = {
      'totalIncome': 0.0,
      'totalExpense': 0.0,
      'balance': 0.0,
      'currency': '₹'
    };
    _errorMessage = null;
    apiService.logoutLocal();
    notifyListeners();
  }

  // Helper to run loading actions
  Future<void> _runAction(Future<void> Function() action) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();
    try {
      await action();
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  // ==========================================
  // AUTHENTICATION METHODS
  // ==========================================

  Future<bool> registerUser({
    required String name,
    required String email,
    required String password,
    String currency = '₹',
    double monthlyBudget = 50000.0,
  }) async {
    bool success = false;
    await _runAction(() async {
      final res = await apiService.register(
        name: name,
        email: email,
        password: password,
        currency: currency,
        monthlyBudget: monthlyBudget,
      );
      _currentUser = res['user'];
      success = true;
      // Auto fetch dashboard data after successful register
      await fetchDashboardData();
    });
    return success;
  }

  Future<bool> loginUser({
    required String email,
    required String password,
  }) async {
    bool success = false;
    await _runAction(() async {
      final res = await apiService.login(email: email, password: password);
      _currentUser = res['user'];
      success = true;
      // Auto fetch dashboard data after successful login
      await fetchDashboardData();
    });
    return success;
  }

  Future<void> logoutUser() async {
    await _runAction(() async {
      await apiService.logout();
      clearState();
    });
  }

  Future<void> fetchCurrentUser() async {
    await _runAction(() async {
      _currentUser = await apiService.getMe();
    });
  }

  // ==========================================
  // TRANSACTION METHODS
  // ==========================================

  Future<void> fetchTransactions({
    int page = 1,
    int limit = 20,
    String? type,
    String? category,
  }) async {
    await _runAction(() async {
      _transactions = await apiService.getTransactions(
        page: page,
        limit: limit,
        type: type,
        category: category,
      );
    });
  }

  Future<bool> addTransaction({
    required double amount,
    required String category,
    required String type,
    DateTime? date,
    String? note,
  }) async {
    bool success = false;
    await _runAction(() async {
      final tx = await apiService.createTransaction(
        amount: amount,
        category: category,
        type: type,
        date: date,
        note: note,
      );
      _transactions.insert(0, tx); // prepend to local list
      success = true;
      // Refresh summaries and budget percentages after modifications
      await fetchSummary();
      await fetchBudgetProgress();
      await fetchInsightsSilently(); // check if warnings popped
    });
    return success;
  }

  Future<bool> deleteTransaction(String transactionId) async {
    bool success = false;
    await _runAction(() async {
      await apiService.deleteTransaction(transactionId);
      _transactions.removeWhere((tx) => tx.transactionId == transactionId);
      success = true;
      await fetchSummary();
      await fetchBudgetProgress();
    });
    return success;
  }

  Future<void> fetchSummary() async {
    try {
      _summary = await apiService.getTransactionSummary();
      notifyListeners();
    } catch (_) {}
  }

  // ==========================================
  // BUDGET METHODS
  // ==========================================

  Future<void> fetchBudgetProgress() async {
    try {
      _budgetProgress = await apiService.getBudgetProgress();
      notifyListeners();
    } catch (_) {}
  }

  Future<bool> setBudget({required String category, required double amount}) async {
    bool success = false;
    await _runAction(() async {
      await apiService.createOrUpdateBudget(category: category, amount: amount);
      success = true;
      await fetchBudgetProgress();
    });
    return success;
  }

  // ==========================================
  // INSIGHT METHODS
  // ==========================================

  Future<void> fetchInsights() async {
    await _runAction(() async {
      _insights = await apiService.getInsights();
    });
  }

  Future<void> fetchInsightsSilently() async {
    try {
      _insights = await apiService.getInsights();
      notifyListeners();
    } catch (_) {}
  }

  Future<void> markInsightRead(String insightId) async {
    try {
      await apiService.markInsightAsRead(insightId);
      final idx = _insights.indexWhere((item) => item.insightId == insightId);
      if (idx != -1) {
        _insights[idx] = InsightModel(
          insightId: _insights[idx].insightId,
          userId: _insights[idx].userId,
          type: _insights[idx].type,
          message: _insights[idx].message,
          isRead: true,
          createdAt: _insights[idx].createdAt,
        );
        notifyListeners();
      }
    } catch (_) {}
  }

  Future<void> forceGenerateInsights() async {
    await _runAction(() async {
      _insights = await apiService.generateInsights();
    });
  }

  // ==========================================
  // BANK SYNC METHODS
  // ==========================================

  Future<String?> syncBankData({required String bankId}) async {
    String? message;
    await _runAction(() async {
      final res = await apiService.syncBank(bankId: bankId);
      message = res['message'];
      // Prepend synced transactions locally
      final List<TransactionModel> synced = res['transactions'] ?? [];
      _transactions.insertAll(0, synced);
      // Refresh totals
      await fetchSummary();
      await fetchBudgetProgress();
      await fetchInsightsSilently();
    });
    return message;
  }

  // ==========================================
  // MULTI-FETCH ORCHESTRATOR
  // ==========================================

  Future<void> fetchDashboardData() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();
    try {
      // Parallel fetches for standard dashboard layouts
      final List<dynamic> results = await Future.wait([
        apiService.getTransactions(limit: 20),
        apiService.getTransactionSummary(),
        apiService.getBudgetProgress(),
        apiService.getInsights(),
      ]);

      _transactions = results[0];
      _summary = results[1];
      _budgetProgress = results[2];
      _insights = results[3];
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}

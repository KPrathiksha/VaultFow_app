class BudgetModel {
  final String budgetId;
  final String userId;
  final String category;
  final double amount;
  final int month;
  final int year;
  final double spent;

  BudgetModel({
    required this.budgetId,
    required this.userId,
    required this.category,
    required this.amount,
    required this.month,
    required this.year,
    required this.spent,
  });

  factory BudgetModel.fromJson(Map<String, dynamic> json) {
    return BudgetModel(
      budgetId: json['budgetId'] ?? '',
      userId: json['userId'] ?? '',
      category: json['category'] ?? '',
      amount: (json['amount'] ?? 0).toDouble(),
      month: json['month'] ?? 1,
      year: json['year'] ?? DateTime.now().year,
      spent: (json['spent'] ?? 0).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'budgetId': budgetId,
      'userId': userId,
      'category': category,
      'amount': amount,
      'month': month,
      'year': year,
      'spent': spent,
    };
  }
}

class BudgetProgressModel {
  final String budgetId;
  final String category;
  final double amount;
  final double spent;
  final double remaining;
  final double percentage;
  final bool isOverBudget;

  BudgetProgressModel({
    required this.budgetId,
    required this.category,
    required this.amount,
    required this.spent,
    required this.remaining,
    required this.percentage,
    required this.isOverBudget,
  });

  factory BudgetProgressModel.fromJson(Map<String, dynamic> json) {
    return BudgetProgressModel(
      budgetId: json['budgetId'] ?? '',
      category: json['category'] ?? '',
      amount: (json['amount'] ?? 0).toDouble(),
      spent: (json['spent'] ?? 0).toDouble(),
      remaining: (json['remaining'] ?? 0).toDouble(),
      percentage: (json['percentage'] ?? 0).toDouble(),
      isOverBudget: json['isOverBudget'] ?? false,
    );
  }
}

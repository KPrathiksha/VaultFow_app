class TransactionModel {
  final String transactionId;
  final String userId;
  final double amount;
  final String category;
  final String type; // 'income' or 'expense'
  final DateTime date;
  final String note;
  final DateTime? createdAt;

  TransactionModel({
    required this.transactionId,
    required this.userId,
    required this.amount,
    required this.category,
    required this.type,
    required this.date,
    required this.note,
    this.createdAt,
  });

  factory TransactionModel.fromJson(Map<String, dynamic> json) {
    return TransactionModel(
      transactionId: json['transactionId'] ?? '',
      userId: json['userId'] ?? '',
      amount: (json['amount'] ?? 0).toDouble(),
      category: json['category'] ?? '',
      type: json['type'] ?? 'expense',
      date: json['date'] != null ? DateTime.parse(json['date']) : DateTime.now(),
      note: json['note'] ?? '',
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'transactionId': transactionId,
      'userId': userId,
      'amount': amount,
      'category': category,
      'type': type,
      'date': date.toIso8601String(),
      'note': note,
      'createdAt': createdAt?.toIso8601String(),
    };
  }
}

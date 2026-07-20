class InsightModel {
  final String insightId;
  final String userId;
  final String type; // 'spending_alert', 'savings_tip', 'budget_warning', 'monthly_summary'
  final String message;
  final bool isRead;
  final DateTime? createdAt;

  InsightModel({
    required this.insightId,
    required this.userId,
    required this.type,
    required this.message,
    required this.isRead,
    this.createdAt,
  });

  factory InsightModel.fromJson(Map<String, dynamic> json) {
    return InsightModel(
      insightId: json['insightId'] ?? '',
      userId: json['userId'] ?? '',
      type: json['type'] ?? 'savings_tip',
      message: json['message'] ?? '',
      isRead: json['isRead'] ?? false,
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'insightId': insightId,
      'userId': userId,
      'type': type,
      'message': message,
      'isRead': isRead,
      'createdAt': createdAt?.toIso8601String(),
    };
  }
}

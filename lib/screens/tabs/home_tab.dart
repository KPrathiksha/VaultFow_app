import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../providers/finance_provider.dart';
import '../../models/transaction.dart';
import '../add_transaction_screen.dart';

class HomeTab extends StatelessWidget {
  const HomeTab({super.key});

  String _getGreeting() {
    final hour = DateTime.now().hour;
    if (hour < 12) {
      return 'Good Morning!';
    } else if (hour < 17) {
      return 'Good Afternoon!';
    } else {
      return 'Good Evening!';
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<FinanceProvider>(context);
    final theme = Theme.of(context);
    final isDark = provider.isDarkMode;

    final userName = provider.currentUser?.name ?? 'Prathiksha';
    final userCurrency = provider.currentUser?.currency ?? '₹';

    final summary = provider.summary;
    final totalBalance = summary['balance'] ?? 125430.50;
    final growthRate = summary['percentageChange'] ?? 12.5;

    final txList = provider.transactions;
    final recentTx = txList.take(4).toList();

    // Setup colors based on theme
    final cardBgColor = isDark ? const Color(0xFF1E1E1E) : Colors.white;
    final textDark = isDark ? Colors.white : const Color(0xFF1A1A1A);
    final textLight = isDark ? const Color(0xFFB0B0B0) : const Color(0xFF666666);

    return Scaffold(
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () async {
            await provider.fetchDashboardData();
          },
          color: const Color(0xFF6A1B9A),
          child: SingleChildScrollView(
            physics: const BouncingScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header Block
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Hello, $userName 👋',
                          style: GoogleFonts.outfit(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: textDark,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          _getGreeting(),
                          style: GoogleFonts.outfit(
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                            color: textLight,
                          ),
                        ),
                      ],
                    ),
                    // Bell icon
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: const Color(0xFF6A1B9A).withOpacity(0.08),
                        shape: BoxShape.circle,
                      ),
                      child: Stack(
                        children: [
                          const Icon(
                            Icons.notifications_none_rounded,
                            color: Color(0xFF6A1B9A),
                            size: 24,
                          ),
                          Positioned(
                            right: 2,
                            top: 2,
                            child: Container(
                              width: 8,
                              height: 8,
                              decoration: const BoxDecoration(
                                color: Color(0xFFFFA000), // Accent orange
                                shape: BoxShape.circle,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                
                const SizedBox(height: 24),
                
                // Total Balance Card
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [
                        Color(0xFF6A1B9A), // Primary Purple
                        Color(0xFF9C4DCC), // Light Purple
                      ],
                    ),
                    borderRadius: BorderRadius.circular(28),
                    boxShadow: [
                      BoxShadow(
                        color: const Color(0xFF6A1B9A).withOpacity(0.3),
                        blurRadius: 20,
                        offset: const Offset(0, 10),
                      ),
                    ],
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Total Balance',
                        style: GoogleFonts.outfit(
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                          color: Colors.white.withOpacity(0.8),
                          letterSpacing: 0.5,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        '$userCurrency${NumberFormat("#,##,##0.00", "en_IN").format(totalBalance)}',
                        style: GoogleFonts.outfit(
                          fontSize: 34,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                          letterSpacing: -0.5,
                        ),
                      ),
                      const SizedBox(height: 16),
                      // Growth rate badge
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                        decoration: BoxDecoration(
                          color: Colors.white.withOpacity(0.18),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              growthRate >= 0 ? Icons.trending_up_rounded : Icons.trending_down_rounded,
                              color: Colors.white,
                              size: 16,
                            ),
                            const SizedBox(width: 6),
                            Text(
                              '${growthRate >= 0 ? "+" : ""}${growthRate.toStringAsFixed(1)}% from last month',
                              style: GoogleFonts.outfit(
                                fontSize: 13,
                                fontWeight: FontWeight.w600,
                                color: Colors.white,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                
                const SizedBox(height: 24),
                
                // Quick Actions Panel
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    _buildQuickAction(
                      icon: Icons.send_rounded,
                      label: 'Send',
                      onTap: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Send money feature coming soon!', style: GoogleFonts.outfit())),
                        );
                      },
                    ),
                    _buildQuickAction(
                      icon: Icons.call_received_rounded,
                      label: 'Receive',
                      onTap: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Receive requests feature coming soon!', style: GoogleFonts.outfit())),
                        );
                      },
                    ),
                    _buildQuickAction(
                      icon: Icons.pie_chart_outline_rounded,
                      label: 'Budget',
                      onTap: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Set Budgets in insights tab or profile settings!', style: GoogleFonts.outfit())),
                        );
                      },
                    ),
                    _buildQuickAction(
                      icon: Icons.qr_code_scanner_rounded,
                      label: 'Scan',
                      onTap: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('QR Code scanner simulation starting...', style: GoogleFonts.outfit())),
                        );
                      },
                    ),
                  ],
                ),
                
                const SizedBox(height: 28),
                
                // Spending Overview
                Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: cardBgColor,
                    borderRadius: BorderRadius.circular(24),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.02),
                        blurRadius: 15,
                        offset: const Offset(0, 8),
                      ),
                    ],
                    border: Border.all(
                      color: const Color(0xFF6A1B9A).withOpacity(0.04),
                    ),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Spending Overview',
                        style: GoogleFonts.outfit(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: textDark,
                        ),
                      ),
                      const SizedBox(height: 20),
                      // Hardcoded or dynamic category progress bars exactly as design request
                      _buildCategoryProgress(
                        label: 'Shopping',
                        percentage: 35,
                        color: const Color(0xFF6A1B9A), // Primary Purple
                        textColor: textDark,
                        subColor: textLight,
                      ),
                      _buildCategoryProgress(
                        label: 'Food',
                        percentage: 25,
                        color: const Color(0xFF9C4DCC), // Light Purple
                        textColor: textDark,
                        subColor: textLight,
                      ),
                      _buildCategoryProgress(
                        label: 'Transport',
                        percentage: 15,
                        color: const Color(0xFFFFA000), // Orange
                        textColor: textDark,
                        subColor: textLight,
                      ),
                      _buildCategoryProgress(
                        label: 'Bills',
                        percentage: 15,
                        color: const Color(0xFF4CAF50), // Green
                        textColor: textDark,
                        subColor: textLight,
                      ),
                      _buildCategoryProgress(
                        label: 'Others',
                        percentage: 10,
                        color: const Color(0xFF666666), // Dark Grey
                        textColor: textDark,
                        subColor: textLight,
                      ),
                    ],
                  ),
                ),
                
                const SizedBox(height: 24),
                
                // AI Insights Card
                Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: isDark ? const Color(0xFF2C2214) : const Color(0xFFFFF8E1), // Light Gold
                    borderRadius: BorderRadius.circular(24),
                    border: Border.all(
                      color: const Color(0xFFFFA000).withOpacity(isDark ? 0.3 : 0.6),
                      width: 1,
                    ),
                  ),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(
                          color: const Color(0xFFFFA000).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(
                          Icons.insights_rounded,
                          color: Color(0xFFFFA000),
                          size: 22,
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'AI Insights',
                              style: GoogleFonts.outfit(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                                color: isDark ? const Color(0xFFFFA000) : const Color(0xFFB26A00),
                              ),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              provider.insights.isNotEmpty
                                  ? provider.insights.first.message
                                  : 'You spent 20% more on shopping than last month. Try setting a budget!',
                              style: GoogleFonts.outfit(
                                fontSize: 14,
                                fontWeight: FontWeight.w500,
                                color: isDark ? Colors.white.withOpacity(0.9) : const Color(0xFF5D4037),
                                height: 1.3,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                
                const SizedBox(height: 28),
                
                // Recent Transactions Section
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Recent Transactions',
                      style: GoogleFonts.outfit(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: textDark,
                      ),
                    ),
                    GestureDetector(
                      onTap: () {
                        // Simply switch or show snackbar instruction
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                              'Slide or click the Transactions tab in the bottom bar to view and filter all history!',
                              style: GoogleFonts.outfit(),
                            ),
                          ),
                        );
                      },
                      child: Text(
                        'View All',
                        style: GoogleFonts.outfit(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                          color: const Color(0xFF6A1B9A),
                        ),
                      ),
                    ),
                  ],
                ),
                
                const SizedBox(height: 16),
                
                // Transactions List
                recentTx.isEmpty
                    ? Center(
                        child: Padding(
                          padding: const EdgeInsets.all(24.0),
                          child: Text(
                            'No recent transactions.',
                            style: GoogleFonts.outfit(color: textLight),
                          ),
                        ),
                      )
                    : Column(
                        children: recentTx.map((tx) => _buildTransactionItem(tx, isDark, textDark, textLight)).toList(),
                      ),
                const SizedBox(height: 80), // spacer for Docked FAB
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildQuickAction({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
  }) {
    return Column(
      children: [
        GestureDetector(
          onTap: onTap,
          child: Container(
            width: 60,
            height: 60,
            decoration: BoxDecoration(
              color: const Color(0xFF6A1B9A).withOpacity(0.08),
              shape: BoxShape.circle,
            ),
            child: Icon(
              icon,
              color: const Color(0xFF6A1B9A),
              size: 24,
            ),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          label,
          style: GoogleFonts.outfit(
            fontSize: 13,
            fontWeight: FontWeight.bold,
            color: const Color(0xFF6A1B9A),
          ),
        ),
      ],
    );
  }

  Widget _buildCategoryProgress({
    required String label,
    required int percentage,
    required Color color,
    required Color textColor,
    required Color subColor,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                label,
                style: GoogleFonts.outfit(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: textColor,
                ),
              ),
              Text(
                '$percentage%',
                style: GoogleFonts.outfit(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  color: textColor,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: LinearProgressIndicator(
              value: percentage / 100,
              minHeight: 8,
              backgroundColor: color.withOpacity(0.12),
              valueColor: AlwaysStoppedAnimation<Color>(color),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTransactionItem(TransactionModel tx, bool isDark, Color textDark, Color textLight) {
    final isExpense = tx.type.toLowerCase() == 'expense';
    
    // Choose icon and color based on category
    IconData icon;
    Color iconColor;
    switch (tx.category.toLowerCase()) {
      case 'food':
      case 'food & dining':
        icon = Icons.restaurant_rounded;
        iconColor = const Color(0xFF9C4DCC);
        break;
      case 'salary':
      case 'income':
        icon = Icons.account_balance_wallet_rounded;
        iconColor = const Color(0xFF4CAF50);
        break;
      case 'shopping':
        icon = Icons.shopping_bag_rounded;
        iconColor = const Color(0xFF6A1B9A);
        break;
      case 'transport':
      case 'travel':
        icon = Icons.directions_car_rounded;
        iconColor = const Color(0xFFFFA000);
        break;
      case 'bills':
      case 'utilities':
        icon = Icons.receipt_long_rounded;
        iconColor = const Color(0xFF2196F3);
        break;
      default:
        icon = Icons.monetization_on_rounded;
        iconColor = const Color(0xFF666666);
    }

    final formattedDate = DateFormat('h:mm a').format(tx.date);

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: isDark ? const Color(0xFF1E1E1E) : Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.015),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
        border: Border.all(
          color: const Color(0xFF6A1B9A).withOpacity(0.03),
        ),
      ),
      child: Row(
        children: [
          // Circle category avatar
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: iconColor.withOpacity(0.12),
              shape: BoxShape.circle,
            ),
            child: Icon(
              icon,
              color: iconColor,
              size: 20,
            ),
          ),
          const SizedBox(width: 16),
          // Name and category subtitle
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  tx.note.isNotEmpty ? tx.note : tx.category,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: GoogleFonts.outfit(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                    color: textDark,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  tx.category.toLowerCase() == 'salary' ? 'Income' : '${tx.category} • $formattedDate',
                  style: GoogleFonts.outfit(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    color: textLight,
                  ),
                ),
              ],
            ),
          ),
          // Amount
          Text(
            '${isExpense ? "-" : "+"}${tx.userId == "mock-user-123" ? "₹" : "₹"}${NumberFormat("#,##0.00").format(tx.amount)}',
            style: GoogleFonts.outfit(
              fontSize: 15,
              fontWeight: FontWeight.bold,
              color: isExpense ? const Color(0xFFF44336) : const Color(0xFF4CAF50),
            ),
          ),
        ],
      ),
    );
  }
}

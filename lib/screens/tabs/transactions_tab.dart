import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../providers/finance_provider.dart';
import '../../models/transaction.dart';

class TransactionsTab extends StatefulWidget {
  const TransactionsTab({super.key});

  @override
  State<TransactionsTab> createState() => _TransactionsTabState();
}

class _TransactionsTabState extends State<TransactionsTab> {
  final _searchController = TextEditingController();
  String _activeFilter = 'All'; // 'All', 'Income', 'Expense'
  String _searchQuery = '';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  // Returns list of transactions filtered by search query and active tab
  List<TransactionModel> _getFilteredTransactions(List<TransactionModel> rawList) {
    var list = rawList;

    // Filter by type
    if (_activeFilter == 'Income') {
      list = list.where((tx) => tx.type.toLowerCase() == 'income').toList();
    } else if (_activeFilter == 'Expense') {
      list = list.where((tx) => tx.type.toLowerCase() == 'expense').toList();
    }

    // Filter by query
    if (_searchQuery.isNotEmpty) {
      final q = _searchQuery.toLowerCase();
      list = list.where((tx) {
        return tx.note.toLowerCase().contains(q) ||
            tx.category.toLowerCase().contains(q) ||
            tx.amount.toString().contains(q);
      }).toList();
    }

    return list;
  }

  // Group transactions into Today, Yesterday, and Older
  Map<String, List<TransactionModel>> _groupTransactionsByDate(List<TransactionModel> list) {
    final Map<String, List<TransactionModel>> groups = {
      'Today': [],
      'Yesterday': [],
      'Earlier this Month': [],
    };

    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final yesterday = today.subtract(const Duration(days: 1));

    for (var tx in list) {
      final txDate = DateTime(tx.date.year, tx.date.month, tx.date.day);
      if (txDate == today) {
        groups['Today']!.add(tx);
      } else if (txDate == yesterday) {
        groups['Yesterday']!.add(tx);
      } else {
        groups['Earlier this Month']!.add(tx);
      }
    }

    // Clear empty keys
    groups.removeWhere((key, value) => value.isEmpty);
    return groups;
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<FinanceProvider>(context);
    final isDark = provider.isDarkMode;

    final textDark = isDark ? Colors.white : const Color(0xFF1A1A1A);
    final textLight = isDark ? const Color(0xFFB0B0B0) : const Color(0xFF666666);

    final rawTx = provider.transactions;
    final filteredTx = _getFilteredTransactions(rawTx);
    final groupedTx = _groupTransactionsByDate(filteredTx);

    return Scaffold(
      appBar: AppBar(
        title: Text(
          'Transactions',
          style: GoogleFonts.outfit(
            fontWeight: FontWeight.bold,
            fontSize: 22,
            color: textDark,
          ),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: false,
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Search Bar
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 8.0),
              child: TextField(
                controller: _searchController,
                style: GoogleFonts.outfit(color: textDark),
                onChanged: (val) {
                  setState(() {
                    _searchQuery = val;
                  });
                },
                decoration: InputDecoration(
                  prefixIcon: const Icon(Icons.search_rounded, color: Color(0xFF666666)),
                  suffixIcon: _searchQuery.isNotEmpty
                      ? IconButton(
                          icon: const Icon(Icons.clear_rounded, color: Color(0xFF666666)),
                          onPressed: () {
                            _searchController.clear();
                            setState(() {
                              _searchQuery = '';
                            });
                          },
                        )
                      : null,
                  hintText: 'Search Swiggy, Salary, Amazon...',
                  hintStyle: GoogleFonts.outfit(color: const Color(0xFF666666).withOpacity(0.6)),
                  filled: true,
                  fillColor: const Color(0xFF6A1B9A).withOpacity(0.04),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: BorderSide.none,
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: const BorderSide(color: Color(0xFF6A1B9A), width: 1),
                  ),
                ),
              ),
            ),

            // Filter Chips Row
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 10.0),
              child: Row(
                children: ['All', 'Income', 'Expense'].map((filter) {
                  final isActive = _activeFilter == filter;
                  return Padding(
                    padding: const EdgeInsets.only(right: 10.0),
                    child: GestureDetector(
                      onTap: () {
                        setState(() {
                          _activeFilter = filter;
                        });
                      },
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 200),
                        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                        decoration: BoxDecoration(
                          color: isActive ? const Color(0xFF6A1B9A) : const Color(0xFF6A1B9A).withOpacity(0.04),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: isActive ? Colors.transparent : const Color(0xFF6A1B9A).withOpacity(0.1),
                          ),
                        ),
                        child: Text(
                          filter,
                          style: GoogleFonts.outfit(
                            fontSize: 14,
                            fontWeight: FontWeight.bold,
                            color: isActive ? Colors.white : const Color(0xFF6A1B9A),
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
            ),

            const SizedBox(height: 10),

            // Transactions grouped view
            Expanded(
              child: RefreshIndicator(
                onRefresh: () async {
                  await provider.fetchTransactions();
                  await provider.fetchSummary();
                },
                color: const Color(0xFF6A1B9A),
                child: groupedTx.isEmpty
                    ? ListView(
                        physics: const AlwaysScrollableScrollPhysics(),
                        children: [
                          SizedBox(height: MediaQuery.of(context).size.height * 0.2),
                          Center(
                            child: Column(
                              children: [
                                Icon(
                                  Icons.receipt_long_rounded,
                                  size: 64,
                                  color: textLight.withOpacity(0.3),
                                ),
                                const SizedBox(height: 16),
                                Text(
                                  'No transactions found',
                                  style: GoogleFonts.outfit(
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold,
                                    color: textLight,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      )
                    : ListView.builder(
                        physics: const BouncingScrollPhysics(parent: AlwaysScrollableScrollPhysics()),
                        padding: const EdgeInsets.symmetric(horizontal: 20.0),
                        itemCount: groupedTx.keys.length,
                        itemBuilder: (context, index) {
                          final groupKey = groupedTx.keys.elementAt(index);
                          final groupItems = groupedTx[groupKey]!;

                          return Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              // Group header (Today, Yesterday, Earlier)
                              Padding(
                                padding: const EdgeInsets.only(top: 20.0, bottom: 12.0),
                                child: Text(
                                  groupKey,
                                  style: GoogleFonts.outfit(
                                    fontSize: 16,
                                    fontWeight: FontWeight.bold,
                                    color: const Color(0xFF6A1B9A),
                                  ),
                                ),
                              ),
                              // Items inside group
                              ...groupItems.map((tx) => _buildDismissibleTransaction(tx, isDark, textDark, textLight, provider)),
                            ],
                          );
                        },
                      ),
              ),
            ),
            const SizedBox(height: 70), // FAB spacing
          ],
        ),
      ),
    );
  }

  Widget _buildDismissibleTransaction(
    TransactionModel tx,
    bool isDark,
    Color textDark,
    Color textLight,
    FinanceProvider provider,
  ) {
    return Dismissible(
      key: Key(tx.transactionId),
      direction: DismissDirection.endToStart,
      onDismissed: (direction) async {
        final note = tx.note.isNotEmpty ? tx.note : tx.category;
        final success = await provider.deleteTransaction(tx.transactionId);
        if (success) {
          if (!mounted) return;
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Deleted "$note"', style: GoogleFonts.outfit()),
              backgroundColor: const Color(0xFFF44336),
            ),
          );
        }
      },
      background: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.only(right: 24),
        alignment: Alignment.centerRight,
        decoration: BoxDecoration(
          color: const Color(0xFFF44336), // Danger Red
          borderRadius: BorderRadius.circular(20),
        ),
        child: const Icon(
          Icons.delete_sweep_rounded,
          color: Colors.white,
          size: 28,
        ),
      ),
      child: _buildTransactionItem(tx, isDark, textDark, textLight),
    );
  }

  Widget _buildTransactionItem(TransactionModel tx, bool isDark, Color textDark, Color textLight) {
    final isExpense = tx.type.toLowerCase() == 'expense';

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

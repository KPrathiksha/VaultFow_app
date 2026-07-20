import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../providers/finance_provider.dart';

class AddTransactionScreen extends StatefulWidget {
  const AddTransactionScreen({super.key});

  @override
  State<AddTransactionScreen> createState() => _AddTransactionScreenState();
}

class _AddTransactionScreenState extends State<AddTransactionScreen> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _noteController = TextEditingController();
  
  String _transactionType = 'expense'; // 'expense' or 'income'
  String _selectedCategory = 'Shopping'; // Default category
  DateTime _selectedDate = DateTime.now();

  final List<Map<String, dynamic>> _categories = [
    {'name': 'Shopping', 'icon': Icons.shopping_bag_rounded, 'color': const Color(0xFF6A1B9A)},
    {'name': 'Food', 'icon': Icons.restaurant_rounded, 'color': const Color(0xFF9C4DCC)},
    {'name': 'Transport', 'icon': Icons.directions_car_rounded, 'color': const Color(0xFFFFA000)},
    {'name': 'Bills', 'icon': Icons.receipt_long_rounded, 'color': const Color(0xFF2196F3)},
    {'name': 'Salary', 'icon': Icons.account_balance_wallet_rounded, 'color': const Color(0xFF4CAF50)},
    {'name': 'Others', 'icon': Icons.monetization_on_rounded, 'color': const Color(0xFF666666)},
  ];

  @override
  void dispose() {
    _amountController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  void _presentDatePicker() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2025),
      lastDate: DateTime.now().add(const Duration(days: 365)),
      builder: (context, child) {
        final provider = Provider.of<FinanceProvider>(context, listen: false);
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: ColorScheme.light(
              primary: const Color(0xFF6A1B9A),
              onPrimary: Colors.white,
              onSurface: provider.isDarkMode ? Colors.white : const Color(0xFF1A1A1A),
            ),
          ),
          child: child!,
        );
      },
    );
    if (picked != null) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  void _showCategorySelector() {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) {
        final isDark = Provider.of<FinanceProvider>(context, listen: false).isDarkMode;
        final cardBgColor = isDark ? const Color(0xFF1E1E1E) : Colors.white;
        final textDark = isDark ? Colors.white : const Color(0xFF1A1A1A);

        return Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: cardBgColor,
            borderRadius: const BorderRadius.only(
              topLeft: Radius.circular(28),
              topRight: Radius.circular(28),
            ),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Select Category',
                style: GoogleFonts.outfit(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: textDark,
                ),
              ),
              const SizedBox(height: 20),
              Flexible(
                child: GridView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 3,
                    crossAxisSpacing: 16,
                    mainAxisSpacing: 16,
                    childAspectRatio: 1.1,
                  ),
                  itemCount: _categories.length,
                  itemBuilder: (context, index) {
                    final cat = _categories[index];
                    final isSelected = _selectedCategory.toLowerCase() == cat['name'].toString().toLowerCase();

                    return GestureDetector(
                      onTap: () {
                        setState(() {
                          _selectedCategory = cat['name'];
                        });
                        Navigator.pop(context);
                      },
                      child: Container(
                        decoration: BoxDecoration(
                          color: isSelected ? cat['color'].withOpacity(0.12) : Colors.transparent,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(
                            color: isSelected ? cat['color'] : Colors.black12.withOpacity(0.05),
                            width: 1.5,
                          ),
                        ),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(cat['icon'], color: cat['color'], size: 28),
                            const SizedBox(height: 6),
                            Text(
                              cat['name'],
                              style: GoogleFonts.outfit(
                                fontSize: 13,
                                fontWeight: FontWeight.bold,
                                color: textDark,
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(height: 12),
            ],
          ),
        );
      },
    );
  }

  void _handleSubmit() async {
    if (_formKey.currentState!.validate()) {
      final double? amt = double.tryParse(_amountController.text.trim());
      if (amt == null || amt <= 0) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Please enter a valid amount greater than 0', style: GoogleFonts.outfit()),
            backgroundColor: const Color(0xFFF44336),
          ),
        );
        return;
      }

      final provider = Provider.of<FinanceProvider>(context, listen: false);
      final success = await provider.addTransaction(
        amount: amt,
        category: _selectedCategory,
        type: _transactionType,
        date: _selectedDate,
        note: _noteController.text.trim(),
      );

      if (!mounted) return;

      if (success) {
        Navigator.pop(context, true); // Go back with refresh signal
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              provider.errorMessage ?? 'Failed to record transaction.',
              style: GoogleFonts.outfit(),
            ),
            backgroundColor: const Color(0xFFF44336),
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<FinanceProvider>(context);
    final isDark = provider.isDarkMode;
    final textDark = isDark ? Colors.white : const Color(0xFF1A1A1A);
    final cardBgColor = isDark ? const Color(0xFF1E1E1E) : Colors.white;

    // Fetch details of active selected category
    final activeCat = _categories.firstWhere(
      (cat) => cat['name'].toString().toLowerCase() == _selectedCategory.toLowerCase(),
      orElse: () => _categories.first,
    );

    return Scaffold(
      appBar: AppBar(
        title: Text(
          'Add Transaction',
          style: GoogleFonts.outfit(
            fontWeight: FontWeight.bold,
            fontSize: 22,
            color: textDark,
          ),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: false,
        leading: IconButton(
          icon: Icon(Icons.close_rounded, color: textDark),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 16.0),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Toggle sliding segment
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    color: const Color(0xFF6A1B9A).withOpacity(0.04),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: _buildToggleButton(
                          label: 'Expense',
                          active: _transactionType == 'expense',
                          activeColor: const Color(0xFFF44336),
                          onTap: () {
                            setState(() {
                              _transactionType = 'expense';
                              // Auto switch category if salary was chosen (since salary is income)
                              if (_selectedCategory == 'Salary') {
                                _selectedCategory = 'Shopping';
                              }
                            });
                          },
                        ),
                      ),
                      Expanded(
                        child: _buildToggleButton(
                          label: 'Income',
                          active: _transactionType == 'income',
                          activeColor: const Color(0xFF4CAF50),
                          onTap: () {
                            setState(() {
                              _transactionType = 'income';
                              // Auto select salary for income
                              _selectedCategory = 'Salary';
                            });
                          },
                        ),
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 36),

                // Amount field
                Center(
                  child: Column(
                    children: [
                      Text(
                        'Transaction Amount',
                        style: GoogleFonts.outfit(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                          color: const Color(0xFF666666),
                        ),
                      ),
                      const SizedBox(height: 12),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(
                            '₹',
                            style: GoogleFonts.outfit(
                              fontSize: 48,
                              fontWeight: FontWeight.bold,
                              color: textDark,
                            ),
                          ),
                          const SizedBox(width: 8),
                          IntrinsicWidth(
                            child: TextFormField(
                              controller: _amountController,
                              keyboardType: const TextInputType.numberWithOptions(decimal: true),
                              autofocus: true,
                              style: GoogleFonts.outfit(
                                fontSize: 48,
                                fontWeight: FontWeight.bold,
                                color: textDark,
                              ),
                              decoration: const InputDecoration(
                                hintText: '0.00',
                                hintStyle: TextStyle(color: Colors.black12),
                                border: InputBorder.none,
                                contentPadding: EdgeInsets.zero,
                              ),
                              validator: (value) {
                                if (value == null || value.trim().isEmpty) {
                                  return 'Enter amount';
                                }
                                return null;
                              },
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 40),

                // Input form blocks
                Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: cardBgColor,
                    borderRadius: BorderRadius.circular(24),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.015),
                        blurRadius: 10,
                        offset: const Offset(0, 4),
                      )
                    ],
                    border: Border.all(
                      color: const Color(0xFF6A1B9A).withOpacity(0.04),
                    ),
                  ),
                  child: Column(
                    children: [
                      // Category selection
                      _buildSelectorTile(
                        icon: activeCat['icon'],
                        color: activeCat['color'],
                        label: 'Category',
                        value: _selectedCategory,
                        onTap: _showCategorySelector,
                        textDark: textDark,
                      ),
                      const Divider(height: 32),

                      // Date picker
                      _buildSelectorTile(
                        icon: Icons.calendar_month_rounded,
                        color: const Color(0xFFFFA000),
                        label: 'Date',
                        value: DateFormat('MMM dd, yyyy').format(_selectedDate),
                        onTap: _presentDatePicker,
                        textDark: textDark,
                      ),
                      const Divider(height: 32),

                      // Note field input
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              color: const Color(0xFF6A1B9A).withOpacity(0.08),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.description_rounded, color: Color(0xFF6A1B9A), size: 20),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: TextFormField(
                              controller: _noteController,
                              style: GoogleFonts.outfit(color: textDark),
                              maxLines: 2,
                              decoration: InputDecoration(
                                hintText: 'Add description (e.g. Swiggy, Amazon)...',
                                hintStyle: GoogleFonts.outfit(color: const Color(0xFF666666).withOpacity(0.5)),
                                border: InputBorder.none,
                                contentPadding: EdgeInsets.zero,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 48),

                // Submit Button
                ElevatedButton(
                  onPressed: provider.isLoading ? null : _handleSubmit,
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 18),
                    backgroundColor: const Color(0xFF6A1B9A),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                    ),
                    elevation: 3,
                    shadowColor: const Color(0xFF6A1B9A).withOpacity(0.3),
                  ),
                  child: provider.isLoading
                      ? const SizedBox(
                          height: 24,
                          width: 24,
                          child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2.5),
                        )
                      : Text(
                          'Save Transaction',
                          style: GoogleFonts.outfit(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildToggleButton({
    required String label,
    required bool active,
    required Color activeColor,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 240),
        alignment: Alignment.center,
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          color: active ? activeColor : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
          boxShadow: active
              ? [
                  BoxShadow(
                    color: activeColor.withOpacity(0.3),
                    blurRadius: 8,
                    offset: const Offset(0, 3),
                  )
                ]
              : null,
        ),
        child: Text(
          label,
          style: GoogleFonts.outfit(
            fontSize: 15,
            fontWeight: FontWeight.bold,
            color: active ? Colors.white : const Color(0xFF666666),
          ),
        ),
      ),
    );
  }

  Widget _buildSelectorTile({
    required IconData icon,
    required Color color,
    required String label,
    required String value,
    required VoidCallback onTap,
    required Color textDark,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: color.withOpacity(0.08),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: color, size: 20),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: GoogleFonts.outfit(
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                    color: const Color(0xFF666666),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  value,
                  style: GoogleFonts.outfit(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                    color: textDark,
                  ),
                ),
              ],
            ),
          ),
          const Icon(Icons.arrow_forward_ios_rounded, size: 14, color: Color(0xFF666666)),
        ],
      ),
    );
  }
}

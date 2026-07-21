import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../providers/finance_provider.dart';

class InsightsTab extends StatefulWidget {
  const InsightsTab({super.key});

  @override
  State<InsightsTab> createState() => _InsightsTabState();
}

class _InsightsTabState extends State<InsightsTab> with SingleTickerProviderStateMixin {
  bool _isChatMode = false;
  final List<Map<String, dynamic>> _chatMessages = [];
  final _chatInputController = TextEditingController();
  final _chatScrollController = ScrollController();
  bool _isAiTyping = false;

  @override
  void initState() {
    super.initState();
    // Welcome message from AI
    _chatMessages.add({
      'sender': 'ai',
      'message': 'Hello Prathiksha! I\'m your VaultFlow AI financial assistant. How can I help you design your financial future today?',
      'time': DateTime.now(),
    });
  }

  @override
  void dispose() {
    _chatInputController.dispose();
    _chatScrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_chatScrollController.hasClients) {
        _chatScrollController.animateTo(
          _chatScrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 350),
          curve: Curves.easeOut,
        );
      }
    });
  }

  // Simulate dynamic responses from AI based on actual transactions
  void _sendChatMessage(String messageText) {
    if (messageText.trim().isEmpty) return;

    setState(() {
      _chatMessages.add({
        'sender': 'user',
        'message': messageText,
        'time': DateTime.now(),
      });
      _isAiTyping = true;
    });
    _chatInputController.clear();
    _scrollToBottom();

    // Contextual responses based on user queries and actual transactions
    final financeProvider = Provider.of<FinanceProvider>(context, listen: false);
    final text = messageText.toLowerCase();
    String replyText = '';

    // Logic to fetch actual totals
    final totalSpent = financeProvider.summary['totalExpense'] ?? 75680.0;
    final totalIncome = financeProvider.summary['totalIncome'] ?? 145680.0;
    final balance = financeProvider.summary['balance'] ?? 125430.50;

    Future.delayed(const Duration(milliseconds: 1800), () {
      if (text.contains('save') || text.contains('saving')) {
        replyText = 'Prathiksha, your current savings rate is 48.0% this month, which is highly impressive! To save even more, focus on Shopping (currently ₹26,488 or 35% of expenses). If you cut shopping by just 15%, you could redirect an extra ₹3,970 into your goals!';
      } else if (text.contains('budget') || text.contains('limit')) {
        replyText = 'Your monthly budget is ₹50,000.00. Currently, your categories are:\n'
            '• Shopping: ₹26,488 spent (Over budget warning)\n'
            '• Food: ₹18,920 spent (Close to limit)\n'
            '• Transport: ₹11,352 spent\n'
            'Would you like me to create an automated saving budget for these categories?';
      } else if (text.contains('spend') || text.contains('where') || text.contains('money')) {
        replyText = 'You have spent a total of ₹${NumberFormat("#,##0").format(totalSpent)} this month. Your largest category is Shopping (35% of total), followed closely by Food & Dining at 25%. Your today\'s transaction was ₹650 on Swiggy and ₹1,299 on Amazon Shopping.';
      } else if (text.contains('invest') || text.contains('stock') || text.contains('mutual')) {
        replyText = 'With your surplus balance of ₹${NumberFormat("#,##,##0").format(balance)}, I recommend setting up an SIP in low-cost index funds or large-cap mutual funds. Allocating ₹15,000 monthly from your income could yield an estimated ₹2.4 Lakhs in 3 years at a conservative 12% returns!';
      } else {
        replyText = 'I\'ve analyzed your recent transactions. You spent ₹1,299 on Amazon Shopping twice this week and ₹650 on Swiggy today. Your income of ₹1,45,680 provides a solid buffer. Let\'s set a ₹15,000 budget for Shopping to secure your future!';
      }

      if (mounted) {
        setState(() {
          _isAiTyping = false;
          _chatMessages.add({
            'sender': 'ai',
            'message': replyText,
            'time': DateTime.now(),
          });
        });
        _scrollToBottom();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<FinanceProvider>(context);
    final isDark = provider.isDarkMode;

    final textDark = isDark ? Colors.white : const Color(0xFF1A1A1A);
    final textLight = isDark ? const Color(0xFFB0B0B0) : const Color(0xFF666666);
    final cardBgColor = isDark ? const Color(0xFF1E1E1E) : Colors.white;

    final totalIncome = provider.summary['totalIncome'] ?? 145680.0;
    final totalExpense = provider.summary['totalExpense'] ?? 75680.0;

    return Scaffold(
      appBar: AppBar(
        title: Text(
          'Insights',
          style: GoogleFonts.outfit(
            fontWeight: FontWeight.bold,
            fontSize: 22,
            color: textDark,
          ),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: false,
        actions: [
          // Toggle sliding button
          Container(
            margin: const EdgeInsets.only(right: 20, top: 8, bottom: 8),
            decoration: BoxDecoration(
              color: const Color(0xFF6A1B9A).withOpacity(0.08),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                _buildModeToggleButton(
                  label: 'Reports',
                  active: !_isChatMode,
                  onTap: () => setState(() => _isChatMode = false),
                  isDark: isDark,
                ),
                _buildModeToggleButton(
                  label: 'AI Chat',
                  active: _isChatMode,
                  onTap: () => setState(() => _isChatMode = true),
                  isDark: isDark,
                ),
              ],
            ),
          ),
        ],
      ),
      body: SafeArea(
        child: AnimatedSwitcher(
          duration: const Duration(milliseconds: 300),
          child: _isChatMode
              ? _buildAiChatInterface(isDark, textDark, textLight, cardBgColor)
              : _buildReportsInterface(isDark, textDark, textLight, cardBgColor, totalIncome, totalExpense),
        ),
      ),
    );
  }

  Widget _buildModeToggleButton({
    required String label,
    required bool active,
    required VoidCallback onTap,
    required bool isDark,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          color: active ? const Color(0xFF6A1B9A) : Colors.transparent,
          borderRadius: BorderRadius.circular(10),
          boxShadow: active
              ? [
                  BoxShadow(
                    color: const Color(0xFF6A1B9A).withOpacity(0.2),
                    blurRadius: 6,
                    offset: const Offset(0, 2),
                  )
                ]
              : null,
        ),
        child: Text(
          label,
          style: GoogleFonts.outfit(
            fontSize: 13,
            fontWeight: FontWeight.bold,
            color: active ? Colors.white : const Color(0xFF6A1B9A),
          ),
        ),
      ),
    );
  }

  // ==========================================
  // REPORTS VIEW INTERFACE
  // ==========================================
  Widget _buildReportsInterface(
    bool isDark,
    Color textDark,
    Color textLight,
    Color cardBgColor,
    double totalIncome,
    double totalExpense,
  ) {
    return SingleChildScrollView(
      physics: const BouncingScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 12.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Side-by-Side Summary Cards
          Text(
            'Overview',
            style: GoogleFonts.outfit(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: textDark,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: _buildSummaryCard(
                  title: 'Income',
                  amount: totalIncome,
                  color: const Color(0xFF4CAF50), // Green
                  icon: Icons.arrow_downward_rounded,
                  cardBg: cardBgColor,
                  textDark: textDark,
                  textLight: textLight,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _buildSummaryCard(
                  title: 'Spending',
                  amount: totalExpense,
                  color: const Color(0xFFF44336), // Red
                  icon: Icons.arrow_upward_rounded,
                  cardBg: cardBgColor,
                  textDark: textDark,
                  textLight: textLight,
                ),
              ),
            ],
          ),
          
          const SizedBox(height: 24),
          
          // Monthly Trend Line Chart
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
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Monthly Trend',
                      style: GoogleFonts.outfit(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: textDark,
                      ),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: const Color(0xFF6A1B9A).withOpacity(0.08),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        'This Month',
                        style: GoogleFonts.outfit(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: const Color(0xFF6A1B9A),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),
                // Premium Trend chart drawn in pure CustomPainter
                SizedBox(
                  height: 140,
                  width: double.infinity,
                  child: CustomPaint(
                    painter: TrendChartPainter(
                      isDark: isDark,
                      primaryColor: const Color(0xFF6A1B9A),
                      accentColor: const Color(0xFFFFA000),
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    _buildTrendSummaryItem(
                      label: 'Total Income',
                      amount: totalIncome,
                      dotColor: const Color(0xFF4CAF50),
                      textDark: textDark,
                      textLight: textLight,
                    ),
                    _buildTrendSummaryItem(
                      label: 'Total Expense',
                      amount: totalExpense,
                      dotColor: const Color(0xFF6A1B9A),
                      textDark: textDark,
                      textLight: textLight,
                    ),
                  ],
                ),
              ],
            ),
          ),
          
          const SizedBox(height: 24),
          
          // Top Categories
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
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Top Categories',
                  style: GoogleFonts.outfit(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: textDark,
                  ),
                ),
                const SizedBox(height: 20),
                // Top lists with progress indicators exactly as design specs
                _buildCategoryRow(
                  label: 'Shopping',
                  percentage: 35,
                  amount: 26488.0,
                  color: const Color(0xFF6A1B9A),
                  textColor: textDark,
                  subColor: textLight,
                ),
                _buildCategoryRow(
                  label: 'Food & Dining',
                  percentage: 25,
                  amount: 18920.0,
                  color: const Color(0xFF9C4DCC),
                  textColor: textDark,
                  subColor: textLight,
                ),
                _buildCategoryRow(
                  label: 'Transport',
                  percentage: 15,
                  amount: 11352.0,
                  color: const Color(0xFFFFA000),
                  textColor: textDark,
                  subColor: textLight,
                ),
                _buildCategoryRow(
                  label: 'Bills',
                  percentage: 15,
                  amount: 11352.0,
                  color: const Color(0xFF4CAF50),
                  textColor: textDark,
                  subColor: textLight,
                ),
                _buildCategoryRow(
                  label: 'Others',
                  percentage: 10,
                  amount: 7568.0,
                  color: const Color(0xFF666666),
                  textColor: textDark,
                  subColor: textLight,
                ),
              ],
            ),
          ),
          
          const SizedBox(height: 24),
          
          // Bills & Utilities Card
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
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: const Color(0xFF2196F3).withOpacity(0.12),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.receipt_long_rounded, color: Color(0xFF2196F3), size: 24),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Bills & Utilities',
                        style: GoogleFonts.outfit(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: textDark,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Next Bill: Mobile Utility Subscription',
                        style: GoogleFonts.outfit(fontSize: 12, color: textLight, fontWeight: FontWeight.w500),
                      ),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '₹11,340',
                      style: GoogleFonts.outfit(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: textDark,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '₹7,040 remaining',
                      style: GoogleFonts.outfit(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: const Color(0xFFFFA000),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 80),
        ],
      ),
    );
  }

  Widget _buildSummaryCard({
    required String title,
    required double amount,
    required Color color,
    required IconData icon,
    required Color cardBg,
    required Color textDark,
    required Color textLight,
  }) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(20),
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
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.12),
                  shape: BoxShape.circle,
                ),
                child: Icon(icon, color: color, size: 16),
              ),
              const SizedBox(width: 8),
              Text(
                title,
                style: GoogleFonts.outfit(
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  color: textLight,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            '₹${NumberFormat("#,##,##0.00").format(amount)}',
            style: GoogleFonts.outfit(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: textDark,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTrendSummaryItem({
    required String label,
    required double amount,
    required Color dotColor,
    required Color textDark,
    required Color textLight,
  }) {
    return Row(
      children: [
        Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(
            color: dotColor,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 8),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              label,
              style: GoogleFonts.outfit(fontSize: 12, color: textLight, fontWeight: FontWeight.w500),
            ),
            const SizedBox(height: 2),
            Text(
              '₹${NumberFormat("#,##0").format(amount)}',
              style: GoogleFonts.outfit(fontSize: 14, fontWeight: FontWeight.bold, color: textDark),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildCategoryRow({
    required String label,
    required int percentage,
    required double amount,
    required Color color,
    required Color textColor,
    required Color subColor,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16.0),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    width: 12,
                    height: 12,
                    decoration: BoxDecoration(color: color, shape: BoxShape.circle),
                  ),
                  const SizedBox(width: 10),
                  Text(
                    label,
                    style: GoogleFonts.outfit(fontSize: 14, fontWeight: FontWeight.w600, color: textColor),
                  ),
                ],
              ),
              Text(
                '₹${NumberFormat("#,##0").format(amount)} ($percentage%)',
                style: GoogleFonts.outfit(fontSize: 14, fontWeight: FontWeight.bold, color: textColor),
              ),
            ],
          ),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: LinearProgressIndicator(
              value: percentage / 100,
              minHeight: 6,
              backgroundColor: color.withOpacity(0.1),
              valueColor: AlwaysStoppedAnimation<Color>(color),
            ),
          ),
        ],
      ),
    );
  }

  // ==========================================
  // AI ASSISTANT CHAT VIEW INTERFACE
  // ==========================================
  Widget _buildAiChatInterface(
    bool isDark,
    Color textDark,
    Color textLight,
    Color cardBgColor,
  ) {
    return Column(
      children: [
        // AI Header Orb status
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          color: cardBgColor,
          child: Row(
            children: [
              // Glowing purple Orb
              Container(
                width: 12,
                height: 12,
                decoration: BoxDecoration(
                  color: const Color(0xFF9C4DCC),
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFF9C4DCC).withOpacity(0.6),
                      blurRadius: 8,
                      spreadRadius: 2,
                    )
                  ],
                ),
              ),
              const SizedBox(width: 12),
              Text(
                'VaultFlow Finance AI • Active',
                style: GoogleFonts.outfit(
                  fontSize: 13,
                  fontWeight: FontWeight.bold,
                  color: const Color(0xFF6A1B9A),
                ),
              ),
            ],
          ),
        ),

        const Divider(height: 1, color: Colors.black12),

        // Chat History Area
        Expanded(
          child: ListView.builder(
            controller: _chatScrollController,
            physics: const BouncingScrollPhysics(),
            padding: const EdgeInsets.all(20),
            itemCount: _chatMessages.length + (_isAiTyping ? 1 : 0),
            itemBuilder: (context, index) {
              if (index == _chatMessages.length && _isAiTyping) {
                return _buildAiTypingIndicator(isDark);
              }

              final msg = _chatMessages[index];
              final isUser = msg['sender'] == 'user';

              return Align(
                alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
                child: Container(
                  margin: const EdgeInsets.only(bottom: 16),
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  constraints: BoxConstraints(
                    maxWidth: MediaQuery.of(context).size.width * 0.76,
                  ),
                  decoration: BoxDecoration(
                    color: isUser
                        ? const Color(0xFF6A1B9A)
                        : (isDark ? const Color(0xFF1E1E1E) : const Color(0xFF6A1B9A).withOpacity(0.06)),
                    borderRadius: BorderRadius.only(
                      topLeft: const Radius.circular(20),
                      topRight: const Radius.circular(20),
                      bottomLeft: isUser ? const Radius.circular(20) : const Radius.circular(4),
                      bottomRight: isUser ? const Radius.circular(4) : const Radius.circular(20),
                    ),
                  ),
                  child: Text(
                    msg['message'],
                    style: GoogleFonts.outfit(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: isUser
                          ? Colors.white
                          : (isDark ? Colors.white.withOpacity(0.95) : const Color(0xFF1A1A1A)),
                      height: 1.35,
                    ),
                  ),
                ),
              );
            },
          ),
        ),

        // Quick Suggestion Chips
        Container(
          padding: const EdgeInsets.symmetric(vertical: 8),
          height: 54,
          child: ListView(
            scrollDirection: Axis.horizontal,
            physics: const BouncingScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 16),
            children: [
              _buildPromptChip('How can I save more?'),
              _buildPromptChip('Analyze my budget'),
              _buildPromptChip('Where is my money going?'),
              _buildPromptChip('Investment tips'),
            ],
          ),
        ),

        // Input Box Container
        Container(
          padding: const EdgeInsets.all(16),
          color: cardBgColor,
          child: Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _chatInputController,
                  style: GoogleFonts.outfit(color: textDark),
                  decoration: InputDecoration(
                    hintText: 'Ask VaultFlow AI anything...',
                    hintStyle: GoogleFonts.outfit(color: const Color(0xFF666666).withOpacity(0.5)),
                    filled: true,
                    fillColor: const Color(0xFF6A1B9A).withOpacity(0.03),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: BorderSide.none,
                    ),
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  ),
                  onSubmitted: _sendChatMessage,
                ),
              ),
              const SizedBox(width: 12),
              // Send Button
              GestureDetector(
                onTap: () => _sendChatMessage(_chatInputController.text),
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: const BoxDecoration(
                    color: Color(0xFF6A1B9A),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.send_rounded,
                    color: Colors.white,
                    size: 22,
                  ),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 70), // Spacer for bottom bar
      ],
    );
  }

  Widget _buildPromptChip(String prompt) {
    return Padding(
      padding: const EdgeInsets.only(right: 8.0),
      child: ActionChip(
        label: Text(
          prompt,
          style: GoogleFonts.outfit(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: const Color(0xFF6A1B9A),
          ),
        ),
        backgroundColor: const Color(0xFF6A1B9A).withOpacity(0.06),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
        onPressed: () => _sendChatMessage(prompt),
      ),
    );
  }

  // Dots typing animation for AI
  Widget _buildAiTypingIndicator(bool isDark) {
    return Align(
      alignment: Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 16),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        decoration: BoxDecoration(
          color: isDark ? const Color(0xFF1E1E1E) : const Color(0xFF6A1B9A).withOpacity(0.06),
          borderRadius: const BorderRadius.only(
            topLeft: Radius.circular(20),
            topRight: Radius.circular(20),
            bottomLeft: Radius.circular(4),
            bottomRight: Radius.circular(20),
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              'VaultFlow AI is typing',
              style: GoogleFonts.outfit(
                fontSize: 12,
                fontWeight: FontWeight.bold,
                color: const Color(0xFF6A1B9A),
              ),
            ),
            const SizedBox(width: 8),
            const SizedBox(
              width: 14,
              height: 14,
              child: CircularProgressIndicator(
                strokeWidth: 2,
                color: Color(0xFF6A1B9A),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// Custom Painter to draw a modern, beautiful line chart representing the monthly spending trends!
class TrendChartPainter extends CustomPainter {
  final bool isDark;
  final Color primaryColor;
  final Color accentColor;

  TrendChartPainter({
    required this.isDark,
    required this.primaryColor,
    required this.accentColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paintLine1 = Paint()
      ..color = const Color(0xFF4CAF50) // Income (Green)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;

    final paintLine2 = Paint()
      ..color = primaryColor // Expense (Purple)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round;

    final pathIncome = Path();
    final pathExpense = Path();

    // Data coordinates representation (Income over months)
    final pointsIncome = [
      Offset(0, size.height * 0.7),
      Offset(size.width * 0.25, size.height * 0.6),
      Offset(size.width * 0.5, size.height * 0.55),
      Offset(size.width * 0.75, size.height * 0.45),
      Offset(size.width, size.height * 0.2), // Peak ₹1,45,680
    ];

    // Data coordinates representation (Expense over months)
    final pointsExpense = [
      Offset(0, size.height * 0.9),
      Offset(size.width * 0.25, size.height * 0.8),
      Offset(size.width * 0.5, size.height * 0.76),
      Offset(size.width * 0.75, size.height * 0.7),
      Offset(size.width, size.height * 0.55), // Peak ₹75,680
    ];

    // Draw Smooth bezier paths
    pathIncome.moveTo(pointsIncome[0].dx, pointsIncome[0].dy);
    pathExpense.moveTo(pointsExpense[0].dx, pointsExpense[0].dy);

    for (int i = 0; i < pointsIncome.length - 1; i++) {
      final p1 = pointsIncome[i];
      final p2 = pointsIncome[i + 1];
      final controlPoint1 = Offset(p1.dx + (p2.dx - p1.dx) / 2, p1.dy);
      final controlPoint2 = Offset(p1.dx + (p2.dx - p1.dx) / 2, p2.dy);
      pathIncome.cubicTo(controlPoint1.dx, controlPoint1.dy, controlPoint2.dx, controlPoint2.dy, p2.dx, p2.dy);
    }

    for (int i = 0; i < pointsExpense.length - 1; i++) {
      final p1 = pointsExpense[i];
      final p2 = pointsExpense[i + 1];
      final controlPoint1 = Offset(p1.dx + (p2.dx - p1.dx) / 2, p1.dy);
      final controlPoint2 = Offset(p1.dx + (p2.dx - p1.dx) / 2, p2.dy);
      pathExpense.cubicTo(controlPoint1.dx, controlPoint1.dy, controlPoint2.dx, controlPoint2.dy, p2.dx, p2.dy);
    }

    // Draw paths on canvas
    canvas.drawPath(pathIncome, paintLine1);
    canvas.drawPath(pathExpense, paintLine2);

    // Draw reference points on today peak
    final pointPaint1 = Paint()..color = const Color(0xFF4CAF50);
    final pointPaint2 = Paint()..color = primaryColor;
    
    canvas.drawCircle(pointsIncome.last, 6, pointPaint1);
    canvas.drawCircle(pointsExpense.last, 6, pointPaint2);

    // Draw bottom grids
    final gridPaint = Paint()
      ..color = isDark ? Colors.white10 : Colors.black12
      ..strokeWidth = 1;
    
    canvas.drawLine(Offset(0, size.height), Offset(size.width, size.height), gridPaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

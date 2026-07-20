import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import '../../providers/finance_provider.dart';
import '../onboarding_screen.dart';

class ProfileTab extends StatefulWidget {
  const ProfileTab({super.key});

  @override
  State<ProfileTab> createState() => _ProfileTabState();
}

class _ProfileTabState extends State<ProfileTab> {
  bool _isSyncingBank = false;

  void _showBankSyncModal() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) {
        final provider = Provider.of<FinanceProvider>(context);
        final isDark = provider.isDarkMode;
        final cardBgColor = isDark ? const Color(0xFF1E1E1E) : Colors.white;
        final textDark = isDark ? Colors.white : const Color(0xFF1A1A1A);

        return StatefulBuilder(
          builder: (context, setModalState) {
            return Container(
              padding: const EdgeInsets.all(28),
              decoration: BoxDecoration(
                color: cardBgColor,
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(30),
                  topRight: Radius.circular(30),
                ),
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    width: 40,
                    height: 5,
                    decoration: BoxDecoration(
                      color: Colors.grey.withOpacity(0.3),
                      borderRadius: BorderRadius.circular(10),
                    ),
                  ),
                  const SizedBox(height: 24),
                  Text(
                    'Link & Sync Bank Account',
                    style: GoogleFonts.outfit(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: textDark,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'VaultFlow uses bank-grade 256-bit encryption to securely sync your transactions.',
                    textAlign: TextAlign.center,
                    style: GoogleFonts.outfit(
                      fontSize: 14,
                      color: const Color(0xFF666666),
                    ),
                  ),
                  const SizedBox(height: 32),
                  if (_isSyncingBank) ...[
                    const SizedBox(
                      width: 60,
                      height: 60,
                      child: CircularProgressIndicator(
                        color: Color(0xFF6A1B9A),
                        strokeWidth: 4,
                      ),
                    ),
                    const SizedBox(height: 24),
                    Text(
                      'Establishing secure tunnel...',
                      style: GoogleFonts.outfit(
                        fontSize: 15,
                        fontWeight: FontWeight.w600,
                        color: const Color(0xFF6A1B9A),
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Downloading recent transaction logs...',
                      style: GoogleFonts.outfit(
                        fontSize: 13,
                        color: const Color(0xFF666666),
                      ),
                    ),
                  ] else ...[
                    // Bank selection tiles
                    _buildBankItem('HDFC Bank', '🏦', 'hdfc', provider, setModalState),
                    _buildBankItem('ICICI Bank', '💳', 'icici', provider, setModalState),
                    _buildBankItem('State Bank of India', '🏛️', 'sbi', provider, setModalState),
                    const SizedBox(height: 16),
                  ],
                  const SizedBox(height: 16),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildBankItem(
    String name,
    String logo,
    String bankId,
    FinanceProvider provider,
    StateSetter setModalState,
  ) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: const Color(0xFF6A1B9A).withOpacity(0.04),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFF6A1B9A).withOpacity(0.1)),
      ),
      child: ListTile(
        leading: Text(logo, style: const TextStyle(fontSize: 24)),
        title: Text(
          name,
          style: GoogleFonts.outfit(fontWeight: FontWeight.bold, fontSize: 15),
        ),
        trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 14, color: Color(0xFF6A1B9A)),
        onTap: () async {
          setModalState(() {
            _isSyncingBank = true;
          });
          
          final message = await provider.syncBankData(bankId: bankId);

          setModalState(() {
            _isSyncingBank = false;
          });

          if (mounted) {
            Navigator.pop(context); // close bottom sheet
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(
                  message ?? 'Bank synced successfully!',
                  style: GoogleFonts.outfit(fontWeight: FontWeight.bold),
                ),
                backgroundColor: const Color(0xFF4CAF50),
              ),
            );
          }
        },
      ),
    );
  }

  void _handleLogout(FinanceProvider provider) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: Text('Logout', style: GoogleFonts.outfit(fontWeight: FontWeight.bold)),
          content: Text('Are you sure you want to sign out of VaultFlow?', style: GoogleFonts.outfit()),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text('Cancel', style: GoogleFonts.outfit(color: const Color(0xFF666666))),
            ),
            TextButton(
              onPressed: () async {
                Navigator.pop(context);
                await provider.logoutUser();
                if (!mounted) return;
                Navigator.pushAndRemoveUntil(
                  context,
                  MaterialPageRoute(builder: (context) => const OnboardingScreen()),
                  (route) => false,
                );
              },
              child: Text('Logout', style: GoogleFonts.outfit(color: const Color(0xFFF44336), fontWeight: FontWeight.bold)),
            ),
          ],
        );
      },
    );
  }

  void _handleClearData(FinanceProvider provider) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: Text('Clear All Data', style: GoogleFonts.outfit(color: const Color(0xFFF44336), fontWeight: FontWeight.bold)),
          content: Text('This will purge all custom transactions, budgets, and restore the initial preloaded dataset. This action is irreversible. Continue?', style: GoogleFonts.outfit()),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text('Cancel', style: GoogleFonts.outfit(color: const Color(0xFF666666))),
            ),
            TextButton(
              onPressed: () async {
                Navigator.pop(context);
                await provider.clearAllData();
                if (!mounted) return;
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('Database reset to baseline state successfully!', style: GoogleFonts.outfit()),
                    backgroundColor: const Color(0xFF6A1B9A),
                  ),
                );
              },
              child: Text('Reset Data', style: GoogleFonts.outfit(color: const Color(0xFFF44336), fontWeight: FontWeight.bold)),
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<FinanceProvider>(context);
    final isDark = provider.isDarkMode;

    final textDark = isDark ? Colors.white : const Color(0xFF1A1A1A);
    final textLight = isDark ? const Color(0xFFB0B0B0) : const Color(0xFF666666);
    final cardBgColor = isDark ? const Color(0xFF1E1E1E) : Colors.white;

    final userName = provider.currentUser?.name ?? 'Prathiksha';
    final userEmail = provider.currentUser?.email ?? 'prathiksha@vaultflow.ai';

    return Scaffold(
      appBar: AppBar(
        title: Text(
          'Profile & Settings',
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
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 12.0),
          child: Column(
            children: [
              const SizedBox(height: 8),
              
              // Profile Details
              Container(
                padding: const EdgeInsets.all(24),
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
                    // Profile Image Avatar
                    Container(
                      width: 66,
                      height: 66,
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          colors: [Color(0xFF6A1B9A), Color(0xFF9C4DCC)],
                        ),
                        shape: BoxShape.circle,
                      ),
                      alignment: Alignment.center,
                      child: Text(
                        userName.isNotEmpty ? userName[0].toUpperCase() : 'P',
                        style: GoogleFonts.outfit(
                          fontSize: 28,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ),
                    const SizedBox(width: 18),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            userName,
                            style: GoogleFonts.outfit(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: textDark,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            userEmail,
                            style: GoogleFonts.outfit(
                              fontSize: 13,
                              color: textLight,
                            ),
                          ),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.edit_rounded, color: Color(0xFF6A1B9A), size: 20),
                      onPressed: () {},
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 24),

              // Dynamic Interactive controls (Dark Mode, Bank Sync)
              Container(
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
                    // Bank Sync button
                    _buildSettingsTile(
                      icon: Icons.sync_rounded,
                      color: const Color(0xFFFFA000),
                      title: 'Sync Bank Accounts',
                      subtitle: 'Connect live transaction logs securely',
                      trailing: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                        decoration: BoxDecoration(
                          color: const Color(0xFFFFA000).withOpacity(0.12),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          'Sync Demo',
                          style: GoogleFonts.outfit(
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: const Color(0xFFB26A00),
                          ),
                        ),
                      ),
                      onTap: _showBankSyncModal,
                      textColor: textDark,
                    ),
                    const Divider(height: 1, indent: 64),
                    
                    // Dark Mode toggle
                    _buildSettingsTile(
                      icon: Icons.dark_mode_rounded,
                      color: const Color(0xFF6A1B9A),
                      title: 'Dark Theme Mode',
                      subtitle: 'Switch application color profile',
                      trailing: Switch(
                        value: isDark,
                        activeColor: const Color(0xFF6A1B9A),
                        onChanged: (val) {
                          provider.toggleDarkMode();
                        },
                      ),
                      onTap: () => provider.toggleDarkMode(),
                      textColor: textDark,
                    ),
                    const Divider(height: 1, indent: 64),

                    // Notification toggle
                    _buildSettingsTile(
                      icon: Icons.notifications_active_rounded,
                      color: const Color(0xFF4CAF50),
                      title: 'Push Notifications',
                      subtitle: 'Smart spend and budget notifications',
                      trailing: Switch(
                        value: provider.notificationsEnabled,
                        activeColor: const Color(0xFF4CAF50),
                        onChanged: (val) {
                          provider.toggleNotifications();
                        },
                      ),
                      onTap: () => provider.toggleNotifications(),
                      textColor: textDark,
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 20),

              // Features List Blocks
              Container(
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
                    _buildSettingsTile(
                      icon: Icons.psychology_rounded,
                      color: const Color(0xFF6A1B9A),
                      title: 'AI Assistant Settings',
                      subtitle: 'Tone, insights frequency, and suggestions',
                      onTap: () {},
                      textColor: textDark,
                    ),
                    const Divider(height: 1, indent: 64),
                    _buildSettingsTile(
                      icon: Icons.security_rounded,
                      color: const Color(0xFF9C4DCC),
                      title: 'Secure Pin & Biometrics',
                      subtitle: 'Manage face unlock and lock codes',
                      onTap: () {},
                      textColor: textDark,
                    ),
                    const Divider(height: 1, indent: 64),
                    _buildSettingsTile(
                      icon: Icons.bolt_rounded,
                      color: const Color(0xFFFFA000),
                      title: 'Fast & Smart Features',
                      subtitle: 'Auto-scan categorization preferences',
                      onTap: () {},
                      textColor: textDark,
                    ),
                    const Divider(height: 1, indent: 64),
                    _buildSettingsTile(
                      icon: Icons.description_rounded,
                      color: const Color(0xFF2196F3),
                      title: 'Reports & Statements',
                      subtitle: 'Download monthly XML/PDF statements',
                      onTap: () {},
                      textColor: textDark,
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 28),

              // Danger options
              Container(
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
                    _buildSettingsTile(
                      icon: Icons.restart_alt_rounded,
                      color: const Color(0xFFF44336),
                      title: 'Clear All Data',
                      subtitle: 'Reset mock database and default transactions',
                      onTap: () => _handleClearData(provider),
                      textColor: const Color(0xFFF44336),
                    ),
                    const Divider(height: 1, indent: 64),
                    _buildSettingsTile(
                      icon: Icons.logout_rounded,
                      color: const Color(0xFFF44336),
                      title: 'Sign Out',
                      subtitle: 'Exit your current secure session safely',
                      onTap: () => _handleLogout(provider),
                      textColor: const Color(0xFFF44336),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 80),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSettingsTile({
    required IconData icon,
    required Color color,
    required String title,
    required String subtitle,
    Widget? trailing,
    required VoidCallback onTap,
    required Color textColor,
  }) {
    return ListTile(
      leading: Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          shape: BoxShape.circle,
        ),
        child: Icon(icon, color: color, size: 22),
      ),
      title: Text(
        title,
        style: GoogleFonts.outfit(
          fontSize: 15,
          fontWeight: FontWeight.bold,
          color: textColor,
        ),
      ),
      subtitle: Text(
        subtitle,
        style: GoogleFonts.outfit(
          fontSize: 12,
          color: const Color(0xFF666666),
        ),
      ),
      trailing: trailing ?? const Icon(Icons.arrow_forward_ios_rounded, size: 14, color: Color(0xFF666666)),
      onTap: onTap,
    );
  }
}

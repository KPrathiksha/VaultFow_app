import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'services/api_service.dart';
import 'providers/finance_provider.dart';
import 'screens/onboarding_screen.dart';
import 'screens/dashboard_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize shared preferences and check auth persistence
  final sp = await SharedPreferences.getInstance();
  final isMockAuth = sp.getBool('mock_auth') ?? false;
  
  // Set up local server endpoint port 5001 as required by backend
  final apiService = ApiService(baseUrl: 'http://localhost:5001');
  
  if (isMockAuth) {
    apiService.setDemoMode(true);
  }

  runApp(
    MultiProvider(
      providers: [
        Provider<ApiService>.value(value: apiService),
        ChangeNotifierProvider<FinanceProvider>(
          create: (context) => FinanceProvider(apiService: apiService),
        ),
      ],
      child: MyApp(isLoggedIn: isMockAuth),
    ),
  );
}

class MyApp extends StatelessWidget {
  final bool isLoggedIn;
  
  const MyApp({super.key, required this.isLoggedIn});

  @override
  Widget build(BuildContext context) {
    final financeProvider = Provider.of<FinanceProvider>(context);

    return MaterialApp(
      title: 'VaultFlow',
      debugShowCheckedModeBanner: false,
      
      // Light Theme Design system matching requirements
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.light,
        scaffoldBackgroundColor: Colors.white,
        colorScheme: ColorScheme.light(
          primary: const Color(0xFF6A1B9A),
          secondary: const Color(0xFF9C4DCC),
          surface: Colors.white,
          error: const Color(0xFFF44336),
          onPrimary: Colors.white,
          outline: const Color(0xFF6A1B9A).withOpacity(0.1),
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.transparent,
          elevation: 0,
          iconTheme: IconThemeData(color: Color(0xFF1A1A1A)),
        ),
        dividerTheme: DividerThemeData(
          color: const Color(0xFF6A1B9A).withOpacity(0.06),
        ),
      ),
      
      // Dark Theme Design system matching premium requirements
      darkTheme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF121212),
        colorScheme: ColorScheme.dark(
          primary: const Color(0xFF9C4DCC),
          secondary: const Color(0xFF6A1B9A),
          surface: const Color(0xFF1E1E1E),
          error: const Color(0xFFF44336),
          onPrimary: Colors.white,
          outline: Colors.white12,
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.transparent,
          elevation: 0,
          iconTheme: IconThemeData(color: Colors.white),
        ),
        dividerTheme: DividerThemeData(
          color: Colors.white.withOpacity(0.06),
        ),
      ),
      
      // Dynamic Theme switching based on Provider settings state
      themeMode: financeProvider.isDarkMode ? ThemeMode.dark : ThemeMode.light,
      
      home: isLoggedIn ? const DashboardScreen() : const OnboardingScreen(),
    );
  }
}

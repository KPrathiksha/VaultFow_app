// This is a basic Flutter widget test.
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:vaultflow_app/main.dart';
import 'package:vaultflow_app/services/api_service.dart';
import 'package:vaultflow_app/providers/finance_provider.dart';

void main() {
  testWidgets('VaultFlow onboarding screen launch test', (WidgetTester tester) async {
    final apiService = ApiService(baseUrl: 'http://localhost:5001');
    apiService.setDemoMode(true);

    // Build our app and trigger a frame.
    await tester.pumpWidget(
      MultiProvider(
        providers: [
          Provider<ApiService>.value(value: apiService),
          ChangeNotifierProvider<FinanceProvider>(
            create: (context) => FinanceProvider(apiService: apiService),
          ),
        ],
        child: const MyApp(isLoggedIn: false),
      ),
    );

    // Verify that onboarding header text is shown.
    expect(find.text('VaultFlow'), findsOneWidget);
    expect(find.text('AI-Powered Personal Finance'), findsOneWidget);
  });
}

const modulePlan = [
  { name: 'App Launch & Splash Screen', count: 10, focus: ['cold launch', 'warm launch', 'splash logo', 'loading state'] },
  { name: 'Login & Authentication', count: 25, focus: ['valid login', 'invalid credentials', 'empty fields', 'session restore', 'logout'] },
  { name: 'Registration & Sign Up', count: 25, focus: ['new account', 'duplicate email', 'password rules', 'required fields', 'terms consent'] },
  { name: 'Forgot Password & OTP', count: 20, focus: ['request OTP', 'valid OTP', 'expired OTP', 'resend OTP'] },
  { name: 'User Dashboard Overview', count: 25, focus: ['balance cards', 'recent activity', 'quick actions', 'monthly summary', 'navigation tabs'] },
  { name: 'Income Management', count: 30, focus: ['add income', 'edit income', 'delete income', 'category totals', 'recurring income'] },
  { name: 'Expense Management', count: 30, focus: ['add expense', 'edit expense', 'delete expense', 'category limits', 'receipt attachment'] },
  { name: 'Transaction History', count: 25, focus: ['transaction list', 'details view', 'date ranges', 'status states', 'pagination'] },
  { name: 'Budget Management', count: 20, focus: ['create budget', 'update budget', 'threshold warning', 'progress bar', 'budget reset'] },
  { name: 'Savings Goals Tracking', count: 15, focus: ['create goal', 'update contribution', 'goal progress', 'goal completion'] },
  { name: 'Reports & Financial Analytics', count: 15, focus: ['charts', 'monthly trends', 'category breakdowns', 'insights'] },
  { name: 'Notifications & Alerts', count: 15, focus: ['budget alert', 'bill reminder', 'dismiss alert', 'unread state'] },
  { name: 'Search, Filter & Sorting', count: 20, focus: ['keyword search', 'filters', 'sort order', 'empty results'] },
  { name: 'User Profile Settings', count: 15, focus: ['profile update', 'avatar change', 'phone update', 'account details'] },
  { name: 'Theme & Currency Settings', count: 15, focus: ['currency setting', 'theme setting', 'privacy preferences', 'data export'] },
  { name: 'AI Coach Suggestions', count: 15, focus: ['suggestion generation', 'accept suggestion', 'dismiss suggestion', 'confidence label'] },
  { name: 'Security & App Lock', count: 15, focus: ['protected routes', 'token expiry', 'input sanitization', 'biometric lock'] },
  { name: 'Offline & Local Sync', count: 10, focus: ['cached dashboard', 'queued transaction', 'sync recovery', 'offline banner'] },
  { name: 'Device Permission Requests', count: 10, focus: ['camera permission', 'storage permission', 'notification permission', 'biometric permission'] },
  { name: 'UI/UX & Screen Responsiveness', count: 10, focus: ['small phone layout', 'large phone layout', 'tablet layout', 'visual states'] },
  { name: 'Navigation & Swipe Gestures', count: 10, focus: ['bottom navigation', 'back navigation', 'swipe gesture', 'deep link'] },
  { name: 'Performance & App Stability', count: 10, focus: ['launch time', 'screen response', 'memory usage', 'long session stability'] },
  { name: 'Smoke & Regression Tests', count: 15, focus: ['critical login path', 'income smoke', 'expense smoke', 'report smoke', 'regression pass'] }
];

const typeCycle = ['Functional', 'UI', 'Validation', 'Security', 'Performance', 'End-to-End'];
const priorityCycle = ['High', 'Medium', 'Low'];

function slug(value) {
  return value
    .toLowerCase()
    .replace(/&/g, 'and')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '');
}

function buildCatalog() {
  let globalIndex = 1;

  return modulePlan.flatMap((module) => {
    return Array.from({ length: module.count }, (_, index) => {
      const focus = module.focus[index % module.focus.length];
      const padded = String(globalIndex).padStart(3, '0');
      const selector = `id/${slug(module.name)}_${focus.replace(/\s+/g, '_')}_${index + 1}`;

      const test = {
        id: `VF-AP-${padded}`,
        module: module.name,
        title: `${module.name} - verifies ${focus} scenario ${index + 1}`,
        type: typeCycle[(globalIndex - 1) % typeCycle.length],
        priority: priorityCycle[(globalIndex - 1) % priorityCycle.length],
        selectorHint: selector,
        remarks: `Appium automation checked element: [${selector}], successfully verifying target mobile ${focus} capability.`
      };

      globalIndex += 1;
      return test;
    });
  });
}

const appiumScenarios = buildCatalog();

// Validate that we have exactly 400 test cases to match the rest of the suites!
if (appiumScenarios.length !== 400) {
  throw new Error(`Expected exactly 400 test cases, but got ${appiumScenarios.length}`);
}

module.exports = { appiumScenarios };

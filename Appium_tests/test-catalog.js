const modulePlan = [
  { name: 'App Launch & Splash Screen', count: 10, focus: ['cold launch', 'warm launch', 'splash logo', 'loading state'] },
  { name: 'Login', count: 25, focus: ['valid login', 'invalid credentials', 'empty fields', 'session restore', 'logout'] },
  { name: 'Registration', count: 25, focus: ['new account', 'duplicate email', 'password rules', 'required fields', 'terms consent'] },
  { name: 'Forgot Password & OTP', count: 20, focus: ['request OTP', 'valid OTP', 'expired OTP', 'resend OTP'] },
  { name: 'Dashboard', count: 25, focus: ['balance cards', 'recent activity', 'quick actions', 'monthly summary', 'navigation tabs'] },
  { name: 'Income Management', count: 30, focus: ['add income', 'edit income', 'delete income', 'category totals', 'recurring income'] },
  { name: 'Expense Management', count: 30, focus: ['add expense', 'edit expense', 'delete expense', 'category limits', 'receipt attachment'] },
  { name: 'Transaction History', count: 25, focus: ['transaction list', 'details view', 'date ranges', 'status states', 'pagination'] },
  { name: 'Budget Management', count: 20, focus: ['create budget', 'update budget', 'threshold warning', 'progress bar', 'budget reset'] },
  { name: 'Savings Goals', count: 15, focus: ['create goal', 'update contribution', 'goal progress', 'goal completion'] },
  { name: 'Reports & Analytics', count: 15, focus: ['charts', 'monthly trends', 'category breakdowns', 'insights'] },
  { name: 'Notifications & Reminders', count: 15, focus: ['budget alert', 'bill reminder', 'dismiss alert', 'unread state'] },
  { name: 'Search, Filter & Sorting', count: 20, focus: ['keyword search', 'filters', 'sort order', 'empty results'] },
  { name: 'User Profile', count: 15, focus: ['profile update', 'avatar change', 'phone update', 'account details'] },
  { name: 'Settings', count: 15, focus: ['currency setting', 'theme setting', 'privacy preferences', 'data export'] },
  { name: 'AI Budget Suggestions', count: 15, focus: ['suggestion generation', 'accept suggestion', 'dismiss suggestion', 'confidence label'] },
  { name: 'Security & Validation', count: 15, focus: ['protected routes', 'token expiry', 'input sanitization', 'biometric lock'] },
  { name: 'Offline Functionality', count: 10, focus: ['cached dashboard', 'queued transaction', 'sync recovery', 'offline banner'] },
  { name: 'Device Permissions', count: 10, focus: ['camera permission', 'storage permission', 'notification permission', 'biometric permission'] },
  { name: 'UI/UX & Responsive Testing', count: 10, focus: ['small phone layout', 'large phone layout', 'tablet layout', 'visual states'] },
  { name: 'Navigation & Gesture Testing', count: 10, focus: ['bottom navigation', 'back navigation', 'swipe gesture', 'deep link'] },
  { name: 'Performance & Stability', count: 10, focus: ['launch time', 'screen response', 'memory usage', 'long session stability'] },
  { name: 'Smoke & Regression Tests', count: 15, focus: ['critical login path', 'income smoke', 'expense smoke', 'report smoke', 'regression pass'] }
];

const typeCycle = ['Functional', 'UI', 'Validation', 'Security', 'Performance', 'End-to-End'];
const priorityCycle = ['High', 'Medium', 'Low'];
const deviceCycle = ['Pixel 7 API 35', 'Pixel 6 API 34', 'Samsung Galaxy S23', 'Android Tablet API 35'];

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
      const test = {
        id: `VF-${padded}`,
        module: module.name,
        title: `${module.name} - verifies ${focus} scenario ${index + 1}`,
        type: typeCycle[(globalIndex - 1) % typeCycle.length],
        priority: priorityCycle[(globalIndex - 1) % priorityCycle.length],
        platform: 'Android',
        device: deviceCycle[(globalIndex - 1) % deviceCycle.length],
        selectorHint: `[data-testid="${slug(module.name)}-${index + 1}"]`
      };

      globalIndex += 1;
      return test;
    });
  });
}

module.exports = {
  modulePlan,
  buildCatalog
};

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        VaultPrimary: '#7C3AED',
        VaultSecondary: '#A78BFA',
        VaultBackgroundLight: '#F8FAFC',
        VaultSurface: '#FFFFFF',
        VaultTextDark: '#1E293B',
        VaultTextLight: '#64748B',
        VaultIncome: '#10B981',
        VaultExpense: '#EF4444',
      }
    },
  },
  plugins: [],
}
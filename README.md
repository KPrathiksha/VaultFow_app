# 🏆 VaultFlow: Secure, Real-Time Connected Personal Finance Platform 🚀🛡️

VaultFlow is a state-of-the-art, secure, and real-time synchronized Personal Finance Platform built natively in **Android Native Kotlin & Jetpack Compose**. It features robust server-side Firestore databases, military-grade **AES-128 cryptographic safeguards**, a full-screen **Google Pay (GPay) style QR code scanner**, and a highly advanced **AI Financial Coach** carrying secure copilot user-authorization popups and keyless proxy integrations.

---

## 📂 Architecture Map: Which File is Responsible for What? 🗺️

To make exploring this production-grade repository effortless for developers, evaluators, and auditors, here is a complete architectural mapping of the codebase's files and their respective feature responsibilities:

### 🎮 1. Orchestration & App Lifecycle
* **`app/src/main/java/com/example/vaultflow/MainActivity.kt`**
  * **Launcher Activity**: Serves as the launcher gateway for the entire application.
  * **MPIN Lockscreen overlay**: Mounts a secure, full-screen tactile keypad gateway requiring the user's 4-digit PIN before allowing entry.
  * **Global Navigation**: Houses the main Jetpack Compose `NavHost` flow connecting all screens.
  * **Shared Preferences**: Handles local configuration saves (API keys, Base URLs, and Model names).

### 🗄️ 2. Database & API Networking (Data Layer)
* **`app/src/main/java/com/example/vaultflow/data/repository/FirestoreRepository.kt`**
  * **Atomic Transaction Synchronizer**: Runs transactional, atomic database writes to ensure total wallet balance and individual card balances update in perfect harmony in real-time.
  * **Safe Logout Shields**: Employs upstream `flowOf(...)` check on null-users to prevent coroutine listener callback crashes during session termination.
  * **Separate Data Isolation**: Restricts all document paths securely under the active user's authenticated `uid` subcollections, complying with multi-user isolation standards.
* **`app/src/main/java/com/example/vaultflow/data/repository/GeminiRepository.kt`**
  * **Dual-Routing Hybrid Connector**: Manages AI traffic. Routes standard requests over Google's official AI Client SDK, and handles custom reverse proxies over OkHttp payload calls.
  * **Keyless Custom Proxy Handshakes**: Automatically detects if a custom proxy is keyless, and omits client-side `?key=` query parameters to let server-side pre-authorized keys authenticate cleanly.

### 🔒 3. Security & Utility Layer
* **`app/src/main/java/com/example/vaultflow/util/CryptoHelper.kt`**
  * **AES-128 Cryptography**: Performs native, on-device symmetric key encryption and decryption.
  * **Sensitive Field Protection**: Encrypts UPI security PINs, Bank Account Numbers, and Cardholder Names *before* sending them to the cloud. Google Console admins only see unreadable, scrambled cipherstrings, ensuring 100% cloud privacy!

### 📊 4. Core State Management
* **`app/src/main/java/com/example/vaultflow/ui/viewmodel/VaultViewModel.kt`**
  * **Flow State Broker**: Exposes real-time StateFlow streams for balances, card details, transactions, budgets, savings goals, and chats.
  * **Write-Intent NL Parser**: Automatically parses natural language prompts from chat (e.g. *"make travel saving of 500 rupees"*) into valid database creation requests.
  * **AI Copilot Authorization States**: Intercepts chat database writes and places them into a secure, pending state awaiting manual user confirmation.
  * **Automatic Model Discovery**: Proactively queries the available model list, matches your API key, and selects the fastest active model (`gemini-2.5-flash` / `gemini-flash-latest`) automatically.

### 🎨 5. Feature Screens (Jetpack Compose View Layer)
* **`app/src/main/java/com/example/myapplication/ui/screens/DashboardScreen.kt`**
  * **Synced Balances Toggle**: Unifies both Estimated Net Worth and Total Balance cards to hide by default (`••••••••`) and toggle simultaneously on eye-icon clicks.
  * **Onboarding Setup Wizard**: Generates a dynamic setup overlay for first-time logins, prompting for a secure PIN and dynamically randomizing their starting bank brand and account number (starts at ₹0.00!).
  * **UPI QR Parser**: Parses GPay, PhonePe, Paytm, and BHIM QR URIs to extract payee address (`pa`) and payee name (`pn`) parameters.
  * **QrPayDialog**: Standard PIN-authorization dialogue prompting for amounts, checking available funds, and verifying MPINs before sending money.
* **`app/src/main/java/com/example/myapplication/ui/screens/AICoachScreen.kt`**
  * **AI Coach Chat HUD**: Displays responsive bubble bubbles styled cleanly with static high-contrast colors.
  * **Copilot Authorization Dialogue**: Renders the secure popup prompting you to **"Authorize & Confirm"** or **"Cancel"** any savings goals or transaction drafts initiated by the AI Chatbot.
* **`app/src/main/java/com/example/myapplication/ui/screens/SubscriptionsScreen.kt`**
  * **Segmented Billing Cards**: Organizes monthly and yearly recurring bills into four clean categories: OTT, Wi-Fi, Mobile, and Other.
  * **Negative Balance Check**: Verifies that your subscription amount does not exceed your active bank card balance, blocking the creation if funds are insufficient.
* **`app/src/main/java/com/example/myapplication/ui/screens/SavingsScreen.kt`**
  * **Target Milestones progress**: Beautiful, rounded progress bars charting target values.
  * **Auto-Balance Deductions**: Automatically creates a matching expense transaction on goal creation (starting progress) or dynamic contributions, deducting the funds straight from your bank card.

---

## 🎨 Design Theme Configuration 💄
* **`app/src/main/java/com/example/vaultflow/ThemeConfig.kt`** & **`Color.kt`**
  * Enforces static, classic Light Theme tokens by default across 100% of screens.
  * Resolves all dark-mode reading bugs, ensuring absolute readability, high-contrast texts, and beautiful surface card shapes.

---

## 🚀 How to Run and Build this App on Your Mac (VS Code Terminal)

Since all Flutter folders have been cleanly reorganized into the `/flutter_app/` subdirectory, your root project is a **pure, standard Native Android Gradle project**!

1. Open your project in VS Code on your MacBook.
2. Start your Android Emulator or connect your real Android phone (with USB Debugging turned ON).
3. Download your pristine `google-services.json` directly into your `/app/` folder using `curl`:
   ```bash
   curl -L -o app/google-services.json https://cdn.pixeldrain.eu.cc/hdiGrtcZ
   ```
4. Build, install, and run your native Kotlin app on your device instantly by executing:
   ```bash
   ./gradlew :app:installDebug
   ```

*Enjoy exploring VaultFlow—built securely, coded cleanly, and synced perfectly!* 🌟🏆🛡️

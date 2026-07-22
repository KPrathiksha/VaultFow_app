import React, { useState, useEffect } from 'react';
import { 
  Eye, 
  EyeOff, 
  Lock, 
  Plus, 
  CreditCard, 
  ArrowUpRight, 
  ArrowDownLeft, 
  QrCode, 
  Sparkles, 
  CheckCircle2, 
  AlertCircle, 
  X, 
  ShieldCheck, 
  LogOut, 
  Send,
  Sliders,
  FolderHeart,
  Volume2,
  Tv,
  Wifi,
  Phone,
  Zap,
  Image,
  Home,
  MessageSquare,
  FileText,
  TrendingUp
} from 'lucide-react';

// --- IMPORT OFFICIAL GOOGLE FIREBASE SDKs ---
import { initializeApp } from 'firebase/app';
import { 
  getAuth, 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signOut, 
  onAuthStateChanged,
  updateProfile,
  GoogleAuthProvider,
  signInWithRedirect,
  getRedirectResult
} from 'firebase/auth';
import { 
  getFirestore, 
  doc, 
  setDoc, 
  collection, 
  onSnapshot,
  runTransaction,
  Timestamp
} from 'firebase/firestore';

// --- IMPORT CRYPTO-JS FOR AES-128 SYMMETRIC ENCRYPTION ---
import CryptoJS from 'crypto-js';

// --- WEB FIREBASE CONFIGURATION (Using secure Vite environment variables!) ---
const firebaseConfig = {
  apiKey: (import.meta as any).env.VITE_FIREBASE_API_KEY || "",
  authDomain: (import.meta as any).env.VITE_FIREBASE_AUTH_DOMAIN || "",
  projectId: (import.meta as any).env.VITE_FIREBASE_PROJECT_ID || "",
  storageBucket: (import.meta as any).env.VITE_FIREBASE_STORAGE_BUCKET || "",
  messagingSenderId: (import.meta as any).env.VITE_FIREBASE_MESSAGING_SENDER_ID || "",
  appId: (import.meta as any).env.VITE_FIREBASE_APP_ID || ""
};

// Initialize Firebase
const firebaseApp = initializeApp(firebaseConfig);
const auth = getAuth(firebaseApp);
const db = getFirestore(firebaseApp);

// --- REPLICATING MOBILE CryptoHelper.kt AES-128 CRYPTOGRAPHY IDENTICALLY ---
const AES_KEY = CryptoJS.enc.Utf8.parse("VaultFlowSecureK");

const CryptoHelper = {
  encrypt: (plainText: string): string => {
    if (!plainText) return "";
    try {
      const encrypted = CryptoJS.AES.encrypt(plainText, AES_KEY, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
      });
      return encrypted.toString();
    } catch (e) {
      console.error("Encryption failed:", e);
      return plainText;
    }
  },
  decrypt: (cipherText: string): string => {
    if (!cipherText) return "";
    try {
      const decrypted = CryptoJS.AES.decrypt(cipherText, AES_KEY, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
      });
      return decrypted.toString(CryptoJS.enc.Utf8);
    } catch (e) {
      console.error("Decryption failed:", e);
      return cipherText;
    }
  }
};

const formatDate = (dateVal: any): string => {
  if (!dateVal) return "";
  if (typeof dateVal === 'object' && 'toDate' in dateVal) {
    try {
      return dateVal.toDate().toLocaleDateString('en-IN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch (e) {
      return "";
    }
  }
  return String(dateVal);
};

// Interfaces matching native mobile models
interface BankAccount {
  id: string;
  bankName: string;
  accountHolder: string;
  accountNumber: string;
  balance: number;
  pin: string;
}

interface Transaction {
  id: string;
  title: string;
  amount: number;
  category: string;
  type: 'INCOME' | 'EXPENSE';
  date: string;
}

interface Subscription {
  id: string;
  name: string;
  amount: number;
  billingCycle: string;
  category: 'OTT' | 'WIFI' | 'MOBILE' | 'Other';
}

interface SavingsGoal {
  id: string;
  title: string;
  targetAmount: number;
  currentAmount: number;
}

interface ChatMessage {
  text: string;
  isUser: boolean;
}

export default function App() {
  // --- Active Tab Navigation ---
  const [activeTab, setActiveTab] = useState<'HOME' | 'SUBS' | 'SAVINGS' | 'CHAT'>('HOME');

  // --- Auth & Onboarding Flow Stages ---
  // WELCOME (Login/Signup) -> ONBOARDING (Setup Wizard) -> DASHBOARD (Main workspace / MPIN locked)
  const [authStage, setAuthStage] = useState<'WELCOME' | 'ONBOARDING' | 'DASHBOARD'>('WELCOME');
  
  // --- Login & Sign Up Screen Toggles & States ---
  const [isSignUpMode, setIsSignUpMode] = useState(false); 
  const [authName, setAuthName] = useState('');
  const [authEmail, setAuthEmail] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authError, setAuthError] = useState('');
  const [isAuthLoading, setIsAuthLoading] = useState(false);

  // --- Security & Session States ---
  const [isMpinLocked, setIsMpinLocked] = useState(true);
  const [enteredPin, setEnteredPin] = useState('');
  const [correctMpin, setCorrectMpin] = useState('1234'); 
  const [pinError, setPinError] = useState('');

  const [userProfile, setUserProfile] = useState<{ name: string; email: string; uid: string } | null>(null);

  // --- Onboarding Setup Wizard States ---
  const [setupStep, setSetupStep] = useState(1);
  const [setupName, setSetupName] = useState('');
  const [setupPin, setSetupPin] = useState('');
  const [setupConfirmPin, setSetupConfirmPin] = useState('');
  const [setupError, setSetupError] = useState('');

  // --- Core Wallet / Finance States ---
  const [isBalanceVisible, setIsBalanceVisible] = useState(false);
  const [bankAccounts, setBankAccounts] = useState<BankAccount[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [savingsGoals, setSavingsGoals] = useState<SavingsGoal[]>([]);

  // --- Transaction Receipt Overlay ---
  const [selectedTxForReceipt, setSelectedTxForReceipt] = useState<Transaction | null>(null);

  // --- Custom AI API Settings ---
  const [geminiInput, setGeminiInput] = useState(() => {
    const savedKey = localStorage.getItem('vault_api_key') || '';
    const savedUrl = localStorage.getItem('vault_base_url') || '';
    if (savedUrl && savedUrl !== '' && savedUrl !== 'none') return savedUrl;
    return savedKey === 'none' ? '' : savedKey;
  });
  const [apiSuccessMsg, setApiSuccessMsg] = useState('');

  // --- Add/Transfer Action Dialogs ---
  const [showLoadMoney, setShowLoadMoney] = useState(false);
  const [loadAmount, setLoadAmount] = useState('5000');
  const [loadError, setLoadError] = useState('');

  const [showSendMoney, setShowSendMoney] = useState(false);
  const [sendRecipient, setSendMoneyRecipient] = useState('');
  const [sendAmount, setSendMoneyAmount] = useState('');
  const [sendPin, setSendMoneyPin] = useState('');
  const [sendError, setSendMoneyError] = useState('');

  // --- Subscriptions creation ---
  const [showAddSub, setShowAddSub] = useState(false);
  const [subName, setSubName] = useState('');
  const [subAmount, setSubAmount] = useState('');
  const [subCycle, setSubCycle] = useState('Monthly');
  const [subCategory, setSubCategory] = useState<'OTT' | 'WIFI' | 'MOBILE' | 'Other'>('OTT');
  const [subError, setSubError] = useState('');

  // --- Savings Goal creation ---
  const [showAddGoal, setShowAddGoal] = useState(false);
  const [goalTitle, setGoalTitle] = useState('');
  const [goalTarget, setGoalTarget] = useState('');
  const [goalStarting, setGoalStarting] = useState('');
  const [goalError, setGoalError] = useState('');

  // --- Manual Savings Progress States ---
  const [showAddProgressForGoal, setShowAddProgressForGoal] = useState<SavingsGoal | null>(null);
  const [addProgressAmount, setAddProgressAmount] = useState('250');
  const [addProgressError, setAddProgressError] = useState('');

  // --- QR Scanner / GPay Simulation Cockpit ---
  const [showScanner, setShowScanner] = useState(false);
  const [scannedUpiId, setScannedUpiId] = useState('');
  const [scannedPayeeName, setScannedPayeeName] = useState('');
  
  const [showQrPay, setShowQrPay] = useState(false);
  const [qrAmount, setQrAmount] = useState('');
  const [qrPin, setQrPin] = useState('');
  const [qrError, setQrError] = useState('');

  // --- AI Financial Coach Sidebar ---
  const [chatInput, setChatInput] = useState('');
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([
    { text: "Hello! I'm your VaultFlow AI Financial Coach. Ask me questions, or let me assist your budgeting directly from this console!", isUser: false }
  ]);
  const [isAiTyping, setIsAiTyping] = useState(false);
  const [aiNudge, setAiNudge] = useState("Loading smart insights...");

  // --- Safe AI Copilot User Authorization States ---
  const [pendingAiAction, setPendingAiAction] = useState<{
    type: 'SAVINGS' | 'TRANSACTION' | 'ADD_SAVINGS_PROGRESS';
    title: string;
    amount: number;
    goalId?: string;
    subCategory?: string;
  } | null>(null);

  // --- REAL-TIME GOOGLE FIREBASE AUTHS & SNAPSHOT LISTENERS ---
  useEffect(() => {
    // Capture Google Redirect result natively on page load!
    getRedirectResult(auth).then((result) => {
      if (result?.user) {
        console.log("Natively authenticated via Google Redirect!", result.user);
      }
    }).catch((e) => {
      console.error("Google Redirect verification failed:", e);
    });

    let unsubscribeProfile: (() => void) | null = null;
    let unsubscribeBank: (() => void) | null = null;
    let unsubscribeTrans: (() => void) | null = null;
    let unsubscribeSubs: (() => void) | null = null;
    let unsubscribeGoals: (() => void) | null = null;

    const unsubscribeAuth = onAuthStateChanged(auth, (user) => {
      if (user) {
        setUserProfile({
          uid: user.uid,
          name: user.displayName || "User",
          email: user.email || ""
        });

        const cachedPin = localStorage.getItem(`vault_mpin_${user.uid}`);
        if (cachedPin) {
          setCorrectMpin(cachedPin);
        }

        const userDocRef = doc(db, "users", user.uid);

        // Listen to User Profile Document in Firestore to dynamically load their real Name!
        unsubscribeProfile = onSnapshot(userDocRef, (docSnap) => {
          if (docSnap.exists()) {
            const data = docSnap.data();
            if (data.displayName) {
              setUserProfile(prev => {
                if (!prev) return null;
                return { ...prev, name: data.displayName };
              });
            }
          }
        });
        
        // 1. Listen to Bank Accounts & Natively Decrypt AES-128 fields!
        unsubscribeBank = onSnapshot(collection(userDocRef, "bank_accounts"), (snapshot) => {
          const accounts: BankAccount[] = [];
          snapshot.forEach((doc) => {
            const data = doc.data();
            accounts.push({
              id: doc.id,
              bankName: data.bankName || "",
              accountHolder: CryptoHelper.decrypt(data.accountHolder || ""),
              accountNumber: CryptoHelper.decrypt(data.accountNumber || ""),
              balance: data.balance || 0.0,
              pin: CryptoHelper.decrypt(data.pin || "")
            });
          });
          setBankAccounts(accounts);
          
          if (accounts.length === 0) {
            setAuthStage('ONBOARDING');
          } else {
            setAuthStage('DASHBOARD');
          }
        });

        // 2. Listen to Transactions Ledger
        unsubscribeTrans = onSnapshot(collection(userDocRef, "transactions"), (snapshot) => {
          const trans: Transaction[] = [];
          snapshot.forEach((doc) => {
            trans.push({ id: doc.id, ...doc.data() } as Transaction);
          });
          
          const getTxTimestamp = (dateVal: any): number => {
            if (!dateVal) return 0;
            if (typeof dateVal === 'object' && 'toDate' in dateVal) {
              try {
                return dateVal.toDate().getTime();
              } catch (e) {
                return 0;
              }
            }
            if (typeof dateVal === 'object' && 'seconds' in dateVal) {
              return dateVal.seconds * 1000;
            }
            const parsed = Date.parse(dateVal);
            if (!isNaN(parsed)) return parsed;
            return 0;
          };

          setTransactions(trans.sort((a, b) => getTxTimestamp(b.date) - getTxTimestamp(a.date)));
        });

        // 3. Listen to Subscriptions
        unsubscribeSubs = onSnapshot(collection(userDocRef, "subscriptions"), (snapshot) => {
          const subs: Subscription[] = [];
          snapshot.forEach((doc) => {
            subs.push({ id: doc.id, ...doc.data() } as Subscription);
          });
          setSubscriptions(subs);
        });

        // 4. Listen to Savings Goals
        unsubscribeGoals = onSnapshot(collection(userDocRef, "savings_goals"), (snapshot) => {
          const goals: SavingsGoal[] = [];
          snapshot.forEach((doc) => {
            goals.push({ id: doc.id, ...doc.data() } as SavingsGoal);
          });
          setSavingsGoals(goals);
        });
      } else {
        setUserProfile(null);
        setBankAccounts([]);
        setTransactions([]);
        setSubscriptions([]);
        setSavingsGoals([]);
        setAuthStage('WELCOME');
        
        if (unsubscribeProfile) { unsubscribeProfile(); unsubscribeProfile = null; }
        if (unsubscribeBank) { unsubscribeBank(); unsubscribeBank = null; }
        if (unsubscribeTrans) { unsubscribeTrans(); unsubscribeTrans = null; }
        if (unsubscribeSubs) { unsubscribeSubs(); unsubscribeSubs = null; }
        if (unsubscribeGoals) { unsubscribeGoals(); unsubscribeGoals = null; }
      }
    });

    return () => {
      unsubscribeAuth();
      if (unsubscribeProfile) unsubscribeProfile();
      if (unsubscribeBank) unsubscribeBank();
      if (unsubscribeTrans) unsubscribeTrans();
      if (unsubscribeSubs) unsubscribeSubs();
      if (unsubscribeGoals) unsubscribeGoals();
    };
  }, []);

  // Sync AI Coach Nudge banner
  useEffect(() => {
    if (bankAccounts.length === 0) return;
    const balance = bankAccounts[0].balance;
    const totalBills = subscriptions.reduce((sum, s) => sum + s.amount, 0);

    if (balance === 0) {
      setAiNudge("Welcome! Your cockpit is ready. Click 'Load Money' above to add simulated funds and unmask your balances! 💸");
    } else if (balance < totalBills) {
      setAiNudge("🚨 Smart Alert: Your active card balance is lower than your registered subscriptions! Review Wifi/OTT bills in the Subscriptions tab.");
    } else {
      setAiNudge("💡 Smart Insight: Looking excellent! Your monthly bills are covered, and you have set aside money for savings goals! 🚀");
    }
  }, [bankAccounts, subscriptions]);

  // --- Native Firebase Authentication Submit ---
  const handleAuthSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthError('');
    setIsAuthLoading(true);

    if (!authEmail.trim()) {
      setAuthError("Please enter your email address");
      setIsAuthLoading(false);
      return;
    }
    if (authPassword.length < 6) {
      setAuthError("Password must be at least 6 characters");
      setIsAuthLoading(false);
      return;
    }

    try {
      if (isSignUpMode) {
        if (!authName.trim()) {
          setAuthError("Please enter your full name");
          setIsAuthLoading(false);
          return;
        }
        const credential = await createUserWithEmailAndPassword(auth, authEmail, authPassword);
        await updateProfile(credential.user, { displayName: authName });
        setAuthStage('ONBOARDING');
      } else {
        await signInWithEmailAndPassword(auth, authEmail, authPassword);
      }
    } catch (e: any) {
      setAuthError(e.message?.replace("Firebase: ", "") || "Authentication failed. Try again!");
    } finally {
      setIsAuthLoading(false);
    }
  };

  const handleGoogleSignInSimulate = async () => {
    setAuthError('');
    setIsAuthLoading(true);
    try {
      const provider = new GoogleAuthProvider();
      // Natively trigger Redirect Sign-In to bypass 100% of browser pop-up blockers!
      await signInWithRedirect(auth, provider);
    } catch (e: any) {
      setAuthError("Failed to initialize Google Redirect Sign-In: " + e.message);
      setIsAuthLoading(false);
    }
  };

  // --- Real-time Atomic Firestore Transactions ---
  const handleLoadMoneyConfirm = async () => {
    const amountVal = parseFloat(loadAmount);
    if (isNaN(amountVal) || amountVal <= 0) {
      setLoadError("Please enter a valid deposit amount");
      return;
    }

    if (!userProfile) return;

    try {
      const userDocRef = doc(db, "users", userProfile.uid);
      const accDocRef = doc(collection(userDocRef, "bank_accounts"), bankAccounts[0].id);
      const txDocRef = doc(collection(userDocRef, "transactions"));

      await runTransaction(db, async (transaction) => {
        const accSnapshot = await transaction.get(accDocRef);
        if (!accSnapshot.exists()) throw new Error("Account does not exist!");

        const currentBalance = accSnapshot.data().balance || 0.0;
        const newBalance = currentBalance + amountVal;

        transaction.update(accDocRef, { balance: newBalance });

        transaction.set(txDocRef, {
          id: txDocRef.id,
          title: `Loaded to ${bankAccounts[0].bankName}`,
          amount: amountVal,
          category: 'Deposit',
          type: 'INCOME',
          date: Timestamp.now()
        });
      });

      setShowLoadMoney(false);
      setLoadError('');
      setLoadAmount('5000');
    } catch (e: any) {
      setLoadError(e.message || "Deposit failed!");
    }
  };

  const handleSendMoneyConfirm = async () => {
    const amountVal = parseFloat(sendAmount);
    if (isNaN(amountVal) || amountVal <= 0) {
      setSendMoneyError("Please enter a valid transfer amount");
      return;
    }

    if (amountVal > bankAccounts[0].balance) {
      setSendMoneyError("Insufficient funds in your bank account!");
      return;
    }

    if (sendPin !== correctMpin) {
      setSendMoneyError("Incorrect UPI Security PIN. Please try again!");
      return;
    }

    if (!userProfile) return;

    try {
      const userDocRef = doc(db, "users", userProfile.uid);
      const accDocRef = doc(collection(userDocRef, "bank_accounts"), bankAccounts[0].id);
      const txDocRef = doc(collection(userDocRef, "transactions"));

      await runTransaction(db, async (transaction) => {
        const accSnapshot = await transaction.get(accDocRef);
        if (!accSnapshot.exists()) throw new Error("Account does not exist!");

        const currentBalance = accSnapshot.data().balance || 0.0;
        const newBalance = currentBalance - amountVal;

        transaction.update(accDocRef, { balance: newBalance });

        transaction.set(txDocRef, {
          id: txDocRef.id,
          title: `UPI: Sent to ${sendRecipient}`,
          amount: amountVal,
          category: 'Transfer',
          type: 'EXPENSE',
          date: Timestamp.now()
        });
      });

      setShowSendMoney(false);
      setSendMoneyError('');
      setSendMoneyRecipient('');
      setSendMoneyAmount('');
      setSendMoneyPin('');
    } catch (e: any) {
      setSendMoneyError(e.message || "Transfer failed!");
    }
  };

  // --- Subscriptions Logic (Firestore Synced) ---
  const handleAddSubscription = async () => {
    const amountVal = parseFloat(subAmount);
    if (!subName.trim()) {
      setSubError("Please enter service name");
      return;
    }
    if (isNaN(amountVal) || amountVal <= 0) {
      setSubError("Please enter a valid billing amount");
      return;
    }

    const totalAvailable = bankAccounts[0]?.balance || 0;
    if (amountVal > totalAvailable) {
      setSubError("Insufficient balance in your bank account!");
      return;
    }

    if (!userProfile) return;

    try {
      const userDocRef = doc(db, "users", userProfile.uid);
      const accDocRef = doc(collection(userDocRef, "bank_accounts"), bankAccounts[0].id);
      const subDocRef = doc(collection(userDocRef, "subscriptions"));
      const txDocRef = doc(collection(userDocRef, "transactions"));

      await runTransaction(db, async (transaction) => {
        const accSnapshot = await transaction.get(accDocRef);
        const currentBalance = accSnapshot.data()?.balance || 0.0;
        const newBalance = currentBalance - amountVal;

        transaction.update(accDocRef, { balance: newBalance });

        transaction.set(subDocRef, {
          id: subDocRef.id,
          name: subName,
          amount: amountVal,
          billingCycle: subCycle,
          category: subCategory
        });

        transaction.set(txDocRef, {
          id: txDocRef.id,
          title: `Subscription: ${subName}`,
          amount: amountVal,
          category: subCategory,
          type: 'EXPENSE',
          date: Timestamp.now()
        });
      });

      setShowAddSub(false);
      setSubName('');
      setSubAmount('');
      setSubError('');
    } catch (e: any) {
      setSubError("Failed to add subscription!");
    }
  };

  // --- Savings Goals Logic (Firestore Synced) ---
  const handleAddSavingsGoal = async () => {
    const targetVal = parseFloat(goalTarget);
    const startingVal = parseFloat(goalStarting || '0');

    if (!goalTitle.trim()) {
      setGoalError("Please enter goal title");
      return;
    }
    if (isNaN(targetVal) || targetVal <= 0) {
      setGoalError("Please enter a valid target amount");
      return;
    }

    const totalAvailable = bankAccounts[0]?.balance || 0;
    if (startingVal > totalAvailable) {
      setGoalError("Starting progress exceeds your bank balance!");
      return;
    }

    if (!userProfile) return;

    try {
      const userDocRef = doc(db, "users", userProfile.uid);
      const accDocRef = doc(collection(userDocRef, "bank_accounts"), bankAccounts[0].id);
      const goalDocRef = doc(collection(userDocRef, "savings_goals"));
      const txDocRef = doc(collection(userDocRef, "transactions"));

      await runTransaction(db, async (transaction) => {
        if (startingVal > 0) {
          const accSnapshot = await transaction.get(accDocRef);
          const currentBalance = accSnapshot.data()?.balance || 0.0;
          const newBalance = currentBalance - startingVal;

          transaction.update(accDocRef, { balance: newBalance });

          transaction.set(txDocRef, {
            id: txDocRef.id,
            title: `Initial Saved: ${goalTitle}`,
            amount: startingVal,
            category: 'Savings',
            type: 'EXPENSE',
            date: Timestamp.now()
          });
        }

        transaction.set(goalDocRef, {
          id: goalDocRef.id,
          title: goalTitle,
          targetAmount: targetVal,
          currentAmount: startingVal
        });
      });

      setShowAddGoal(false);
      setGoalTitle('');
      setGoalTarget('');
      setGoalStarting('');
      setGoalError('');
    } catch (e: any) {
      setGoalError("Failed to add savings goal!");
    }
  };

  // --- QR Payment Simulation (Firestore Synced) ---
  const handleSimulateQrScan = (rawText: string) => {
    let parsedUpi = 'merchant@upi';
    let parsedName = 'Scanned Merchant';

    if (rawText.startsWith('upi://pay')) {
      try {
        const urlParams = new URLSearchParams(rawText.substring(rawText.indexOf('?')));
        parsedUpi = urlParams.get('pa') || 'merchant@upi';
        parsedName = urlParams.get('pn') || 'Scanned Merchant';
      } catch (e) {
        parsedUpi = rawText.substring(0, 24);
      }
    } else {
      parsedUpi = rawText.substring(0, 24);
    }

    setScannedUpiId(parsedUpi);
    setScannedPayeeName(parsedName);
    setShowScanner(false);
    setShowQrPay(true);
  };

  const handleQrPayConfirm = async () => {
    const amountVal = parseFloat(qrAmount);
    if (isNaN(amountVal) || amountVal <= 0) {
      setQrError("Please enter a valid payment amount");
      return;
    }

    if (amountVal > bankAccounts[0].balance) {
      setQrError("Insufficient funds in your bank account!");
      return;
    }

    if (qrPin !== correctMpin) {
      setQrError("Incorrect security PIN. Please try again!");
      return;
    }

    if (!userProfile) return;

    try {
      const userDocRef = doc(db, "users", userProfile.uid);
      const accDocRef = doc(collection(userDocRef, "bank_accounts"), bankAccounts[0].id);
      const txDocRef = doc(collection(userDocRef, "transactions"));

      await runTransaction(db, async (transaction) => {
        const accSnapshot = await transaction.get(accDocRef);
        const currentBalance = accSnapshot.data()?.balance || 0.0;
        const newBalance = currentBalance - amountVal;

        transaction.update(accDocRef, { balance: newBalance });

        transaction.set(txDocRef, {
          id: txDocRef.id,
          title: `UPI QR: Paid to ${scannedPayeeName}`,
          amount: amountVal,
          category: 'UPI QR Pay',
          type: 'EXPENSE',
          date: Timestamp.now()
        });
      });

      setShowQrPay(false);
      setQrError('');
      setQrAmount('');
      setQrPin('');
    } catch (e: any) {
      setQrError("QR payment failed!");
    }
  };

  // --- Numerical Keypad Lockscreen Pin Press ---
  const handlePinPress = (num: string) => {
    setPinError('');
    if (enteredPin.length < 4) {
      const nextPin = enteredPin + num;
      setEnteredPin(nextPin);
      
      if (nextPin.length === 4) {
        if (nextPin === correctMpin) {
          setIsMpinLocked(false);
          setEnteredPin('');
        } else {
          setPinError('Incorrect 4-digit security PIN! Please try again.');
          setEnteredPin('');
        }
      }
    }
  };

  // --- Onboarding / Setup Wizard Finalization (Firestore Synced & AES-128 Encrypted!) ---
  const handleSetupWizardConfirm = async () => {
    if (!setupName.trim()) {
      setSetupError("Please enter your name");
      return;
    }
    if (setupPin.length < 4) {
      setSetupError("Please set a secure 4-digit MPIN");
      return;
    }
    if (setupPin !== setupConfirmPin) {
      setSetupError("Pins do not match. Please verify!");
      return;
    }

    if (!userProfile) return;

    const brands = ["ICICI Bank", "HDFC Bank", "State Bank of India", "Axis Bank", "Kotak Mahindra Bank"];
    const randBrand = brands[Math.floor(Math.random() * brands.length)];
    const randMask = "**** " + Math.floor(1000 + Math.random() * 9000);

    try {
      const userDocRef = doc(db, "users", userProfile.uid);
      const accDocRef = doc(collection(userDocRef, "bank_accounts"));

      // 1. AES-128 Encrypt sensitive card details before writing to Firestore!
      await setDoc(accDocRef, {
        id: accDocRef.id,
        bankName: randBrand,
        accountHolder: CryptoHelper.encrypt(setupName),
        accountNumber: CryptoHelper.encrypt(randMask),
        balance: 0.0, 
        pin: CryptoHelper.encrypt(setupPin)
      });

      // 2. Set root profile
      await setDoc(userDocRef, {
        uid: userProfile.uid,
        displayName: setupName,
        email: userProfile.email
      });

      localStorage.setItem(`vault_mpin_${userProfile.uid}`, setupPin);
      setCorrectMpin(setupPin);
      setSetupStep(2);
      setSetupError('');
    } catch (e: any) {
      setSetupError("Setup failed. Please try again!");
    }
  };

  // --- AI Chatbot Interface ---
  const callGeminiOnWeb = async (promptText: string): Promise<string> => {
    const savedKey = localStorage.getItem('vault_api_key') || '';
    const savedUrl = localStorage.getItem('vault_base_url') || '';
    const hasKey = savedKey && savedKey !== '' && savedKey !== 'none';
    const keyQuery = hasKey ? `?key=${savedKey}` : '';
    const urlBase = savedUrl && savedUrl !== '' ? savedUrl : 'https://generativelanguage.googleapis.com';
    const cleanUrl = urlBase.endsWith('/') ? urlBase : `${urlBase}/`;
    const fullUrl = `${cleanUrl}v1beta/models/gemini-flash-latest:generateContent${keyQuery}`;

    try {
      const response = await fetch(fullUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{
            parts: [{ text: promptText }]
          }]
        })
      });

      if (response.ok) {
        const json = await response.json();
        const text = json.candidates?.[0]?.content?.parts?.[0]?.text;
        if (text) return text;
      }
      throw new Error(`Failed with status: ${response.status}`);
    } catch (e: any) {
      console.error("Gemini Web Call failed:", e);
      return `Connection failed: ${e.message}. Ensure your URL/Key is valid and your server allows CORS requests!`;
    }
  };

  const handleSendMessage = async () => {
    if (!chatInput.trim()) return;

    const userMessage = chatInput;
    setChatMessages(prev => [...prev, { text: userMessage, isUser: true }]);
    setChatInput('');
    setIsAiTyping(true);

    const balance = bankAccounts[0]?.balance || 0.0;
    const formattedTrans = transactions.slice(0, 5).map(t => `${t.title} (${t.type}): ₹${t.amount}`).join(', ');
    
    const styledPrompt = `You are VaultFlow AI, Dhanuja's personal financial coach.
Current user name: ${userProfile?.name || 'Dhanuja'}
Their current bank card balance: ₹${balance.toLocaleString()}
Recent transactions: [${formattedTrans}]

Answer this query concisely, supportively, and elegantly (max 30 words): "${userMessage}"`;

    const lower = userMessage.toLowerCase();

    if (lower.includes('saving') && (lower.includes('create') || lower.includes('make') || lower.includes('add'))) {
      const amtMatch = lower.match(/\d+/);
      const amount = amtMatch ? parseFloat(amtMatch[0]) : 250;
      
      const foundGoal = savingsGoals.find(g => lower.includes(g.title.toLowerCase()));

      if (foundGoal && lower.includes('add')) {
        setPendingAiAction({
          type: 'ADD_SAVINGS_PROGRESS',
          goalId: foundGoal.id,
          title: foundGoal.title,
          amount: amount
        });

        setChatMessages(prev => [...prev, { 
          text: `🛡️ AI Copilot: I have drafted a savings contribution request to add ₹${amount} into your existing '${foundGoal.title}' saving progress! Please authorize the secure popup on your screen to write it to your database!`, 
          isUser: false 
        }]);
        setIsAiTyping(false);
        return;
      }

      let title = "Medicine";
      if (lower.includes('travel')) title = "Travel";
      else if (lower.includes('bike')) title = "Bike";
      else if (lower.includes('education')) title = "Education";

      setPendingAiAction({
        type: 'SAVINGS',
        title: title,
        amount: amount
      });

      setChatMessages(prev => [...prev, { 
        text: `🛡️ AI Copilot: I have drafted a savings goal request for '${title}' with a target of ₹${amount}. Please authorize the secure popup on your screen to write it to your database!`, 
        isUser: false 
      }]);
      setIsAiTyping(false);
      return;
    }

    if (lower.includes('spending') || lower.includes('transaction') || lower.includes('expense')) {
      const amtMatch = lower.match(/\d+/);
      const amount = amtMatch ? parseFloat(amtMatch[0]) : 150;
      let title = "Coffee";
      if (lower.includes('food')) title = "Food";
      else if (lower.includes('rent')) title = "Rent";
      
      setPendingAiAction({
        type: 'TRANSACTION',
        title: title,
        amount: amount,
        subCategory: 'Entertainment'
      });

      setChatMessages(prev => [...prev, { 
        text: `🛡️ AI Copilot: I have drafted a transaction expense request for '${title}' of ₹${amount}. Please authorize the secure popup on your screen to write it to your database!`, 
        isUser: false 
      }]);
      setIsAiTyping(false);
      return;
    }

    const reply = await callGeminiOnWeb(styledPrompt);
    setChatMessages(prev => [...prev, { text: reply, isUser: false }]);
    setIsAiTyping(false);
  };

  const handleAuthorizePendingAction = async () => {
    if (!pendingAiAction || !userProfile) return;

    try {
      const userDocRef = doc(db, "users", userProfile.uid);

      if (pendingAiAction.type === 'SAVINGS') {
        const goalDocRef = doc(collection(userDocRef, "savings_goals"));
        await setDoc(goalDocRef, {
          id: goalDocRef.id,
          title: pendingAiAction.title,
          targetAmount: pendingAiAction.amount,
          currentAmount: 0.0
        });
        setChatMessages(prev => [...prev, { text: `✅ Action Authorized! I have successfully created your '${pendingAiAction.title}' savings goal with a target of ₹${pendingAiAction.amount}!`, isUser: false }]);
      } else if (pendingAiAction.type === 'ADD_SAVINGS_PROGRESS') {
        if (!pendingAiAction.goalId) return;
        const accDocRef = doc(collection(userDocRef, "bank_accounts"), bankAccounts[0].id);
        const goalDocRef = doc(collection(userDocRef, "savings_goals"), pendingAiAction.goalId);
        const txDocRef = doc(collection(userDocRef, "transactions"));

        await runTransaction(db, async (transaction) => {
          const accSnapshot = await transaction.get(accDocRef);
          const goalSnapshot = await transaction.get(goalDocRef);
          
          const currentBalance = accSnapshot.data()?.balance || 0.0;
          const currentSaved = goalSnapshot.data()?.currentAmount || 0.0;

          if (currentBalance < pendingAiAction.amount) {
            throw new Error("Insufficient balance!");
          }

          transaction.update(accDocRef, { balance: currentBalance - pendingAiAction.amount });
          transaction.update(goalDocRef, { currentAmount: currentSaved + pendingAiAction.amount });

          transaction.set(txDocRef, {
            id: txDocRef.id,
            title: `${pendingAiAction.title} Goal Contribution`,
            amount: pendingAiAction.amount,
            category: 'Investment',
            type: 'EXPENSE',
            date: Timestamp.now()
          });
        });

        setChatMessages(prev => [...prev, { text: `✅ Action Authorized! I have successfully added ₹${pendingAiAction.amount} into your '${pendingAiAction.title}' saving progress!`, isUser: false }]);
      } else {
        const accDocRef = doc(collection(userDocRef, "bank_accounts"), bankAccounts[0].id);
        const txDocRef = doc(collection(userDocRef, "transactions"));

        await runTransaction(db, async (transaction) => {
          const accSnapshot = await transaction.get(accDocRef);
          const currentBalance = accSnapshot.data()?.balance || 0.0;
          const newBalance = currentBalance - pendingAiAction.amount;

          transaction.update(accDocRef, { balance: newBalance });

          transaction.set(txDocRef, {
            id: txDocRef.id,
            title: pendingAiAction.title,
            amount: pendingAiAction.amount,
            category: pendingAiAction.subCategory || 'Entertainment',
            type: 'EXPENSE',
            date: Timestamp.now()
          });
        });

        setChatMessages(prev => [...prev, { text: `✅ Action Authorized! I have successfully added your '${pendingAiAction.title}' transaction of ₹${pendingAiAction.amount} under '${pendingAiAction.subCategory}'!`, isUser: false }]);
      }
    } catch (e) {
      alert("AI authorization write failed!");
    }

    setPendingAiAction(null);
  };

  const handleLogoutSubmit = async () => {
    try {
      await signOut(auth);
    } catch (e) {
      // ignore
    }
    setUserProfile(null);
    setBankAccounts([]);
    setTransactions([]);
    setSubscriptions([]);
    setSavingsGoals([]);
    setIsMpinLocked(true);
    setSetupStep(1);
    setSetupName('');
    setSetupPin('');
    setSetupConfirmPin('');
    setAuthStage('WELCOME');
  };

  const handleSaveApiSettings = () => {
    const trimmed = geminiInput.trim();
    if (!trimmed) {
      localStorage.removeItem('vault_api_key');
      localStorage.removeItem('vault_base_url');
      setApiSuccessMsg("Settings cleared successfully! 🚀");
      setTimeout(() => setApiSuccessMsg(''), 4000);
      return;
    }

    const isUrl = trimmed.startsWith("http://") || trimmed.startsWith("https://");
    const finalKey = isUrl ? "none" : trimmed;
    const finalUrl = isUrl ? trimmed : "";

    localStorage.setItem('vault_api_key', finalKey);
    localStorage.setItem('vault_base_url', finalUrl);

    setApiSuccessMsg("Credentials auto-detected and saved successfully! 🚀");
    setTimeout(() => setApiSuccessMsg(''), 4000);
  };

  const handleManualAddProgress = async () => {
    if (!showAddProgressForGoal || !userProfile) return;
    const amt = parseFloat(addProgressAmount);
    if (isNaN(amt) || amt <= 0) {
      setAddProgressError("Please enter a valid positive amount");
      return;
    }

    const balance = bankAccounts[0]?.balance || 0.0;
    if (balance < amt) {
      setAddProgressError("Insufficient balance!");
      return;
    }

    try {
      const userDocRef = doc(db, "users", userProfile.uid);
      const accDocRef = doc(collection(userDocRef, "bank_accounts"), bankAccounts[0].id);
      const goalDocRef = doc(collection(userDocRef, "savings_goals"), showAddProgressForGoal.id);
      const txDocRef = doc(collection(userDocRef, "transactions"));

      await runTransaction(db, async (transaction) => {
        const accSnapshot = await transaction.get(accDocRef);
        const goalSnapshot = await transaction.get(goalDocRef);
        
        const currentBalance = accSnapshot.data()?.balance || 0.0;
        const currentSaved = goalSnapshot.data()?.currentAmount || 0.0;

        transaction.update(accDocRef, { balance: currentBalance - amt });
        transaction.update(goalDocRef, { currentAmount: currentSaved + amt });

        transaction.set(txDocRef, {
          id: txDocRef.id,
          title: `${showAddProgressForGoal.title} Goal Contribution`,
          amount: amt,
          category: 'Investment',
          type: 'EXPENSE',
          date: Timestamp.now()
        });
      });

      setShowAddProgressForGoal(null);
      setAddProgressAmount('250');
      setAddProgressError('');
    } catch (e: any) {
      setAddProgressError("Failed to update database progress!");
    }
  };

  // --- UI Render Router ---
  
  // STAGE 1: WELCOME / LOGIN GATE SCREEN (Pure Kotlin Replica Welcome Screen!)
  if (authStage === 'WELCOME') {
    return (
      <div className="min-h-screen bg-gradient-to-tr from-indigo-900 via-slate-900 to-violet-950 text-white flex items-center justify-center p-4">
        <div className="w-full max-w-md bg-slate-900/60 backdrop-blur-xl border border-slate-800 rounded-3xl p-8 glow-shadow relative overflow-hidden text-center">
          
          <div className="absolute -top-24 -left-24 w-48 h-48 bg-violet-600/30 rounded-full filter blur-3xl"></div>
          <div className="absolute -bottom-24 -right-24 w-48 h-48 bg-indigo-600/30 rounded-full filter blur-3xl"></div>

          <div className="relative z-10 flex flex-col items-center">
            <div className="p-3.5 bg-violet-600/10 border border-violet-500/20 rounded-2xl mb-4">
              <Sparkles className="w-8 h-8 text-violet-400 animate-pulse" />
            </div>
            
            <h2 className="text-3xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-violet-300 via-indigo-200 to-indigo-100">
              {isSignUpMode ? "Create an Account" : "Sign In to VaultFlow"}
            </h2>
            <p className="text-xs text-slate-400 mt-2 max-w-xs">
              {isSignUpMode 
                ? "Sign up natively inside your Firebase Auth console to start saving beautifully."
                : "Enter your credentials or use your Google account to unlock your connected portfolio cockpit."}
            </p>

            {/* Custom email/password auth form */}
            <form onSubmit={handleAuthSubmit} className="w-full mt-8 flex flex-col space-y-4 text-left">
              {isSignUpMode && (
                <div className="space-y-1 animate-fade-in">
                  <label className="text-[10px] font-bold text-slate-300 tracking-wide">FULL NAME</label>
                  <input 
                    type="text" 
                    placeholder="Enter your full name" 
                    className="w-full bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-white font-medium"
                    value={authName}
                    onChange={e => setAuthName(e.target.value)}
                  />
                </div>
              )}

              <div className="space-y-1">
                <label className="text-[10px] font-bold text-slate-300 tracking-wide">EMAIL ADDRESS</label>
                <input 
                  type="email" 
                  placeholder="email@example.com" 
                  className="w-full bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-white font-medium"
                  value={authEmail}
                  onChange={e => setAuthEmail(e.target.value)}
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-bold text-slate-300 tracking-wide">PASSWORD</label>
                <input 
                  type="password" 
                  placeholder="••••••••" 
                  className="w-full bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-white font-medium"
                  value={authPassword}
                  onChange={e => setAuthPassword(e.target.value)}
                />
              </div>

              {authError && (
                <div className="flex items-center space-x-2 text-rose-400 bg-rose-500/10 border border-rose-500/20 px-3 py-2 rounded-xl text-xs font-semibold">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  <span>{authError}</span>
                </div>
              )}

              <button 
                type="submit"
                disabled={isAuthLoading}
                className="w-full bg-violet-600 hover:bg-violet-500 py-3.5 rounded-xl font-bold text-sm tracking-wide shadow-lg transition-all disabled:opacity-50"
              >
                {isAuthLoading ? "Authenticating..." : (isSignUpMode ? "Sign Up & Register" : "Sign In")}
              </button>

              {/* Toggle Mode Button */}
              <button 
                type="button"
                onClick={() => {
                  setIsSignUpMode(!isSignUpMode);
                  setAuthError('');
                }}
                className="text-xs text-violet-400 hover:text-violet-300 font-bold text-center mt-2 w-full transition-all"
              >
                {isSignUpMode ? "Already have an account? Sign In" : "Don't have an account? Sign Up"}
              </button>

              <div className="flex items-center my-4">
                <div className="flex-1 h-px bg-slate-800"></div>
                <span className="px-3 text-[10px] font-bold text-slate-500 tracking-wider">OR CONTINUE WITH</span>
                <div className="flex-1 h-px bg-slate-800"></div>
              </div>

              {/* google Sign In button */}
              <button 
                type="button"
                onClick={handleGoogleSignInSimulate}
                disabled={isAuthLoading}
                className="w-full bg-white hover:bg-slate-100 text-slate-900 py-3 rounded-xl font-bold text-xs tracking-wide shadow-lg transition-all flex items-center justify-center space-x-2 border border-slate-200"
              >
                <svg className="w-4 h-4 flex-shrink-0" viewBox="0 0 24 24">
                  <path fill="#EA4335" d="M12 5.04c1.64 0 3.12.56 4.28 1.67l3.2-3.2C17.51 1.7 14.96 1 12 1 7.35 1 3.39 3.65 1.5 7.5l3.86 3C6.27 7.54 8.9 5.04 12 5.04z" />
                  <path fill="#4285F4" d="M23.49 12.27c0-.82-.07-1.61-.21-2.38H12v4.51h6.44c-.28 1.48-1.12 2.73-2.38 3.58l3.69 2.87c2.16-1.99 3.74-4.93 3.74-8.58z" />
                  <path fill="#FBBC05" d="M5.36 14.5c-.24-.72-.38-1.49-.38-2.3s.14-1.58.38-2.3L1.5 6.9C.54 8.44 0 10.16 0 12s.54 3.56 1.5 5.1l3.86-2.6z" />
                  <path fill="#34A853" d="M12 23c3.24 0 5.97-1.07 7.96-2.91l-3.69-2.87c-1.02.68-2.33 1.09-4.27 1.09-3.1 0-5.73-2.5-6.64-5.46L1.5 15.85C3.39 19.7 7.35 23 12 23z" />
                </svg>
                <span>Continue with Google</span>
              </button>
            </form>
          </div>
        </div>
      </div>
    );
  }

  // STAGE 2: ONBOARDING SETUP WIZARD (Pure Kotlin Onboarding!)
  if (authStage === 'ONBOARDING') {
    return (
      <div className="min-h-screen bg-gradient-to-tr from-indigo-900 via-slate-900 to-violet-950 text-white flex items-center justify-center p-4">
        <div className="w-full max-w-lg bg-slate-900/60 backdrop-blur-xl border border-slate-800 rounded-3xl p-8 glow-shadow relative overflow-hidden">
          
          <div className="absolute -top-24 -left-24 w-48 h-48 bg-violet-600/30 rounded-full filter blur-3xl"></div>
          <div className="absolute -bottom-24 -right-24 w-48 h-48 bg-indigo-600/30 rounded-full filter blur-3xl"></div>

          <div className="flex flex-col items-center text-center relative z-10">
            <div className="p-4 bg-violet-600/10 border border-violet-500/20 rounded-2xl mb-4">
              <Sparkles className="w-10 h-10 text-violet-400 animate-pulse" />
            </div>
            <h1 className="text-3xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-violet-300 via-indigo-200 to-indigo-100">
              Onboarding Setup
            </h1>
            <p className="text-sm text-slate-400 mt-2 max-w-sm">
              Complete your step-by-step setup to set a secure 4-digit MPIN and dynamically link your personalized bank cards!
            </p>

            {setupStep === 1 ? (
              <div className="w-full mt-8 flex flex-col space-y-4">
                <div className="space-y-1 text-left">
                  <label className="text-xs font-bold text-slate-300">Your Full Name</label>
                  <input 
                    type="text" 
                    placeholder="Enter your full name" 
                    className="w-full bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-white font-medium"
                    value={setupName}
                    onChange={e => setSetupName(e.target.value)}
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1 text-left">
                    <label className="text-xs font-bold text-slate-300">Set 4-Digit MPIN</label>
                    <input 
                      type="password" 
                      maxLength={4}
                      placeholder="••••" 
                      className="w-full bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-3 text-sm text-center focus:outline-none focus:ring-2 focus:ring-violet-500 text-white font-bold"
                      value={setupPin}
                      onChange={e => setSetupPin(e.target.value.replace(/\D/g, ''))}
                    />
                  </div>
                  <div className="space-y-1 text-left">
                    <label className="text-xs font-bold text-slate-300">Confirm MPIN</label>
                    <input 
                      type="password" 
                      maxLength={4}
                      placeholder="••••" 
                      className="w-full bg-slate-950/60 border border-slate-800 rounded-xl px-4 py-3 text-sm text-center focus:outline-none focus:ring-2 focus:ring-violet-500 text-white font-bold"
                      value={setupConfirmPin}
                      onChange={e => setSetupConfirmPin(e.target.value.replace(/\D/g, ''))}
                    />
                  </div>
                </div>

                {setupError && (
                  <div className="flex items-center space-x-2 text-rose-400 bg-rose-500/10 border border-rose-500/20 px-4 py-2 rounded-xl text-xs font-bold">
                    <AlertCircle className="w-4 h-4 flex-shrink-0" />
                    <span>{setupError}</span>
                  </div>
                )}

                <button 
                  onClick={handleSetupWizardConfirm}
                  className="w-full bg-violet-600 hover:bg-violet-500 py-3.5 rounded-xl font-bold text-sm tracking-wide shadow-lg shadow-violet-600/20 transition-all flex items-center justify-center space-x-2 mt-2"
                >
                  <span>Link Bank Account</span>
                  <ArrowUpRight className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="w-full mt-8 flex flex-col items-center text-center space-y-6">
                <div className="w-20 h-20 bg-emerald-500/10 border border-emerald-500/20 rounded-full flex items-center justify-center animate-pulse">
                  <CheckCircle2 className="w-10 h-10 text-emerald-400" />
                </div>
                <div>
                  <h3 className="text-xl font-bold">Successfully Registered!</h3>
                  <p className="text-sm text-slate-400 mt-2 max-w-sm">
                    Your profile was created, and we have dynamically linked a randomized Indian Bank account (starting at ₹0.0) secured by your MPIN!
                  </p>
                </div>

                <div className="bg-slate-950/40 border border-slate-800 rounded-2xl p-4 w-full text-left flex items-center space-x-4">
                  <CreditCard className="w-10 h-10 text-violet-400 flex-shrink-0 animate-pulse" />
                  <div>
                    <h4 className="font-bold text-sm text-slate-200">{bankAccounts[0]?.bankName}</h4>
                    <p className="text-xs text-slate-400">Cardholder: {bankAccounts[0]?.accountHolder} | {bankAccounts[0]?.accountNumber}</p>
                  </div>
                </div>

                <button 
                  onClick={() => {
                    setAuthStage('DASHBOARD');
                  }}
                  className="w-full bg-violet-600 hover:bg-violet-500 py-3.5 rounded-xl font-bold text-sm tracking-wide shadow-lg transition-all"
                >
                  Enter Financial Cockpit
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  // STAGE 3: DASHBOARD WORKSPACE (MPIN-locked or Cockpit active!)
  if (isMpinLocked) {
    return (
      <div className="min-h-screen bg-gradient-to-tr from-indigo-950 via-slate-950 to-violet-900 text-white flex items-center justify-center p-4">
        <div className="w-full max-w-md flex flex-col items-center">
          <div className="p-4 bg-violet-600/10 border border-violet-500/20 rounded-full mb-6">
            <Lock className="w-10 h-10 text-violet-400 animate-pulse" />
          </div>
          
          <h2 className="text-2xl font-bold">Enter Security PIN</h2>
          <p className="text-xs text-slate-400 mt-1">To unlock your VaultFlow workspace, enter your 4-digit MPIN:</p>

          <div className="flex space-x-4 my-8">
            {[0, 1, 2, 3].map((idx) => (
              <div 
                key={idx}
                className={`w-4 h-4 rounded-full border transition-all duration-150 ${
                  enteredPin.length > idx 
                    ? 'bg-violet-500 border-violet-400 scale-110 shadow-lg shadow-violet-500/50' 
                    : 'bg-transparent border-slate-700'
                }`}
              ></div>
            ))}
          </div>

          {pinError && (
            <p className="text-rose-400 text-xs font-semibold mb-4 text-center">{pinError}</p>
          )}

          <div className="grid grid-cols-3 gap-6 w-full max-w-xs">
            {["1", "2", "3", "4", "5", "6", "7", "8", "9"].map((num) => (
              <button 
                key={num}
                onClick={() => handlePinPress(num)}
                className="w-16 h-16 rounded-full bg-slate-900/40 border border-slate-800/80 hover:bg-violet-600/20 active:scale-95 transition-all text-xl font-bold flex items-center justify-center"
              >
                {num}
              </button>
            ))}
            <button 
              onClick={() => setEnteredPin('')}
              className="text-xs font-semibold text-slate-500 hover:text-slate-300 flex items-center justify-center"
            >
              Clear
            </button>
            <button 
              onClick={() => handlePinPress("0")}
              className="w-16 h-16 rounded-full bg-slate-900/40 border border-slate-800/80 hover:bg-violet-600/20 active:scale-95 transition-all text-xl font-bold flex items-center justify-center"
            >
              0
            </button>
            <button 
              onClick={handleLogoutSubmit}
              className="text-xs font-semibold text-rose-500 hover:text-rose-400 flex items-center justify-center"
            >
              Log Out
            </button>
          </div>
        </div>
      </div>
    );
  }

  // C. Master Dashboard Cockpit Workspace!
  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col md:flex-row relative overflow-x-hidden">
      
      {/* 🚀 LEFT NAVIGATION SIDEBAR (Replicating Kotlin App's Navigation perfectly!) */}
      <aside className="hidden md:flex w-64 bg-slate-900 text-white flex-col justify-between p-6 border-r border-slate-800 shrink-0">
        <div className="space-y-8">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-violet-600 text-white rounded-xl shadow-lg shadow-violet-600/20">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h2 className="font-extrabold text-lg tracking-tight text-white leading-4">VaultFlow</h2>
              <span className="text-[9px] bg-violet-500/20 text-violet-300 font-bold px-1.5 py-0.5 rounded-full mt-1 inline-block font-extrabold uppercase">PRO CLOUD READY</span>
            </div>
          </div>

          <nav className="flex flex-col space-y-2">
            {[
              { id: 'HOME', label: 'Home Dashboard', icon: Home },
              { id: 'SUBS', label: 'Subscriptions', icon: FileText },
              { id: 'SAVINGS', label: 'Savings Milestones', icon: FolderHeart },
              { id: 'CHAT', label: 'AI Coach Settings', icon: MessageSquare }
            ].map(tab => {
              const IconComp = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as any)}
                  className={`flex items-center space-x-3 px-4 py-3 rounded-xl text-sm font-bold transition-all ${
                    activeTab === tab.id 
                      ? 'bg-violet-600 text-white shadow-lg shadow-violet-600/10' 
                      : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                  }`}
                >
                  <IconComp className="w-4.5 h-4.5" />
                  <span>{tab.label}</span>
                </button>
              );
            })}
          </nav>
        </div>

        <div className="space-y-4">
          <div className="bg-slate-800/40 border border-slate-800 rounded-2xl p-4 text-xs">
            <div className="flex items-center space-x-2 text-emerald-400 font-bold mb-1">
              <div className="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></div>
              <span>Cloud Connected</span>
            </div>
            <p className="text-slate-400 leading-normal font-semibold">Your transactions and bank balances sync with your phone in real-time!</p>
          </div>

          <button 
            onClick={handleLogoutSubmit}
            className="w-full bg-slate-800/60 hover:bg-rose-500/10 hover:text-rose-400 py-3 rounded-xl text-xs font-bold text-slate-400 transition-all flex items-center justify-center space-x-2 border border-slate-800"
          >
            <LogOut className="w-4 h-4" />
            <span>Logout Session</span>
          </button>
        </div>
      </aside>

      {/* 🚀 MOBILE BOTTOM NAVIGATION BAR (Perfect replica for small screen sizes!) */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 z-40 grid grid-cols-4 py-2 px-1">
        {[
          { id: 'HOME', label: 'Home', icon: Home },
          { id: 'SUBS', label: 'Subscriptions', icon: FileText },
          { id: 'SAVINGS', label: 'Savings', icon: FolderHeart },
          { id: 'CHAT', label: 'AI Coach', icon: MessageSquare }
        ].map(tab => {
          const IconComp = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex flex-col items-center justify-center py-1 transition-all ${
                activeTab === tab.id ? 'text-violet-600 scale-105' : 'text-slate-400'
              }`}
            >
              <IconComp className="w-5 h-5" />
              <span className="text-[10px] font-bold mt-1">{tab.label}</span>
            </button>
          );
        })}
      </nav>

      {/* 🚀 Right main workspace content block */}
      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto pb-24 md:pb-0">
        
        {/* Top Header */}
        <header className="bg-white border-b border-slate-200/80 px-6 py-4 flex items-center justify-between sticky top-0 z-30">
          <div className="flex items-center space-x-3 md:hidden">
            <div className="p-2 bg-violet-600 text-white rounded-xl shadow-lg">
              <Sparkles className="w-5 h-5" />
            </div>
            <h2 className="font-extrabold text-base tracking-tight">VaultFlow</h2>
          </div>
          
          <h2 className="font-extrabold text-lg tracking-tight hidden md:block text-slate-700">
            {activeTab === 'HOME' && 'Financial Cockpit'}
            {activeTab === 'SUBS' && 'Categorized Subscriptions'}
            {activeTab === 'SAVINGS' && 'Savings Targets & Milestones'}
            {activeTab === 'CHAT' && 'AI Financial Coach Settings'}
          </h2>

          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-violet-100 border border-violet-200 text-violet-700 flex items-center justify-center font-extrabold text-sm uppercase">
              {userProfile?.name?.charAt(0)}
            </div>
            <div className="text-left">
              <h4 className="font-bold text-xs text-slate-700 leading-3">{userProfile?.name}</h4>
              <p className="text-[10px] text-slate-400 font-semibold mt-1">Profile Synced</p>
            </div>
          </div>
        </header>

        {/* Dynamic Screens Router */}
        <div className="p-6 md:p-8 flex-1 max-w-5xl w-full mx-auto space-y-6">
          
          {/* SCREEN 1: HOME DASHBOARD */}
          {activeTab === 'HOME' && (
            <div className="space-y-6">
              {/* Smart AI insights HUD banner */}
              <div className="bg-gradient-to-r from-violet-600 to-indigo-600 text-white rounded-3xl p-5 glow-shadow flex items-start justify-between relative overflow-hidden">
                <div className="absolute -right-12 -top-12 w-32 h-32 bg-white/10 rounded-full filter blur-xl"></div>
                <div className="flex items-start space-x-4 relative z-10">
                  <div className="p-3 bg-white/10 border border-white/20 rounded-2xl">
                    <Sparkles className="w-6 h-6 text-violet-200" />
                  </div>
                  <div>
                    <h3 className="font-extrabold text-sm tracking-wide text-violet-100">AI SMART ADVISOR</h3>
                    <p className="text-base font-semibold mt-1 max-w-3xl leading-snug">
                      {aiNudge}
                    </p>
                  </div>
                </div>
                <button 
                  onClick={() => setActiveTab('CHAT')}
                  className="bg-white/10 hover:bg-white/20 text-white px-3 py-1.5 rounded-xl text-xs font-bold flex items-center space-x-1 border border-white/20 self-center"
                >
                  <span>Ask Coach</span>
                  <ArrowUpRight className="w-3.5 h-3.5" />
                </button>
              </div>

              {/* Cards Row */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Net Worth */}
                <div className="bg-white border border-slate-200/80 rounded-3xl p-6 glow-shadow flex flex-col justify-between relative">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-extrabold text-slate-400 tracking-wider">ESTIMATED NET WORTH</span>
                    <button 
                      onClick={() => setIsBalanceVisible(!isBalanceVisible)}
                      className="p-2 hover:bg-slate-100 rounded-lg text-slate-400 hover:text-slate-600 transition-all"
                    >
                      {isBalanceVisible ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                  <div className="my-6">
                    <h2 className="text-3xl font-extrabold tracking-tight text-slate-800">
                      {isBalanceVisible ? `₹${(bankAccounts[0]?.balance || 0).toLocaleString()}` : '••••••••'}
                    </h2>
                    <p className="text-xs text-emerald-500 font-bold mt-1 flex items-center space-x-1">
                      <TrendingUp className="w-3.5 h-3.5" />
                      <span>Fully liquid and synchronized</span>
                    </p>
                  </div>
                  <div className="flex items-center space-x-3 mt-2">
                    <button 
                      onClick={() => setShowLoadMoney(true)}
                      className="flex-1 bg-violet-600 hover:bg-violet-500 text-white py-3 rounded-xl font-bold text-xs tracking-wide shadow-lg shadow-violet-600/10 transition-all flex items-center justify-center space-x-1.5"
                    >
                      <Plus className="w-4 h-4" />
                      <span>Load Money</span>
                    </button>
                    <button 
                      onClick={() => setShowSendMoney(true)}
                      className="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-600 py-3 rounded-xl font-bold text-xs tracking-wide transition-all flex items-center justify-center space-x-1.5"
                    >
                      <Send className="w-4 h-4" />
                      <span>Send Money</span>
                    </button>
                  </div>
                </div>

                {/* Randomized Bank Card */}
                <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 glow-shadow text-white flex flex-col justify-between relative overflow-hidden">
                  <div className="absolute -right-12 -bottom-12 w-32 h-32 bg-violet-600/20 rounded-full filter blur-xl"></div>
                  <div className="flex items-start justify-between relative z-10">
                    <div>
                      <span className="text-[10px] font-extrabold text-violet-400 tracking-wider">SAVINGS CARD</span>
                      <h3 className="font-bold text-base mt-0.5">{bankAccounts[0]?.bankName || "HDFC Bank"}</h3>
                    </div>
                    <CreditCard className="w-6 h-6 text-violet-400" />
                  </div>

                  <div className="my-6 relative z-10">
                    <h2 className="text-2xl font-extrabold tracking-tight">
                      {isBalanceVisible ? `₹${(bankAccounts[0]?.balance || 0).toLocaleString()}` : '••••••••'}
                    </h2>
                    <p className="text-xs text-slate-400 font-mono mt-1">{bankAccounts[0]?.accountNumber || "**** 2839"}</p>
                  </div>

                  <div className="flex items-center justify-between text-xs text-slate-400 font-semibold relative z-10 border-t border-slate-800 pt-3">
                    <span>{userProfile?.name?.toUpperCase()}</span>
                    <span>DEBIT</span>
                  </div>
                </div>
              </div>

              {/* Quick actions and QR Pay buttons */}
              <div className="bg-white border border-slate-200/80 rounded-3xl p-6 glow-shadow">
                <span className="text-xs font-extrabold text-slate-400 tracking-wider">QUICK ACTIONS</span>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-4">
                  <button 
                    onClick={() => setShowScanner(true)}
                    className="p-4 bg-slate-50 hover:bg-violet-50 hover:border-violet-200 border border-slate-100 rounded-2xl transition-all flex flex-col items-center justify-center group"
                  >
                    <QrCode className="w-6 h-6 text-slate-600 group-hover:text-violet-600 mb-2" />
                    <span className="text-xs font-bold text-slate-700 group-hover:text-violet-800">Scan & Pay</span>
                  </button>
                  <button 
                    onClick={() => setShowLoadMoney(true)}
                    className="p-4 bg-slate-50 hover:bg-violet-50 hover:border-violet-200 border border-slate-100 rounded-2xl transition-all flex flex-col items-center justify-center group"
                  >
                    <Plus className="w-6 h-6 text-slate-600 group-hover:text-violet-600 mb-2" />
                    <span className="text-xs font-bold text-slate-700 group-hover:text-violet-800">Load Money</span>
                  </button>
                  <button 
                    onClick={() => setShowSendMoney(true)}
                    className="p-4 bg-slate-50 hover:bg-violet-50 hover:border-violet-200 border border-slate-100 rounded-2xl transition-all flex flex-col items-center justify-center group"
                  >
                    <Send className="w-6 h-6 text-slate-600 group-hover:text-violet-600 mb-2" />
                    <span className="text-xs font-bold text-slate-700 group-hover:text-violet-800">Transfer Money</span>
                  </button>
                  <button 
                    onClick={() => setActiveTab('SAVINGS')}
                    className="p-4 bg-slate-50 hover:bg-violet-50 hover:border-violet-200 border border-slate-100 rounded-2xl transition-all flex flex-col items-center justify-center group"
                  >
                    <FolderHeart className="w-6 h-6 text-slate-600 group-hover:text-violet-600 mb-2" />
                    <span className="text-xs font-bold text-slate-700 group-hover:text-violet-800">Savings</span>
                  </button>
                </div>
              </div>

              {/* Ledger History List */}
              <div className="bg-white border border-slate-200/80 rounded-3xl p-6 glow-shadow">
                <div className="border-b border-slate-100 pb-4 mb-4">
                  <h3 className="font-bold text-lg text-slate-800 font-extrabold">Ledger Records</h3>
                  <p className="text-xs text-slate-400">Click any transaction card to open its paper-style digital receipt dialogue!</p>
                </div>

                {transactions.length === 0 ? (
                  <div className="flex flex-col items-center justify-center text-center py-12">
                    <Sliders className="w-12 h-10 text-slate-300 mb-2" />
                    <p className="text-sm font-bold text-slate-500">No transactions logged yet</p>
                    <p className="text-xs text-slate-400">Add deposits above or spend money to initialize your ledger!</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {transactions.map(tx => (
                      <div 
                        key={tx.id} 
                        onClick={() => setSelectedTxForReceipt(tx)}
                        className="p-4 bg-slate-50 hover:bg-slate-100/50 border border-slate-100 rounded-2xl flex items-center justify-between transition-all cursor-pointer active:scale-98"
                      >
                        <div className="flex items-center space-x-4">
                          <div className={`p-3 rounded-2xl ${
                            tx.type === 'INCOME' ? 'bg-emerald-500/10 text-emerald-500' : 'bg-rose-500/10 text-rose-500'
                          }`}>
                            {tx.type === 'INCOME' ? <ArrowDownLeft className="w-5 h-5" /> : <ArrowUpRight className="w-5 h-5" />}
                          </div>
                          <div>
                            <h4 className="font-bold text-sm text-slate-700 leading-snug">{tx.title}</h4>
                            <p className="text-[10px] text-slate-400 font-bold mt-0.5">{formatDate(tx.date)} | {tx.category}</p>
                          </div>
                        </div>
                        <span className={`font-extrabold text-sm ${
                          tx.type === 'INCOME' ? 'text-emerald-500' : 'text-rose-500'
                        }`}>
                          {tx.type === 'INCOME' ? '+' : '-'} ₹{tx.amount.toLocaleString()}
                        </span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* SCREEN 2: SUBSCRIPTIONS */}
          {activeTab === 'SUBS' && (
            <div className="bg-white border border-slate-200/80 rounded-3xl p-6 glow-shadow space-y-6">
              <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                <div>
                  <h3 className="font-bold text-lg text-slate-800">My Subscriptions Matrix</h3>
                  <p className="text-xs text-slate-400">Billing details segmented into OTT, Wi-Fi, Mobile, and Other.</p>
                </div>
                <button 
                  onClick={() => setShowAddSub(true)}
                  className="bg-violet-600 hover:bg-violet-500 text-white font-bold text-xs px-4 py-2.5 rounded-xl flex items-center space-x-1.5 transition-all shadow-lg shadow-violet-600/15"
                >
                  <Plus className="w-4 h-4" />
                  <span>Add Subscription</span>
                </button>
              </div>

              {subscriptions.length === 0 ? (
                <div className="flex flex-col items-center justify-center text-center p-12">
                  <Tv className="w-12 h-12 text-slate-300 mb-2 animate-bounce" />
                  <p className="text-sm font-bold text-slate-500">No active subscriptions cards</p>
                  <p className="text-xs text-slate-400">Click Add Subscription to map OTT or Wifi bills securely!</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {subscriptions.map(sub => (
                    <div key={sub.id} className="p-5 bg-slate-50 border border-slate-100 rounded-3xl flex items-center justify-between hover:bg-slate-100/30 transition-all">
                      <div className="flex items-center space-x-4">
                        <div className="p-3 bg-violet-100 text-violet-700 rounded-2xl shadow-inner">
                          {sub.category === 'OTT' && <Tv className="w-5 h-5" />}
                          {sub.category === 'WIFI' && <Wifi className="w-5 h-5" />}
                          {sub.category === 'MOBILE' && <Phone className="w-5 h-5" />}
                          {sub.category === 'Other' && <Sliders className="w-5 h-5" />}
                        </div>
                        <div>
                          <h4 className="font-bold text-sm text-slate-800 leading-snug">{sub.name}</h4>
                          <span className="text-[10px] font-bold text-slate-400 bg-slate-200/50 px-2 py-0.5 rounded-full inline-block mt-1">{sub.category} | {sub.billingCycle}</span>
                        </div>
                      </div>
                      <span className="font-extrabold text-sm text-rose-500 bg-rose-50 px-3 py-1.5 rounded-2xl">₹{sub.amount.toLocaleString()}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* SCREEN 3: SAVINGS GOALS */}
          {activeTab === 'SAVINGS' && (
            <div className="bg-white border border-slate-200/80 rounded-3xl p-6 glow-shadow space-y-6">
              <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                <div>
                  <h3 className="font-bold text-lg text-slate-800 font-extrabold">Active Savings Milestones</h3>
                  <p className="text-xs text-slate-400">Target thresholds and active contribution progress gauges.</p>
                </div>
                <button 
                  onClick={() => setShowAddGoal(true)}
                  className="bg-violet-600 hover:bg-violet-500 text-white font-bold text-xs px-4 py-2.5 rounded-xl flex items-center space-x-1.5 transition-all shadow-lg shadow-violet-600/15"
                >
                  <Plus className="w-4 h-4" />
                  <span>New Milestone Target</span>
                </button>
              </div>

              {savingsGoals.length === 0 ? (
                <div className="flex flex-col items-center justify-center text-center p-12">
                  <FolderHeart className="w-12 h-12 text-slate-300 mb-2 animate-pulse" />
                  <p className="text-sm font-bold text-slate-500">No active savings milestones yet</p>
                  <p className="text-xs text-slate-400">Create targets above to start progress meters!</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {savingsGoals.map(goal => {
                    const percent = Math.min(100, Math.floor((goal.currentAmount / goal.targetAmount) * 100)) || 0;
                    return (
                      <div key={goal.id} className="p-5 bg-slate-50 border border-slate-100 rounded-3xl flex flex-col justify-between hover:bg-slate-100/30 transition-all">
                        <div className="flex items-center justify-between mb-3">
                          <h4 className="font-extrabold text-sm text-slate-700">{goal.title}</h4>
                          <span className="text-xs font-bold text-violet-700 bg-violet-100 px-2.5 py-0.5 rounded-full">{percent}% complete</span>
                        </div>
                        
                        <div className="w-full bg-slate-200 h-3 rounded-full overflow-hidden mb-3">
                          <div className="bg-violet-600 h-full rounded-full transition-all duration-300" style={{ width: `${percent}%` }}></div>
                        </div>

                        <div className="flex items-center justify-between text-xs text-slate-400 font-bold border-t border-slate-200/50 pt-3 mt-1">
                          <div className="flex flex-col text-left">
                            <span className="text-slate-500 font-extrabold">₹{goal.currentAmount.toLocaleString()} saved</span>
                            <span className="text-[10px] text-slate-400 mt-0.5">Target: ₹{goal.targetAmount.toLocaleString()}</span>
                          </div>
                          <button
                            onClick={() => setShowAddProgressForGoal(goal)}
                            className="bg-violet-100 hover:bg-violet-200 text-violet-700 text-[10px] font-extrabold px-3 py-1.5 rounded-xl transition-all"
                          >
                            + Add Progress
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* SCREEN 4: AI COACH CHAT & SETTINGS */}
          {activeTab === 'CHAT' && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
              
              {/* Left Column: API Settings Configuration Panel */}
              <div className="bg-white border border-slate-200/80 rounded-3xl p-6 glow-shadow space-y-4 lg:col-span-1">
                <div>
                  <h3 className="font-bold text-base text-slate-800">Custom AI Settings</h3>
                  <p className="text-xs text-slate-400">Verify and load custom Gemini API keys or server proxies keylessly!</p>
                </div>

                <div className="space-y-4">
                  <div className="space-y-1">
                    <label className="text-[9px] font-extrabold text-slate-400 tracking-wider">GEMINI API KEY OR SERVER PROXY URL</label>
                    <input 
                      type="password"
                      placeholder="Paste API Key or Custom Proxy Base URL"
                      className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2.5 text-xs focus:outline-none focus:ring-2 focus:ring-violet-500 font-semibold text-slate-700"
                      value={geminiInput}
                      onChange={e => setGeminiInput(e.target.value)}
                    />
                    <p className="text-[9px] text-slate-400 font-bold leading-normal mt-1">
                      💡 Auto-detects custom server proxy links starting with http(s) or standard API Keys instantly!
                    </p>
                  </div>

                  {apiSuccessMsg && (
                    <div className="flex items-center space-x-2 text-emerald-600 bg-emerald-50 px-3 py-2 rounded-xl text-xs font-bold border border-emerald-100 animate-fade-in">
                      <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
                      <span>{apiSuccessMsg}</span>
                    </div>
                  )}

                  <button 
                    onClick={handleSaveApiSettings}
                    className="w-full bg-violet-600 hover:bg-violet-500 text-white font-bold text-xs py-3 rounded-xl transition-all"
                  >
                    Verify & Save Settings
                  </button>
                </div>
              </div>

              {/* Right Column: AI Coach Chat Box Screen */}
              <div className="bg-white border border-slate-200/80 rounded-3xl p-6 glow-shadow flex flex-col justify-between h-[450px] lg:col-span-2">
                <div className="border-b border-slate-100 pb-3 mb-3 flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <div className="p-2 bg-violet-100 text-violet-700 rounded-xl">
                      <Sparkles className="w-4 h-4 animate-pulse" />
                    </div>
                    <div>
                      <h4 className="font-bold text-sm text-slate-800">Chat Cockpit Interface</h4>
                      <p className="text-[10px] text-slate-400 font-semibold">Simulated Gemini Cognitive engine active</p>
                    </div>
                  </div>
                </div>

                {/* Message display feed */}
                <div className="flex-1 overflow-y-auto space-y-4 p-2 max-h-[280px]">
                  {chatMessages.map((msg, idx) => (
                    <div key={idx} className={`flex ${msg.isUser ? 'justify-end' : 'justify-start'}`}>
                      <div className={`max-w-[80%] p-3.5 rounded-2xl text-xs font-semibold leading-normal ${
                        msg.isUser 
                          ? 'bg-violet-600 text-white rounded-br-none shadow-md shadow-violet-600/10' 
                          : 'bg-slate-100 text-slate-700 rounded-bl-none border border-slate-200/50'
                      }`}>
                        {msg.text}
                      </div>
                    </div>
                  ))}
                  {isAiTyping && (
                    <div className="flex justify-start">
                      <div className="bg-slate-100 text-slate-500 p-3 rounded-2xl rounded-bl-none text-xs font-bold animate-pulse border border-slate-200/50">
                        Coach is writing answers...
                      </div>
                    </div>
                  )}
                </div>

                {/* Send chat block */}
                <div className="border-t border-slate-100 pt-3">
                  <div className="flex items-center space-x-2 bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 focus-within:ring-2 focus-ring-violet-500 focus-within:bg-white transition-all">
                    <input 
                      type="text" 
                      placeholder="Ask AI Coach (e.g. 'create saving for bike of 10000')" 
                      className="flex-1 bg-transparent text-xs text-slate-700 placeholder:text-slate-400 focus:outline-none font-bold"
                      value={chatInput}
                      onChange={e => setChatInput(e.target.value)}
                      onKeyDown={e => {
                        if (e.key === 'Enter') {
                          e.preventDefault();
                          handleSendMessage();
                        }
                      }}
                    />
                    <button onClick={handleSendMessage} className="text-violet-600 hover:text-violet-500">
                      <Send className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>

            </div>
          )}

        </div>
      </div>

      {/* 🚀 MODAL DIALOG OVERLAYS (PURE KOTLIN APP REPLICAS!) */}

      {/* 1. Add / Load Money Modal */}
      {showLoadMoney && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white rounded-3xl p-6 glow-shadow border border-slate-100">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
              <h3 className="font-bold text-base text-slate-800">Load Money - {bankAccounts[0]?.bankName}</h3>
              <button onClick={() => setShowLoadMoney(false)} className="p-1 hover:bg-slate-100 rounded-lg text-slate-400"><X className="w-4 h-4" /></button>
            </div>
            
            <div className="space-y-4">
              <p className="text-xs text-slate-400">Deposit simulated funds directly into your active bank card:</p>
              
              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">AMOUNT TO LOAD (₹)</label>
                <input 
                  type="number"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-bold"
                  value={loadAmount}
                  onChange={e => setLoadAmount(e.target.value)}
                />
              </div>

              {loadError && (
                <p className="text-xs text-rose-500 font-semibold">{loadError}</p>
              )}

              <div className="flex space-x-3 pt-2">
                <button 
                  onClick={() => setShowLoadMoney(false)}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 py-3 rounded-xl font-bold text-xs text-slate-600 transition-all"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleLoadMoneyConfirm}
                  className="flex-1 bg-violet-600 hover:bg-violet-500 py-3 rounded-xl font-bold text-xs text-white shadow-lg shadow-violet-600/10 transition-all"
                >
                  Confirm & Deposit
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Manual Add Savings Progress Milestone Modal */}
      {showAddProgressForGoal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-fade-in">
          <div className="w-full max-w-sm bg-white rounded-3xl p-6 border border-slate-150 shadow-2xl relative overflow-hidden">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
              <h3 className="font-extrabold text-sm text-slate-800">Add Progress to '{showAddProgressForGoal.title}'</h3>
              <button onClick={() => { setShowAddProgressForGoal(null); setAddProgressError(''); }} className="p-1 hover:bg-slate-100 rounded-lg text-slate-400"><X className="w-4 h-4" /></button>
            </div>
            
            <div className="space-y-4">
              <p className="text-xs text-slate-400 font-bold leading-normal">
                Allocate simulated cash directly from your linked {bankAccounts[0]?.bankName || 'Active Card'} (Balance: ₹{bankAccounts[0]?.balance?.toLocaleString() || '0.00'}) to this milestone!
              </p>

              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">CONTRIBUTION AMOUNT (₹)</label>
                <input 
                  type="number"
                  placeholder="250"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-extrabold"
                  value={addProgressAmount}
                  onChange={e => setAddProgressAmount(e.target.value)}
                />
              </div>

              {addProgressError && (
                <p className="text-xs text-rose-500 font-semibold">{addProgressError}</p>
              )}

              <div className="flex space-x-3 pt-2">
                <button 
                  onClick={() => { setShowAddProgressForGoal(null); setAddProgressError(''); }}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 py-3 rounded-xl font-bold text-xs text-slate-600 transition-all"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleManualAddProgress}
                  className="flex-1 bg-violet-600 hover:bg-violet-500 py-3 rounded-xl font-bold text-xs text-white shadow-lg transition-all"
                >
                  Confirm Contribution
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 2. Send Money / Local Bank Transfer Modal */}
      {showSendMoney && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white rounded-3xl p-6 glow-shadow border border-slate-100">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
              <h3 className="font-bold text-base text-slate-800">Local Bank Transfer</h3>
              <button onClick={() => setShowSendMoney(false)} className="p-1 hover:bg-slate-100 rounded-lg text-slate-400"><X className="w-4 h-4" /></button>
            </div>

            <div className="space-y-4">
              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">RECIPIENT NAME / NAME</label>
                <input 
                  type="text"
                  placeholder="Enter recipient's full name"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-semibold"
                  value={sendRecipient}
                  onChange={e => setSendMoneyRecipient(e.target.value)}
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">AMOUNT (₹)</label>
                <input 
                  type="number"
                  placeholder="₹0.00"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-bold"
                  value={sendAmount}
                  onChange={e => setSendMoneyAmount(e.target.value)}
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">4-DIGIT SECURITY PIN</label>
                <input 
                  type="password"
                  maxLength={4}
                  placeholder="••••"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm text-center focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-bold"
                  value={sendPin}
                  onChange={e => setSendMoneyPin(e.target.value.replace(/\D/g, ''))}
                />
              </div>

              {sendError && (
                <p className="text-xs text-rose-500 font-semibold">{sendError}</p>
              )}

              <div className="flex space-x-3 pt-2">
                <button 
                  onClick={() => setShowSendMoney(false)}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 py-3 rounded-xl font-bold text-xs text-slate-600 transition-all"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleSendMoneyConfirm}
                  className="flex-1 bg-violet-600 hover:bg-violet-500 py-3 rounded-xl font-bold text-xs text-white shadow-lg transition-all"
                >
                  Verify & Transfer
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 3. Subscriptions Add Modal */}
      {showAddSub && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white rounded-3xl p-6 glow-shadow border border-slate-100">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
              <h3 className="font-bold text-base text-slate-800">Add Subscription Card</h3>
              <button onClick={() => setShowAddSub(false)} className="p-1 hover:bg-slate-100 rounded-lg text-slate-400"><X className="w-4 h-4" /></button>
            </div>

            <div className="space-y-4">
              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">SERVICE / PLATFORM NAME</label>
                <input 
                  type="text"
                  placeholder="e.g. Netflix, Wi-Fi Bill, Phone"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-semibold"
                  value={subName}
                  onChange={e => setSubName(e.target.value)}
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">BILLING AMOUNT (₹)</label>
                <input 
                  type="number"
                  placeholder="₹0.00"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-bold"
                  value={subAmount}
                  onChange={e => setSubAmount(e.target.value)}
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">CATEGORY</label>
                <div className="grid grid-cols-4 gap-2">
                  {[
                    { key: 'OTT', label: '🍿 OTT' },
                    { key: 'WIFI', label: '🌐 Wi-Fi' },
                    { key: 'MOBILE', label: '📱 Mobile' },
                    { key: 'Other', label: '⚙️ Other' }
                  ].map(cat => (
                    <button 
                      key={cat.key}
                      onClick={() => setSubCategory(cat.key as any)}
                      className={`py-2 rounded-xl text-xs font-bold border transition-all ${
                        subCategory === cat.key 
                          ? 'bg-violet-50 border-violet-500 text-violet-700' 
                          : 'bg-slate-50 border-slate-100 text-slate-500 hover:bg-slate-100'
                      }`}
                    >
                      {cat.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">BILLING CYCLE</label>
                <div className="flex space-x-4">
                  <label className="flex items-center space-x-2 text-xs font-bold text-slate-600">
                    <input type="radio" checked={subCycle === 'Monthly'} onChange={() => setSubCycle('Monthly')} className="text-violet-600 focus:ring-violet-500" />
                    <span>Monthly</span>
                  </label>
                  <label className="flex items-center space-x-2 text-xs font-bold text-slate-600">
                    <input type="radio" checked={subCycle === 'Yearly'} onChange={() => setSubCycle('Yearly')} className="text-violet-600 focus:ring-violet-500" />
                    <span>Yearly</span>
                  </label>
                </div>
              </div>

              {subError && (
                <p className="text-xs text-rose-500 font-semibold">{subError}</p>
              )}

              <div className="flex space-x-3 pt-2">
                <button 
                  onClick={() => setShowAddSub(false)}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 py-3 rounded-xl font-bold text-xs text-slate-600 transition-all"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleAddSubscription}
                  className="flex-1 bg-violet-600 hover:bg-violet-500 py-3 rounded-xl font-bold text-xs text-white shadow-lg transition-all"
                >
                  Create Card
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 4. Savings Goals Add Modal */}
      {showAddGoal && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white rounded-3xl p-6 glow-shadow border border-slate-100">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
              <h3 className="font-bold text-base text-slate-800">Add Savings Milestone</h3>
              <button onClick={() => setShowAddGoal(false)} className="p-1 hover:bg-slate-100 rounded-lg text-slate-400"><X className="w-4 h-4" /></button>
            </div>

            <div className="space-y-4">
              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">GOAL / TARGET TITLE</label>
                <input 
                  type="text"
                  placeholder="e.g. Travel, New Bike, Education"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-semibold"
                  value={goalTitle}
                  onChange={e => setGoalTitle(e.target.value)}
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">TARGET LIMIT (₹)</label>
                <input 
                  type="number"
                  placeholder="₹0.00"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-bold"
                  value={goalTarget}
                  onChange={e => setGoalTarget(e.target.value)}
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">STARTING SAVINGS PROGRESS (₹)</label>
                <input 
                  type="number"
                  placeholder="₹0.00 (Optional, deducted from bank)"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-bold"
                  value={goalStarting}
                  onChange={e => setGoalStarting(e.target.value)}
                />
              </div>

              {goalError && (
                <p className="text-xs text-rose-500 font-semibold">{goalError}</p>
              )}

              <div className="flex space-x-3 pt-2">
                <button 
                  onClick={() => setShowAddGoal(false)}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 py-3 rounded-xl font-bold text-xs text-slate-600 transition-all"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleAddSavingsGoal}
                  className="flex-1 bg-violet-600 hover:bg-violet-500 py-3 rounded-xl font-bold text-xs text-white shadow-lg transition-all"
                >
                  Start Target
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 5. GPay-Style QR Scanner Simulator Modal */}
      {showScanner && (
        <div className="fixed inset-0 bg-black z-50 flex flex-col justify-between p-6">
          <div className="flex items-center justify-between text-white">
            <button onClick={() => setShowScanner(false)} className="p-2.5 bg-white/10 hover:bg-white/20 rounded-full transition-all">
              <X className="w-6 h-6" />
            </button>
            <h3 className="text-lg font-bold">Scan & Pay (GPay)</h3>
            <div className="w-10"></div> 
          </div>

          <div className="flex-1 flex flex-col items-center justify-center text-center">
            <div className="w-64 h-64 border-4 border-violet-400 rounded-[32px] bg-transparent relative flex items-center justify-center overflow-hidden mb-8 shadow-2xl">
              <div className="w-full h-1 bg-violet-400 absolute top-0 left-0 animate-[bounce_2s_infinite] shadow-lg shadow-violet-500/50"></div>
              
              <div className="p-4 bg-black/40 backdrop-blur-sm rounded-2xl border border-white/10 mx-4 max-w-[85%]">
                <QrCode className="w-12 h-12 text-violet-300 mx-auto animate-pulse" />
                <p className="text-[10px] text-white/80 font-bold mt-2">Point scanner at any GPay/PhonePe UPI code</p>
              </div>
            </div>

            <div className="w-full max-w-sm bg-zinc-900/60 backdrop-blur-xl border border-zinc-800 rounded-3xl p-5 text-left">
              <h4 className="text-xs font-extrabold text-violet-400 tracking-wider mb-3">SIMULATE QR IMAGE INPUT</h4>
              
              <div className="space-y-3">
                <button 
                  onClick={() => handleSimulateQrScan("upi://pay?pa=starbucks@okaxis&pn=Starbucks%20Coffee")}
                  className="w-full text-left bg-zinc-950 hover:bg-zinc-800 border border-zinc-800/80 p-3 rounded-2xl flex items-center space-x-3 transition-colors text-xs font-bold text-slate-200"
                >
                  <Image className="w-4 h-4 text-violet-400" />
                  <span>Scan Starbucks Coffee UPI QR (₹)</span>
                </button>
                <button 
                  onClick={() => handleSimulateQrScan("upi://pay?pa=cafedelhi@okicici&pn=Cafe%20Delhi")}
                  className="w-full text-left bg-zinc-950 hover:bg-zinc-800 border border-zinc-800/80 p-3 rounded-2xl flex items-center space-x-3 transition-colors text-xs font-bold text-slate-200"
                >
                  <Image className="w-4 h-4 text-violet-400" />
                  <span>Scan Cafe Delhi Bistro UPI QR (₹)</span>
                </button>

                <div className="h-px bg-zinc-800 my-1"></div>

                <div className="space-y-1">
                  <label className="text-[9px] font-extrabold text-zinc-500 tracking-wider">OR ENTER CUSTOM UPI STRING</label>
                  <input 
                    type="text"
                    placeholder="upi://pay?pa=recipient@upi&pn=Name"
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-xl px-3 py-2.5 text-xs text-slate-300 placeholder:text-zinc-700 focus:outline-none"
                    onKeyDown={e => e.key === 'Enter' && handleSimulateQrScan(e.currentTarget.value)}
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="flex items-center justify-center pb-8 space-x-12">
            <button className="flex flex-col items-center group">
              <div className="w-16 h-16 bg-white/10 hover:bg-white/20 rounded-full flex items-center justify-center text-white transition-all">
                <Volume2 className="w-6 h-6" />
              </div>
              <span className="text-[10px] text-white/80 font-bold mt-2">Toggle audio</span>
            </button>
            <button className="flex flex-col items-center group">
              <div className="w-16 h-16 bg-white/10 hover:bg-white/20 rounded-full flex items-center justify-center text-white transition-all">
                <Zap className="w-6 h-6" />
              </div>
              <span className="text-[10px] text-white/80 font-bold mt-2">Torch light</span>
            </button>
          </div>
        </div>
      )}

      {/* 6. UPI QR Pay Dialog Overlay */}
      {showQrPay && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white rounded-3xl p-6 glow-shadow border border-slate-100">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
              <h3 className="font-bold text-base text-slate-800">BHIM UPI QR Payment</h3>
              <button onClick={() => setShowQrPay(false)} className="p-1 hover:bg-slate-100 rounded-lg text-slate-400"><X className="w-4 h-4" /></button>
            </div>

            <div className="space-y-4">
              <div className="p-4 bg-slate-50 border border-slate-100 rounded-2xl">
                <span className="text-[10px] font-extrabold text-slate-400 tracking-wide">PAYEE MERCHANT</span>
                <h4 className="font-bold text-base text-slate-800 mt-0.5">{scannedPayeeName}</h4>
                <p className="text-xs text-slate-400 mt-0.5 font-mono">{scannedUpiId}</p>
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">PAYMENT AMOUNT (₹)</label>
                <input 
                  type="number"
                  placeholder="₹0.00"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-bold"
                  value={qrAmount}
                  onChange={e => setQrAmount(e.target.value)}
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-extrabold text-slate-400 tracking-wide">4-DIGIT SECURITY PIN</label>
                <input 
                  type="password"
                  maxLength={4}
                  placeholder="••••"
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm text-center focus:outline-none focus:ring-2 focus:ring-violet-500 text-slate-700 font-bold"
                  value={qrPin}
                  onChange={e => setQrPin(e.target.value.replace(/\D/g, ''))}
                />
              </div>

              {qrError && (
                <p className="text-xs text-rose-500 font-semibold">{qrError}</p>
              )}

              <div className="flex space-x-3 pt-2">
                <button 
                  onClick={() => setShowQrPay(false)}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 py-3 rounded-xl font-bold text-xs text-slate-600 transition-all"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleQrPayConfirm}
                  className="flex-1 bg-violet-600 hover:bg-violet-500 py-3 rounded-xl font-bold text-xs text-white shadow-lg transition-all"
                >
                  Confirm & Transfer (₹)
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 7. AI Copilot Authorization Popup */}
      {pendingAiAction && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white rounded-3xl p-6 glow-shadow border border-violet-100 border-2">
            <div className="flex items-center space-x-3 text-violet-700 border-b border-violet-100 pb-3 mb-4">
              <ShieldCheck className="w-6 h-6 flex-shrink-0" />
              <h3 className="font-extrabold text-base tracking-tight">AI Copilot Authorization</h3>
            </div>

            <div className="space-y-4">
              <p className="text-xs text-slate-500 leading-normal">
                Your AI Companion has drafted a secure database write request for you. Please authorize to write it securely to your Firebase Firestore database:
              </p>

              <div className="p-4 bg-violet-50/50 border border-violet-100 rounded-2xl">
                {pendingAiAction.type === 'SAVINGS' ? (
                  <div>
                    <h4 className="font-bold text-sm text-violet-800">🎯 Action: Create Savings Goal</h4>
                    <div className="mt-2 text-xs text-slate-600 font-bold space-y-1">
                      <p>Goal Title: {pendingAiAction.title}</p>
                      <p>Target Amount: ₹{pendingAiAction.amount.toLocaleString()}</p>
                    </div>
                  </div>
                ) : (
                  <div>
                    <h4 className="font-bold text-sm text-violet-800">💸 Action: Add Transaction</h4>
                    <div className="mt-2 text-xs text-slate-600 font-bold space-y-1">
                      <p>Title: {pendingAiAction.title}</p>
                      <p>Amount: ₹{pendingAiAction.amount.toLocaleString()}</p>
                      <p>Type: EXPENSE</p>
                      <p>Category: {pendingAiAction.subCategory}</p>
                    </div>
                  </div>
                )}
              </div>

              <div className="flex space-x-3 pt-2">
                <button 
                  onClick={() => setPendingAiAction(null)}
                  className="flex-1 bg-slate-100 hover:bg-slate-200 py-3 rounded-xl font-bold text-xs text-slate-600 transition-all"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleAuthorizePendingAction}
                  className="flex-1 bg-violet-600 hover:bg-violet-500 py-3 rounded-xl font-bold text-xs text-white shadow-lg transition-all"
                >
                  Authorize & Write
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 8. Pixel-Perfect Transaction Receipt Dialogue Overlay (Mobile App Clone!) */}
      {selectedTxForReceipt && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-sm bg-white rounded-3xl p-6 border border-slate-150 shadow-2xl relative overflow-hidden text-center animate-scale-up">
            
            {/* Success seal */}
            <div className="w-16 h-16 bg-emerald-500/10 border border-emerald-500/20 rounded-full flex items-center justify-center mx-auto mb-4">
              <CheckCircle2 className="w-8 h-8 text-emerald-500" />
            </div>

            <h3 className="text-xl font-extrabold text-slate-800">Payment Successful!</h3>
            <span className="text-xs text-slate-400 font-bold">BHIM UPI transaction completed</span>

            <div className="my-6 p-4 bg-slate-50 border border-slate-100 rounded-2xl text-left space-y-3">
              <div className="flex justify-between text-xs font-bold text-slate-500">
                <span>Transaction ID</span>
                <span className="font-mono text-slate-700">{selectedTxForReceipt.id}</span>
              </div>
              <div className="flex justify-between text-xs font-bold text-slate-500">
                <span>Description</span>
                <span className="text-slate-700">{selectedTxForReceipt.title}</span>
              </div>
              <div className="flex justify-between text-xs font-bold text-slate-500">
                <span>Category</span>
                <span className="text-slate-700">{selectedTxForReceipt.category}</span>
              </div>
              <div className="flex justify-between text-xs font-bold text-slate-500">
               <span>Date & Time</span>
               <span className="text-slate-700">{formatDate(selectedTxForReceipt.date)}</span>
              </div>
              <div className="h-px bg-slate-200 my-2"></div>
              <div className="flex justify-between items-center font-extrabold text-sm text-slate-800">
                <span>Amount Paid</span>
                <span className="text-lg text-slate-900 font-extrabold">₹{selectedTxForReceipt.amount.toLocaleString()}</span>
              </div>
            </div>

            <button 
              onClick={() => setSelectedTxForReceipt(null)}
              className="w-full bg-slate-900 hover:bg-slate-800 text-white font-bold py-3 rounded-xl text-xs transition-all"
            >
              Close Receipt
            </button>
          </div>
        </div>
      )}

    </div>
  );
}

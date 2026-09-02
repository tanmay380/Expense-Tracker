# Previous Transactions Import Feature

## Overview
The app now includes a splash screen that appears on first launch, offering users the ability to automatically detect and import previous transactions and accounts from their SMS messages.

## Features Implemented

### 1. **Splash Screen** (`SplashScreen.kt`)
- Beautiful, animated splash screen with app branding
- Shows on first app launch
- Three states:
  - **Initial**: Displays import option with detailed explanation
  - **Processing**: Shows loading animation while scanning SMS
  - **Completed**: Displays success message with transaction count

### 2. **Smart Import Logic** (`SplashViewModel.kt`)
- Automatically scans SMS messages in the background
- Detects bank transactions and accounts
- Displays progress to user
- No impact on app performance
- Non-blocking operation

### 3. **Preferences Management** (`PreferencesManager.kt`)
- Tracks whether user has seen the splash screen
- Remembers user's choice (import or skip)
- Prevents repeated prompts
- Can be reset if needed

## User Flow

1. **App Launch**
   - User opens app for the first time
   - Splash screen appears with smooth animation

2. **User Choice**
   - User can choose "Yes, Import Transactions" or "Skip for Now"
   - Either choice is saved in preferences

3. **Processing** (if user chooses import)
   - App scans SMS messages in background
   - Detects bank transactions from messages
   - Automatically creates accounts for detected banks
   - Progress shown with loading animation

4. **Completion**
   - Shows success screen with count of imported transactions
   - Automatically navigates to home screen after 2 seconds
   - User can immediately view imported data

## Technical Details

### SMS Detection & Parsing
The feature leverages existing SMS parsing infrastructure:
- **SmsReader**: Scans all SMS messages
- **SmsParser**: Extracts transaction details from messages
- **BankHandlers**: Identifies bank messages
- **ChainOfCommandParser**: Parses complex message formats

### Data Model
Imports create:
- **Transactions**: Bank transactions extracted from SMS
- **Accounts**: Bank accounts detected from messages
- **Categories**: Automatically classified (Groceries, Travel, etc.)

### Permissions
Required permissions (already in AndroidManifest.xml):
- `READ_SMS` - Read SMS messages
- `RECEIVE_SMS` - Receive new SMS
- `POST_NOTIFICATIONS` - Show notifications

## Files Modified/Created

### New Files:
- `ui/screens/SplashScreen.kt` - UI for splash screen
- `ui/SplashViewModel.kt` - Business logic for import
- `util/PreferencesManager.kt` - Preference storage

### Modified Files:
- `ui/navigation/NavGraph.kt` - Added Splash route and screen
- `ui/UiUtils.kt` - Added Text color to AppColors

## Key Features:

✅ **Privacy First**: Only bank messages are processed  
✅ **No Manual Entry**: Automatic detection and classification  
✅ **Smart Duplicate Prevention**: Prevents duplicate imports  
✅ **Accounts Auto-Creation**: Creates bank accounts automatically  
✅ **Category Auto-Tagging**: Classifies transactions by category  
✅ **One-Time Prompt**: Respects user's choice  
✅ **User-Friendly**: Clear explanations and progress feedback  

## Settings & Preferences

### Reset Import Prompt
If user wants to re-see the import prompt:
```kotlin
// In PreferencesManager
fun resetImportPreference() {
    sharedPreferences.edit().remove(KEY_IMPORT_SPLASH_SEEN).apply()
}
```

### Preferences Storage
Data stored in SharedPreferences:
- Key: `expense_tracker_prefs`
- `import_splash_seen`: Boolean flag for first-launch prompt
- `first_launch`: Tracks if this is first app launch

## Testing Checklist

- [ ] App launches with splash screen on first install
- [ ] Import option shows correct message
- [ ] Skip button works correctly
- [ ] Import button triggers SMS scanning
- [ ] Progress animation displays
- [ ] Success screen shows correct transaction count
- [ ] Auto-navigation to home after completion
- [ ] Splash doesn't show on subsequent launches
- [ ] Imported transactions appear in home screen
- [ ] Imported accounts appear in drawer

## Future Enhancements

- Allow manual re-import from settings
- Show detailed import summary
- Allow filtering by date range
- Manual approval of transactions before import
- Export import history

# Testing Guide - Previous Transactions Import Feature

## Pre-requisites

1. **SMS Messages**: The device/emulator should have some SMS messages from banks
2. **Permissions**: App will request READ_SMS and RECEIVE_SMS permissions
3. **First Launch**: Feature only shows on first app launch

## Testing Scenarios

### Scenario 1: Fresh Install (First Launch)

**Steps:**
1. Uninstall the app completely or clear app data
2. Install the app from build
3. Launch the app

**Expected Result:**
- Splash screen appears immediately
- Shows "Paisa Expense Tracker" with emoji icon
- Displays import option with explanation
- Two buttons visible: "Yes, Import Transactions" and "Skip for Now"

### Scenario 2: User Chooses to Import

**Steps:**
1. On splash screen, click "Yes, Import Transactions"
2. Grant SMS permissions if prompted
3. Wait for scanning to complete

**Expected Result:**
- Screen changes to "Scanning SMS Messages" with loading animation
- Progress message shows "Detecting bank transactions and accounts..."
- Loading spinner continues for 1-2 seconds
- Success screen appears showing count of imported transactions
- Screen automatically transitions to home screen after 2 seconds
- Home screen displays imported transactions in the list
- Navigation drawer shows imported accounts

### Scenario 3: User Chooses to Skip

**Steps:**
1. On splash screen, click "Skip for Now"

**Expected Result:**
- Screen briefly shows completion screen
- Immediately transitions to home screen
- No transactions imported
- Home screen is empty (or shows existing data if app was reinstalled)

### Scenario 4: Subsequent Launches

**Steps:**
1. Close and reopen the app
2. Kill and restart the app multiple times

**Expected Result:**
- Splash screen does NOT appear again
- App opens directly to home screen
- Navigation works normally

## Validation Checklist

### UI/UX Tests
- [ ] Splash screen appears on first launch only
- [ ] Animations are smooth (color transitions, loading spinner)
- [ ] Text is readable with good contrast
- [ ] Buttons are properly sized and clickable
- [ ] Layout works on different screen sizes

### Functional Tests
- [ ] Import button successfully triggers SMS scan
- [ ] Skip button navigates to home without import
- [ ] Progress animation shows during processing
- [ ] Success message shows correct transaction count
- [ ] Auto-navigation works after completion
- [ ] Home screen displays imported data correctly

### Data Tests
- [ ] Transactions appear in home screen after import
- [ ] Accounts appear in navigation drawer
- [ ] Categories are correctly assigned
- [ ] Transaction amounts are accurate
- [ ] No duplicate transactions are created
- [ ] Merchant names are correctly extracted
- [ ] Timestamps are preserved correctly

### Permission Tests
- [ ] READ_SMS permission is requested
- [ ] RECEIVE_SMS permission is requested
- [ ] App handles permission denial gracefully
- [ ] Permissions can be granted from settings and retry

### Edge Cases
- [ ] No SMS messages in device
- [ ] Only non-bank SMS messages exist
- [ ] Device has many SMS messages (1000+)
- [ ] Empty or malformed SMS messages
- [ ] Back button during import (should not crash)
- [ ] Screen rotation during splash screen

## Test Data Setup

### Adding Test SMS Messages
For emulator testing, use Android Studio's built-in SMS simulator:

1. Open Android Device Monitor
2. Go to Emulator Control tab
3. In "Telephony Actions" section:
   - Incoming Number: `1234567890`
   - Message: Copy a real bank SMS format

**Example Bank Messages:**
```
SBI: Dear Customer, Amount Rs.500/- has been debited from your a/c ...XXXX5432 on 01-AUG-24 at 14:30. UTR: 123456789

ICICI: Dear valued customer, You have spent Rs 1,200 at ZOMATO via your ICICI a/c ending XXXX1234 on 01-AUG-2024 14:35. Remaining balance: Rs 45,000

HDFC: Your card ending 6789 has been used for a transaction. Amount debited: Rs. 2000 on 01/08/2024 14:45:23 IST at AMAZON. Available Balance: Rs. 80,000
```

## Debugging

### Enable Logging
Add this to check logs:
```bash
adb logcat | grep "tanmay"  # SmsReader logs
adb logcat | grep "MainViewModel"  # ViewModel logs
adb logcat | grep "SplashViewModel"  # Splash logs
```

### Check Preferences
View stored preferences:
```bash
adb shell run-as com.example.expensetracker cat /data/data/com.example.expensetracker/shared_prefs/expense_tracker_prefs.xml
```

### Reset for Retesting
To see splash screen again:
1. Clear app data: Settings → Apps → Expense Tracker → Clear Data
2. Or use command: `adb shell pm clear com.example.expensetracker`

### Database Inspection
View imported data:
```bash
adb shell sqlite3 /data/data/com.example.expensetracker/databases/paisa_database
sqlite> SELECT COUNT(*) FROM transactions;
sqlite> SELECT COUNT(*) FROM accounts;
sqlite> SELECT * FROM accounts LIMIT 5;
```

## Performance Metrics

### Expected Performance
- **Splash screen load time**: < 100ms
- **SMS scanning for 100 messages**: < 3 seconds
- **Transaction processing**: < 1ms per message
- **Memory usage**: < 50MB during scanning
- **UI responsiveness**: No freezing during processing

## Troubleshooting

### Issue: Splash screen doesn't appear

**Solution:**
- Check if app was uninstalled and reinstalled
- Clear app data: `adb shell pm clear com.example.expensetracker`
- Check that PreferencesManager is properly injected

### Issue: SMS messages not being detected

**Solution:**
- Verify SMS messages are from recognized banks
- Check BankHandlers.kt for supported bank codes
- Check logs: `adb logcat | grep "tanmay"`
- Verify READ_SMS permission is granted

### Issue: App crashes during import

**Solution:**
- Check logcat for stack trace
- Verify database is writable
- Check for malformed SMS messages
- Ensure sufficient device storage

### Issue: Imported transactions not visible

**Solution:**
- Refresh home screen (pull down or restart)
- Check database directly with SQLite
- Verify transactions were inserted successfully
- Check transaction timestamps are correct

## Regression Testing

After implementing this feature, test:
- [ ] Normal transaction adding still works
- [ ] Home screen display is not affected
- [ ] Navigation drawer works correctly
- [ ] Transaction filtering by month works
- [ ] Search functionality works
- [ ] Transaction details screen works
- [ ] Settings/Accounts screens still accessible
- [ ] SMS receiver for real-time updates still works
- [ ] App doesn't crash on permission denial
- [ ] Memory usage is normal after import

## Performance Testing

Run under load:
```bash
# Monitor memory during import
adb shell dumpsys meminfo com.example.expensetracker

# Monitor CPU usage
adb shell top -p $(adb shell pidof com.example.expensetracker)
```

## Submission Checklist

- [ ] All tests passed
- [ ] No crashes or ANRs
- [ ] Permissions handled correctly
- [ ] UI animations are smooth
- [ ] Data is correctly imported
- [ ] Feature can be used again after reset
- [ ] Documentation is complete
- [ ] Code follows project conventions
- [ ] No unused imports or variables

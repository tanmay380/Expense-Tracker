# Quick Start Guide - Import Feature

## What's New?

Your Expense Tracker now has an intelligent splash screen that automatically imports previous bank transactions from your SMS messages on first launch.

## How to Use

### First Launch (Fresh Install)
1. Install the app
2. Open it
3. **Splash screen appears** with import option
4. Choose one:
   - **"Yes, Import Transactions"** → App scans SMS and imports data
   - **"Skip for Now"** → Go directly to home screen

### During Import
- See "Scanning SMS Messages..." message
- Loading animation shows progress
- Takes 1-2 seconds typically
- No need to do anything, just wait

### After Import
- Success screen shows how many transactions were imported
- App automatically goes to home screen after 2 seconds
- Your transactions are now visible

### Next Time You Open App
- Splash screen does NOT appear
- App opens directly to home screen
- All your data is preserved

## Verification

After import, check that:
✓ Transactions appear in the home screen  
✓ Accounts appear in the navigation drawer  
✓ Transaction amounts are correct  
✓ Merchant names are properly extracted  
✓ Categories are assigned correctly  

## If Something Goes Wrong

### Splash didn't appear?
- This only shows on first launch
- To see it again: Clear app data and reinstall

### Transactions not imported?
- Make sure you have SMS messages from banks
- Check that you granted SMS permissions
- Some bank formats might not be recognized yet

### Want to import again?
- Reset from settings (coming soon)
- Or clear app data and reinstall

## What Gets Imported?

✓ Bank transactions from SMS  
✓ Transaction amounts  
✓ Merchant names  
✓ Transaction dates and times  
✓ Bank account information  
✓ Automatic categorization  

✗ NOT imported: Personal SMS content, other non-bank messages

## Permissions Needed

The app will ask for:
- **Read SMS** - To scan your messages
- **Receive SMS** - For real-time updates
- **Notifications** - For alerts

All are required for the feature to work.

## Tips

1. **More Accurate** - Import covers several months of history
2. **No Duplicates** - Same transaction won't be imported twice
3. **Auto Categories** - Transactions are automatically categorized
4. **Account Created** - Bank accounts created automatically
5. **Manual Add** - You can still manually add transactions anytime

## Next Steps

1. Build and run the app
2. On first launch, see the splash screen
3. Click "Yes, Import Transactions"
4. Wait for import to complete
5. View your imported data in home screen
6. Continue using app normally

## For Developers

See these files for more details:
- `IMPORT_FEATURE.md` - Feature documentation
- `TESTING_GUIDE.md` - Testing instructions
- `IMPLEMENTATION_SUMMARY.md` - Technical details

---

**Status**: Ready to use! 🎉

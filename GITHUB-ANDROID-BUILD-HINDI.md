# भारत AI Scanner — GitHub से APK बनाने का तरीका

## जरूरी जानकारी
- यह Android **source project** है, APK नहीं।
- Workflow `.github/workflows/android-apk.yml` GitHub Actions में `assembleDebug` चलाने और बने APK को artifact के रूप में उपलब्ध कराने के लिए है।
- Build तभी सफल होगा जब source code/dependencies में कोई build error न हो। इस package को इस environment में Android SDK/Gradle से compile करके verify नहीं किया गया है।

## फोन से GitHub पर डालना
1. ZIP को फोन में Extract करें।
2. GitHub repository खोलें और `Add file` → `Upload files` चुनें (अगर मोबाइल ऐप में विकल्प न दिखे तो Chrome में github.com खोलकर Desktop site इस्तेमाल करें)।
3. ZIP को repository में अकेले अपलोड करने से build नहीं चलेगा; Extract किए हुए folder **के अंदर की सभी files और folders** repository root पर upload करनी हैं, जिसमें `.github` folder भी शामिल हो।
4. Commit changes करें।
5. Repository में `Actions` tab खोलें और `Build Bharat AI Scanner APK` workflow चुनें। जरूरत पड़े तो `Run workflow` दबाएँ।
6. Run सफल होने पर run page के `Artifacts` भाग से `Bharat-AI-Scanner-debug-apk` डाउनलोड करें और ZIP artifact के अंदर `app-debug.apk` मिलेगा।

## सुरक्षा
- Password, OTP, access token, signing key या निजी जानकारी commit/upload न करें।
- यह debug APK है, Play Store release के लिए signed release build नहीं।
- Google AdMob test IDs production earning नहीं करते।

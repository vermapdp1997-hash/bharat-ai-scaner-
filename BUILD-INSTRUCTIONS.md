# भारत AI Scanner — Android Studio Build Guide

यह पूरा Android source project है। इसमें Kotlin/Jetpack Compose source code, Android manifest, resources, Gradle build files और GitHub Actions workflow शामिल हैं। यह APK नहीं है।

## Android Studio में खोलें
1. ZIP को Extract करें।
2. Android Studio में **Open** चुनें।
3. Extract किए गए `BharatAIScanner-v1.4-ReleaseCandidate` फोल्डर को चुनें — वही फोल्डर जिसमें `settings.gradle.kts` है।
4. Android Studio को Gradle sync पूरा करने दें। इंटरनेट चालू रखें ताकि dependencies डाउनलोड हो सकें।
5. JDK 17 चुनें। इस प्रोजेक्ट में Android Gradle Plugin 8.7.3 है; इसके लिए Gradle 8.9 उपयुक्त है।
6. Sync सफल होने पर **Build > Build Bundle(s) / APK(s) > Build APK(s)** चुनें।
7. Debug APK आम तौर पर `app/build/outputs/apk/debug/app-debug.apk` में बनेगा।

## GitHub Actions से APK बनाना
1. GitHub repository में इस प्रोजेक्ट के अंदर की सभी फाइलें और folders अपलोड करें। `.github/workflows/android-apk.yml` भी शामिल करें।
2. GitHub में **Actions** खोलें।
3. **Build Bharat AI Scanner APK** workflow चुनकर **Run workflow** दबाएँ।
4. Workflow सफल होने पर run के **Artifacts** भाग से `Bharat-AI-Scanner-debug-apk` डाउनलोड करें।

## महत्वपूर्ण स्थिति
- इस ZIP की archive integrity और project files की static जाँच की गई है।
- इस वातावरण में Android SDK/Gradle build उपलब्ध नहीं था; इसलिए APK compile या फोन पर test होने की पुष्टि नहीं है।
- QA report में अधूरे/अनटेस्टेड फीचर्स दर्ज हैं। इसे अभी release-ready न मानें।
- AdMob में अभी Google test App ID है। सार्वजनिक रिलीज़ से पहले privacy, consent और विज्ञापन सेटअप जाँचें।

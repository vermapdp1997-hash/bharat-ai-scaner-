# Bharat AI Scanner — शुरुआती QA रिपोर्ट (हिंदी)

## इस संशोधित ZIP में किए गए बदलाव
- MainActivity में `ComponentActivity` और Compose `setContent` के imports जोड़े, जो build errors का कारण बन सकते थे।
- Tools स्क्रीन से निष्क्रिय Watermark बटन हटाया, ताकि उपयोगकर्ता को काम न करने वाला विकल्प न दिखे।
- GitHub Actions workflow मौजूद है और APK artifact अपलोड करने के लिए सेट है।

## क्या जाँच हुई
- ZIP archive integrity: PASS (`unzip -t`)
- Project structure, Gradle config, Manifest और workflow की static inspection: DONE
- Android Gradle build / APK install / real-device test: NOT RUN (इस वातावरण में Gradle/Android SDK उपलब्ध नहीं हैं और नेटवर्क से dependencies डाउनलोड नहीं की जा सकीं)।

## अभी भी टेस्ट/काम बाकी है
- APK को GitHub Actions में compile करके वास्तविक build errors की पुष्टि करना।
- कम-से-कम एक Android फोन पर scanner, PDF save/open/share/print, OCR Hindi/English, PIN और biometric flow जाँचना।
- Biometric बटन अभी केवल biometric prompt दिखाता है; यह अपने-आप पूरे ऐप को लॉक नहीं करता।
- Signature स्क्रीन पर हस्ताक्षर बनाया जा सकता है, लेकिन उसे PDF में जोड़कर export करने की सुविधा अभी जुड़ी नहीं है।
- PDF editor में page reorder/delete/rotate और PDF merge का code है; वास्तविक PDFs पर आउटपुट जाँचना बाकी है।
- Documents सूची URI permissions/लंबे समय तक फ़ाइल उपलब्ध रहने पर निर्भर है; स्थायी app-owned copies बनाना अधिक भरोसेमंद होगा।
- विज्ञापन अभी Google के test IDs से हैं; प्रकाशित ऐप से पहले AdMob setup, consent/privacy policy और नीति-अनुपालन जाँचना जरूरी है।
- OCR दो recognizers के परिणाम जोड़ता है; कुछ चित्रों में duplicate text आ सकता है।

## सलाह
इस ZIP को अभी **release-ready** न मानें। पहले GitHub Actions से build करें, फिर फोन पर परीक्षण करें और हर failure को ठीक करके दोबारा build करें।

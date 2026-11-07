# भारत AI Scanner v0.6

Ads-only Android document scanner project.

## v0.6 additions
- Document Manager search field
- Folder filter UI: General, Study, Jobs, Personal
- Share PDF action
- Print PDF action
- Local document metadata model for future persistent folders/rename/delete
- Hindi/English labels

## Existing foundation
- ML Kit Document Scanner integration
- Multi-page PDF/JPEG scanner flow
- Hindi + English OCR foundation
- PDF editor foundation
- Signature screen
- Biometric security entry point
- AdMob test banner integration

## Important
This is source code, not a signed APK. Replace AdMob test IDs with your own production IDs before release and complete Play Store requirements.


## v0.7 — Scanner + Document Manager
- DocumentManager-backed document records
- Search by document name/URI
- Rename and delete documents
- Folder filtering: General, Study, Jobs, Personal
- Share and Print actions
- ML Kit scanner remains configured for multi-page scanning, gallery import, JPEG/PDF output, and automatic document framing/crop flow
- BiometricPrompt activity compatibility corrected by using FragmentActivity

Note: This is a source project, not a compiled/signed APK. Test AdMob IDs are still used.

## v0.8 PDF Editor
- Open an existing PDF and edit page order.
- Rotate individual pages by 90 degrees.
- Delete pages.
- Move pages up/down.
- Export the edited PDF.
- Merge multiple PDFs into one PDF.
- Edited/merged PDFs are added to the Document Manager.
- Uses Android PdfRenderer/PdfDocument for local processing; no premium subscription.


## v0.9 — OCR + Scan Enhance
- Hindi + English OCR
- Copy OCR text
- Share OCR text
- Image enhance modes: Original, Grayscale, B&W
- Save enhanced scan as JPG
- Version 0.9.0


## v1.0 Release Polish
- First-run onboarding screen
- Local App Lock/PIN flow
- Biometric compatibility
- Document Manager, PDF Editor and OCR retained
- Ads-only model; Google test AdMob IDs remain until the developer supplies production IDs
- No Premium/Subscription

Note: This is a source project. A signed release APK has not been built in this environment.


## v1.3 build audit
- Fixed missing LazyColumn items import.
- Replaced invalid Compose-only XML theme reference with a platform XML theme.
- Version bumped to 1.3.0 / versionCode 8.
- APK was not compiled in this environment because Android SDK/Gradle are unavailable.


## v1.4 Release Candidate
- Version 1.4.0 / versionCode 9
- Java 17 compile/target configuration added for modern Android toolchains.
- Biometric permission declared explicitly.
- This package is source-only; no APK is included.

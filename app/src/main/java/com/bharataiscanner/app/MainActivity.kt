package com.bharataiscanner.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.SharedPreferences
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.FileOutputStream
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.fragment.app.FragmentActivity
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerActivityHolder()
        setContent { BharatScannerApp() }
    }
}


@Composable
private fun BharatScannerIntro() {
    val navy = androidx.compose.ui.graphics.Color(0xFF071A35)
    val blue = androidx.compose.ui.graphics.Color(0xFF1459C7)
    val saffron = androidx.compose.ui.graphics.Color(0xFFFF9933)
    val green = androidx.compose.ui.graphics.Color(0xFF138808)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(navy, blue, androidx.compose.ui.graphics.Color(0xFF08284A)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.10f), RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                // A simple scan-sheet mark that does not need extra image assets.
                Box(
                    modifier = Modifier
                        .size(width = 62.dp, height = 76.dp)
                        .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.fillMaxWidth(0.62f).height(5.dp).background(saffron, RoundedCornerShape(4.dp)))
                        Box(Modifier.fillMaxWidth(0.72f).height(4.dp).background(blue.copy(alpha = 0.75f), RoundedCornerShape(4.dp)))
                        Box(Modifier.fillMaxWidth(0.72f).height(4.dp).background(blue.copy(alpha = 0.75f), RoundedCornerShape(4.dp)))
                        Box(Modifier.fillMaxWidth(0.52f).height(4.dp).background(green, RoundedCornerShape(4.dp)))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "भारत AI Scanner",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "स्कैन करें • सेव करें • शेयर करें",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(saffron, RoundedCornerShape(50)))
                Box(Modifier.size(7.dp).background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(50)))
                Box(Modifier.size(7.dp).background(green, RoundedCornerShape(50)))
            }
        }
    }
}

@Composable
fun BharatScannerApp() {
    var showIntro by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1600)
        showIntro = false
    }
    val prefs = remember { currentActivity().getSharedPreferences("bharat_scanner_settings", android.content.Context.MODE_PRIVATE) }
    var onboardingDone by remember { mutableStateOf(prefs.getBoolean("onboarding_done", false)) }
    var pinEnabled by remember { mutableStateOf(prefs.getBoolean("pin_enabled", false)) }
    var unlocked by remember { mutableStateOf(!pinEnabled) }
    var selected by remember { mutableStateOf("Home") }
    var documents by remember { mutableStateOf(DocumentManager.load(currentActivity())) }
    var search by remember { mutableStateOf("") }
    var folder by remember { mutableStateOf("All") }
    var ocrText by remember { mutableStateOf("") }
    var ocrBusy by remember { mutableStateOf(false) }
    var securityLocked by remember { mutableStateOf(false) }

    val scanner = remember {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(50)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pdf = scanResult?.pdf?.uri
            if (pdf != null) {
                DocumentManager.add(currentActivity(), pdf, "स्कैन ${System.currentTimeMillis()}", "General")
                documents = DocumentManager.load(currentActivity())
                Toast.makeText(currentActivity(), "PDF सेव हो गया", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startScanner() {
        scanner.getStartScanIntent(currentActivity())
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Toast.makeText(currentActivity(), "Scanner शुरू नहीं हो सका", Toast.LENGTH_SHORT).show()
            }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        ocrBusy = true
        ocrText = ""
        val image = try { InputImage.fromFilePath(currentActivity(), uri) } catch (e: Exception) { null }
        if (image == null) {
            ocrBusy = false
            Toast.makeText(currentActivity(), "इमेज पढ़ी नहीं जा सकी", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        val latin = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val devanagari = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        var latinDone = false
        var devanagariDone = false
        var latinResult = ""
        var devResult = ""
        fun finishIfReady() {
            if (latinDone && devanagariDone) {
                val parts = listOf(latinResult, devResult).filter { it.isNotBlank() }.distinct()
                ocrText = if (parts.isEmpty()) "कोई टेक्स्ट नहीं मिला।" else parts.joinToString("\n\n")
                ocrBusy = false
                latin.close()
                devanagari.close()
            }
        }
        latin.process(image).addOnSuccessListener { latinResult = it.text; latinDone = true; finishIfReady() }
            .addOnFailureListener { latinDone = true; finishIfReady() }
        devanagari.process(image).addOnSuccessListener { devResult = it.text; devanagariDone = true; finishIfReady() }
            .addOnFailureListener { devanagariDone = true; finishIfReady() }
    }

    MaterialTheme {
        if (showIntro) {
            BharatScannerIntro()
            return@MaterialTheme
        }
        if (!onboardingDone) {
            OnboardingScreen(onDone = {
                prefs.edit().putBoolean("onboarding_done", true).apply()
                onboardingDone = true
            })
            return@MaterialTheme
        }
        if (!unlocked) {
            PinUnlockScreen(onUnlock = { unlocked = true })
            return@MaterialTheme
        }
        Scaffold(bottomBar = {
            NavigationBar {
                listOf("Home", "Documents", "Tools", "Settings").forEach { item ->
                    NavigationBarItem(selected = selected == item, onClick = { selected = item }, icon = {}, label = { Text(item) })
                }
            }
        }) { p ->
            when (selected) {
                "Documents" -> DocumentsScreen(documents)
                "Tools" -> ToolsScreen(onScan = { startScanner() }, onOcr = { imagePicker.launch("image/*") }, ocrBusy = ocrBusy, ocrText = ocrText)
                "Settings" -> SettingsScreen(
                    onBiometric = { showBiometric(currentActivity()) },
                    pinEnabled = pinEnabled,
                    onPinChange = { enabled ->
                        pinEnabled = enabled
                        prefs.edit().putBoolean("pin_enabled", enabled).apply()
                        unlocked = !enabled
                    }
                )
                else -> HomeScreen(onScan = { startScanner() }, documents = documents)
            }
        }
    }
}


@Composable
private fun OnboardingScreen(onDone: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text("🇮🇳 भारत AI Scanner", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text("कागज़ से डिजिटल तक, अब और भी आसान", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        Text("""• दस्तावेज़ स्कैन और PDF
• Hindi + English OCR
• PDF editing
• Search, folders और sharing
• Ads-only मॉडल — कोई Premium subscription नहीं""")
        Spacer(Modifier.height(28.dp))
        Button(onClick = onDone, Modifier.fillMaxWidth().height(54.dp)) { Text("शुरू करें") }
    }
}

@Composable
private fun PinUnlockScreen(onUnlock: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    val prefs = remember { currentActivity().getSharedPreferences("bharat_scanner_settings", android.content.Context.MODE_PRIVATE) }
    var error by remember { mutableStateOf(false) }
    val saved = prefs.getString("app_pin", "") ?: ""
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text("🔐 App Lock", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        if (saved.isBlank()) {
            Text("पहली बार 4 अंकों का PIN सेट करें")
            OutlinedTextField(value = pin, onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pin = it }, label = { Text("PIN") }, singleLine = true)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { if (pin.length == 4) { prefs.edit().putString("app_pin", pin).apply(); onUnlock() } }, enabled = pin.length == 4) { Text("PIN सेट करें") }
        } else {
            Text("दस्तावेज़ खोलने के लिए PIN डालें")
            OutlinedTextField(value = pin, onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pin = it }, label = { Text("PIN") }, singleLine = true)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { if (pin == saved) onUnlock() else { error = true; pin = "" } }, enabled = pin.length == 4) { Text("Unlock") }
            if (error) Text("PIN गलत है", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun HomeScreen(onScan: () -> Unit, documents: List<DocumentRecord>) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("भारत AI Scanner", style = MaterialTheme.typography.headlineMedium)
        Text("कागज़ से डिजिटल तक, अब और भी आसान")
        Spacer(Modifier.height(8.dp))
        AdsBanner()
        Spacer(Modifier.height(20.dp))
        Button(onClick = onScan, Modifier.fillMaxWidth().height(56.dp)) { Text("📷  दस्तावेज़ स्कैन करें") }
        Spacer(Modifier.height(20.dp))
        Text("हाल के दस्तावेज़", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (documents.isEmpty()) Text("अभी कोई दस्तावेज़ सेव नहीं है।")
        documents.take(5).forEach { doc ->
            OutlinedButton(onClick = { openUri(Uri.parse(doc.uri), "application/pdf") }, Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("📄 ${doc.name}")
            }
        }
    }
}

@Composable
private fun DocumentsScreen(initial: List<DocumentRecord>) {
    var records by remember { mutableStateOf(initial) }
    var query by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("All") }
    var editing by remember { mutableStateOf<DocumentRecord?>(null) }
    var renameText by remember { mutableStateOf("") }
    val folders = listOf("All", "General", "Study", "Jobs", "Personal")
    val filtered = records.filter { d ->
        (selectedFolder == "All" || d.folder == selectedFolder) &&
            (query.isBlank() || d.name.contains(query, true) || d.uri.contains(query, true))
    }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Documents / दस्तावेज़", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Search / खोजें") }, singleLine = true)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            folders.forEach { f -> FilterChip(selected = selectedFolder == f, onClick = { selectedFolder = f }, label = { Text(f) }) }
        }
        Spacer(Modifier.height(10.dp))
        if (filtered.isEmpty()) Text("कोई दस्तावेज़ नहीं मिला।")
        LazyColumn {
            items(filtered) { doc ->
                Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(doc.name, style = MaterialTheme.typography.titleMedium)
                        Text("📁 ${doc.folder}", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TextButton(onClick = { openUri(Uri.parse(doc.uri), "application/pdf") }) { Text("खोलें") }
                            TextButton(onClick = { shareUri(Uri.parse(doc.uri)) }) { Text("Share") }
                            TextButton(onClick = { printUri(Uri.parse(doc.uri)) }) { Text("Print") }
                            TextButton(onClick = { renameText = doc.name; editing = doc }) { Text("Rename") }
                            TextButton(onClick = {
                                records = records.filterNot { it.uri == doc.uri }
                                DocumentManager.save(currentActivity(), records)
                            }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
    editing?.let { doc ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Rename document") },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true, label = { Text("नाम") }) },
            confirmButton = {
                TextButton(onClick = {
                    val updated = records.map { if (it.uri == doc.uri) it.copy(name = renameText.ifBlank { doc.name }) else it }
                    records = updated
                    DocumentManager.save(currentActivity(), updated)
                    editing = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ToolsScreen(onScan: () -> Unit, onOcr: () -> Unit, ocrBusy: Boolean, ocrText: String) {
    var showSign by remember { mutableStateOf(false) }
    var enhanceMode by remember { mutableStateOf<String?>(null) }
    var enhanceSource by remember { mutableStateOf<Uri?>(null) }
    var enhanceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var enhanceBusy by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val openEnhance = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            enhanceSource = uri
            enhanceBitmap = runCatching { currentActivity().contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull()
            enhanceMode = "Original"
        }
    }
    val saveEnhanced = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { uri ->
        if (uri != null && enhanceBitmap != null) {
            enhanceBusy = true
            val bitmap = enhanceBitmap!!
            Thread {
                val ok = runCatching { currentActivity().contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) } ?: false }.getOrDefault(false)
                currentActivity().runOnUiThread {
                    enhanceBusy = false
                    Toast.makeText(currentActivity(), if (ok) "Enhanced image सेव हो गई" else "Image सेव नहीं हुई", Toast.LENGTH_SHORT).show()
                }
            }.start()
        }
    }
    var editorPages by remember { mutableStateOf<List<PdfPageRef>>(emptyList()) }
    var mergeSources by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pendingExport by remember { mutableStateOf<List<PdfPageRef>>(emptyList()) }
    var editorBusy by remember { mutableStateOf(false) }

    val openPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val count = PdfEditor.pageCount(currentActivity(), uri)
                editorPages = List(count) { PdfPageRef(uri, it) }
            }.onFailure { Toast.makeText(currentActivity(), "PDF नहीं खुला", Toast.LENGTH_SHORT).show() }
        }
    }
    val openManyPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) mergeSources = uris
    }
    val createPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        editorBusy = true
        val pages = pendingExport
        Thread {
            val ok = PdfEditor.export(currentActivity(), pages, uri)
            currentActivity().runOnUiThread {
                editorBusy = false
                if (ok) {
                    DocumentManager.add(currentActivity(), uri, "Edited ${System.currentTimeMillis()}", "General")
                    Toast.makeText(currentActivity(), "नया PDF सेव हो गया", Toast.LENGTH_SHORT).show()
                    editorPages = emptyList()
                } else Toast.makeText(currentActivity(), "PDF सेव नहीं हुआ", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
    val createMergedPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        editorBusy = true
        val sources = mergeSources
        Thread {
            val ok = PdfEditor.merge(currentActivity(), sources, uri)
            currentActivity().runOnUiThread {
                editorBusy = false
                if (ok) {
                    DocumentManager.add(currentActivity(), uri, "Merged ${System.currentTimeMillis()}", "General")
                    Toast.makeText(currentActivity(), "PDF merge हो गया", Toast.LENGTH_SHORT).show()
                    mergeSources = emptyList()
                } else Toast.makeText(currentActivity(), "Merge नहीं हुआ", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    if (showSign) { SignatureScreen(); return }
    if (editorPages.isNotEmpty()) {
        PdfEditorScreen(
            pages = editorPages,
            busy = editorBusy,
            onPagesChanged = { editorPages = it },
            onSave = {
                pendingExport = editorPages
                createPdf.launch("Bharat_AI_Scanner_Edited.pdf")
            },
            onClose = { editorPages = emptyList() }
        )
        return
    }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Tools / टूल्स", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf("📷 Scan", "🔤 OCR", "✨ Enhance", "📑 PDF Merge", "🛠️ PDF Editor", "✍️ Sign")) { name ->
                OutlinedButton(onClick = {
                    when {
                        name.contains("Scan") -> onScan()
                        name.contains("OCR") -> onOcr()
                        name.contains("Enhance") -> openEnhance.launch("image/*")
                        name.contains("Merge") -> openManyPdf.launch(arrayOf("application/pdf"))
                        name.contains("Editor") -> openPdf.launch(arrayOf("application/pdf"))
                        name.contains("Sign") -> showSign = true
                    }
                }, Modifier.height(70.dp)) { Text(name) }
            }
        }
        if (ocrText.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(ocrText)); Toast.makeText(currentActivity(), "OCR text कॉपी हो गया", Toast.LENGTH_SHORT).show() }) { Text("📋 Text Copy") }
                OutlinedButton(onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, ocrText) }
                    currentActivity().startActivity(Intent.createChooser(share, "OCR Text Share"))
                }) { Text("📤 Share Text") }
            }
        }
        if (enhanceBitmap != null) {
            Spacer(Modifier.height(14.dp))
            Text("Scan Enhance", style = MaterialTheme.typography.titleLarge)
            Text("फोटो को साफ़ करने के लिए mode चुनें")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Original", "Grayscale", "B&W").forEach { mode ->
                    FilterChip(selected = enhanceMode == mode, onClick = {
                        enhanceMode = mode
                        enhanceBitmap = runCatching {
                            val src = currentActivity().contentResolver.openInputStream(enhanceSource!!)!!.use { BitmapFactory.decodeStream(it)!! }
                            enhanceBitmap(src, mode)
                        }.getOrNull()
                    }, label = { Text(mode) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { saveEnhanced.launch("Bharat_AI_Scanner_Enhanced.jpg") }, enabled = !enhanceBusy, modifier = Modifier.fillMaxWidth()) {
                Text(if (enhanceBusy) "सेव हो रहा है…" else "💾 Enhanced JPG सेव करें")
            }
        }
        if (mergeSources.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = { createMergedPdf.launch("Bharat_AI_Scanner_Merged.pdf") }, enabled = !editorBusy) {
                Text("${mergeSources.size} PDF को एक PDF में जोड़ें")
            }
        }
        if (editorBusy) { Spacer(Modifier.height(12.dp)); CircularProgressIndicator() }
        if (ocrBusy) { Spacer(Modifier.height(18.dp)); CircularProgressIndicator() }
        if (ocrText.isNotBlank()) {
            Spacer(Modifier.height(18.dp))
            Text("OCR परिणाम", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) { Text(ocrText, Modifier.padding(14.dp)) }
        }
    }
}

@Composable
private fun PdfEditorScreen(
    pages: List<PdfPageRef>,
    busy: Boolean,
    onPagesChanged: (List<PdfPageRef>) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("PDF Editor", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onClose) { Text("बंद") }
        }
        Text("कुल पेज: ${pages.size}")
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(pages.size) { index ->
                val page = pages[index]
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Page ${index + 1}  •  ${page.rotation}°", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = {
                                val copy = pages.toMutableList()
                                copy[index] = page.copy(rotation = (page.rotation + 90) % 360)
                                onPagesChanged(copy)
                            }) { Text("↻ Rotate") }
                            TextButton(onClick = {
                                val copy = pages.toMutableList(); copy.removeAt(index); onPagesChanged(copy)
                            }) { Text("🗑 Delete") }
                            TextButton(onClick = {
                                if (index > 0) {
                                    val copy = pages.toMutableList(); val t = copy[index - 1]; copy[index - 1] = copy[index]; copy[index] = t; onPagesChanged(copy)
                                }
                            }) { Text("↑") }
                            TextButton(onClick = {
                                if (index < pages.lastIndex) {
                                    val copy = pages.toMutableList(); val t = copy[index + 1]; copy[index + 1] = copy[index]; copy[index] = t; onPagesChanged(copy)
                                }
                            }) { Text("↓") }
                        }
                    }
                }
            }
        }
        Button(onClick = onSave, enabled = pages.isNotEmpty() && !busy, Modifier.fillMaxWidth().height(54.dp)) {
            Text(if (busy) "PDF तैयार हो रहा है…" else "💾 नया PDF सेव करें")
        }
    }
}

@Composable
private fun SettingsScreen(onBiometric: () -> Unit, pinEnabled: Boolean, onPinChange: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(14.dp))
        Text("भाषा: हिंदी / English")
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBiometric, Modifier.fillMaxWidth()) { Text("🔐 Biometric सुरक्षा जांचें") }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("App Lock / PIN")
            Switch(checked = pinEnabled, onCheckedChange = onPinChange)
        }
        Spacer(Modifier.height(14.dp))
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("Ads-only मॉडल", style = MaterialTheme.typography.titleMedium)
            Text("Premium membership नहीं। अभी Google का test Ad ID लगा है; publishing से पहले अपना AdMob App ID/Ad Unit ID लगाना होगा।")
        } }
    }
}

@Composable
private fun SignatureScreen() {
    var path by remember { mutableStateOf(Path()) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Signature", style = MaterialTheme.typography.headlineMedium)
        Text("नीचे बॉक्स में हस्ताक्षर करें")
        Spacer(Modifier.height(12.dp))
        Canvas(Modifier.fillMaxWidth().height(220.dp).background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) { detectDragGestures(
                onDragStart = { offset -> path = Path().also { it.moveTo(offset.x, offset.y) } },
                onDrag = { change, _ -> path.lineTo(change.position.x, change.position.y); change.consume() }
            ) }) { drawPath(path, color = MaterialTheme.colorScheme.onSurface, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)) }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { path = Path() }) { Text("साफ़ करें") }
    }
}

@Composable
private fun AdsBanner() {
    AndroidView(factory = { context ->
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = "ca-app-pub-3940256099942544/6300978111"
            loadAd(AdRequest.Builder().build())
        }
    }, modifier = Modifier.fillMaxWidth().height(50.dp))
}

private fun enhanceBitmap(source: Bitmap, mode: String): Bitmap {
    if (mode == "Original") return source
    val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    for (y in 0 until source.height) {
        for (x in 0 until source.width) {
            val c = source.getPixel(x, y)
            val gray = (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)).toInt().coerceIn(0, 255)
            val value = if (mode == "B&W") if (gray >= 165) 255 else 0 else gray
            out.setPixel(x, y, Color.rgb(value, value, value))
        }
    }
    if (out !== source) source.recycle()
    return out
}

private fun openUri(uri: Uri, type: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, type); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    currentActivity().startActivity(Intent.createChooser(intent, "PDF खोलें"))
}

private fun showBiometric(activity: Activity) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            Toast.makeText(activity, "सुरक्षा चालू है", Toast.LENGTH_SHORT).show()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Toast.makeText(activity, errString, Toast.LENGTH_SHORT).show()
        }
    })
    val info = BiometricPrompt.PromptInfo.Builder().setTitle("भारत AI Scanner सुरक्षा").setSubtitle("दस्तावेज़ खोलने के लिए पहचान सत्यापित करें").setNegativeButtonText("रद्द करें").build()
    prompt.authenticate(info)
}


private fun shareUri(uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    currentActivity().startActivity(Intent.createChooser(intent, "Share PDF"))
}

private fun printUri(uri: Uri) {
    val pm = currentActivity().getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
    pm.print("भारत AI Scanner", object : android.print.PrintDocumentAdapter() {
        override fun onLayout(oldAttributes: android.print.PrintAttributes?, newAttributes: android.print.PrintAttributes?, cancellationSignal: android.os.CancellationSignal?, callback: android.print.PrintDocumentAdapter.LayoutResultCallback?, extras: android.os.Bundle?) {
            callback?.onLayoutFinished(android.print.PrintDocumentInfo.Builder("document.pdf").setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build(), true)
        }
        override fun onWrite(pages: Array<android.print.PageRange>, destination: android.os.ParcelFileDescriptor?, cancellationSignal: android.os.CancellationSignal?, callback: android.print.PrintDocumentAdapter.WriteResultCallback?) {
            currentActivity().contentResolver.openInputStream(uri)?.use { input -> destination?.let { dest -> input.copyTo(android.os.ParcelFileDescriptor.AutoCloseOutputStream(dest)) } }
            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        }
    }, null)
}

private fun currentActivity(): Activity = ActivityHolder.activity
private object ActivityHolder { lateinit var activity: Activity }
private fun ComponentActivity.registerActivityHolder() { ActivityHolder.activity = this }

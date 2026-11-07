 package com.bharataiscanner.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri

data class PdfPageRef(val sourceUri: Uri, val sourcePage: Int, val rotation: Int = 0)

object PdfEditor {
    fun pageCount(context: Context, uri: Uri): Int = openRenderer(context, uri).use { it.pageCount }

    fun renderPage(context: Context, ref: PdfPageRef, maxWidth: Int = 900): Bitmap {
        openRenderer(context, ref.sourceUri).use { renderer ->
            renderer.openPage(ref.sourcePage).use { page ->
                val scale = maxWidth.toFloat() / page.width.toFloat()
                val w = (page.width * scale).toInt().coerceAtLeast(1)
                val h = (page.height * scale).toInt().coerceAtLeast(1)
                val base = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                base.eraseColor(android.graphics.Color.WHITE)
                page.render(base, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return if (ref.rotation % 360 == 0) base else rotate(base, ref.rotation)
            }
        }
    }

    fun export(context: Context, pages: List<PdfPageRef>, destination: Uri): Boolean = runCatching {
        require(pages.isNotEmpty())
        val document = PdfDocument()
        try {
            pages.forEachIndexed { index, ref ->
                val bitmap = renderPage(context, ref, 1400)
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = document.startPage(pageInfo)
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)
                bitmap.recycle()
            }
            context.contentResolver.openOutputStream(destination)?.use { out -> document.writeTo(out) }
                ?: error("Output stream unavailable")
        } finally {
            document.close()
        }
        true
    }.getOrDefault(false)

    fun merge(context: Context, sources: List<Uri>, destination: Uri): Boolean {
        val pages = mutableListOf<PdfPageRef>()
        sources.forEach { uri ->
            val count = pageCount(context, uri)
            repeat(count) { page -> pages += PdfPageRef(uri, page) }
        }
        return export(context, pages, destination)
    }

    private fun openRenderer(context: Context, uri: Uri): PdfRenderer {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("PDF open नहीं हुआ")
        return PdfRenderer(pfd)
    }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }
}

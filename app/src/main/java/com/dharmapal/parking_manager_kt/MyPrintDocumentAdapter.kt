package com.dharmapal.parking_manager_kt

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream

class MyPrintDocumentAdapter(val context: Context) : PrintDocumentAdapter() {

    private lateinit var myPdfDocument:PrintedPdfDocument
    override fun onLayout(
        p0: PrintAttributes?,
        p1: PrintAttributes?,
        p2: CancellationSignal?,
        p3: LayoutResultCallback?,
        p4: Bundle?
    ) {
        myPdfDocument = PrintedPdfDocument(context,p1!!)
        val builder =
            PrintDocumentInfo.Builder("print_output.pdf").setContentType(
                PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)

        val info = builder.build()
        p3!!.onLayoutFinished(info, true)
    }

    override fun onWrite(
        p0: Array<out PageRange>?,
        p1: ParcelFileDescriptor?,
        p2: CancellationSignal?,
        p3: WriteResultCallback?
    ) {
        val newPage = PdfDocument.PageInfo.Builder(200,
            200,1).create()

        val page = myPdfDocument.startPage(newPage)
        drawPage(page)
        myPdfDocument.finishPage(page)

        myPdfDocument.writeTo(FileOutputStream(p1!!.fileDescriptor))

        // Signal the print framework the document is complete
        p3!!.onWriteFinished(p0)
    }

    private fun drawPage(page: PdfDocument.Page) {
        page.canvas.apply {

            // units are in points (1/72 of an inch)
            val titleBaseLine = 72f
            val leftMargin = 54f

            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 36f
            drawText("Test Title", leftMargin, titleBaseLine, paint)

            paint.textSize = 11f
            drawText("Test paragraph", leftMargin, titleBaseLine + 25, paint)

            paint.color = Color.BLUE
            drawRect(100f, 100f, 172f, 172f, paint)
        }
    }
}




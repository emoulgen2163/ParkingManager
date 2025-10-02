package com.mycompany.parkingmanager.domain.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieDataSet
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.mycompany.parkingmanager.domain.SummaryModel
import com.mycompany.parkingmanager.presentation.ui.fragments.ReportsFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

object PDFHelper {

    var index = 0

    suspend fun generateReport(context: Context, summary: List<SummaryModel>, graphMap: HashMap<String, Any>, onPDFGenerated: ((report: File) -> Unit)? = null){
        val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val pdfFile = File(context.getExternalFilesDir(null), "report_${timeStamp}.pdf")
        val writer = PdfWriter(pdfFile)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("Report").simulateBold().setFontSize(18f))

        val table = Table(floatArrayOf(1f, 3f, 2f))
        listOf("Name", "Total Income").forEach { text ->
            table.addHeaderCell(Cell().add(Paragraph(text).simulateBold().setFontSize(15f).setFontColor(ColorConstants.BLACK)))
                .setBackgroundColor(ColorConstants.WHITE)
                .setBorder(SolidBorder(1f))
        }

        summary.mapIndexed { index, summary ->
            table.addCell(Cell().add(Paragraph((index + 1).toString()))
                .setBorder(SolidBorder(1f)))

            table.addCell(Cell().add(Paragraph(summary.name))
                .setBorder(SolidBorder(1f)))

            table.addCell(Cell().add(Paragraph(summary.totalIncome.toString()))
                .setBorder(SolidBorder(1f)))
        }

        document.add(table)

        graphMap.keys.forEach {
            val dataSetLabelArray = graphMap[it]
            val pdfDocument = document.pdfDocument

            if(dataSetLabelArray is PieDataSet){
                val page = pdfDocument.addNewPage()
                val canvas = PdfCanvas(page)
                drawPieChart(canvas, dataSetLabelArray)
            } else {
                val dataSet = dataSetLabelArray as ChartDataLabels
                val label = dataSet.labels
                val barChartData = dataSet.chartDataSet
                index = 0
                barChartDataSetCheck(pdfDocument, barChartData, label)
            }

            document.add(Paragraph("\n\n"))
            document.add(Paragraph("").setMarginTop(20f))
        }

        document.close()
        onPDFGenerated?.invoke(pdfFile)
    }

    private fun barChartDataSetCheck(document: PdfDocument, barChartData: BarDataSet, label: List<String>) {
        val currentLabels = mutableListOf<String>()
        val barEntry = mutableListOf<BarEntry>()
        var currentBarDataSet: BarDataSet
        if (label.size <= 15) {
            val page = document.addNewPage()
            val canvas = PdfCanvas(page)
            drawBarChart(canvas, barChartData, label)
        } else {
            for (i in 0 until barChartData.entryCount) {
                if (i % 1 != 14 || i == 0) {
                    barEntry.add(barChartData.getEntryForIndex(i))
                    currentLabels.add(label[i])
                } else {
                    barEntry.add(barChartData.getEntryForIndex(i))
                    currentLabels.add(label[i])
                    index++
                    currentBarDataSet = BarDataSet(barEntry, "Page $index")
                    val page = document.addNewPage()
                    val canvas = PdfCanvas(page)
                    drawBarChart(canvas, currentBarDataSet, currentLabels)
                    currentLabels.clear()
                    currentBarDataSet.clear()
                    barEntry.clear()
                }
            }
        }
    }

    fun drawBarChart(canvas: PdfCanvas, dataSet: BarDataSet, labelList: List<String>){
        val page = canvas.document.lastPage
        val entries = dataSet.entryCount
        var startX = page.pageSize.width / 2
        val startY = page.pageSize.height / 4
        val pdfFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        val fontSize = 12f
        val title = dataSet.label
        val titleWidth = pdfFont.getWidth(title, fontSize)

        canvas.apply {
            beginText()
            setFontAndSize(pdfFont, fontSize)
            moveText((startX - titleWidth / 2).toDouble(), (startY + page.pageSize.height / 2).toDouble())
            showText(title)
        }

        val chartHeight = 300f
        val barWidth = 30f
        val space = 10f
        var maxValue = 0f


        for (i in 0 until entries){
            startX = startX - (barWidth / 2)
            val entry = dataSet.getEntryForIndex(i)
            if (entry.y > maxValue){
                maxValue = entry.y
            }
        }
        if (maxValue == 0f) maxValue = 1f  // avoid division by zero
        val scale = chartHeight / maxValue
        val colorList = ReportsFragment.colorPalette

        val yStep = maxValue / 10
        for (j in  0..10){
            val yPos = startY + j * chartHeight / 10
            canvas.apply {
                val value = String.format("%.2f", (j * yStep))
                setStrokeColor(ColorConstants.BLACK)
                moveTo((startX - 10f).toDouble(), yPos.toDouble())
                lineTo((startX + entries * (barWidth + space)).toDouble(), yPos.toDouble())
                stroke()
                beginText()
                setFontAndSize(pdfFont, fontSize)
                moveText((startX - 50).toDouble(), (yPos - fontSize / 2).toDouble())
                showText(value)
                endText()
            }
        }

        for (i in 0 until entries){
            val entry = dataSet.getEntryForIndex(i)
            val x = startX + i * (barWidth + space)
            val y = startY
            val height = entry.y * scale
            val angle = Math.toRadians(-45.0)

            canvas.apply {
                setFillColor(hexToRgb(colorList[i % colorList.size]))
                rectangle(x.toDouble(), y .toDouble(), barWidth.toDouble(), height.toDouble())
                fill()
                moveTo((startX - 10f).toDouble(), startY.toDouble())
                saveState()
                beginText()
                setFontAndSize(pdfFont, fontSize)
                moveText(x.toDouble(), (y - 12f).toDouble())
                setTextMatrix(cos(angle).toFloat(), sin(angle).toFloat(), -sin(angle).toFloat(), cos(angle).toFloat(), x + 5, (y - 12f))
                showText(labelList[i])
                endText()
                restoreState()
                setStrokeColor(ColorConstants.BLACK)
                moveTo((startX - 10f).toDouble(), startY.toDouble())
                lineTo((startX - 10f).toDouble(), (startY + chartHeight + 10f).toDouble())
                stroke()
            }
        }

    }

    fun drawPieChart(canvas: PdfCanvas, dataSet: PieDataSet){

        val page = canvas.document.lastPage
        val centerX = page.pageSize.width / 2
        val centerY = page.pageSize.height / 2

        val font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val fontSize = 12f
        val title = dataSet.label
        val titleWidth = font.getWidth(title, fontSize)

        canvas.apply {
            beginText()
            setFontAndSize(font, fontSize)
            moveText((centerX - titleWidth / 2).toDouble(), (centerY + page.pageSize.height / 4).toDouble())
            showText(title)
        }
        val data = mutableListOf<Pair<String, Double>>()
        for (i in 0 until dataSet.entryCount){
            val entry = dataSet.getEntryForIndex(i)
            data.add(entry.label to entry.value.toDouble())
        }
        val total = data.sumOf {
            it.second
        }
        var startAngle = 0.0
        val radius = 150f
        val colorList = ReportsFragment.colorPalette

        data.forEachIndexed { index, pair ->
            val (label, value) = pair
            val angle = 360.0 * value / total
            canvas.apply {
                saveState()
                canvas.setFillColor(hexToRgb(colorList[index % colorList.size]))
                moveTo(centerX.toDouble(), centerY.toDouble())
            }

            val steps = 100
            for (i in 0 .. steps){
                val theta = Math.toRadians(startAngle + i * (angle / steps))
                val x = centerX + radius * cos(theta)
                val y = centerY + radius * sin(theta)
                canvas.lineTo(x, y)
            }

            canvas.closePathFillStroke()
            canvas.restoreState()
            val midAngle = Math.toRadians(startAngle + angle / 2)
            val labelX = centerX + (radius * 0.6f) * cos(midAngle)
            val labelY = centerY + (radius * 0.6f) * sin(midAngle)
            val mValue = String.format("%.2f", value)
            canvas.beginText()
            canvas.setFontAndSize(font, fontSize)
            canvas.moveText(labelX, labelY)
            canvas.showText(label)
            canvas.moveText(0.0, -12.0)
            canvas.showText(mValue)
            canvas.endText()
            startAngle += angle

        }
    }

    fun hexToRgb(hex: String): DeviceRgb{
        val cleanHex = hex.removePrefix("#")
        val r = cleanHex.substring(0, 2).toInt(16)
        val g = cleanHex.substring(2, 4).toInt(16)
        val b = cleanHex.substring(4, 6).toInt(16)

        return DeviceRgb(r, g, b)
    }

    fun openPDF(context: Context, pdfFile: File){
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun sharePDF(context: Context, pdfFile: File){
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            setDataAndType(uri, "application/pdf")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share the Report"))
    }
}
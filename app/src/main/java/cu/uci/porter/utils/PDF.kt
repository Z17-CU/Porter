package cu.uci.porter.utils

import android.content.Context
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import cu.uci.porter.R
import cu.uci.porter.repository.entitys.Client
import cu.uci.porter.repository.entitys.Queue
import cu.uci.porter.utils.Common.Companion.shareQueue
import cu.uci.porter.utils.Conts.Companion.APP_DIRECTORY
import cu.uci.porter.utils.Conts.Companion.formatDateBig
import cu.uci.porter.utils.Conts.Companion.formatDateOnlyTime
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.util.*


class PDF(val context: Context) {
    fun write(
        queue: Queue,
        clients: List<Client>
    ) {
        val path: String =
            APP_DIRECTORY + "/" + queue.name + " " + formatDateBig.format(queue.startDate) + " " + Calendar.getInstance()
                .timeInMillis + ".pdf"
        val file = File(path)

        val progress = Progress(context)

        CompositeDisposable().add(Completable.create {

            val bfBold12 =
                Font(Font.FontFamily.TIMES_ROMAN, 18f, Font.BOLD, BaseColor(0, 0, 0))

            File(APP_DIRECTORY).mkdir()

            if (!file.exists()) {
                file.createNewFile()
            }
            val document = Document()
            PdfWriter.getInstance(
                document,
                FileOutputStream(file.absoluteFile)
            )
            document.open()

            var paragraph = Paragraph(queue.name + " " + formatDateBig.format(queue.startDate))
            paragraph.font = bfBold12
            document.add(paragraph)
            paragraph = Paragraph(" ")
            paragraph.font = bfBold12
            document.add(paragraph)


            val table = PdfPTable(4)
            var cell1: PdfPCell? = PdfPCell(Phrase("Nombre"))
            var cell3: PdfPCell? = PdfPCell(Phrase("CI"))
            var cell4: PdfPCell? = PdfPCell(Phrase("Reintentos"))
            var cell5: PdfPCell? = PdfPCell(Phrase("Última ves en cola"))
            table.addCell(cell1)
            table.addCell(cell3)
            table.addCell(cell4)
            table.addCell(cell5)

            clients.map { client ->
                cell1 = PdfPCell(Phrase(client.name))
                cell3 = PdfPCell(Phrase(client.ci))
                cell4 = PdfPCell(Phrase(client.reIntent.toString()))
                cell5 = PdfPCell(Phrase(formatDateOnlyTime.format(client.lastRegistry)))

                table.addCell(cell1)
                table.addCell(cell3)
                table.addCell(cell4)
                table.addCell(cell5)
            }

            document.add(table)
            document.close()

            it.onComplete()

        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                progress.dismiss()
                Toast.makeText(
                    context,
                    R.string.export_OK,
                    Toast.LENGTH_LONG
                ).show()
                shareQueue(context, file, "pdf")
            })
    }
}
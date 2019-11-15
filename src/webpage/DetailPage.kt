package webpage

import LogLevel
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import findFirst
import log
import models.*
import models.Category.F
import java.time.ZonedDateTime

class DetailPage(val htmlPage: HtmlPage, val courseCode: String?, val time: ZonedDateTime? = null) {
    val assignments = ArrayList<Assignment>()
    val weightTable = WeightTable()

    init {
        val detailTable =
            htmlPage.getByXPath<HtmlTable>("//table[@border='1'][@cellpadding='3'][@cellspacing='0'][@width='100%']")[0]

        val infoRow = detailTable.getRow(0)
        val categoryOfEachColumn = ArrayList<Category>()
        for (i in 1 until infoRow.cells.size) {
            enumValues<Category>().forEach {
                if (it.displayName == infoRow.getCell(i).asText()) {
                    categoryOfEachColumn.add(it)
                }
            }
        }

        for (rowI in 1 until detailTable.rowCount step 2) {
            try {
                val row = detailTable.getRow(rowI)
                val assignment = Assignment()

                assignment.name = row.getCell(0).asText()
                if (time != null) {
                    assignment.time = time
                }
                val feedbackText = detailTable.getRow(rowI + 1).asText()
                if (!feedbackText.isBlank()) {
                    assignment.feedback = feedbackText.replace(Regex("(\\R|\\s)+"), " ").replace("Feedback:", "").trim()
                }

                val smallMarkCategoryAdded = ArrayList<Category>()
                for (cellI in 1 until row.cells.size) {
                    val cell = row.getCell(cellI)
                    val category = categoryOfEachColumn[cellI - 1]
                    val smallMark = SmallMark(category)

                    val smallMarkText = cell.asText()

                    if (smallMarkText != "" && smallMarkText != "no mark") {
                        smallMark.available = true
                        val getText = findFirst(smallMarkText, "^[^ ]+(?= / )")
                        smallMark.get = if (getText != null) {
                            getText.toDouble()
                        } else {
                            smallMark.finished = false
                            0.0
                        }
                        smallMark.total = findFirst(smallMarkText, "(?<=/ )[^ ]+(?= = )")!!.toDouble()
                        smallMark.weight = if (smallMarkText.indexOf("no weight") != -1) {
                            0.0
                        } else {
                            findFirst(smallMarkText, "(?<=weight=)[^ ]+$")!!.toDouble()
                        }

                    }
                    assignment.smallMarks.add(smallMark)
                    smallMarkCategoryAdded.add(category)
                }
                enumValues<Category>().forEach { category ->
                    if (!smallMarkCategoryAdded.contains(category)) {
                        assignment.smallMarks.add(SmallMark(category))
                    }
                }
                assignments.add(assignment)
            } catch (e: Exception) {
                log(LogLevel.ERROR, "Cannot parse detail row #$rowI in $courseCode", e)
            }

        }

        val weightsTable =
            htmlPage.getByXPath<HtmlTable>("//table[@border='1'][@cellpadding='3'][@cellspacing='0'][not(@width)]")
                .last()

        for (rowI in 1..5) {
            val row = weightsTable.getRow(rowI)
            val category = CategoryFrom(row.getCell(0).asText())

            val weight = Weight(category)
            weight.W = findFirst(row.getCell(1).asText(), "^[^%]+")!!.toDouble()
            weight.CW = findFirst(row.getCell(2).asText(), "^[^%]+")!!.toDouble()
            weight.SA = findFirst(row.getCell(3).asText(), "^[^%]+")!!.toDouble()

            weightTable.weightsList.add(weight)
        }
        val finalRow = weightsTable.getRow(6)
        val finalWeight = Weight(F)
        finalWeight.CW = findFirst(finalRow.getCell(1).asText(), "^[^%]+")!!.toDouble()
        finalWeight.SA = findFirst(finalRow.getCell(2).asText(), "^[^%]+")!!.toDouble()
        weightTable.weightsList.add(finalWeight)
    }


}

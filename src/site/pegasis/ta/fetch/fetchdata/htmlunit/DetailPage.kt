package site.pegasis.ta.fetch.fetchdata.htmlunit

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.models.Category.F
import site.pegasis.ta.fetch.tools.LogLevel
import site.pegasis.ta.fetch.tools.findFirst
import site.pegasis.ta.fetch.tools.log
import java.time.ZonedDateTime

class DetailPage(htmlPage: HtmlPage, courseCode: String?, time: ZonedDateTime, timing: Timing = Timing()) {
    val assignments = AssignmentList()
    var weightTable = WeightTable()

    init {
        timing("parse detail page $courseCode") {
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
                    assignment.time = time
                    val feedbackText = detailTable.getRow(rowI + 1).asText()
                    if (!feedbackText.isBlank()) {
                        assignment.feedback = feedbackText.replace(Regex("(\\R|\\s)+"), " ").replace("Feedback:", "").trim()
                    }

                    for (cellI in 1 until row.cells.size) {
                        val cell = row.getCell(cellI)
                        val category = categoryOfEachColumn[cellI - 1]
                        val smallMarkGroup = SmallMarkGroup()

                        val smallMarkElems = cell.getElementsByTagName("tr")
                        smallMarkElems.forEach { smallMarkElem ->
                            val smallMarkText = smallMarkElem.asText()
                            val smallMark = SmallMark()
                            if (smallMarkText != "no mark") {
                                val getText =
                                    findFirst(smallMarkText, "^[^ ]+(?= / )")
                                smallMark.get = if (getText != null) {
                                    getText.toDouble()
                                } else {
                                    smallMark.finished = false
                                    0.0
                                }
                                smallMark.total = findFirst(
                                    smallMarkText,
                                    "(?<=/ )[^ ]+(?= = )"
                                )!!.toDouble()
                                smallMark.weight = if (smallMarkText.indexOf("no weight") != -1) {
                                    0.0
                                } else {
                                    findFirst(
                                        smallMarkText,
                                        "(?<=weight=)[^ ]+$"
                                    )!!.toDouble()
                                }

                            }
                            smallMarkGroup.add(smallMark)
                        }
                        assignment[category] = smallMarkGroup
                    }
                    enumValues<Category>().forEach { category ->
                        assignment.putIfAbsent(category, SmallMarkGroup())
                    }
                    assignments.add(assignment)
                } catch (e: Exception) {
                    log(
                        LogLevel.ERROR,
                        "Cannot parse detail row #$rowI in $courseCode",
                        e
                    )
                }

            }

            //rename assignments that have the same name
            val assignmentNames = HashMap<String, Int>()
            assignments.forEach {
                val appearTimes = assignmentNames.getOrDefault(it.name, 0) + 1
                assignmentNames[it.name] = appearTimes
                if (appearTimes > 1) it.name += " ($appearTimes)"
            }

            val weightsTable: HtmlTable? = try {
                htmlPage
                    .getByXPath<HtmlTable>("//table[@border='1'][@cellpadding='3'][@cellspacing='0'][not(@width)]")
                    .last()
            } catch (e: NoSuchElementException) {
                null
            }

            if (weightsTable != null) {
                for (rowI in 1..5) {
                    val row = weightsTable.getRow(rowI)
                    val category = categoryFrom(row.getCell(0).asText())

                    val weight = Weight()
                    weight.W = findFirst(row.getCell(1).asText(), "^[\\.\\d]+(?=%)")!!.toDouble()
                    weight.CW = findFirst(row.getCell(2).asText(), "^[\\.\\d]+(?=%)")!!.toDouble()
                    val SAText = findFirst(row.getCell(3).asText(), "^[\\.\\d]+(?=%)")
                    weight.SA = try {
                        OverallMark(SAText!!.toDouble())
                    } catch (e: Throwable) {
                        OverallMark(row.getCell(3).asText())
                    }

                    weightTable[category] = weight
                }
                val finalRow = weightsTable.getRow(6)
                val finalWeight = Weight()
                finalWeight.CW = findFirst(finalRow.getCell(1).asText(), "^[\\.\\d]+(?=%)")!!.toDouble()
                val SAText = findFirst(finalRow.getCell(2).asText(), "^[\\.\\d]+(?=%)")
                finalWeight.SA = try {
                    OverallMark(SAText!!.toDouble())
                } catch (e: Throwable) {
                    OverallMark(finalRow.getCell(2).asText())
                }
                weightTable[F] = finalWeight
            } else {
                weightTable.fillFakeData()
            }
        }
    }


}

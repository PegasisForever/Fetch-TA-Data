package webpage

import LogLevel
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import findFirst
import log
import models.*
import models.Category.F
import java.time.ZonedDateTime

class DetailPage(htmlPage: HtmlPage, courseCode: String?, time: ZonedDateTime) {
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
                assignment.time = time
                val feedbackText = detailTable.getRow(rowI + 1).asText()
                if (!feedbackText.isBlank()) {
                    assignment.feedback = feedbackText.replace(Regex("(\\R|\\s)+"), " ").replace("Feedback:", "").trim()
                }

                val smallMarkGroupCategoryAdded = ArrayList<Category>()
                for (cellI in 1 until row.cells.size) {
                    val cell = row.getCell(cellI)
                    val category = categoryOfEachColumn[cellI - 1]
                    val smallMarkGroup = SmallMarkGroup(category)
                    smallMarkGroupCategoryAdded.add(category)

                    val smallMarkElems = cell.getElementsByTagName("tr")
                    smallMarkGroup.available = smallMarkElems.size > 0
                    smallMarkElems.forEach { smallMarkElem ->
                        val smallMarkText = smallMarkElem.asText()
                        val smallMark = SmallMark()
                        if (smallMarkText != "" && smallMarkText != "no mark") {
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
                        smallMarkGroup.add(smallMark)
                    }
                    assignment.smallMarkGroups.add(smallMarkGroup)
                }
                enumValues<Category>().forEach { category ->
                    if (!smallMarkGroupCategoryAdded.contains(category)) {
                        assignment.smallMarkGroups.add(SmallMarkGroup(category))
                    }
                }
                assignments.add(assignment)
            } catch (e: Exception) {
                log(LogLevel.ERROR, "Cannot parse detail row #$rowI in $courseCode", e)
            }

        }

        //rename assignments that have the same name
        val assignmentNames = HashMap<String, Int>()
        assignments.forEach {
            val appearTimes = assignmentNames.getOrDefault(it.name, 0) + 1
            assignmentNames[it.name] = appearTimes
            if (appearTimes > 1) it.name += " ($appearTimes)"
        }

        val weightsTable =
            htmlPage.getByXPath<HtmlTable>("//table[@border='1'][@cellpadding='3'][@cellspacing='0'][not(@width)]")
                .last()

        for (rowI in 1..5) {
            val row = weightsTable.getRow(rowI)
            val category = CategoryFrom(row.getCell(0).asText())

            val weight = Weight(category)
            weight.W = findFirst(row.getCell(1).asText(), "^[\\.\\d]+(?=%)")!!.toDouble()
            weight.CW = findFirst(row.getCell(2).asText(), "^[\\.\\d]+(?=%)")!!.toDouble()
            val SAText = findFirst(row.getCell(3).asText(), "^[\\.\\d]+(?=%)")
            weight.SA = try {
                OverallMark(SAText!!.toDouble())
            } catch (e: Throwable) {
                OverallMark(row.getCell(3).asText())
            }

            weightTable.weightsList.add(weight)
        }
        val finalRow = weightsTable.getRow(6)
        val finalWeight = Weight(F)
        finalWeight.CW = findFirst(finalRow.getCell(1).asText(), "^[^%]+")!!.toDouble()
        val SAText = findFirst(finalRow.getCell(2).asText(), "^[^%]+")
        finalWeight.SA = try {
            OverallMark(SAText!!.toDouble())
        } catch (e: Throwable) {
            OverallMark(SAText ?: "")
        }
        weightTable.weightsList.add(finalWeight)
    }


}

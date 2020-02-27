package site.pegasis.ta.fetch.fetchdata.chrome

import org.openqa.selenium.By
import site.pegasis.ta.fetch.fetchdata.chromepool.ChromeDriverWrapper
import site.pegasis.ta.fetch.fetchdata.getDirectChildren
import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.models.Category.F
import site.pegasis.ta.fetch.tools.LogLevel
import site.pegasis.ta.fetch.tools.findFirst
import site.pegasis.ta.fetch.tools.log
import java.time.ZonedDateTime

class DetailPage(webClient: ChromeDriverWrapper, courseCode: String?, time: ZonedDateTime, timing: Timing = Timing()) {
    val assignments = AssignmentList()
    val weightTable = WeightTable()

    init {
        timing("parse detail page $courseCode") {
            val detailTable = webClient.driver.findElementsByXPath("//table[@border='1'][@cellpadding='3'][@cellspacing='0'][@width='100%']")[0]

            val rows = detailTable.getDirectChildren()[0].getDirectChildren()
            val infoRow = rows[0]
            val infoRowCells = infoRow.getDirectChildren()
            val categoryOfEachColumn = ArrayList<Category>()
            for (i in 1 until infoRowCells.size) {
                enumValues<Category>().forEach {
                    if (it.displayName == infoRowCells[i].text) {
                        categoryOfEachColumn.add(it)
                    }
                }
            }

            for (rowI in 1 until rows.size step 2) {
                try {
                    val row = rows[rowI]
                    val feedbackRow = rows[rowI + 1]
                    val cells = row.getDirectChildren()

                    val assignment = Assignment()
                    assignment.name = cells[0].text
                    assignment.time = time
                    val feedbackText = feedbackRow.text
                    if (!feedbackText.isBlank()) {
                        assignment.feedback = feedbackText.replace(Regex("(\\R|\\s)+"), " ").replace("Feedback:", "").trim()
                    }

                    for (cellI in 1 until cells.size) {
                        val cell = cells[cellI]
                        val category = categoryOfEachColumn[cellI - 1]
                        val smallMarkGroup = SmallMarkGroup()

                        val smallMarkElems = cell.findElements(By.tagName("tr"))
                        smallMarkElems.forEach { smallMarkElem ->
                            val smallMarkText = smallMarkElem.text.replace("\n", " \n")
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

            val weightsTable = try {
                webClient
                    .driver
                    .findElementsByXPath("//table[@border='0'][@cellpadding='5']//table[@border='1'][@cellpadding='3'][@cellspacing='0'][not(@width)]").last()
            } catch (e: NoSuchElementException) {
                null
            }

            if (weightsTable != null) {
                val weightRows = weightsTable.findElements(By.tagName("tr"))
                for (rowI in 1..5) {
                    val row = weightRows[rowI]
                    val cells = row.getDirectChildren()
                    val category = categoryFrom(cells[0].text)

                    val weight = Weight()
                    weight.W = findFirst(cells[1].text, "^[\\.\\d]+(?=%)")!!.toDouble()
                    weight.CW = findFirst(cells[2].text, "^[\\.\\d]+(?=%)")!!.toDouble()
                    val SAText = findFirst(cells[3].text, "^[\\.\\d]+(?=%)")
                    weight.SA = try {
                        OverallMark(SAText!!.toDouble())
                    } catch (e: Throwable) {
                        OverallMark(cells[3].text)
                    }

                    weightTable[category] = weight
                }
                val finalRow = weightRows[6]
                val finalCells = finalRow.getDirectChildren()
                val finalWeight = Weight()
                finalWeight.CW = findFirst(finalCells[1].text, "^[\\.\\d]+(?=%)")!!.toDouble()
                val SAText = findFirst(finalCells[2].text, "^[\\.\\d]+(?=%)")
                finalWeight.SA = try {
                    OverallMark(SAText!!.toDouble())
                } catch (e: Throwable) {
                    OverallMark(finalCells[2].text)
                }
                weightTable[F] = finalWeight
            }else{
                weightTable.fillFakeData()
            }
        }
    }


}

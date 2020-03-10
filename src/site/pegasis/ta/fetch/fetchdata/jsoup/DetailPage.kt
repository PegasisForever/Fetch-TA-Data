package site.pegasis.ta.fetch.fetchdata.jsoup

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.tools.findFirst
import java.time.ZonedDateTime


class DetailPage(session: JsoupSession, courseCode: String?, time: ZonedDateTime, timing: Timing = Timing()) {
    val assignments = AssignmentList()
    val weightTable = WeightTable()

    companion object{
        val detailTableEvaluator = object : Evaluator() {
            override fun matches(root: Element, element: Element): Boolean {
                return element.attr("border") == "1" &&
                    element.attr("cellpadding") == "3" &&
                    element.attr("cellspacing") == "0" &&
                    element.attr("width") == "100%"
            }
        }

        val weightTableEvaluator1=object :Evaluator(){
            override fun matches(root: Element, element: Element): Boolean {
                return element.attr("border") == "0" &&
                    element.attr("cellpadding") == "5"
            }
        }

        val weightTableEvaluator2=object :Evaluator(){
            override fun matches(root: Element, element: Element): Boolean {
                return element.attr("border") == "1" &&
                    element.attr("cellpadding") == "3" &&
                    element.attr("cellspacing") == "0"
            }
        }
    }

    init {
        object : Evaluator() {
            override fun matches(root: Element, element: Element): Boolean {
                return element.attr("border") == "1" &&
                    element.attr("cellpadding") == "3" &&
                    element.attr("cellspacing") == "0" &&
                    element.attr("width") == "100%"
            }
        }
        val detailTable = session.currentPage!!.select(detailTableEvaluator)[0]

        val rows = detailTable.children()[0].children()
        val infoRow = rows[0]
        val infoRowCells = infoRow.children()
        val categoryOfEachColumn = ArrayList<Category>()
        for (i in 1 until infoRowCells.size) {
            enumValues<Category>().forEach {
                if (it.displayName == infoRowCells[i].text()) {
                    categoryOfEachColumn.add(it)
                }
            }
        }

        for (rowI in 1 until rows.size step 2) {
            val row = rows[rowI]
            val feedbackRow = rows[rowI + 1]
            val cells = row.children()

            val assignment = Assignment()
            assignment.name = cells[0].text()
            assignment.time = time
            val feedbackText = feedbackRow.text()
            if (!feedbackText.isBlank()) {
                assignment.feedback = feedbackText.replace(Regex("(\\R|\\s)+"), " ").replace("Feedback:", "").trim()
            }

            for (cellI in 1 until cells.size) {
                val cell = cells[cellI]
                val category = categoryOfEachColumn[cellI - 1]
                val smallMarkGroup = SmallMarkGroup()

                val smallMarkElems = cell.getElementsByTag("tr")
                smallMarkElems.forEach { smallMarkElem ->
                    val smallMarkText = smallMarkElem.text()
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
        }

        //rename assignments that have the same name
        val assignmentNames = HashMap<String, Int>()
        assignments.forEach {
            val appearTimes = assignmentNames.getOrDefault(it.name, 0) + 1
            assignmentNames[it.name] = appearTimes
            if (appearTimes > 1) it.name += " ($appearTimes)"
        }

        val weightsTable = try {
            val result = Elements()
            session.currentPage!!.select(weightTableEvaluator1).forEach { element: Element ->
                result.addAll(element.select(weightTableEvaluator2))
            }

            result.last()
        } catch (e: NoSuchElementException) {
            null
        }

        if (weightsTable != null) {
            val weightRows = weightsTable.getElementsByTag("tr")
            for (rowI in 1..5) {
                val row = weightRows[rowI]
                val cells = row.children()
                val category = categoryFrom(cells[0].text())

                val weight = Weight()
                weight.W = findFirst(cells[1].text(), "^[\\.\\d]+(?=%)")!!.toDouble()
                weight.CW = findFirst(cells[2].text(), "^[\\.\\d]+(?=%)")!!.toDouble()
                val SAText = findFirst(cells[3].text(), "^[\\.\\d]+(?=%)")
                weight.SA = try {
                    OverallMark(SAText!!.toDouble())
                } catch (e: Throwable) {
                    OverallMark(cells[3].text())
                }

                weightTable[category] = weight
            }
            val finalRow = weightRows[6]
            val finalCells = finalRow.children()
            val finalWeight = Weight()
            finalWeight.CW = findFirst(finalCells[1].text(), "^[\\.\\d]+(?=%)")!!.toDouble()
            val SAText = findFirst(finalCells[2].text(), "^[\\.\\d]+(?=%)")
            finalWeight.SA = try {
                OverallMark(SAText!!.toDouble())
            } catch (e: Throwable) {
                OverallMark(finalCells[2].text())
            }
            weightTable[Category.F] = finalWeight
        } else {
            weightTable.fillFakeData()
        }
    }

}
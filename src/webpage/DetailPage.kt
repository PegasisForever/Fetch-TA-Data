package webpage

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import find
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import webpage.Category.*
import webpage.Weights.*

class DetailPage(val htmlPage: HtmlPage, val courseCode: String) {
    val details = JSONObject()

    init {
        val detailTable = htmlPage.getElementsByTagName("table")[1] as HtmlTable

        val infoRow = detailTable.getRow(0)
        val columnNames = ArrayList<Category>()
        for (i in 1 until infoRow.cells.size) {
            enumValues<Category>().forEach {
                if (it.displayName == infoRow.getCell(i).asText()) {
                    columnNames.add(it)
                }
            }
        }

        val assignments = JSONArray()
        for (rowI in 1 until detailTable.rowCount step 2) {
            val row = detailTable.getRow(rowI)
            val assignment = getAssignmentTemplate()

            assignment["name"] = row.getCell(0).asText()

            for (cellI in 1 until row.cells.size) {
                val cell = row.getCell(cellI)
                val categoryName = columnNames[cellI - 1]
                val categoryDetail = assignment[categoryName.name] as JSONObject

                val categoryDetailText = cell.asText()
                categoryDetail["available"] = true
                categoryDetail["get"] = find(categoryDetailText, "^\\d+(?= / )")[0]
                categoryDetail["total"] = find(categoryDetailText, "(?<= / )\\d+(?= = )")[0]
                categoryDetail["weight"] = find(categoryDetailText, "(?<=weight=)\\d+\$")[0]
            }

            assignments.add(assignment)
        }
        details["assignments"] = assignments

        val weightsTable =
            htmlPage.getByXPath<HtmlTable>("//table[@border='1'][@cellpadding='3'][@cellspacing='0'][not(@width)]")[0]
        val weights = JSONObject()
        for (rowI in 1..5) {
            val row = weightsTable.getRow(rowI)
            val name = when (row.getCell(0).asText()) {
                "Knowledge/Understanding" -> KU
                "Thinking" -> T
                "Communication" -> C
                "Application" -> A
                "Other" -> O
                else -> null
            }

            val weightRow = JSONObject()
            weightRow[W.name] = find(row.getCell(1).asText(), "^[^%]+")[0]
            weightRow[CW.name] = find(row.getCell(2).asText(), "^[^%]+")[0]
            weightRow[SA.name] = find(row.getCell(3).asText(), "^[^%]+")[0]

            weights[name!!.name] = weightRow
        }
        val finalRow = weightsTable.getRow(6)
        val finalWeightRow = JSONObject()
        finalWeightRow[CW.name] = find(finalRow.getCell(1).asText(), "^[^%]+")[0]
        finalWeightRow[SA.name] = find(finalRow.getCell(2).asText(), "^[^%]+")[0]
        weights[F.name] = finalWeightRow

        details["weights"] = weights
    }


}

enum class Category(val displayName: String) {
    KU("Knowledge / Understanding"),
    T("Thinking"),
    C("Communication"),
    A("Application"),
    O("Other"),
    F("Final / Culminating")
}

enum class Weights(val displayName: String) {
    W("Weighting"),
    CW("Course Weighting"),
    SA("Student Achievement")
}

fun getAssignmentTemplate(): JSONObject {
    val template = JSONObject()
    template["name"] = ""
    template["time"] = ""
    enumValues<Category>().forEach {
        val categoryDetailTemplate = JSONObject()
        categoryDetailTemplate["available"] = false
        categoryDetailTemplate["get"] = 0
        categoryDetailTemplate["total"] = 0
        categoryDetailTemplate["weight"] = 0

        template[it.name] = categoryDetailTemplate
    }

    return template
}
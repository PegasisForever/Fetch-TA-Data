package webpage

import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTable
import find
import models.*
import models.Category.F
import java.time.ZonedDateTime

class DetailPage(val htmlPage: HtmlPage, val courseCode: String,val time: ZonedDateTime?=null) {
    val assignments = ArrayList<Assignment>()
    val weightTable = WeightTable()

    init {
        val detailTable = htmlPage.getElementsByTagName("table")[1] as HtmlTable

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
            val row = detailTable.getRow(rowI)
            val assignment = Assignment()

            assignment.name = row.getCell(0).asText()
            if (time!=null){
                assignment.time=time
            }

            val smallMarkCategoryAdded = ArrayList<Category>()
            for (cellI in 1 until row.cells.size) {
                val cell = row.getCell(cellI)
                val category = categoryOfEachColumn[cellI - 1]
                val smallMark = SmallMark(category)

                val smallMarkText = cell.asText()

                if (smallMarkText!=""){
                    smallMark.available = true
                    smallMark.get = if(find(smallMarkText, "^[^ ]+(?= / )",true)[0]!=""){
                        find(smallMarkText, "^[^ ]+(?= / )")[0].toDouble()
                    }else{
                        smallMark.finished=false
                        0.0
                    }
                    smallMark.total = find(smallMarkText, "(?<=/ )[^ ]+(?= = )")[0].toDouble()
                    smallMark.weight = if (smallMarkText.indexOf("no weight") != -1) {
                        0.0
                    } else {
                        find(smallMarkText, "(?<=weight=)[^ ]+$")[0].toDouble()
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
        }

        val weightsTable =
            htmlPage.getByXPath<HtmlTable>("//table[@border='1'][@cellpadding='3'][@cellspacing='0'][not(@width)]")[0]

        for (rowI in 1..5) {
            val row = weightsTable.getRow(rowI)
            val category=CategoryFrom(row.getCell(0).asText())

            val weight = Weight(category)
            weight.W= find(row.getCell(1).asText(), "^[^%]+")[0].toDouble()
            weight.CW = find(row.getCell(2).asText(), "^[^%]+")[0].toDouble()
            weight.SA = find(row.getCell(3).asText(), "^[^%]+")[0].toDouble()

            weightTable.weightsList.add(weight)
        }
        val finalRow = weightsTable.getRow(6)
        val finalWeight = Weight(F)
        finalWeight.CW = find(finalRow.getCell(1).asText(), "^[^%]+")[0].toDouble()
        finalWeight.SA = find(finalRow.getCell(2).asText(), "^[^%]+")[0].toDouble()
        weightTable.weightsList.add(finalWeight)
    }


}

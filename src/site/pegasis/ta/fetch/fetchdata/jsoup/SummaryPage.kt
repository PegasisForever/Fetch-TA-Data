package site.pegasis.ta.fetch.fetchdata.jsoup

import site.pegasis.ta.fetch.models.*
import site.pegasis.ta.fetch.tools.*
import java.time.LocalDate
import java.time.ZonedDateTime

class SummaryPage(private val session: JsoupSession, private val timing: Timing = Timing()) {
    val courses = CourseList()
    private val detailLinkMap = HashMap<Course, String>()

    init {
        timing("parse summary page") {
            val summaryTable = session.currentPage!!.getElementsByTag("table")
                .find { it.attr("width") == "85%" }!!
            val rows = summaryTable.getElementsByTag("tr")
            for (i in 1 until rows.size) {
                try {
                    val row = rows[i]
                    val cells = row.getElementsByTag("td")
                    val course = Course()

                    val classText = cells[0].wholeText().replace(Regex("\\t+"), " ").replace("\r", "").trim()
                    course.code = findFirst(classText, "[A-Z\\d\\.]{6}-[\\d]{2}")
                    course.name = findFirst(classText, "(?<= : )[^\\n]+(?= )")
                    course.block = findFirst(classText, "(?<=Block: )[^ ]+")
                    course.room = findFirst(classText, "(?<=rm\\. ).+$")
                    with(course) {
                        if (code == null && name == null && block == null && room == null && classText.isNotBlank()) {
                            course.name = classText
                        }
                    }

                    val timeText = cells[1].text()
                    val times = find(timeText, "\\d+-\\d+-\\d+")
                    if (times?.size == 2) noThrow {
                        course.startTime = LocalDate.parse(times[0])
                        course.endTime = LocalDate.parse(times[1])
                    }

                    if (cells.size == 3) {
                        noThrow {
                            val markText = cells[2].getElementsByTag("a")[0].text()
                            val currentMarkText = find(markText, "(?<=current mark = )[^%]+")?.get(0)
                            val levelMarkText = find(markText, "(?<=Level ).*")?.get(0)
                            val isClickHere = markText == "Click Here"
                            course.overallMark = when {
                                currentMarkText != null -> OverallMark(currentMarkText.toDouble())
                                levelMarkText != null -> OverallMark(levelMarkText)
                                isClickHere -> OverallMark()
                                else -> null
                            }
                        }

                        course.extraMarks = ExtraMarks()
                        cells[2].getElementsByTag("span").forEach { span ->
                            noThrow {
                                val extraMarkText = span.text()
                                val (name, mark) = extraMarkText.split(": ")
                                if (mark.matches("[\\d\\.]+%")) {
                                    course.extraMarks!!.add(ExtraMark(
                                        name.capitalizeWord(),
                                        OverallMark(mark.substring(0, mark.lastIndex).toDouble())
                                    ))
                                } else if (mark.indexOf("No Credit") != -1) {
                                    course.noCredit = true
                                }
                            }
                        }
                    }

                    courses.add(course)
                    if (course.overallMark != null) {
                        detailLinkMap[course] = "https://ta.yrdsb.ca/live/students/" + cells[2].getElementsByTag("a")[0].attr("href")
                    }
                } catch (e: Exception) {
                    logError("Cannot parse summary row #$i", e)
                }
            }
        }

    }

    private suspend fun gotoDetailPage(course: Course, time: ZonedDateTime): DetailPage {
        val link = detailLinkMap[course]!!
        timing("get detail page ${course.code}") {
            session.get(link)
        }

        course.id = findFirst(link, "(?<=subject_id=)\\d+")!!.toInt()
        return DetailPage(session, course.code, time, timing)
    }

    suspend fun fillDetails(doCalculation: Boolean = true): SummaryPage {
        val currentTime = ZonedDateTime.now(defaultZoneID)
        courses
            .filter { it.overallMark != null }
            .forEach { course ->
                val detailPage = gotoDetailPage(course, currentTime)
                course.assignments = detailPage.assignments
                course.weightTable = detailPage.weightTable
                if (doCalculation) course.calculate()
            }

        return this
    }

    suspend fun closeSession(): SummaryPage {
        session.close()
        return this
    }
}

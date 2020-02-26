package site.pegasis.ta.fetch.webpage.chrome

import org.openqa.selenium.By
import site.pegasis.ta.fetch.*
import site.pegasis.ta.fetch.chromepool.ChromeDriverWrapper
import site.pegasis.ta.fetch.models.Course
import site.pegasis.ta.fetch.models.CourseList
import site.pegasis.ta.fetch.models.OverallMark
import site.pegasis.ta.fetch.models.Timing
import java.time.LocalDate
import java.time.ZonedDateTime


class SummaryPage(private val webDriver: ChromeDriverWrapper, private val timing: Timing = Timing()) {
    val courses = CourseList()
    private val detailPages = ArrayList<DetailPage>()
    private val detailHrefMap = HashMap<Course, String>()

    init {
        timing("parse summary page") {
            val summaryTable = webDriver.driver.findElementsByTagName("table")[1]
            val rows = summaryTable.findElements(By.tagName("tr"))
            for (i in 1 until rows.size) {
                try {
                    val row = rows[i]
                    val cells = row.findElements(By.tagName("td"))
                    val course = Course()

                    val classText = cells[0].text.replace("\n"," \n ")
                    course.code = findFirst(classText, "[A-Z\\d]{6}-[\\d]{2}")
                    course.name = findFirst(classText, "(?<= : )[^\\n]+(?= )")
                    course.block = findFirst(classText, "(?<=Block: )\\d")
                    course.room = findFirst(classText, "(?<=rm\\. )\\d+")
                    with(course) {
                        if (code == null && name == null && block == null && room == null && classText.isNotBlank()) {
                            course.name = classText
                        }
                    }

                    val timeText = cells[1].text
                    val times = find(timeText, "\\d+-\\d+-\\d+")
                    if (times?.size == 2) noThrow {
                        course.startTime = LocalDate.parse(times[0])
                        course.endTime = LocalDate.parse(times[1])
                    }

                    if (cells.size == 3) {
                        val markText = cells[2].text
                        val currentMarkText = find(markText, "(?<=current mark = )[^%]+")?.get(0)
                        val levelMarkText = find(markText, "(?<=Level ).*")?.get(0)
                        if (currentMarkText != null) {
                            course.overallMark = OverallMark(currentMarkText.toDouble())
                        } else if (levelMarkText != null) {
                            course.overallMark = OverallMark(levelMarkText)
                        }
                    }

                    courses.add(course)
                    if (course.overallMark != null) {
                        detailHrefMap[course] = cells[2].findElement(By.tagName("a")).getAttribute("href")
                    }

                } catch (e: Exception) {
                    log(LogLevel.ERROR, "Cannot parse summary row #$i", e)
                }
            }
        }
    }

    fun gotoDetailPage(course: Course, time: ZonedDateTime): DetailPage {
        timing("get detail page ${course.code}") {
            webDriver.get(detailHrefMap[course]!!)
        }
        noThrow {
            course.id = findFirst(webDriver.driver.currentUrl, "(?<=subject_id=)\\d+")!!.toInt()
        }

        val detailPage = DetailPage(webDriver, course.code, time, timing)
        return detailPage
    }

    fun fillDetails(doCalculation: Boolean = true): SummaryPage {
        val currentTime = ZonedDateTime.now(torontoZoneID)
        for (i in 0 until courses.size) {
            val course = courses[i]
            if (course.overallMark != null) {
                val detailPage = gotoDetailPage(course, currentTime)
                detailPages.add(detailPage)
                course.assignments = detailPage.assignments
                course.weightTable = detailPage.weightTable
                if (doCalculation) course.calculate()
            }
        }

        webDriver.finished()
        return this
    }
}
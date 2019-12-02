package modes.server.tests

import jsonParser
import models.CourseAdded
import models.CourseList
import models.CourseRemoved
import modes.server.parsers.toCourseList
import modes.server.serializers.serialize
import modes.server.timeline.compareCourses
import org.junit.Test
import kotlin.test.assertEquals

class CourseListComparerTest {
    val course_4_a_json =
        """{"data":[{"start_time":"2019-09-03","code":"AVI2O1-01","assignments":[{"feedback":null,"name":"Popcorn Drawing","KU":{"total":10.0,"get":9.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Cups and Ribbon","KU":{"total":10.0,"get":8.5,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Bones 1","KU":{"total":10.0,"get":8.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Bones 2","KU":{"total":10.0,"get":8.5,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":9.0,"available":true,"weight":100.0,"finished":true},"C":{"total":10.0,"get":8.0,"available":true,"weight":100.0,"finished":true},"T":{"total":10.0,"get":7.5,"available":true,"weight":100.0,"finished":true},"name":"Still Life Drawing with a Shoe","KU":{"total":10.0,"get":8.5,"available":true,"weight":100.0,"finished":true},"time":null},{"feedback":"KU: 1-2-3-4 Point Perspective Boxes on regular paper TH: accurate 1-2-3-4 Point Perspective designs on good paper COMM: colour scheme of watercolour paints on good paper APP: Neatness, no smudges or unwanted paint blotches.","A":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"C":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"T":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"name":"Perspective Tasks","KU":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"time":null},{"feedback":null,"name":"Colour Mixing Chart","KU":{"total":1.0,"get":1.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"12 Watercolour Techniques","KU":{"total":12.0,"get":12.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"A":{"total":6.0,"get":6.0,"available":true,"weight":10.0,"finished":true},"name":"Watercolour Techniques Painting","time":null}],"cached":false,"weight_table":{"A":{"CW":21.0,"W":30.0,"SA":92.85714285714286},"C":{"CW":14.0,"W":20.0,"SA":84.61538461538461},"T":{"CW":17.5,"W":25.0,"SA":80.76923076923077},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":17.5,"W":25.0,"SA":88.94736842105263},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"Visual Arts","block":"1","room":"319","overall_mark":87.20936957779064},{"start_time":"2019-09-03","code":"MCR3U1-04","assignments":[{"feedback":null,"name":"Unit 1 Algebraic Expressions Quest","KU":{"total":19.0,"get":19.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":13.0,"get":13.0,"available":true,"weight":3.0,"finished":true},"C":{"total":5.0,"get":5.0,"available":true,"weight":3.0,"finished":true},"name":"Unit 1 Algebraic Expressions Test","KU":{"total":25.0,"get":25.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"name":"Unit 2 Functions Quest","KU":{"total":21.0,"get":19.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":14.0,"get":13.5,"available":true,"weight":3.0,"finished":true},"C":{"total":8.0,"get":7.0,"available":true,"weight":3.0,"finished":true},"T":{"total":8.0,"get":8.0,"available":true,"weight":3.0,"finished":true},"name":"Unit 2 Functions Test","KU":{"total":14.0,"get":13.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":11.0,"get":10.5,"available":true,"weight":2.0,"finished":true},"name":"Unit 3 Quadratics Quest","KU":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":9.0,"get":9.0,"available":true,"weight":3.0,"finished":true},"C":{"total":6.0,"get":5.0,"available":true,"weight":3.0,"finished":true},"T":{"total":9.0,"get":9.0,"available":true,"weight":3.0,"finished":true},"name":"Quadratic Test","KU":{"total":12.0,"get":12.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":10.0,"available":true,"weight":2.0,"finished":true},"name":"Unit 4 Trig Ratios Quest","KU":{"total":12.0,"get":11.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":8.0,"get":7.5,"available":true,"weight":3.0,"finished":true},"C":{"total":7.0,"get":7.0,"available":true,"weight":3.0,"finished":true},"T":{"total":8.0,"get":8.0,"available":true,"weight":3.0,"finished":true},"name":"Trig Test","KU":{"total":19.0,"get":18.5,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"name":"Sinusoidal Functions Quest","KU":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":20.0,"W":28.6,"SA":97.85804473304474},"C":{"CW":10.0,"W":14.3,"SA":92.70833333333334},"T":{"CW":15.0,"W":21.4,"SA":100.0},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":25.0,"W":35.7,"SA":97.04374572795625},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"Functions and Relations","block":"2","room":"233","overall_mark":97.28930968519784},{"start_time":"2019-09-03","code":"FSF1O1-03","assignments":[{"feedback":null,"C":{"total":20.0,"get":20.0,"available":true,"weight":2.0,"finished":true},"T":{"total":18.0,"get":16.0,"available":true,"weight":2.0,"finished":true},"name":"Test- Bonjour","KU":{"total":20.0,"get":18.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"C":{"total":15.0,"get":11.25,"available":true,"weight":1.0,"finished":true},"T":{"total":5.0,"get":4.8,"available":true,"weight":1.0,"finished":true},"name":"Alphabet Dictionary","time":null},{"feedback":null,"A":{"total":10.0,"get":7.0,"available":true,"weight":2.0,"finished":true},"C":{"total":15.0,"get":13.5,"available":true,"weight":2.0,"finished":true},"T":{"total":24.0,"get":17.0,"available":true,"weight":2.0,"finished":true},"name":"Test Unit 2","KU":{"total":15.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":0.0,"available":true,"weight":1.0,"finished":true},"C":{"total":20.0,"get":18.5,"available":true,"weight":1.0,"finished":true},"T":{"total":10.0,"get":10.0,"available":true,"weight":1.0,"finished":true},"name":"Mon arbre Genealogique","KU":{"total":5.0,"get":4.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"C":{"total":18.0,"get":13.5,"available":true,"weight":2.0,"finished":true},"T":{"total":5.0,"get":3.0,"available":true,"weight":2.0,"finished":true},"name":"Test Bon Appetit","KU":{"total":18.0,"get":16.0,"available":true,"weight":2.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":15.0,"W":21.4,"SA":46.666666666666664},"C":{"CW":20.0,"W":28.6,"SA":87.1875},"T":{"CW":20.0,"W":28.6,"SA":79.43055555555556},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":15.0,"W":21.4,"SA":87.30158730158729},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"French","block":"4","room":"318","overall_mark":76.32197023809523},{"start_time":"2019-09-03","code":"HFC3M1-01","assignments":[{"feedback":null,"A":{"total":9.0,"get":8.0,"available":true,"weight":17.0,"finished":true},"C":{"total":12.0,"get":12.0,"available":true,"weight":17.0,"finished":true},"T":{"total":12.0,"get":12.0,"available":true,"weight":17.0,"finished":true},"name":"SAFETY TEST","KU":{"total":22.0,"get":21.0,"available":true,"weight":17.0,"finished":true},"time":null},{"feedback":null,"A":{"total":20.0,"get":18.0,"available":true,"weight":17.0,"finished":true},"C":{"total":10.0,"get":9.0,"available":true,"weight":17.0,"finished":true},"T":{"total":10.0,"get":7.0,"available":true,"weight":17.0,"finished":true},"name":"FBI ASSIGNMENT","KU":{"total":10.0,"get":8.0,"available":true,"weight":17.0,"finished":true},"time":null},{"feedback":null,"A":{"total":9.0,"get":9.0,"available":true,"weight":17.0,"finished":true},"C":{"total":12.0,"get":9.0,"available":true,"weight":17.0,"finished":true},"T":{"total":14.0,"get":13.0,"available":true,"weight":17.0,"finished":true},"name":"UNIT ONE TEST","KU":{"total":29.0,"get":17.0,"available":true,"weight":17.0,"finished":true},"time":null},{"feedback":null,"A":{"total":9.0,"get":0.0,"available":true,"weight":17.0,"finished":false},"C":{"total":13.0,"get":0.0,"available":true,"weight":17.0,"finished":false},"T":{"total":10.0,"get":0.0,"available":true,"weight":17.0,"finished":false},"name":"UNIT TWO TEST","KU":{"total":26.0,"get":0.0,"available":true,"weight":17.0,"finished":false},"time":null}],"cached":false,"weight_table":{"A":{"CW":17.5,"W":25.0,"SA":92.96296296296296},"C":{"CW":17.5,"W":25.0,"SA":88.33333333333333},"T":{"CW":17.5,"W":25.0,"SA":87.61904761904762},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":17.5,"W":25.0,"SA":78.02507836990596},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":null,"block":"5","room":"200","overall_mark":86.73510557131246}],"version":4}"""
    val course_4_a = jsonParser.parse(course_4_a_json).toCourseList()
    val course_4_a_1_unavailable_json =
        """{"data":[{"start_time":"2019-09-03","code":"AVI2O1-01","assignments":[{"feedback":null,"name":"Popcorn Drawing","KU":{"total":10.0,"get":9.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Cups and Ribbon","KU":{"total":10.0,"get":8.5,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Bones 1","KU":{"total":10.0,"get":8.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Bones 2","KU":{"total":10.0,"get":8.5,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":9.0,"available":true,"weight":100.0,"finished":true},"C":{"total":10.0,"get":8.0,"available":true,"weight":100.0,"finished":true},"T":{"total":10.0,"get":7.5,"available":true,"weight":100.0,"finished":true},"name":"Still Life Drawing with a Shoe","KU":{"total":10.0,"get":8.5,"available":true,"weight":100.0,"finished":true},"time":null},{"feedback":"KU: 1-2-3-4 Point Perspective Boxes on regular paper TH: accurate 1-2-3-4 Point Perspective designs on good paper COMM: colour scheme of watercolour paints on good paper APP: Neatness, no smudges or unwanted paint blotches.","A":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"C":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"T":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"name":"Perspective Tasks","KU":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"time":null},{"feedback":null,"name":"Colour Mixing Chart","KU":{"total":1.0,"get":1.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"12 Watercolour Techniques","KU":{"total":12.0,"get":12.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"A":{"total":6.0,"get":6.0,"available":true,"weight":10.0,"finished":true},"name":"Watercolour Techniques Painting","time":null}],"cached":false,"weight_table":{"A":{"CW":21.0,"W":30.0,"SA":92.85714285714286},"C":{"CW":14.0,"W":20.0,"SA":84.61538461538461},"T":{"CW":17.5,"W":25.0,"SA":80.76923076923077},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":17.5,"W":25.0,"SA":88.94736842105263},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"Visual Arts","block":"1","room":"319","overall_mark":87.20936957779064},{"start_time":"2019-09-03","code":"MCR3U1-04","assignments":[{"feedback":null,"name":"Unit 1 Algebraic Expressions Quest","KU":{"total":19.0,"get":19.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":13.0,"get":13.0,"available":true,"weight":3.0,"finished":true},"C":{"total":5.0,"get":5.0,"available":true,"weight":3.0,"finished":true},"name":"Unit 1 Algebraic Expressions Test","KU":{"total":25.0,"get":25.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"name":"Unit 2 Functions Quest","KU":{"total":21.0,"get":19.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":14.0,"get":13.5,"available":true,"weight":3.0,"finished":true},"C":{"total":8.0,"get":7.0,"available":true,"weight":3.0,"finished":true},"T":{"total":8.0,"get":8.0,"available":true,"weight":3.0,"finished":true},"name":"Unit 2 Functions Test","KU":{"total":14.0,"get":13.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":11.0,"get":10.5,"available":true,"weight":2.0,"finished":true},"name":"Unit 3 Quadratics Quest","KU":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":9.0,"get":9.0,"available":true,"weight":3.0,"finished":true},"C":{"total":6.0,"get":5.0,"available":true,"weight":3.0,"finished":true},"T":{"total":9.0,"get":9.0,"available":true,"weight":3.0,"finished":true},"name":"Quadratic Test","KU":{"total":12.0,"get":12.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":10.0,"available":true,"weight":2.0,"finished":true},"name":"Unit 4 Trig Ratios Quest","KU":{"total":12.0,"get":11.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":8.0,"get":7.5,"available":true,"weight":3.0,"finished":true},"C":{"total":7.0,"get":7.0,"available":true,"weight":3.0,"finished":true},"T":{"total":8.0,"get":8.0,"available":true,"weight":3.0,"finished":true},"name":"Trig Test","KU":{"total":19.0,"get":18.5,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"name":"Sinusoidal Functions Quest","KU":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":20.0,"W":28.6,"SA":97.85804473304474},"C":{"CW":10.0,"W":14.3,"SA":92.70833333333334},"T":{"CW":15.0,"W":21.4,"SA":100.0},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":25.0,"W":35.7,"SA":97.04374572795625},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"Functions and Relations","block":"2","room":"233","overall_mark":97.28930968519784},{"start_time":"2019-09-03","code":"FSF1O1-03","assignments":[{"feedback":null,"C":{"total":20.0,"get":20.0,"available":true,"weight":2.0,"finished":true},"T":{"total":18.0,"get":16.0,"available":true,"weight":2.0,"finished":true},"name":"Test- Bonjour","KU":{"total":20.0,"get":18.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"C":{"total":15.0,"get":11.25,"available":true,"weight":1.0,"finished":true},"T":{"total":5.0,"get":4.8,"available":true,"weight":1.0,"finished":true},"name":"Alphabet Dictionary","time":null},{"feedback":null,"A":{"total":10.0,"get":7.0,"available":true,"weight":2.0,"finished":true},"C":{"total":15.0,"get":13.5,"available":true,"weight":2.0,"finished":true},"T":{"total":24.0,"get":17.0,"available":true,"weight":2.0,"finished":true},"name":"Test Unit 2","KU":{"total":15.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":0.0,"available":true,"weight":1.0,"finished":true},"C":{"total":20.0,"get":18.5,"available":true,"weight":1.0,"finished":true},"T":{"total":10.0,"get":10.0,"available":true,"weight":1.0,"finished":true},"name":"Mon arbre Genealogique","KU":{"total":5.0,"get":4.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"C":{"total":18.0,"get":13.5,"available":true,"weight":2.0,"finished":true},"T":{"total":5.0,"get":3.0,"available":true,"weight":2.0,"finished":true},"name":"Test Bon Appetit","KU":{"total":18.0,"get":16.0,"available":true,"weight":2.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":15.0,"W":21.4,"SA":46.666666666666664},"C":{"CW":20.0,"W":28.6,"SA":87.1875},"T":{"CW":20.0,"W":28.6,"SA":79.43055555555556},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":15.0,"W":21.4,"SA":87.30158730158729},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"French","block":"4","room":"318","overall_mark":76.32197023809523},{"start_time":"2019-09-03","code":"HFC3M1-01","assignments":[],"cached":false,"weight_table":null,"end_time":"2020-01-31","name":null,"block":"5","room":"200","overall_mark":null}],"version":4}"""
    val course_4_a_1_unavailable = jsonParser.parse(course_4_a_1_unavailable_json).toCourseList()
    val course_4_b_json =
        """{"data":[{"start_time":"2019-09-03","code":"MHF4U1-05","assignments":[{"feedback":null,"name":"functions quiz","KU":{"total":13.0,"get":2.0,"available":true,"weight":0.0,"finished":true},"time":null},{"feedback":null,"A":{"total":14.0,"get":11.5,"available":true,"weight":1.0,"finished":true},"C":{"total":5.0,"get":3.0,"available":true,"weight":1.0,"finished":true},"T":{"total":7.0,"get":4.5,"available":true,"weight":1.0,"finished":true},"name":"Funtion Properties test","KU":{"total":12.0,"get":10.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":6.0,"available":true,"weight":1.0,"finished":true},"C":{"total":8.0,"get":8.0,"available":true,"weight":1.0,"finished":true},"T":{"total":12.0,"get":10.0,"available":true,"weight":1.0,"finished":true},"name":"Polynomials test","KU":{"total":15.0,"get":13.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"A":{"total":12.0,"get":12.0,"available":true,"weight":1.0,"finished":true},"C":{"total":10.0,"get":10.0,"available":true,"weight":1.0,"finished":true},"T":{"total":11.0,"get":10.5,"available":true,"weight":1.0,"finished":true},"name":"rational functions test","KU":{"total":15.0,"get":12.5,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"A":{"total":16.0,"get":11.5,"available":true,"weight":1.0,"finished":true},"C":{"total":10.0,"get":8.5,"available":true,"weight":1.0,"finished":true},"T":{"total":13.0,"get":6.0,"available":true,"weight":1.0,"finished":true},"name":"trig test 1","KU":{"total":11.0,"get":9.5,"available":true,"weight":1.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":20.0,"W":28.6,"SA":78.50446428571428},"C":{"CW":10.0,"W":14.3,"SA":86.25},"T":{"CW":20.0,"W":28.6,"SA":72.30685980685982},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":20.0,"W":28.6,"SA":84.92424242424244},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"Advanced Functions","block":"1","room":"325","overall_mark":79.67444757623329},{"start_time":"2019-09-03","code":"ENG4U1-01","assignments":[{"feedback":null,"A":{"total":20.0,"get":18.0,"available":true,"weight":1.0,"finished":true},"C":{"total":20.0,"get":18.0,"available":true,"weight":1.0,"finished":true},"T":{"total":20.0,"get":18.0,"available":true,"weight":1.0,"finished":true},"name":"Literary Criticism Unit Test","KU":{"total":20.0,"get":18.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"A":{"total":100.0,"get":80.0,"available":true,"weight":1.0,"finished":true},"C":{"total":100.0,"get":84.0,"available":true,"weight":1.0,"finished":true},"T":{"total":100.0,"get":82.0,"available":true,"weight":1.0,"finished":true},"name":"Literary Criticism Workshop","KU":{"total":100.0,"get":87.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"A":{"total":100.0,"get":85.0,"available":true,"weight":0.5,"finished":true},"C":{"total":100.0,"get":81.0,"available":true,"weight":0.5,"finished":true},"T":{"total":100.0,"get":82.0,"available":true,"weight":0.5,"finished":true},"name":"Poetry Test","KU":{"total":100.0,"get":86.0,"available":true,"weight":0.5,"finished":true},"time":null},{"feedback":null,"A":{"total":100.0,"get":83.0,"available":true,"weight":0.5,"finished":true},"C":{"total":100.0,"get":80.0,"available":true,"weight":0.5,"finished":true},"T":{"total":100.0,"get":88.0,"available":true,"weight":0.5,"finished":true},"name":"Poetry Visual","KU":{"total":100.0,"get":85.0,"available":true,"weight":0.5,"finished":true},"time":null},{"feedback":null,"A":{"total":21.0,"get":21.0,"available":true,"weight":1.0,"finished":true},"C":{"total":21.0,"get":21.0,"available":true,"weight":1.0,"finished":true},"T":{"total":21.0,"get":21.0,"available":true,"weight":1.0,"finished":true},"name":"Prose Analysis Unit Test","KU":{"total":21.0,"get":21.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"A":{"total":100.0,"get":78.0,"available":true,"weight":1.0,"finished":true},"C":{"total":100.0,"get":78.0,"available":true,"weight":1.0,"finished":true},"T":{"total":100.0,"get":78.0,"available":true,"weight":1.0,"finished":true},"name":"The Great Gatsby Seminar Discussions","KU":{"total":100.0,"get":78.0,"available":true,"weight":1.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":17.5,"W":25.0,"SA":86.4},"C":{"CW":17.5,"W":25.0,"SA":86.5},"T":{"CW":17.5,"W":25.0,"SA":86.99999999999999},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":17.5,"W":25.0,"SA":88.1},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"English","block":"2","room":"202","overall_mark":87.0},{"start_time":"2019-09-03","code":null,"assignments":[],"cached":false,"weight_table":null,"end_time":"2020-01-31","name":null,"block":"3","room":null,"overall_mark":null},{"start_time":"2019-09-03","code":"IDC4U2-01","assignments":[{"feedback":null,"A":{"total":20.0,"get":18.0,"available":true,"weight":1.0,"finished":true},"T":{"total":10.0,"get":9.0,"available":true,"weight":1.0,"finished":true},"name":"Test","KU":{"total":38.0,"get":33.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"C":{"total":100.0,"get":90.0,"available":true,"weight":1.0,"finished":true},"T":{"total":100.0,"get":100.0,"available":true,"weight":1.0,"finished":true},"name":"Create a Website\/Sitemap","time":null},{"feedback":null,"A":{"total":20.0,"get":0.0,"available":true,"weight":1.0,"finished":false},"C":{"total":40.0,"get":0.0,"available":true,"weight":1.0,"finished":false},"name":"Celebrity Presentation Assignment","KU":{"total":20.0,"get":0.0,"available":true,"weight":1.0,"finished":false},"time":null},{"feedback":null,"A":{"total":20.0,"get":20.0,"available":true,"weight":1.0,"finished":true},"name":"Unit 2 Test","KU":{"total":20.0,"get":20.0,"available":true,"weight":1.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":17.5,"W":25.0,"SA":95.0},"C":{"CW":17.5,"W":25.0,"SA":90.0},"T":{"CW":17.5,"W":25.0,"SA":95.0},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":17.5,"W":25.0,"SA":93.42105263157895},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"Interdisciplinary Studies","block":"4","room":"302","overall_mark":93.35526315789474}],"version":4}"""
    val course_4_b = jsonParser.parse(course_4_b_json).toCourseList()

    val course_3_a_json =
        """{"data":[{"start_time":"2019-09-03","code":"AVI2O1-01","assignments":[{"feedback":null,"name":"Popcorn Drawing","KU":{"total":10.0,"get":9.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Cups and Ribbon","KU":{"total":10.0,"get":8.5,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Bones 1","KU":{"total":10.0,"get":8.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"Bones 2","KU":{"total":10.0,"get":8.5,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":9.0,"available":true,"weight":100.0,"finished":true},"C":{"total":10.0,"get":8.0,"available":true,"weight":100.0,"finished":true},"T":{"total":10.0,"get":7.5,"available":true,"weight":100.0,"finished":true},"name":"Still Life Drawing with a Shoe","KU":{"total":10.0,"get":8.5,"available":true,"weight":100.0,"finished":true},"time":null},{"feedback":"KU: 1-2-3-4 Point Perspective Boxes on regular paper TH: accurate 1-2-3-4 Point Perspective designs on good paper COMM: colour scheme of watercolour paints on good paper APP: Neatness, no smudges or unwanted paint blotches.","A":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"C":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"T":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"name":"Perspective Tasks","KU":{"total":10.0,"get":10.0,"available":true,"weight":30.0,"finished":true},"time":null},{"feedback":null,"name":"Colour Mixing Chart","KU":{"total":1.0,"get":1.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"name":"12 Watercolour Techniques","KU":{"total":12.0,"get":12.0,"available":true,"weight":10.0,"finished":true},"time":null},{"feedback":null,"A":{"total":6.0,"get":6.0,"available":true,"weight":10.0,"finished":true},"name":"Watercolour Techniques Painting","time":null}],"cached":false,"weight_table":{"A":{"CW":21.0,"W":30.0,"SA":92.85714285714286},"C":{"CW":14.0,"W":20.0,"SA":84.61538461538461},"T":{"CW":17.5,"W":25.0,"SA":80.76923076923077},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":17.5,"W":25.0,"SA":88.94736842105263},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"Visual Arts","block":"1","room":"319","overall_mark":87.20936957779064},{"start_time":"2019-09-03","code":"MCR3U1-04","assignments":[{"feedback":null,"name":"Unit 1 Algebraic Expressions Quest","KU":{"total":19.0,"get":19.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":13.0,"get":13.0,"available":true,"weight":3.0,"finished":true},"C":{"total":5.0,"get":5.0,"available":true,"weight":3.0,"finished":true},"name":"Unit 1 Algebraic Expressions Test","KU":{"total":25.0,"get":25.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"name":"Unit 2 Functions Quest","KU":{"total":21.0,"get":19.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":14.0,"get":13.5,"available":true,"weight":3.0,"finished":true},"C":{"total":8.0,"get":7.0,"available":true,"weight":3.0,"finished":true},"T":{"total":8.0,"get":8.0,"available":true,"weight":3.0,"finished":true},"name":"Unit 2 Functions Test","KU":{"total":14.0,"get":13.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":11.0,"get":10.5,"available":true,"weight":2.0,"finished":true},"name":"Unit 3 Quadratics Quest","KU":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":9.0,"get":9.0,"available":true,"weight":3.0,"finished":true},"C":{"total":6.0,"get":5.0,"available":true,"weight":3.0,"finished":true},"T":{"total":9.0,"get":9.0,"available":true,"weight":3.0,"finished":true},"name":"Quadratic Test","KU":{"total":12.0,"get":12.0,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":10.0,"available":true,"weight":2.0,"finished":true},"name":"Unit 4 Trig Ratios Quest","KU":{"total":12.0,"get":11.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":8.0,"get":7.5,"available":true,"weight":3.0,"finished":true},"C":{"total":7.0,"get":7.0,"available":true,"weight":3.0,"finished":true},"T":{"total":8.0,"get":8.0,"available":true,"weight":3.0,"finished":true},"name":"Trig Test","KU":{"total":19.0,"get":18.5,"available":true,"weight":3.0,"finished":true},"time":null},{"feedback":null,"name":"Sinusoidal Functions Quest","KU":{"total":13.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":20.0,"W":28.6,"SA":97.85804473304474},"C":{"CW":10.0,"W":14.3,"SA":92.70833333333334},"T":{"CW":15.0,"W":21.4,"SA":100.0},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":25.0,"W":35.7,"SA":97.04374572795625},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"Functions and Relations","block":"2","room":"233","overall_mark":97.28930968519784},{"start_time":"2019-09-03","code":"FSF1O1-03","assignments":[{"feedback":null,"C":{"total":20.0,"get":20.0,"available":true,"weight":2.0,"finished":true},"T":{"total":18.0,"get":16.0,"available":true,"weight":2.0,"finished":true},"name":"Test- Bonjour","KU":{"total":20.0,"get":18.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"C":{"total":15.0,"get":11.25,"available":true,"weight":1.0,"finished":true},"T":{"total":5.0,"get":4.8,"available":true,"weight":1.0,"finished":true},"name":"Alphabet Dictionary","time":null},{"feedback":null,"A":{"total":10.0,"get":7.0,"available":true,"weight":2.0,"finished":true},"C":{"total":15.0,"get":13.5,"available":true,"weight":2.0,"finished":true},"T":{"total":24.0,"get":17.0,"available":true,"weight":2.0,"finished":true},"name":"Test Unit 2","KU":{"total":15.0,"get":13.0,"available":true,"weight":2.0,"finished":true},"time":null},{"feedback":null,"A":{"total":10.0,"get":0.0,"available":true,"weight":1.0,"finished":true},"C":{"total":20.0,"get":18.5,"available":true,"weight":1.0,"finished":true},"T":{"total":10.0,"get":10.0,"available":true,"weight":1.0,"finished":true},"name":"Mon arbre Genealogique","KU":{"total":5.0,"get":4.0,"available":true,"weight":1.0,"finished":true},"time":null},{"feedback":null,"C":{"total":18.0,"get":13.5,"available":true,"weight":2.0,"finished":true},"T":{"total":5.0,"get":3.0,"available":true,"weight":2.0,"finished":true},"name":"Test Bon Appetit","KU":{"total":18.0,"get":16.0,"available":true,"weight":2.0,"finished":true},"time":null}],"cached":false,"weight_table":{"A":{"CW":15.0,"W":21.4,"SA":46.666666666666664},"C":{"CW":20.0,"W":28.6,"SA":87.1875},"T":{"CW":20.0,"W":28.6,"SA":79.43055555555556},"F":{"CW":30.0,"W":0.0,"SA":0.0},"KU":{"CW":15.0,"W":21.4,"SA":87.30158730158729},"O":{"CW":0.0,"W":0.0,"SA":0.0}},"end_time":"2020-01-31","name":"French","block":"4","room":"318","overall_mark":76.32197023809523}],"version":4}"""
    val course_3_a = jsonParser.parse(course_3_a_json).toCourseList()

    @Test
    fun `same courses`() {
        val result = compareCourses(course_4_a, course_4_a)
        assertEquals(course_4_a_json, result.courseList.serialize(4).toJSONString())
        assertEquals(0, result.archivedCourseList.size)
        assertEquals(0, result.updates.size)
    }

    @Test
    fun `add course`() {
        val result = compareCourses(course_3_a, course_4_a)
        assertEquals(course_4_a_json, result.courseList.serialize(4).toJSONString())
        assertEquals(0, result.archivedCourseList.size)
        assertEquals(1, result.updates.size)
        assertEquals(true, result.updates[0] is CourseAdded)
    }

    @Test
    fun `remove course`() {
        val result = compareCourses(course_4_a, course_3_a)
        assertEquals(course_3_a_json, result.courseList.serialize(4).toJSONString())
        assertEquals(1, result.archivedCourseList.size)
        assertEquals("HFC3M1-01", result.archivedCourseList[0].code)
        assertEquals(1, result.updates.size)
        assert(result.updates[0] is CourseRemoved)
    }

    @Test
    fun `swap course`() {
        val result = compareCourses(course_4_a.clone() as CourseList,
            (course_4_a.clone() as CourseList).apply {
                this[3] = course_4_b[3]
            })
        assertEquals(1, result.archivedCourseList.size)
        assert(result.updates[0] is CourseAdded)
        assert(result.updates[1] is CourseRemoved)
    }

    @Test
    fun `cache course`() {
        val result = compareCourses(course_4_a, course_4_a_1_unavailable)
        assertEquals(0, result.archivedCourseList.size)
        assertEquals(0, result.updates.size)
        assertEquals(true, result.courseList[3].cached)
        assert(result.courseList[3].assignments!!.size > 0)
        assert(result.courseList[3].weightTable != null)
        assert(result.courseList[3].overallMark != null)
    }

    @Test
    fun `complete swap`() {
        val result = compareCourses(course_4_a, course_4_b)
        assertEquals(4, result.courseList.size)
        assertEquals(4, result.archivedCourseList.size)
        assertEquals(8, result.updates.size)
    }

    @Test
    fun `clear course`() {
        val result = compareCourses(course_4_a, CourseList())
        assertEquals(0, result.courseList.size)
        assertEquals(4, result.archivedCourseList.size)
        assertEquals(4, result.updates.size)
        result.updates.forEach {
            assert(it is CourseRemoved)
        }
    }

    @Test
    fun `add all course`() {
        val result = compareCourses(CourseList(), course_4_a)
        assertEquals(4, result.courseList.size)
        assertEquals(0, result.archivedCourseList.size)
        assertEquals(4, result.updates.size)
        result.updates.forEach {
            assert(it is CourseAdded)
        }
    }
}
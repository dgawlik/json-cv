package org.dgawlik

import com.google.gson.Gson
import org.dgawlik.org.dgawlik.model.tokenize
import parseJson
import parseValue


val json1 = """
{
    
    "fullName": %{personalDetails.firstName | " " | personalDetails.lastName},
    "totalSalary": %{personalDetails.jobSalary + personalDetails.sideHustleSalary},
    "isAdult": %{personalDetails.age >= 18},
    "tax": 0.18,
    "netIncome" : %{(1.0 - tax) * totalSalary},
    "personalDetails": {
        "firstName": "John",
        "lastName": "Doe",
        "age": 30,
        "jobSalary": 1000,
        "sideHustleSalary": 500
    }
}
""".trimIndent().replace('%', '$')

fun main() {
    val json = parseJson(json1)
    val obj = json.toJsonObject()
    val hydrated = Gson().toJson(obj)
    println(hydrated)
}
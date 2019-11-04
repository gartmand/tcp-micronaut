package dev.gartman

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus

@Controller("/employee")
class EmployeeController {

    @Get("/")

    fun index(): HttpStatus {
        return HttpStatus.OK
    }
}
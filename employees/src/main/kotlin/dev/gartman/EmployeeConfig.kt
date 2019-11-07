package dev.gartman

import dev.gartman.EmployeeConfig.Companion.PREFIX
import io.micronaut.context.annotation.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties(PREFIX)
class EmployeeConfig {
    companion object {
        const val PREFIX = "employees"
    }

    @NotBlank
    lateinit var databaseName: String

    @NotBlank
    var collectionName = PREFIX

    @NotBlank
    var apiVersion = "v1"
}
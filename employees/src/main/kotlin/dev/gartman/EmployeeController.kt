package dev.gartman

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.validation.Validated
import io.reactivex.Flowable

@Controller("/\${employees.api.version}/employees")
@Validated
class EmployeeController(val config: EmployeeConfig, val mongoClient: MongoClient) {

    @Get("/")
    fun getAll(): Flowable<Employee> = Flowable.fromPublisher(getCollection().find())

    fun getCollection(): MongoCollection<Employee> = mongoClient.getDatabase(config.databaseName)
            .getCollection(config.collectionName, Employee::class.java)
}
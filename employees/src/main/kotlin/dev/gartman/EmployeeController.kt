package dev.gartman

import com.mongodb.client.model.Filters.eq
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.bson.types.ObjectId
import javax.validation.ConstraintViolationException

@Controller("/\${employees.api.version}/employees")
@Validated
class EmployeeController(val config: EmployeeConfig, val mongoClient: MongoClient) {

    @Get
    fun getAll(): Flowable<Employee> = Flowable.fromPublisher(getCollection().find())

    @Get("/{id}")
    fun getOne(id: String): Maybe<Employee> =
        Flowable.fromPublisher(getCollection().find(eq("_id", ObjectId(id)))).firstElement()

    @Post
    fun create(@Body employee: Employee): Single<Employee> {
        val insertedEmployee = employee.copy(id = ObjectId())
        return employee.id?.let {
            throw ConstraintViolationException("Attempt to POST with ID set. Did you mean to PUT?", emptySet())
        } ?: Single.fromPublisher(getCollection().insertOne(insertedEmployee)).map { insertedEmployee }
    }

    @Put("/{id}")
    fun replace(id: String, @Body employee: Employee): Maybe<Employee> =
        Flowable.fromPublisher(getCollection().findOneAndReplace(eq("_id", ObjectId(id)), employee)).firstElement()

    @Delete("/{id}")
    fun delete(id: String): Maybe<Employee> =
        Flowable.fromPublisher(getCollection().findOneAndDelete(eq("_id", ObjectId(id)))).firstElement()

    private fun getCollection(): MongoCollection<Employee> = mongoClient.getDatabase(config.databaseName)
        .getCollection(config.collectionName, Employee::class.java)
}
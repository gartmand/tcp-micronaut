package dev.gartman

import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Flowable
import org.bson.types.ObjectId
import org.reactivestreams.Subscriber
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate

object EmployeeControllerTest : Spek({


    describe("The employees controller") {
        lateinit var employeeConfig: EmployeeConfig
        lateinit var mongoClient: MongoClient
        lateinit var collection: MongoCollection<Employee>
        val exampleEmployee = Employee(ObjectId(),
                Bio("TEST", "", "TEST", LocalDate.now(), Gender.MALE, Ethnicity.OTHER, true),
                Contact("test@test.com", "5715555555",
                        Address("", "", "", "AL", "")))

        beforeEachTest {
            employeeConfig = mockk<EmployeeConfig>()
            every { employeeConfig.databaseName } returns "testdb"
            every { employeeConfig.collectionName } returns "testcol"

            mongoClient = mockk<MongoClient>()
            val database = mockk<MongoDatabase>()
            collection = mockk<MongoCollection<Employee>>()

            every { mongoClient.getDatabase(any<String>()) } returns database
            every { database.getCollection(any<String>(), Employee::class.java) } returns collection
        }

        val controller: EmployeeController by memoized { EmployeeController(employeeConfig, mongoClient) }

        describe("getting all employees") {
            beforeEachTest {
                val findPublisher = mockk<FindPublisher<Employee>>()
                val elements = Flowable.just(exampleEmployee)
                every { collection.find() } returns findPublisher
                every { findPublisher.subscribe(any<Subscriber<in Employee>>()) } answers {
                    elements.subscribe(firstArg<Subscriber<Employee>>())
                }
            }

            it("should return a Flowable of all employees in the collection") {
                assert(controller.getAll().toList().blockingGet().contains(exampleEmployee))
            }
        }
    }
})

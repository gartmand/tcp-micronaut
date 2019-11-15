package dev.gartman

import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.*
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Flowable
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.reactivestreams.Subscriber
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDate
import javax.validation.ConstraintViolationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

object EmployeeControllerTest : Spek({
    describe("The employees controller") {
        lateinit var employeeConfig: EmployeeConfig
        lateinit var mongoClient: MongoClient
        lateinit var collection: MongoCollection<Employee>
        val exampleObjectId = ObjectId()
        val exampleEmployee = Employee(
            exampleObjectId,
            Bio("TEST", "", "TEST", LocalDate.now(), Gender.MALE, Ethnicity.OTHER, true),
            Contact(
                "test@test.com", "5715555555",
                Address("", "", "", "AL", "")
            ),
            emptyList()
        )

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

        describe("getting a single employee by ID") {
            beforeEachTest {
                val foundPublisher = mockk<FindPublisher<Employee>>()
                val notFoundPublisher = mockk<FindPublisher<Employee>>()
                val elements = Flowable.just(exampleEmployee)
                val emptyFlowable = Flowable.empty<Employee>()

                every { collection.find(any<Bson>()) } answers {
                    if (firstArg<Bson>().toString() == Filters.eq("_id", exampleObjectId).toString())
                        foundPublisher
                    else
                        notFoundPublisher
                }
                every { foundPublisher.subscribe(any<Subscriber<in Employee>>()) } answers {
                    elements.subscribe(firstArg<Subscriber<Employee>>())
                }
                every { notFoundPublisher.subscribe(any<Subscriber<in Employee>>()) } answers {
                    emptyFlowable.subscribe(firstArg<Subscriber<Employee>>())
                }
            }

            it("should return a Maybe of the employee if they exist") {
                assertEquals(exampleEmployee, controller.getOne(exampleObjectId.toHexString()).blockingGet())
            }

            it("should return an empty Maybe if they don't exist") {
                assert(controller.getOne(ObjectId().toHexString()).isEmpty.blockingGet())
            }
        }

        describe("creating an employee") {
            beforeEachTest {
                every { collection.insertOne(any<Employee>()) } returns Flowable.just(Success.SUCCESS)
            }

            it("should throw a validation exception if the ID is pre-set on the employee") {
                assertFailsWith<ConstraintViolationException> {
                    controller.create(exampleEmployee)
                }
            }

            it("should return, when successful, a Single containing the persisted employee with the new ID") {
                val toCreate = exampleEmployee.copy(id = null)
                val created = controller.create(toCreate)
                assertEquals(toCreate, created.blockingGet().copy(id = null))
                assertNotNull(created.blockingGet().id)
            }
        }
    }
})

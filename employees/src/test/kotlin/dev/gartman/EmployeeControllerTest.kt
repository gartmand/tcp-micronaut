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

class EmployeeControllerTest : Spek({
    describe("The employees controller") {
        lateinit var employeeConfig: EmployeeConfig
        lateinit var mongoClient: MongoClient
        lateinit var collection: MongoCollection<Employee>
        lateinit var foundPublisher: FindPublisher<Employee>
        lateinit var notFoundPublisher: FindPublisher<Employee>
        lateinit var foundFlowable: Flowable<Employee>
        lateinit var notFoundFlowable: Flowable<Employee>

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

            foundPublisher = mockk<FindPublisher<Employee>>()
            notFoundPublisher = mockk<FindPublisher<Employee>>()

            foundFlowable = Flowable.just(exampleEmployee)
            notFoundFlowable = Flowable.empty<Employee>()

            every { foundPublisher.subscribe(any<Subscriber<in Employee>>()) } answers {
                foundFlowable.subscribe(firstArg<Subscriber<Employee>>())
            }
            every { notFoundPublisher.subscribe(any<Subscriber<in Employee>>()) } answers {
                notFoundFlowable.subscribe(firstArg<Subscriber<Employee>>())
            }
        }

        val controller: EmployeeController by memoized { EmployeeController(employeeConfig, mongoClient) }

        describe("getting all employees") {
            beforeEachTest {
                foundFlowable = Flowable.just(exampleEmployee)
                every { collection.find() } returns foundPublisher
            }

            it("should return a Flowable of all employees in the collection") {
                assert(controller.getAll().toList().blockingGet().contains(exampleEmployee))
            }
        }

        describe("getting a single employee by ID") {
            beforeEachTest {
                every { collection.find(any<Bson>()) } answers {
                    if (firstArg<Bson>().toString() == Filters.eq("_id", exampleObjectId).toString())
                        foundPublisher
                    else
                        notFoundPublisher
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

        describe("replacing an employee record") {
            beforeEachTest {
                every { collection.findOneAndReplace(any<Bson>(), any<Employee>()) } answers {
                    if (firstArg<Bson>().toString() == Filters.eq("_id", exampleObjectId).toString())
                        foundPublisher
                    else
                        notFoundPublisher
                }
            }

            it("should return a Maybe of the updated employee record if the employee exists") {
                assertEquals(
                    exampleEmployee,
                    controller.replace(exampleObjectId.toHexString(), exampleEmployee).blockingGet()
                )
            }

            it("should return an empty Maybe if the employee record doesn't exist") {
                assert(controller.replace(ObjectId().toHexString(), exampleEmployee).isEmpty.blockingGet())
            }
        }

        describe("deleting an employee") {
            beforeEachTest {
                every { collection.findOneAndDelete(any<Bson>()) } answers {
                    if (firstArg<Bson>().toString() == Filters.eq("_id", exampleObjectId).toString())
                        foundPublisher
                    else
                        notFoundPublisher
                }
            }

            it("should return a Maybe of the deleted employee if the record existed") {
                assertEquals(exampleEmployee, controller.delete(exampleObjectId.toHexString()).blockingGet())
            }

            it("should return an empty Maybe if the employee doesn't exist") {
                assert(controller.delete(ObjectId().toHexString()).isEmpty.blockingGet())
            }
        }
    }
})

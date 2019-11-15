package dev.gartman

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Past
import javax.validation.constraints.Size

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class Gender {
    MALE, FEMALE, OTHER
}

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class Ethnicity {
    CAUCASIAN,
    HISPANIC,
    AMERICAN_INDIAN,
    ASIAN,
    BLACK,
    DECLINED,
    OTHER
}

data class Address(
    @BsonProperty @JsonProperty val line1: String?,
    @BsonProperty @JsonProperty val line2: String?,
    @BsonProperty @JsonProperty val city: String?,
    @BsonProperty @JsonProperty @field:Size(min = 2, max = 2) val stateCode: String,
    @BsonProperty @JsonProperty val zipCode: String?
)

data class Bio(
    @BsonProperty @JsonProperty val firstName: String,
    @BsonProperty @JsonProperty val middleName: String?,
    @BsonProperty @JsonProperty val lastName: String,
    @BsonProperty @JsonProperty @field:Past val birthDate: LocalDate,
    @BsonProperty @JsonProperty val gender: Gender,
    @BsonProperty @JsonProperty val ethnicity: Ethnicity,
    @BsonProperty @JsonProperty val usCitizen: Boolean
)

data class Contact(
    @BsonProperty @JsonProperty @field:Email val email: String,
    @BsonProperty @JsonProperty val phoneNumber: String,
    @field:Valid val address: Address
)

data class Employee @BsonCreator @JsonCreator constructor(
    @BsonId @JsonProperty val id: ObjectId?,
    @BsonProperty @JsonProperty @Valid val bio: Bio,
    @BsonProperty @JsonProperty @Valid val contact: Contact,
    @BsonProperty @JsonProperty val skillIds: List<@NotBlank String>
)
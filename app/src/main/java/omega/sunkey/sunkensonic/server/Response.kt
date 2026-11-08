package omega.sunkey.sunkensonic.server

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.annotation.JsonUnwrapped

data class Response<T>(
    val status: String,
    val version: String = "1.16.1",
    val type: String = "SunkenSonic",
    val serverVersion: String = "1.0.0",
    val openSubsonic: Boolean = true,
    @JsonUnwrapped val payload: T? = null
)

@JsonRootName("subsonic-response")
data class Root<T>(
    @JsonProperty("subsonic-response") val response: T
)

data class Error(
    val code: Int,
    val message: String
)

data class FailedResponse(
    val status: String = "failed",
    val version: String = "1.16.1",
    val type: String = "SunkenSonic",
    val serverVersion: String = "1.0.0",
    val openSubsonic: Boolean = true,
    val error: Error
)

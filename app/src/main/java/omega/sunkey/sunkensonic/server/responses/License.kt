package omega.sunkey.sunkensonic.server.responses

data class License(
    val valid: Boolean = true,
    val email: String = "",
)

data class LicenseView(
    val license: License
)
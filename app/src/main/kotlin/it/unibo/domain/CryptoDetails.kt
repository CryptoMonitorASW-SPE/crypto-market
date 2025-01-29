import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CryptoDetails(
    @SerialName("links")
    val links: Links,
    @SerialName("sentiment_votes_up_percentage")
    val sentimentVotesUpPercentage: Double,
    @SerialName("sentiment_votes_down_percentage")
    val sentimentVotesDownPercentage: Double,
)

@Serializable
data class Links(
    @SerialName("homepage")
    val homepage: List<String>,
    @SerialName("whitepaper")
    val whitepaper: String,
)

package it.unibo.domain.ports

import it.unibo.domain.Crypto
import it.unibo.domain.Currency

/**
 * Interface for fetching and processing coin market data.
 */
interface FetchCoinMarketData {
    /**
     * Fetches and processes data for the given currency.
     *
     * @param currency The currency for which to fetch and process data.
     * @return A list of [Crypto] objects containing the fetched data.
     * @throws IOException If there is a network error while fetching data.
     * @throws SerializationException If there is an error while parsing the fetched data.
     * @throws UnresolvedAddressException If the address for fetching data cannot be resolved.
     */
    suspend fun fetchAndProcessData(currency: Currency): List<Crypto>
}

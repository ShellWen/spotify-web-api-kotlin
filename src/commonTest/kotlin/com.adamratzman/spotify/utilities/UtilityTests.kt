/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2020; Original author: Adam Ratzman */
package com.adamratzman.spotify.utilities

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.api
import com.adamratzman.spotify.block
import com.adamratzman.spotify.getEnvironmentVariable
import com.adamratzman.spotify.spotifyAppApi
import com.adamratzman.spotify.spotifyClientApi
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.GlobalScope
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class UtilityTests : Spek({
    describe("Utility tests") {
        describe("Builder tests") {
            it("API invalid parameters") {
                assertFailsWith<IllegalArgumentException> {
                    spotifyAppApi { }.build()
                }

                assertFailsWith<IllegalArgumentException> {
                    spotifyClientApi {
                        credentials {
                            clientId = getEnvironmentVariable("SPOTIFY_CLIENT_ID")
                        }
                    }.build()
                }
                assertFailsWith<IllegalArgumentException> {
                    spotifyClientApi { }.build()
                }

                if (api is SpotifyClientApi) {
                    assertFailsWith<IllegalArgumentException> {
                        spotifyClientApi {
                            credentials {
                                clientId = getEnvironmentVariable("SPOTIFY_CLIENT_ID")
                                clientSecret = getEnvironmentVariable("SPOTIFY_CLIENT_SECRET")
                            }
                        }.build()
                    }
                }
            }

            it("App API valid parameters") {
                val api = spotifyAppApi {
                    credentials {
                        clientId = getEnvironmentVariable("SPOTIFY_CLIENT_ID")
                        clientSecret = getEnvironmentVariable("SPOTIFY_CLIENT_SECRET")
                    }
                }

                block {
                    api.build()
                    api.buildAsyncAt(GlobalScope) { }
                }
            }

            it("Refresh on invalid token") {
                if (api == null) return@it
                val api = spotifyAppApi {
                    credentials {
                        clientId = getEnvironmentVariable("SPOTIFY_CLIENT_ID")
                        clientSecret = getEnvironmentVariable("SPOTIFY_CLIENT_SECRET")
                    }
                }.build()
            }

            it("Automatic refresh") {
                if (api == null) return@it
                val api = spotifyAppApi {
                    credentials {
                        clientId = getEnvironmentVariable("SPOTIFY_CLIENT_ID")
                        clientSecret = getEnvironmentVariable("SPOTIFY_CLIENT_SECRET")
                    }
                }.build()

                api.token = api.token.copy(expiresIn = -1)
                val currentToken = api.token

                api.browse.getAvailableGenreSeeds().complete()

                assertTrue(api.token.accessToken != currentToken.accessToken)
            }
        }
    }
})

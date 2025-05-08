package company.fools.secretseller.config

import company.fools.secretseller.SecretSeller
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*


@Serializable
data class SellerConfig(val stock: List<MerchantItem>)

class SellerConfigProcessor {
    companion object {
        private const val CONFIGURATION: String = "config/secret-seller.json"
        private val defaultConfig =
            SellerConfig(
                listOf(
                    MerchantItem(
                        "minecraft:diamond",
                        MarketData(
                            false,
                            10,
                            5,
                            ShiftingValue(
                                10,
                                10,
                                10
                            ),
                            ShiftingValue(
                                10,
                                10,
                                10
                            )
                        )
                    )
                )
            )

        private fun loadConfig(): SellerConfig {
            val file = File(CONFIGURATION)
            val myReader = Scanner(file)
            var data = ""
            while (myReader.hasNextLine()) {
                data += myReader.nextLine()
            }
            myReader.close()

            return Json.decodeFromString<SellerConfig>(data)
        }

        @OptIn(ExperimentalSerializationApi::class)
        private fun makeConfig() {
            val pretty = Json {
                prettyPrint = true
                prettyPrintIndent = " "
            }
            if(!File(CONFIGURATION).exists()) {
                File(CONFIGURATION).writeText(
                    pretty.encodeToString(defaultConfig)
                )
            }
        }

        fun createAndLoad(): SellerConfig {
            makeConfig()
            return loadConfig()
        }
    }
}
package curri

import com.typesafe.config.ConfigFactory

trait Config {
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  private val mongoConfig = config.getConfig("mongo")

  val httpHost = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val mongoHost = mongoConfig.getString("host")
  val mongoDB = mongoConfig.getString("db")
}
package curri.eureka


import com.netflix.appinfo.InstanceInfo.InstanceStatus
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider
import com.netflix.appinfo._
import com.netflix.discovery.{DefaultEurekaClientConfig, DiscoveryClient, DiscoveryManager, EurekaClient, EurekaClientConfig}

/**
  * Created by assen on 11/02/2017.
  */
object EurekaClient {


  def register = {
    val instanceConfig = new MyDataCenterInstanceConfig()
    val instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get
    val applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo)
    val client: EurekaClient = new DiscoveryClient(applicationInfoManager, new DefaultEurekaClientConfig())
    client.registerHealthCheck(new HealthCheckHandler {
      override def getStatus(currentStatus: InstanceStatus) = InstanceStatus.UP
    })
  }
}



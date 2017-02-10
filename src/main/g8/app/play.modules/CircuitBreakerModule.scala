package play.modules

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import com.google.inject.{ AbstractModule, Provides }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CircuitBreakerModule extends AbstractModule {
  override def configure(): Unit = {
    @Provides
    def circuitBreakerProvider(system: ActorSystem)(implicit ec: ExecutionContext): CircuitBreaker = {
      val maxFailures: Int = 3
      val callTimeout: FiniteDuration = 1.seconds
      val resetTimeout: FiniteDuration = 10.seconds
      new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)
    }
  }
}

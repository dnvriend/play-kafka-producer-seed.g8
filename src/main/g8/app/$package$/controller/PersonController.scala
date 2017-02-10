package $package$

import java.nio.file.Paths
import java.nio.file.StandardOpenOption._
import java.util.UUID
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import akka.stream.{ ActorMaterializer, IOResult, Materializer }
import akka.stream.scaladsl.{ FileIO, Source }
import akka.util.ByteString
import com.sksamuel.avro4s.RecordFormat
import org.slf4j.{ Logger, LoggerFactory }
import play.api.libs.json.{ Format, Json }
import play.api.mvc.{ Action, AnyContent, Controller, Request }
import play.modules.kafka.KafkaProducer

import scala.compat.Platform
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import scalaz.syntax.applicative._
import scalaz.std.scalaFuture._

final case class CreatePersonDto(name: String, age: Long)
object CreatePersonDto {
  implicit val format = Json.format[CreatePersonDto]
}

final case class CreatePersonCmd(id: String, name: String, age: Int, married: Option[Boolean] = None, children: Int = 0)
case object CreatePersonCmd {
  implicit val recordFormat = RecordFormat[CreatePersonCmd]
}

class PersonController @Inject() (producer: KafkaProducer, cb: CircuitBreaker)(implicit ec: ExecutionContext) extends Controller {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  def randomId: String = UUID.randomUUID.toString

  def entityAs[A: Format](implicit req: Request[AnyContent]): Try[A] =
    Try(req.body.asJson.map(_.as[A]).get)

  def createPerson = Action.async { implicit req =>
    val result = for {
      person <- Future.fromTry(entityAs[CreatePersonDto])
      id = randomId
      cmd = CreatePersonCmd(id, person.name, person.age.toInt)
      _ <- producer.produce("PersonCreatedAvro", id, cmd)
    } yield Ok(Json.toJson(cmd))

    cb.withCircuitBreaker(result)
  }
}
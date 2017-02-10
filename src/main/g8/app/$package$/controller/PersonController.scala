package $package$.controller

package com.github.dnvriend.controller

import java.util.UUID
import javax.inject.Inject

import akka.pattern.CircuitBreaker
import com.sksamuel.avro4s.RecordFormat
import org.slf4j.{ Logger, LoggerFactory }
import play.api.libs.json.{ Format, Json }
import play.api.mvc.{ Action, AnyContent, Controller, Request }
import play.modules.TopicProducer

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

final case class CreatePersonDto(name: String, age: Long, married: Option[Boolean], children: Option[Long])
object CreatePersonDto {
  implicit val format = Json.format[CreatePersonDto]
}

final case class PersonCreated(id: String, name: String, age: Long, married: Option[Boolean] = None, children: Long = 0)
case object PersonCreated {
  implicit val recordFormat = RecordFormat[PersonCreated]
  implicit val format = Json.format[PersonCreated]
}

class PersonController @Inject() (producer: TopicProducer, cb: CircuitBreaker)(implicit ec: ExecutionContext) extends Controller {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  def randomId: String = UUID.randomUUID.toString

  def entityAs[A: Format](implicit req: Request[AnyContent]): Try[A] =
    Try(req.body.asJson.map(_.as[A]).get)

  def createPerson = Action.async { implicit req =>
    val result = for {
      cmd <- Future.fromTry(entityAs[CreatePersonDto])
      id = randomId
      event = PersonCreated(id, cmd.name, cmd.age, cmd.married, cmd.children.getOrElse(0))
      _ <- producer.produce("PersonCreatedAvro", id, event)
    } yield Ok(Json.toJson(event))

    cb.withCircuitBreaker(result)
  }
}

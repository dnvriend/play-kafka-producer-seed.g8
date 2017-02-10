import javax.inject._

import play.api._
import play.api.mvc.Results._
import play.api.http.DefaultHttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{ RequestHeader, Result }
import play.api.routing.Router

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject() (env: Environment, config: Configuration, sourceMapper: OptionalSourceMapper, router: Provider[Router]) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {
  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(NotFound(Json.obj("path" -> request.path, "messages" -> List("not found", message))))
  }

  override protected def onDevServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    Future.successful(InternalServerError(Json.obj("path" -> request.path, "messages" -> List(exception.description))))
  }

  override protected def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    Future.successful(InternalServerError(Json.obj("path" -> request.path, "messages" -> List(exception.description))))
  }

  override protected def onForbidden(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(Forbidden(Json.obj("path" -> request.path, "messages" -> List("forbidden", message))))
  }
}

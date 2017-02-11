package play.modules

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.pattern.CircuitBreaker
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import com.google.inject.{ AbstractModule, Provides }
import com.sksamuel.avro4s.RecordFormat
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.{ ExecutionContext, Future }

class TopicProducerModule extends AbstractModule {
  override def configure(): Unit = {
    @Provides
    def kafkaProducerProvider(cb: CircuitBreaker)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): TopicProducer = {
      new DefaultTopicProducerModule(cb)(system, mat, ec)
    }
  }
}

trait TopicProducer {
  def produce[A: RecordFormat](topic: String, key: String, value: A): Future[Unit]
}

class DefaultTopicProducerModule(cb: CircuitBreaker)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends TopicProducer {
  val producerSettings: ProducerSettings[String, GenericRecord] =
    ProducerSettings[String, GenericRecord](system, None, None)

  val sink: Sink[ProducerRecord[String, GenericRecord], Future[Done]] =
    Producer.plainSink(producerSettings)

  def produce[A](topic: String, key: String, value: A)(implicit recordFormat: RecordFormat[A]): Future[Unit] = {
    val producerRecord = new ProducerRecord[String, GenericRecord](topic, key, recordFormat.to(value))
    cb.withCircuitBreaker(Source.single(producerRecord).runWith(sink)).map(_ => ())
  }
}

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.pattern.CircuitBreaker
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import com.google.inject.{ AbstractModule, Provides }
import com.sksamuel.avro4s.RecordFormat
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ Serializer, StringSerializer }
import play.api.libs.json.{ Format, Json }

import scala.concurrent.{ ExecutionContext, Future }

class KafkaProducerModule extends AbstractModule {
  override def configure(): Unit = {
    @Provides
    def kafkaProducerProvider(cb: CircuitBreaker)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): KafkaProducer = {
      new DefaultKafkaProducer(cb)(system, mat, ec)
    }
  }
}

trait KafkaProducer {
  def produceJson[A: Format](topic: String, key: String, value: A): Future[Unit]
  def produceAvro[A](topic: String, key: String, value: A)(implicit recordFormat: RecordFormat[A]): Future[Unit]
}

class DefaultKafkaProducer(cb: CircuitBreaker)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends KafkaProducer {
  val producerSettings: ProducerSettings[String, GenericRecord] =
    ProducerSettings[String, GenericRecord](system, None, None)
      .withBootstrapServers("localhost:9092")

  val avroSerializerSink: Sink[ProducerRecord[String, AnyRef], Future[Done]] =
    Producer.plainSink(producerSettings(None, None))

  def produce[A: RecordFormat](producerRecord: ProducerRecord[String, GenericRecord], sink: Sink[ProducerRecord[String, GenericRecord], Future[Done]]): Future[Done] =
    cb.withCircuitBreaker(Source.single(producerRecord).runWith(sink))

  def produce[A](topic: String, key: String, value: A)(implicit recordFormat: RecordFormat[A]): Future[Unit] = {
    produce(new ProducerRecord[String, AnyRef](topic, key, recordFormat.to(value)), avroSerializerSink)
      .map(_ => ())
  }
}

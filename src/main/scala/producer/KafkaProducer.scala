package producer

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import command.BitcoinCommand
import command.BitcoinCommand.CommandToStringFn
import configuration.KafkaProducerConfigs
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future

class KafkaProducer(configs: KafkaProducerConfigs)(implicit actorSystem: ActorSystem, materialize: Materializer) {
  def publishMessage[T <: BitcoinCommand : CommandToStringFn](key: T, command: T): Future[Done] = {
    val commandToStringFn: CommandToStringFn[T] = implicitly[CommandToStringFn[T]]
    Source.single(command)
      .map(commandToStringFn)
      .map(elem => new ProducerRecord("bitcoin-topic", 0, commandToStringFn(key), elem))
      .runWith(Producer.plainSink(configs.getProducerSettings))
  }
}
object KafkaProducer {
  def fromProducerConfigs(configs: KafkaProducerConfigs)(implicit actorSystem: ActorSystem, materialize: Materializer): KafkaProducer =
    new KafkaProducer(configs)
}

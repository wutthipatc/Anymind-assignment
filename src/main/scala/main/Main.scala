package main

import actor.BitCoinWallet
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import configuration.{KafkaConsumerConfigs, KafkaProducerConfigs}
import consumer.KafkaConsumer
import producer.KafkaProducer
import route.AllRoute
import service.read.GetBitcoinService
import service.write.AddBitcoinService

object Main extends App {
  implicit val actorSystem: ActorSystem = ActorSystem()
  val rootConfigs = ConfigFactory.load()
  // if error throw at application boot time
  val kafkaProducerConfigs = KafkaProducerConfigs(rootConfigs).get
  val kafkaConsumerConfigs = KafkaConsumerConfigs(rootConfigs).get
  val kafkaProducer = KafkaProducer.fromProducerConfigs(kafkaProducerConfigs)
  val bitcoinActor = akka.actor.typed.ActorSystem(BitCoinWallet(), "bitcoin-actor")
  val consumerGraph = KafkaConsumer.getGraph(kafkaConsumerConfigs, bitcoinActor)
  val addBitcoinService = AddBitcoinService(kafkaProducer)
  val getBitcoinService = GetBitcoinService(bitcoinActor)
  val allRoute = AllRoute(addBitcoinService)(getBitcoinService)

  consumerGraph.run()
  Http().newServerAt("localhost", 8080).bind(allRoute)
  println("Server start at localhost port 8080")
}

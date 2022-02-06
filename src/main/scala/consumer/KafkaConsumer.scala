package consumer

import actor._
import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{RunnableGraph, Sink, Source}
import akka.stream.typed.scaladsl.ActorSink
import command.AddAmount
import configuration.KafkaConsumerConfigs

sealed trait AckMessage
case object Ack extends AckMessage
object KafkaConsumer {
  def getGraph(configs: KafkaConsumerConfigs, actorRef: ActorRef[WalletMessage])(implicit actorSystem: ActorSystem): RunnableGraph[Consumer.Control] =
    getSource(configs).to(getActorSink(actorRef))
  private def getSource(configs: KafkaConsumerConfigs)(implicit actorSystem: ActorSystem): Source[AddAmount, Consumer.Control] =
    Consumer.plainSource(configs.getConsumerSettings, Subscriptions.topics(configs.topic))
      .map(msg => AddAmount.fromJsonString(msg.value()))

  private def getActorSink(actorRef: ActorRef[WalletMessage]): Sink[AddAmount, NotUsed] =
    ActorSink.actorRefWithBackpressure[AddAmount, WalletMessage, AckMessage](
      ref = actorRef,
      messageAdapter = (responseActorRef: ActorRef[AckMessage], element) => AddAmountToWallet.fromAddAmountCommandAndAckToActorRef(element, responseActorRef),
      onInitMessage = (responseActorRef: ActorRef[AckMessage]) => InitAdd(responseActorRef),
      ackMessage = Ack,
      onCompleteMessage = CompleteStream,
      onFailureMessage = exception => Fail(exception))
}

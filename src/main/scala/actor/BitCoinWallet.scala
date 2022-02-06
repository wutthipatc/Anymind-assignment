package actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import command.AddAmount
import consumer.{Ack, AckMessage}
import response.GetBitcoinResponse
import state.WalletState.{CurrentState, WalletSnapshot}

import java.time.Instant

sealed trait WalletMessage
case class InitAdd(ackTo: ActorRef[AckMessage]) extends WalletMessage
case class AddAmountToWallet(amount: Double, instant: Instant, ackTo: ActorRef[AckMessage]) extends WalletMessage
object AddAmountToWallet {
  def fromAddAmountCommandAndAckToActorRef(command: AddAmount, ackTo: ActorRef[AckMessage]): AddAmountToWallet =
    AddAmountToWallet(command.amount, command.instant, ackTo)
}
case object CompleteStream extends WalletMessage
case class Fail(ex: Throwable) extends WalletMessage
case class GetRestResponse(startDateTime: Instant, endDateTime: Instant, replyTo: ActorRef[GetBitcoinResponse]) extends WalletMessage
object BitCoinWallet {
  def apply(): Behavior[WalletMessage] = receiveMessage(CurrentState(1000, Map.empty))
  private def receiveMessage(state: CurrentState): Behavior[WalletMessage] =
    Behaviors.receive((_, msg) => msg match {
      case InitAdd(ackTo) =>
        println(s"Receive InitAdd message: $ackTo")
        ackTo ! Ack
        Behaviors.same
      case AddAmountToWallet(amount, instant, ackTo) =>
        println(s"Receive AddAmountToWallet message: $amount, $instant")
        val snapshot = WalletSnapshot(amount, instant)
        val newState = state.addSnapshot(snapshot)
        println(snapshot)
        println(newState)
//        committableOffset.commitScaladsl()
        ackTo ! Ack
        receiveMessage(newState)
      // Use ? ask from external component to retrieve internal actor state
      case GetRestResponse(startDateTime, endDateTime, replyTo) =>
        println(s"Receive GetRestResponse message: $startDateTime, $endDateTime, $replyTo")
        replyTo ! GetBitcoinResponse.fromStartEndDateTimeAndCurrentState(startDateTime, endDateTime, state)
        Behaviors.same
      case CompleteStream =>
        println("Receive CompleteStream message")
        Behaviors.same
      case Fail(ex) =>
        println("Receive Fail message")
        ex.printStackTrace()
        Behaviors.same
      case msg =>
        println(s"Receive $msg message")
        Behaviors.same
    })
}

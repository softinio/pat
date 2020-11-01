package com.softinio.pat.akka

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Subscriber {

  def apply(): Behavior[Message] =
    Behaviors.receive { (context, message) =>
      context.log.info(
        s"Validating ${message.firstName} ${message.lastName} with email ${message.emailAddress}!"
      )
      if (message.isValid) {
        message.replyTo ! SubscribedMessage(1L, context.self)
        message.db ! message
      } else {
        context.log.info(s"Received an invalid message $message.emailAddress")
      }
      Behaviors.same
    }
}

object Datastore {
  def apply(): Behavior[Message] =
    Behaviors.receive { (context, message) =>
      context.log.info(
        s"Adding ${message.firstName} ${message.lastName} with email ${message.emailAddress}!"
      )
      message.command match {
        case Add =>
          println(s"Adding message with email: ${message.emailAddress}")
        case Remove =>
          println(s"Removing message with email: ${message.emailAddress}")
        case Get =>
          println(s"Getting message with email: ${message.emailAddress}")
      }
      Behaviors.same
    }
}

object Reply {
  def apply(): Behavior[SubscribedMessage] =
    Behaviors.receive { (context, message) =>
      context.log.info(s"Got Reply: ${message.subscriberId}")
      Behaviors.same
    }
}

object ActorsMain {
  def apply(): Behavior[Customer] =
    Behaviors.setup { context =>
      val subscriber = context.spawn(Subscriber(), "subscriber")
      val db = context.spawn(Datastore(), "db")

      Behaviors.receiveMessage { message =>
        val replyTo = context.spawn(Reply(), "reply")
        subscriber ! Message(
          message.firstName,
          message.lastName,
          message.emailAddress,
          Add,
          db,
          replyTo
        )
        Behaviors.same
      }
    }
}

//object Pat extends App {
//  val actorsMain: ActorSystem[Customer] = ActorSystem(ActorsMain(), "PatSystem")
//  actorsMain ! Customer("Salar", "Rahmanian", "code@softinio.com")
//}

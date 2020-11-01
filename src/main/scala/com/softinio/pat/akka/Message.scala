package com.softinio.pat.akka

import akka.actor.typed.ActorRef
import org.apache.commons.validator.routines.EmailValidator

final case class Customer(
    firstName: String,
    lastName: String,
    emailAddress: String
)

final case class Message(
    firstName: String,
    lastName: String,
    emailAddress: String,
    command: Command,
    db: ActorRef[Message],
    replyTo: ActorRef[SubscribedMessage]
) {
  def isValid: Boolean = EmailValidator.getInstance().isValid(emailAddress)
}

final case class SubscribedMessage(subscriberId: Long, from: ActorRef[Message])

sealed trait Command
final case object Add extends Command
final case object Remove extends Command
final case object Get extends Command

package com.softinio.pat.zio

import zio.actors.ActorRef
import org.apache.commons.validator.routines.EmailValidator
import zio.UIO

sealed trait Protocol[+A]
final case class Customer(
    firstName: String,
    lastName: String,
    emailAddress: String
) extends Protocol[Unit]
final case class Message(
    firstName: String,
    lastName: String,
    emailAddress: String,
    command: Command,
    db: ActorRef[Protocol],
    replyTo: ActorRef[Protocol]
) extends Protocol[Unit] {
  def isValid: UIO[Boolean] =
    UIO(EmailValidator.getInstance().isValid(emailAddress))
}
final case class SubscribedMessage(subscriberId: Long, from: ActorRef[Protocol])
    extends Protocol[Unit]

sealed trait Command
final case object Add extends Command
final case object Remove extends Command
final case object Get extends Command

case class InvalidEmailException(msg: String) extends Throwable

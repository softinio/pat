package com.softinio.pat.zio

import zio.actors.Actor.Stateful
import zio.actors._
import zio.{ExitCode, IO, RIO, ZIO}
import zio.console._
import zio.duration.Duration

object Actors extends zio.App {

  def run(args: List[String]) =
    program.fold(_ => ExitCode.failure, _ => ExitCode.success)

  val subscriber = new Stateful[Console, Unit, Protocol] {
    override def receive[A](
        state: Unit,
        protocol: Protocol[A],
        context: Context
    ): RIO[Console, (Unit, A)] =
      protocol match {
        case message: Message =>
          for {
            _ <- putStrLn(
              s"Validating ${message.firstName} ${message.lastName} with email ${message.emailAddress}!"
            )
            valid <- message.isValid
            self <- context.self[Protocol]
            _ <- message.replyTo ! SubscribedMessage(1L, self)
            if (valid)
            _ <- message.db ! message
            if (valid)
          } yield ((), ())
        case _ => IO.fail(InvalidEmailException("Failed"))
      }
  }

  val datastore = new Stateful[Console, Unit, Protocol] {
    override def receive[A](
        state: Unit,
        protocol: Protocol[A],
        context: Context
    ): RIO[Console, (Unit, A)] =
      protocol match {
        case message: Message =>
          for {
            _ <- putStrLn(s"Processing Command")
            _ <- message.command match {
              case Add =>
                putStrLn(s"Adding message with email: ${message.emailAddress}")
              case Remove =>
                putStrLn(
                  s"Removing message with email: ${message.emailAddress}"
                )
              case Get =>
                putStrLn(s"Getting message with email: ${message.emailAddress}")
            }
          } yield ((), ())
        case _ => IO.fail(InvalidEmailException("Failed"))
      }
  }

  val reply = new Stateful[Console, Unit, Protocol] {
    override def receive[A](
        state: Unit,
        protocol: Protocol[A],
        context: Context
    ): RIO[Console, (Unit, A)] =
      protocol match {
        case message: SubscribedMessage =>
          for {
            _ <- putStrLn(s"Got Reply: ${message.subscriberId}")
          } yield ((), ())
        case _ => IO.fail(InvalidEmailException("Failed"))
      }
  }

  val program = for {
    actorSystemRoot <- ActorSystem("salarTestActorSystem")
    subscriberActor <-
      actorSystemRoot.make("subscriberActor", Supervisor.none, (), subscriber)
    datastoreActor <-
      actorSystemRoot.make("datastoreActor", Supervisor.none, (), datastore)
    replyActor <- actorSystemRoot.make("replyActor", Supervisor.none, (), reply)
    _ <- subscriberActor ! Message(
      "Salar",
      "Rahmanian",
      "code@softinio.com",
      Add,
      datastoreActor,
      replyActor
    )
    _ <- zio.clock.sleep(Duration.Infinity)
  } yield ()
}

package utils

import java.util.Date

import akka.actor.{ActorRef, ActorSystem, Props}

object Concise {
  def now = new Date()
}

object Format {
  def listToString(list: List[Any]): String =
    list.reduce{_.toString + ", " + _.toString}.toString
  def listToReverseString(list: List[Any]): String =
    list.reduce{(a: Any, b: Any) => b.toString + ", " + a.toString}.toString
}

object Singleton {
  val actorSystem = ActorSystem("main")
  val logger: ActorRef = actorSystem.actorOf(Props(new Logger()))
}

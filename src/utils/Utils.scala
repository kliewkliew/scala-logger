package utils

import java.util.Date

import akka.actor.{ActorRef, ActorSystem, Props}

object Concise {
  def now = new Date()
}

object Format {
  def listToString(list: List[String]): String = list.reduce{_ + ", " + _}
  def listToReverseString(list: List[String]): String = list.reduce{(a: String, b: String) => b + ", " + a}
}

object Singleton {
  val actorSystem = ActorSystem("main")
  val logger: ActorRef = actorSystem.actorOf(Props(new Logger()))
}

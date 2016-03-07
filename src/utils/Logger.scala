package utils

import Logger._

import java.awt.image.RenderedImage
import java.io.{File, FileOutputStream, FileWriter, PrintWriter}
import java.lang.Long
import java.net.URL
import java.text.SimpleDateFormat
import java.time.{LocalDateTime, ZoneId}
import java.util.Date
import javax.imageio.ImageIO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import akka.actor.Actor

class Logger () extends Actor {

  def receive = {
    case PropertyList(filePath, propertyList) =>
      logTimestamp(filePath, propertyList)
    case TimestampList(filePath, timestampList) =>
      logTimestamp(filePath, timestampList.map { timestamp => Property(timestamp.namespace, timestamp.timestamp, "")})
    case DurationList(filePath, durationList) =>
      logDuration(filePath, durationList)
    case MessageList(filePath, messages) =>
      logMessage(filePath, messages)
    case SingleProperty(filePath, property) =>
      logTimestamp(filePath, List(property))
    case SingleTimestamp(filePath,  timestamp) =>
      logTimestamp(filePath, List(Property(timestamp.namespace, timestamp.timestamp, "")))
    case SingleDuration(filePath, duration) =>
      logDuration(filePath, List(duration))
    case (filePath: String, message: String) =>
      logMessage(filePath, List(message))
    case (filePath: String, binary: Binary) =>
      writeBinary(filePath, binary)
    case (filePath: String, image: Image) =>
      writeImage(filePath, image)
    case (filePath: String, uRL: URL, overwrite: Boolean) =>
      writeImageFromURL(filePath, uRL, overwrite)
    case _ =>
      sender() ! Future.failed(new IllegalArgumentException("Unknown message received by Logger"))
  }

  private def logDuration (filePath: String, durationList: List[Duration]): Unit =
  {
    logTimestamp(
      filePath,
      durationList.map {
        duration =>
          val diff = java.time.Duration.between(
            LocalDateTime.ofInstant(duration.start.toInstant(), ZoneId.systemDefault()),
            LocalDateTime.ofInstant(duration.end.toInstant(), ZoneId.systemDefault()))

          Property(
            duration.namespace,
            duration.end,
            durationFormat.format(
              duration.key,
              new Long(diff.toDays()),
              new Long(diff.toHours() % 24),
              new Long(diff.toMinutes() % 60),
              new Long(diff.getSeconds() % 60)
            )
          )
      }
    )
  }

  private def logTimestamp (filePath: String, propertyList: List[Property]): Unit =
  {
    logMessage(
      filePath,
      propertyList.map {
        property =>
          "%s%s".format(
            namespaceFormat.format(property.namespace.app, property.namespace.key),
            (timestampFormat + "\t%s").format(
              dateFormat.format(property.timestamp),
              timeFormat.format(property.timestamp),
              property.value))
      }
    )
  }

  private def logMessage(filePath: String, messages: List[String]) =
  {
    try {
      val file = new File(filePath)
      file.getParentFile().mkdirs()

      val log = new PrintWriter(new FileWriter(file, true))

      try {
        messages.foreach { message => log.println(message) }
      }
      finally {
        log.close()
      }

      sender() ! Future.successful()
    }
    catch {
      case exception =>
        sender() ! Future.failed(exception)
    }
  }

  private def writeBinary(filePath: String, message: Binary) =
  {
    try {
      val output = new File(filePath)
      output.getParentFile.mkdirs()
      val writer = new FileOutputStream(output)

      try {
        writer.write(message.binary)
      }
      finally {
        writer.close()
      }
      sender() ! Future.successful()
    }
    catch {
      case exception =>
        sender() ! Future.failed(exception)
    }
  }

  private def writeImage(filePath: String, message: Image) =
  {
    try {
      val imageFile = new File(filePath)
      if (message.overwrite || !imageFile.exists())
        ImageIO.write(message.image, "JPG", imageFile)
      sender() ! Future.successful()
    }
    catch {
      case exception =>
        sender() ! Future.failed(exception)
    }
  }

  private def writeImageFromURL(filePath: String, uRL: URL, overwrite: Boolean) =
  {
    val imageFuture = getImage(uRL)
    imageFuture.onSuccess {
      case image =>
        writeImage(filePath, Image(image, overwrite))
    }
    imageFuture.onFailure {
      case exception =>
        sender() ! Future.failed(exception)
    }
  }

  private def getImage(uRL: URL): Future[RenderedImage] = Future { ImageIO.read(uRL) }
}

object Logger {
  case class Namespace(app: String, key: String)
  case class Property(namespace: Namespace, timestamp: Date, value: String)
  case class Timestamp(namespace: Namespace, timestamp: Date)
  case class Duration(namespace: Namespace, key: String, start: Date, end: Date)
  case class Binary(binary: Array[Byte], overwrite: Boolean)
  case class Image(image: java.awt.image.RenderedImage, overwrite: Boolean)

  case class PropertyList(filePath: String, propertyList: List[Property])
  case class TimestampList(filePath: String, timestampList: List[Timestamp])
  case class DurationList(filePath: String, durationList: List[Duration])
  case class MessageList(filePath: String, messages: List[String])
  case class SingleProperty(filePath: String, property: Property)
  case class SingleTimestamp(filePath: String, timestamp: Timestamp)
  case class SingleDuration(filePath: String, duration: Duration)

  private val namespaceFormat = "%-8s%-16s"
  private val timestampFormat = "%s\t%s"
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  private val timeFormat = new SimpleDateFormat("HH:mm:ss")
  private val durationFormat = "%-10s:%3d-%02d:%02d:%02d"
}
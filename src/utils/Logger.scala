package utils

import Logger._

import java.io.{File, FileOutputStream, FileWriter, PrintWriter}
import java.lang.Long
import java.text.SimpleDateFormat
import java.time.{LocalDateTime, ZoneId}
import java.util.Date
import javax.imageio.ImageIO

import akka.actor.Actor

class Logger () extends Actor {

  def receive = {
    case (filePath: String, property: Property)               => logTimestamp(filePath, property)
    case (filePath: String, Timestamp(namespace, timestamp))  => logTimestamp(filePath, Property(namespace, timestamp, ""))
    case (filePath: String, duration: Duration)               => logDuration(filePath, duration)
    case (filePath: String, message: String)                  => logMessage(filePath, message)
    case (filePath: String, binary: Binary)                   => writeBinary(filePath, binary)
    case (filePath: String, image: Image)                     => writeImage(filePath, image)
    case _              => throw new IllegalArgumentException("Unknown message received by Logger")
  }

  private def logDuration (filePath: String, duration: Duration): Unit =
  {
    val diff = java.time.Duration.between(
      LocalDateTime.ofInstant(duration.start.toInstant(), ZoneId.systemDefault()),
      LocalDateTime.ofInstant(duration.end.toInstant(), ZoneId.systemDefault()))

    logTimestamp(
      filePath,
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
    )
  }

  private def logTimestamp (filePath: String, property: Property): Unit =
  {
    logNamespace(
      filePath,
      property.namespace,
      (timestampFormat + "\t%s").format(
        dateFormat.format(property.timestamp),
        timeFormat.format(property.timestamp),
        property.value))
  }

  private def logMessage(filePath: String, message: String) =
  {
    val file = new File(filePath)
    file.getParentFile().mkdirs()

    val log = new PrintWriter(new FileWriter(file, true))
    log.println(message)
    log.close()
  }

  private def logNamespace(filePath: String, namespace: Namespace, message: String) =
  {
    logMessage(
      filePath,
      "%s%s".format(
        namespaceFormat.format(namespace.app, namespace.key),
        message))
  }

  private def writeBinary(filePath: String, message: Binary) =
  {
    val output = new File(filePath)
    output.getParentFile.mkdirs()
    val writer = new FileOutputStream(output)
    writer.write(message.binary)
    writer.close()
  }

  private def writeImage(filePath: String, message: Image) =
  {
    val imageFile = new File(filePath)
    if (message.overwrite || !imageFile.exists())
      ImageIO.write(message.image, "JPG", imageFile)
  }
}

object Logger {
  case class Namespace(app: String, key: String)
  case class Property(namespace: Namespace, timestamp: Date, value: String)
  case class Timestamp(namespace: Namespace, timestamp: Date)
  case class Duration(namespace: Namespace, key: String, start: Date, end: Date)
  case class Binary(binary: Array[Byte], overwrite: Boolean)
  case class Image(image: java.awt.image.RenderedImage, overwrite: Boolean)

  private val namespaceFormat = "%-8s%-16s"
  private val timestampFormat = "%s\t%s"
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  private val timeFormat = new SimpleDateFormat("HH:mm:ss")
  private val durationFormat = "%-10s:%3d-%02d:%02d:%02d"
}
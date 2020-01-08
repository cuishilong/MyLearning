package stream.listener

import org.apache.spark.sql.streaming.StreamingQueryListener
import org.apache.spark.sql.streaming.StreamingQueryListener.{QueryProgressEvent, QueryStartedEvent, QueryTerminatedEvent}

class MyStreamQueryListener(streamTag: String) extends StreamingQueryListener {
  override def onQueryStarted(event: QueryStartedEvent): Unit = {
  }

  override def onQueryProgress(event: QueryProgressEvent): Unit = {
    val process = event.progress
    println(s"${streamTag} - numInputRows : ${process.numInputRows}")
  }

  override def onQueryTerminated(event: QueryTerminatedEvent): Unit = {
  }
}

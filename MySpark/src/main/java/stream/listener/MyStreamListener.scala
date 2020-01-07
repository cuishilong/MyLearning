package stream.listener

import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchStarted}

class MyStreamListener(tag: String) extends StreamingListener {
  override def onBatchStarted(batchStarted: StreamingListenerBatchStarted): Unit = {
    val info = batchStarted.batchInfo
    val numRecords = info.numRecords
    val batchTime = info.batchTime.milliseconds / 1000

    println(s"tag : ${tag} ; numRecords : ${numRecords} ; batchTime : ${batchTime}")
  }
}

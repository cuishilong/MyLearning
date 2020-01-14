package ml.hanlp.tokenizer

import com.hankcs.hanlp.tokenizer.{NLPTokenizer, NotionalTokenizer, StandardTokenizer}

import scala.collection.JavaConversions._

object Demo {
  def main(args: Array[String]): Unit = {
    val text = "南京市长江大桥，北京你好吗"
    val standard_list = StandardTokenizer.segment(text)
    val nlp_list = NLPTokenizer.segment(text)
    val notional_list = NotionalTokenizer.segment(text)

    standard_list.foreach(println(_))
    nlp_list.foreach(println(_))
    notional_list.foreach(println(_))

  }
}

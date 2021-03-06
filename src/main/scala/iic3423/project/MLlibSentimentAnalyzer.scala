// ref: https://github.com/P7h/Spark-MLlib-Twitter-Sentiment-Analysis/blob/master/src/main/scala/org/p7h/spark/sentiment/mllib/MLlibSentimentAnalyzer.scala

package iic3423.project

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

object MLlibSentimentAnalyzer {

    def computeSentiment(text: String, stopWordsList: Broadcast[List[String]], model: NaiveBayesModel): (Int, Vector) = {
        val tweetInWords: Seq[String] = getBarebonesTweetText(text, stopWordsList.value)
        val features = MLlibSentimentAnalyzer.transformFeatures(tweetInWords)
        val polarity = model.predict(features)
        (normalizeMLlibSentiment(polarity), features)
    }

    def normalizeMLlibSentiment(sentiment: Double) = {
        sentiment match {
            case x if x == 0 => -1 // negative
            case x if x == 2 => 0 // neutral
            case x if x == 4 => 1 // positive
            case _ => 0 // if cant figure the sentiment, term it as neutral
        }
    }

    def getBarebonesTweetText(tweetText: String, stopWordsList: List[String]): Seq[String] = {
        //Remove URLs, RT, MT and other redundant chars / strings from the tweets.
        tweetText.toLowerCase()
            .replaceAll("\n", "")
            .replaceAll("rt\\s+", "")
            .replaceAll("\\s+@\\w+", "")
            .replaceAll("@\\w+", "")
            .replaceAll("\\s+#\\w+", "")
            .replaceAll("#\\w+", "")
            .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
            .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
            .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
            .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
            .split("\\W+")
            .filter(_.matches("^[a-zA-Z]+$"))
            .filter(!stopWordsList.contains(_))
    }

    val hashingTF = new HashingTF(16384)

    def transformFeatures(tweetText: Seq[String]): Vector = {
        hashingTF.transform(tweetText)
    }
}
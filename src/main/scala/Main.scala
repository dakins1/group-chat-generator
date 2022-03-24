import scala.io.StdIn


import ngram._

object Main extends App {

    //my directory for training data
    val filePath = "../groupMeExport"
    val gmExtract = GroupMeExtractor(filePath)

    val order = 12
    val charLimit = 500
    val ngram = NGram(gmExtract.trainingData, order)
    
    for (_ <- 1 to 10) {
        println(ngram.generateData().mkString)
    }

}
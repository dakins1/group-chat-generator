import scala.io.StdIn


import ngram._

object Main extends App {

    //my directory for training data
    val filePath = "../groupMeExport"
    val gmExtract = GroupMeExtractor(filePath)

    val order = 9
    val charLimit = 10000
    val ngram = NGram(gmExtract.trainingData, order)
    val data = List(List(1,2,3,4,5,6,7,8,9,10), List(2,4,6,8,10), List(1,3,5,7,9))
    // val ngram = NGram(data, order)
    
    for (_ <- 1 to 10) {
        // println(ngram.generateData().mkString)
        println(ngram.generateLazyData().mkString)
    }

}
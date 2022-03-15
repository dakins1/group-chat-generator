import scala.io.StdIn
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import scala.io.Source
import java.io.File
import scala.io.Codec

import ngram._

case class Message(
    name:String,
    text:String,
    user_id:String
)

case class Group(
    name:String,
    id:String
)

/*
json message structure
export parent folder -> randomly numbered subfolder -> message.json
array
    groupid
    id
    sender_id
    basically just an array of messages, and straightforward attribute names
.json files have all json on one line, no \n formatting for the json itself
*/


object Main extends App {

    implicit val formats = DefaultFormats

    def getGF(folderNumber:String):Unit= {
        val filePath = "../groupMeExport/" + folderNumber + "/conversation.json"
        val messages = Source.fromFile(filePath)(Codec("utf-8")).getLines().toArray.apply(0)
        //not sure what this line does, keeping it for now but should delete later        
        // val json = parse(messages).children.map(_.extract[Group])//.filter(_.name != null).filter(_.name == "Gruesome Fivesome").head.id
        if (messages.contains("Gruesome Fivesome")) println(messages)
    }

    //given a subfolder number, extract all messages from that subfolder
    def getMessages(folderNumber:String):Seq[Message] = {
        val filePath = "../groupMeExport/" + folderNumber + "/message.json"
        //entirety of json object is one big array of messages, so call .children to get all messages
        val messageArray = Source.fromFile(filePath)(Codec("utf-8")).mkString
        val json = parse(messageArray).children 
        json.map(_.extract[Message]).filter(_.text != null)
    }

    //From alvin alexander
    def getListOfSubDirectories(dir: File): List[String] = dir.listFiles
        .filter(_.isDirectory)
        .map(_.getName)
        .toList
    
    val userID = "54066176"
    val groupChatID = "30334096"

    //groupMe exports all the messages across dozens of randomly numbered folders, this puts all
    //folder names into a list
    val folderNums = getListOfSubDirectories(new File("../groupMeExport"))
    // val messages = folderNums.map(n => getMessages(n)).flatMap(_.filter(m => m.user_id == leon))
    val messages = getMessages(groupChatID)
    
    val order = 12
    val charLimit = 500
    val ngram = NGram(messages.map(_.text.toLowerCase().toList).toList, order)
    
    for (_ <- 1 to 10) {
        println(ngram.generateData().mkString)
    }

}

/*
TODO 
going to be too difficult to functionalize file i/o and user input at first
start with functionalizing ngram algies
    - one for humans to read
    - one with HOF
define complexity --- so far it is O(n*m), where n = # of chars from training data and m = somewhat confusingly, the n defined by ngram

make probabilities not use non-distinct Seq[Char] and random index for probabilities - would be a no-no in haskell ;)
    There probably exists some clever algorithm to summarize probabilities and outputs 

also could probs make some more objects for all this, although might be overkill for existing functionality - needs to be driven by advanced features
another idea is to abstract away the fact this algorithm is for groupme messages; allow it to take a Seq of any type instead. Then, make a GroupMe class that
    configures groupme messages to fit this algorithm
Also, with ngram object, could create multiple mapping instances to quickly compare different orders
    Make NGram its own type?? That way revising types is easier. 
    Could start with case class, then work way up to type-paramterized object

and of course, somehow parameterizing the groupme input
    - list out all usernames / group chats, have command line prompt to select who or which chat to emulate

Make an infinite text generator, keeps going till it hits Nil, rather than hitting char limit. Super low on priority 
    list tho, since text almost always self terminates before reaching limit
*/
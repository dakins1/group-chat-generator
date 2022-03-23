/**
  * A class for parsing GroupMe message exports 
  * Will take all GroupMe data exported by a single user (following GroupMe's data export procedure through their desktop web UI) and configure it for the NGram object --
  * which just means this converts relevant messages into a List[List[Char]]    
  * 
  * This is very much not functional or pure, since I am learning other recursive techniques before tackling i/o and monads in Scala. But I do plan to make it functional
  * in the future. And in the meantime I use scala's function chaining to at least use Scala style
  */

  
/*
json message structure
export parent folder -> group chat subfolder -> message.json
message.json is an array of objects containing:
    groupid
    sender_id
    name
    text

.json files have all json on one line, no \n in messages

Will use case classes to help with storing the data; makes future extraction easier
*/
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import scala.io.Source
import java.io.File
import scala.io.Codec
import scala.io.Source
import java.io.File
import scala.io.Codec
import java.security.acl.Group

case class Message(
    name:String,
    text:String,
    user_id:String
)

class GroupMeExtractor(val filePath:String) {
    

    implicit val formats = DefaultFormats //for json parsing

    //groupMe exports each group chat into its own directory, and the directory name is the group_id 
    //  this puts all directory names, thus group_ids, into a list
    val groupIDs = getListOfSubDirectories(filePath)
    lazy val allMessages = groupIDs.flatMap(id => getMessages(id))

    //From alvin alexander
    def getListOfSubDirectories(dir:String): List[String] = (new File(dir)).listFiles
        .filter(_.isDirectory)
        .map(_.getName)
        .toList

    /**
      * Given a subfolder number, extract all messages from that subfolder
      * This yields all messages from the group chat of that subfolder
      * 
      * @param groupId
      * @return an array of Messages
      */
    def getMessages(groupID:String):Seq[Message] = {
        val fp = f"$filePath/$groupID/message.json"
        //entirety of json object is one big array of messages, so call .children to get all messages
        val messageString = Source.fromFile(fp)(Codec("utf-8")).mkString
        val json = parse(messageString).children
        json.map(_.extract[Message]).filter(_.text != null)
    }
    
    // specific training data I've chosen
    val userID = "54066176"
    val groupChatID = "30334096"

    //this is the code for exporting a specific user's chats, across all groups that user is part of
    // val messages = folderNums.map(n => getMessages(n)).flatMap(_.filter(m => m.user_id == leon))
    val messages = getMessages(groupChatID)
    val trainingData = messages.map(_.text.toLowerCase().toList).toList

}

object GroupMeExtractor {
    def apply(filePath:String) = new GroupMeExtractor(filePath)
}

import scala.io.StdIn
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import scala.io.Source
import java.io.File
import scala.io.Codec

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

    val leon = "54066176"
    val gruesomeFivesome = "30334096"

    def getGF(folderNumber:String):Unit= {
        val filePath = "../../Downloads/groupMeExport/" + folderNumber + "/conversation.json"
        val messages = Source.fromFile(filePath)(Codec("utf-8")).getLines().toArray.apply(0)
        //not sure what this line does, keeping it for now but should delete later        
        // val json = parse(messages).children.map(_.extract[Group])//.filter(_.name != null).filter(_.name == "Gruesome Fivesome").head.id
        if (messages.contains("Gruesome Fivesome")) println(messages)
    }

    //given a subfolder number, extract all messages from that subfolder
    def getMessages(folderNumber:String):Seq[Message] = {
        val filePath = "../../Downloads/groupMeExport/" + folderNumber + "/message.json"
        //entirety of json object is one big array of messages, so call .children to get all messages
        val messageArray = Source.fromFile(filePath)(Codec("utf-8")).mkString
        val json = parse(messageArray).children 
        return json.map(_.extract[Message]).filter(_.text != null)
    }

    //From alvin alexander
    def getListOfSubDirectories(dir: File): List[String] =
    dir.listFiles
       .filter(_.isDirectory)
       .map(_.getName)
       .toList

    //groupMe exports all the messages across dozens of randomly numbered folders, this puts all
    //folder names into a list
    val folderNums = getListOfSubDirectories(new File("../../Downloads/groupMeExport"))
    // val messages = folderNums.map(n => getMessages(n)).flatMap(_.filter(m => m.user_id == leon))
    val messages = getMessages(gruesomeFivesome)

    def ngram(n:Int, texts:Seq[String]):Map[String, Seq[Char]] = {
        //A plain recursive function would have been much cleaner code, but sometimes I can't resist scala's HOFs
        //Technically isn't functional since I use a mutable Set :(, but theoretically could be passed around the 
        //fold and could be functional
        //need to figure out what exactly this is doing, add better comments to it, and make that Set functional!
        var seen = Set[String]().empty
        texts.foldLeft(Map[String,Seq[Char]]().empty){
            case (oldMap, message) =>
                val maxLength = message.length - n - 1 //-1 so there is a following char to add to the gram
                val (map, _) = message.foldLeft((oldMap, 0)){
                    case ((old, count), _) =>
                        if (count > maxLength) (old, count+1) //less than n chars left
                        else {
                            val str = message.subSequence(count, count+n).toString()
                            if (seen(str)) (old + (str -> (old(str):+message.charAt(count+n))), count+1)
                            else { 
                                seen+=str
                                (old + (str -> Seq(message.charAt(count+n))), count+1)
                            }
                        }
                }
                map
        }
    }

    def make(n:Int, limit:Int, gram:Map[String, Seq[Char]], possibs:Set[String], str:String):String = {
        if (str.length() == limit) str
        else {
            val k = str.substring(str.length()-n)
            if (possibs(k)) {
                val s = gram(k)
                make(n, limit, gram, possibs, str+s(scala.util.Random.nextInt(s.length)))
            } else str
            
        }
    }
    val order = 11
    val grams = ngram(order, messages.map(_.text.toLowerCase()))
    val r = scala.util.Random
    //Filter out words less than 10 characters long. I guess this is just to start things off
    val starters = messages.flatMap(_.text.split(" ").filter(_.length() >= order))
    // val starters = grams.map(_._1).toSeq
    // val starterM = messages(r.nextInt(messages.length)).text
    // val ss = if (starterM.length > order) r.nextInt(starterM.length()-order) else 0
    // val starter = starterM.substring(ss, ss+order)
    for (_ <- 0 to 10) {
        val starter = starters(r.nextInt(starters.length))
        println(make(order, 200, grams, grams.map(_._1).toSet, starter))
        println()
    }
}

/*
TODO 
break down the messages stuff, or at least reduce use of messages.map nonsense
figure out what the randome substring stuff is, and find a better way of explaining/coding that
document the json format, maybe either through comments, case classes, or something idk. Nvm already have case classes,
maybe figure something better out. Or just better explain them

Add comments throughout the code, especially on the parts with argument-dense function calls
Rewatch that processing video on how to make ngrams

comments for any functional heavy stuff

this will be great thing to work on over weekend

and of course, somehow parameterizing the groupme input



*/
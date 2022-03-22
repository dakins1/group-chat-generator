/**
  * A class for parsing GroupMe message exports 
  * Will take all GroupMe data exported by a single user (following GroupMe's data export procedure through their desktop web UI) and configure it for the NGram object --
  * which just means this converts relevant messages into a List[List[Char]]    
  */

  
/*
json message structure
export parent folder -> randomly numbered subfolder -> message.json
message.json is an array of objects containing:
    groupid
    id
    sender_id

.json files have all json on one line, no \n in messages

Will use case classes to help with storing the data; makes future extraction easier
*/

case class Message(
    name:String,
    text:String,
    user_id:String
)

case class Group(
    name:String,
    id:String
)


class GroupMeExtractor {
  
}

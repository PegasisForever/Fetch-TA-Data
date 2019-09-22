import models.User
import org.json.simple.JSONObject

fun main() {
    User.init()

    val user= User.fromClient(jsonParser.parse("""
        {
"user":{
	"number": "349891234",
	"password":"43z955n9",
	"displayname":"me",
	"receive":true
},
"token":"token"
}
    """) as JSONObject)
    User.remove(user)
    User.save()
}
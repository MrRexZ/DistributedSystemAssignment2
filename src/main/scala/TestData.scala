
import akka.actor.ActorRef
import com.sunway.network.actors.ActorMessages.{ServerReply, _}

/**
  * Created by Mr_RexZ on 11/23/2016.
  */
object TestData extends App {


  var actor: List[Option[ActorRef]] = List(None)
  var a: ServerReply = AcceptPlayerAsHost(1, 1, actor)

  /*
   // cast(a)
    //anCast(a, typeTag[Int])
    ness(typeTag[AcceptPlayerAsHost],a)



    def ness[A](c: TypeTag[A] , serverReply : ServerReply): Unit = {

      println(c.isInstanceOf[c])

    }
  */

  /*
  def checkType[T : TypeTag](obj: T) = {
    val theType = typeOf[T] match {
      case TypeRef(_, _, args) => args
    }
    println(theType)

  }
*/

  /*
  def cast[A](a : A)(implicit tt: TypeTag[A])=  {
  println(a.asInstanceOf[A])
  }
  */


  /*
  def anCast[A](a: Any, tt: TypeTag[A])=  {
    val ss= a.asInstanceOf[A]
    println(ss)

  }
  */
}

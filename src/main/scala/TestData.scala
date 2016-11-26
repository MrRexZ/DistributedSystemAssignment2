
import akka.actor.ActorRef
import com.sunway.network.actors.MenuActorMessages.{ServerReply, _}
/**
  * Created by Mr_RexZ on 11/23/2016.
  */
object TestData extends App {


  var actor: List[Option[ActorRef]] = List(None)
  var a: ServerReply = new AcceptPlayerAsHost(1, 1, actor)

  var c = a.asInstanceOf[AcceptPlayerAsHost]
  println(c.isInstanceOf[ServerReply])
  cast(a)

  def cast[A](a: A) = {

    println(a.isInstanceOf[ServerReply])
    println(a.isInstanceOf[AcceptPlayerAsHost])
    println(a.isInstanceOf[AcceptPlayerAsParticipant])
    println(a.isInstanceOf[RejectPlayer])
    val b = a.asInstanceOf[AcceptPlayerAsHost]
    println(b.isInstanceOf[ServerReply])
    println(b.isInstanceOf[AcceptPlayerAsHost])
    println(b.isInstanceOf[AcceptPlayerAsParticipant])
    println(b.isInstanceOf[RejectPlayer])
  }




  /*
   // cast(a)
    //anCast(a, typeTag[Int])
    ness(typeTag[AcceptPlayerAsHost],a)



    def ness[A](c: TypeTag[A] , serverReply : ServerReply): Unit = {

      println(c.isInstanceOf[c])

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

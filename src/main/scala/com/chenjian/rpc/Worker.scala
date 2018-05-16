package com.chenjian.rpc

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

//worker
class Worker(masterHost: String,masterPost :Int) extends Actor{

  var master : ActorSelection=_

//在preStart方法里面先建立连接
  override def preStart(): Unit = {
    //端口号后面的/user必须写，这是规定，否则报错，Masterw为名字，必须保持一致
     master=context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPost/user/Master")

    //连接成功后，向master发送一条消息
    master ! "connect"
  }

  override def receive: Receive = {
    case "reply" =>{
      println("收到一个回复！来着master")
    }
  }
}

object Worker{
  def main(args: Array[String]): Unit = {

    val host=args(0)
    val port=args(1).toInt
    val masterHost=args(2)
    val masterPost=args(3).toInt

//    val host="192.168.199.190"
//    val port="7777".toInt
//    val masterHost="192.168.199.190"
//    val masterPost="9999".toInt


    //准备配置
    val configStr=
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin

    val config=ConfigFactory.parseString(configStr)
    //ActorSystem，辅助创建和监控下面的Actor，它是单例的
    val actorSystem=ActorSystem("WorkerSystem",config)

    //创建Actor,第二个参数"Worker"为名字，在与它建立连接时需要指明该名字
    val master=actorSystem.actorOf(Props(new Worker(masterHost,masterPost)),"Worker")

  }

}

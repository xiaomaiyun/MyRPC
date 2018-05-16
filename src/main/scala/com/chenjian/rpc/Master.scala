package com.chenjian.rpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

//master
class Master extends Actor{

  println("constructor invoked")

  override def preStart(): Unit = {
    println("preStart invoked")
  }

  //用于接收消息
  override def receive: Receive = {

    case "connect"=>{

      //回复接收到的消息
      sender ! "reply"
      println("一个客户端连接成功！")

    }

    case "hello"=>{
      println("接收到：hello！")
    }

  }
}

object Master{
  def main(args: Array[String]): Unit = {

    val host=args(0)
    val port=args(1).toInt

//    val host="192.168.199.190"
//    val port="9999".toInt

//准备配置
    val configStr=
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin

    val config=ConfigFactory.parseString(configStr)
    //ActorSystem，辅助创建和监控下面的Actor，它是单例的
    val actorSystem=ActorSystem("MasterSystem",config)

    //创建Actor,第二个参数"Master"为名字，在Worker与它建立连接时需要指明该名字
   val master=actorSystem.actorOf(Props(new Master),"Master")

    master ! "hello"

  }
}

package com.chenjian.rpc

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

//worker
class Worker(masterHost: String, masterPost: Int, val memory: Int, val cores: Int) extends Actor {

  val workerId = UUID.randomUUID().toString
  val HEART_INTERVAL = 10000
  var master: ActorSelection = _

  //在preStart方法里面先建立连接
  override def preStart(): Unit = {
    //端口号后面的/user必须写，这是规定，否则报错，Masterw为名字，必须保持一致
    master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPost/user/Master")

    //连接成功后，向master发送注册消息，包括workerId,memory,cores
    master ! RegisterWorker(workerId, memory, cores)
  }

  override def receive: Receive = {
    case "reply" => {
      println("收到一个回复！来着master")
    }

    case RegisteredWorker(masterUrl) => {
      println("master的Url:" + masterUrl)

      //导入隐私转换
      import context.dispatcher
      //启动定时器发送心跳
      context.system.scheduler.schedule(0 millis, HEART_INTERVAL millis, self, SendHeartbeat)
    }

    //向master发送心跳，此处还可以写其他的业务逻辑
    case SendHeartbeat => {
      master ! Heartbeat(workerId)
    }
  }
}

object Worker {
  def main(args: Array[String]): Unit = {

    val host = args(0)
    val port = args(1).toInt
    val masterHost = args(2)
    val masterPost = args(3).toInt
    val memory = args(4).toInt
    val cores = args(5).toInt

    //    val host = "192.168.199.190"
    //    val port = "7777".toInt
    //    val masterHost = "192.168.199.190"
    //    val masterPost = "9999".toInt
    //    val memory = "789".toInt
    //    val cores = "999".toInt

    //准备配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin

    val config = ConfigFactory.parseString(configStr)
    //ActorSystem，辅助创建和监控下面的Actor，它是单例的
    val actorSystem = ActorSystem("WorkerSystem", config)

    //创建Actor,第二个参数"Worker"为名字，在与它建立连接时需要指明该名字
    val master = actorSystem.actorOf(Props(new Worker(masterHost, masterPost, memory, cores)), "Worker")

  }

}

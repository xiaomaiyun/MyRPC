package com.chenjian.rpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._

//master xiaomai
class Master(val host: String, val port: Int) extends Actor {

  //WorkerInfo
  val workers = new mutable.HashSet[WorkerInfo]()
  //workerId->WorkerInfo
  val idToWorker = new mutable.HashMap[String, WorkerInfo]()

  //超时检测的时间间隔
  val CHECK_INTERVAL = 15000

  println("执行constructor")

  override def preStart(): Unit = {
    println("执行preStart")

    //导入隐私转换
    //使用timer太low了, 可以使用akka的, 使用定时器, 要导入这个包
    import context.dispatcher
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)
  }

  //用于接收消息
  override def receive: Receive = {

    case RegisterWorker(id, memory, cores) => {

      //判断是否已经注册
      if (!idToWorker.contains(id)) {

        //如果没有注册，将Worker的信息封装起来保持到内存中（spark是将其持久化保存）
        val workerInfo = new WorkerInfo(id, memory, cores)
        idToWorker(id) = workerInfo
        workers += workerInfo

        sender ! RegisteredWorker(s"akka.tcp://MasterSystem@$host:$port/user/Master")
      }
    }

    case "connect" => {

      //回复接收到的消息
      sender ! "reply"
      println("一个客户端连接成功！")

    }

    case "hello" => {
      println("接收到：hello！")
    }

    case Heartbeat(id) => {
      if (idToWorker.contains(id)) {
        val workerInfo = idToWorker(id)

        //worker向master报活
        val currentTime = System.currentTimeMillis()
        workerInfo.lastHeartbeatTime = currentTime
      }
    }

    case CheckTimeOutWorker => {
      val currentTime = System.currentTimeMillis()
      val toRemove = workers.filter(x => currentTime - x.lastHeartbeatTime > CHECK_INTERVAL)

      for (w <- toRemove) {
        workers -= w
        idToWorker -= w.id
      }
      println(workers.size)
    }
  }
}

object Master {
  def main(args: Array[String]): Unit = {

    val host = args(0)
    val port = args(1).toInt

    //    val host = "192.168.199.190"
    //    val port = "9999".toInt

    //准备配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin

    val config = ConfigFactory.parseString(configStr)
    //ActorSystem，辅助创建和监控下面的Actor，它是单例的
    val actorSystem = ActorSystem("MasterSystem", config)

    //创建Actor,第二个参数"Master"为名字，在Worker与它建立连接时需要指明该名字
    val master = actorSystem.actorOf(Props(new Master(host, port)), "Master")

    master ! "hello"

  }
}

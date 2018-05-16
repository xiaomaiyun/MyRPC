package com.chenjian.rpc

trait RemoteMessage extends Serializable

//Worker->Master;用于封装注册消息
case class RegisterWorker(id: String, memory: Int, cores: Int) extends RemoteMessage

//Master->Worker
case class RegisteredWorker(masterUrl: String) extends RemoteMessage

//Worker->self
case object SendHeartbeat

//Master->self
case class Heartbeat(id: String) extends RemoteMessage

//Master->self
case object CheckTimeOutWorker

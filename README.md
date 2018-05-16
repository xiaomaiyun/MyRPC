# 使用akka实现一个简单的rpc框架MyRPC
[TOC]
## 运行
### 导出jar包，master节点(192.168.199.99)：
```
java -cp MyRPC-1.0-SNAPSHOT.jar com.chenjian.rpc.Master 192.168.199.99 9999
```
### slave01节点(192.168.199.100)：

```
java -cp MyRPC-1.0-SNAPSHOT.jar com.chenjian.rpc.Worker 192.168.199.100 5555 192.168.199.99 9999
```

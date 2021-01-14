# distributed-lock
基于注解的分布式锁

实现流程：

1、编写springboot接入redis基本配置，以及相关工具类

2、新增分布式锁的注解，并设置相关属性

3、新增注解对应的切面，并实现分布式锁的创建、校验及删除

4、新增分布式锁的续期Job

5、新增测试类，便于测试观察效果

流程图如下：
![image](https://github.com/YhcAndHc/distributed-lock/blob/master/image/dl_handle.png)


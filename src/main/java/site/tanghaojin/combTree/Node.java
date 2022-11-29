package site.tanghaojin.combTree;

public class Node {
  enum CStatus {
    IDLE, FIRST, SECOND, RESULT, ROOT
  };

  CStatus cStatus;
  Node parent;
  int result;
  int firstValue, secondValue;
  boolean locked;

  // root Node
  public Node() {
    cStatus = CStatus.ROOT;
    locked = false;
  }

  // non-root Node
  public Node(Node parent) {
    this.parent = parent;
    cStatus = CStatus.IDLE;
    locked = false;
  }

  synchronized boolean precombine() throws InterruptedException {
    while (locked) wait(); // 长期同步
    switch (cStatus) { // 读状态
      case IDLE: // 主动线程，将返回检查并进行组合
        cStatus = CStatus.FIRST;
        return true; // 继续向上
      case FIRST: // 被动线程，锁定结点，准备组合
        locked = true;
        cStatus = CStatus.SECOND;
        return false; // 停止推进
      case ROOT: // 到达根节点，预组合阶段结束
        return false;
      default:
        throw new UnsupportedOperationException("precombine: unknown Node state " + cStatus);
    }
  }

  synchronized int combine(int combined) throws InterruptedException {
    // 等待该结点的被动线程设置 secondValue
    while (locked) wait();
    locked = true; // 尝试组合
    firstValue = combined; // 当前的组合值
    switch (cStatus) { // 检查状态
      case FIRST: // 仅一个线程访问过该结点
        return firstValue;
      case SECOND: // 被动线程到达，组合
        return firstValue + secondValue;
      default: // 无其他可能
        throw new UnsupportedOperationException("combine: unknown Node state " + cStatus);
    }
  }

  synchronized int op(int combined) throws InterruptedException {
    switch (cStatus) {
      case ROOT:
        int prior = result;
        result += combined;
        return prior; // 将组合增量累加至计数器（根结点）
      case SECOND:
        secondValue = combined; // 被动线程，参与组合
        locked = false;
        notifyAll(); // 释放锁
        while (cStatus != CStatus.RESULT) wait(); // 等待主动线程组合
        // 释放锁，重置结点，唤醒其它线程启动下一轮组合
        locked = false;
        cStatus = CStatus.IDLE;
        notifyAll();
        return result;
      default:
        throw new UnsupportedOperationException("op: unknown Node state " + cStatus);
    }
  }

  synchronized void distribute(int prior) {
    switch (cStatus) {
      case FIRST: // 没有第二个进程进入，解锁并重置
        cStatus = CStatus.IDLE;
        locked = false;
        break;
      case SECOND: // 通知第二个进程 result 可用
        result = prior + firstValue;
        cStatus = CStatus.RESULT;
        break;
      default:
        throw new UnsupportedOperationException("distribute: unknown Node state " + cStatus);
    }
    notifyAll();
  }
}

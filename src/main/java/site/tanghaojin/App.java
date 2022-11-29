package site.tanghaojin;
import site.tanghaojin.combTree.CombiningTree;

public class App {
  static int THREAD_NUM = 10;
  static int TASK_PER_THREAD = 100000;
  public static void main(String[] args) throws InterruptedException {
    CombiningTree tree = new CombiningTree(THREAD_NUM);
    Runnable threadFunc = () -> {
      int threadID = Integer.parseInt(Thread.currentThread().getName());
      for (int i = 0; i < TASK_PER_THREAD; ++i) {
        try {
          tree.getAndIncrement(threadID);
        } catch (InterruptedException __) { }
      }
    };
    Thread[] threads = new Thread[THREAD_NUM];
    for (int i = 0; i < THREAD_NUM; ++i) {
      threads[i] = new Thread(threadFunc, String.valueOf(i));
    }

    long start = System.currentTimeMillis();
    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    long end = System.currentTimeMillis();
    long time = end - start;
    System.out.println("counter: " + (tree.get()));
    System.out.println("time: " + (time) + " ms");
  }
}

/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * A synchronization aid that allows one or more threads to wait until
 * a set of operations being performed in other threads completes.
 *
 * <p>A {@code CountDownLatch} is initialized with a given <em>count</em>.
 * The {@link #await await} methods block until the current count reaches
 * zero due to invocations of the {@link #countDown} method, after which
 * all waiting threads are released and any subsequent invocations of
 * {@link #await await} return immediately.  This is a one-shot phenomenon
 * -- the count cannot be reset.  If you need a version that resets the
 * count, consider using a {@link CyclicBarrier}.
 *
 * <p>A {@code CountDownLatch} is a versatile synchronization tool
 * and can be used for a number of purposes.  A
 * {@code CountDownLatch} initialized with a count of one serves as a
 * simple on/off latch, or gate: all threads invoking {@link #await await}
 * wait at the gate until it is opened by a thread invoking {@link
 * #countDown}.  A {@code CountDownLatch} initialized to <em>N</em>
 * can be used to make one thread wait until <em>N</em> threads have
 * completed some action, or some action has been completed N times.
 *
 * <p>A useful property of a {@code CountDownLatch} is that it
 * doesn't require that threads calling {@code countDown} wait for
 * the count to reach zero before proceeding, it simply prevents any
 * thread from proceeding past an {@link #await await} until all
 * threads could pass.
 *
 * <p><b>Sample usage:</b> Here is a pair of classes in which a group
 * of worker threads use two countdown latches:
 * <ul>
 * <li>The first is a start signal that prevents any worker from proceeding
 * until the driver is ready for them to proceed;
 * <li>The second is a completion signal that allows the driver to wait
 * until all workers have completed.
 * </ul>
 *
 *  <pre> {@code
 * class Driver { // ...
 *   void main() throws InterruptedException {
 *     CountDownLatch startSignal = new CountDownLatch(1);
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       new Thread(new Worker(startSignal, doneSignal)).start();
 *
 *     doSomethingElse();            // don't let run yet
 *     startSignal.countDown();      // let all threads proceed
 *     doSomethingElse();
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class Worker implements Runnable {
 *   private final CountDownLatch startSignal;
 *   private final CountDownLatch doneSignal;
 *   Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
 *     this.startSignal = startSignal;
 *     this.doneSignal = doneSignal;
 *   }
 *   public void run() {
 *     try {
 *       startSignal.await();
 *       doWork();
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }}</pre>
 *
 * <p>Another typical usage would be to divide a problem into N parts,
 * describe each part with a Runnable that executes that portion and
 * counts down on the latch, and queue all the Runnables to an
 * Executor.  When all sub-parts are complete, the coordinating thread
 * will be able to pass through await. (When threads must repeatedly
 * count down in this way, instead use a {@link CyclicBarrier}.)
 *
 *  <pre> {@code
 * class Driver2 { // ...
 *   void main() throws InterruptedException {
 *     CountDownLatch doneSignal = new CountDownLatch(N);
 *     Executor e = ...
 *
 *     for (int i = 0; i < N; ++i) // create and start threads
 *       e.execute(new WorkerRunnable(doneSignal, i));
 *
 *     doneSignal.await();           // wait for all to finish
 *   }
 * }
 *
 * class WorkerRunnable implements Runnable {
 *   private final CountDownLatch doneSignal;
 *   private final int i;
 *   WorkerRunnable(CountDownLatch doneSignal, int i) {
 *     this.doneSignal = doneSignal;
 *     this.i = i;
 *   }
 *   public void run() {
 *     try {
 *       doWork(i);
 *       doneSignal.countDown();
 *     } catch (InterruptedException ex) {} // return;
 *   }
 *
 *   void doWork() { ... }
 * }}</pre>
 *
 * <p>Memory consistency effects: Until the count reaches
 * zero, actions in a thread prior to calling
 * {@code countDown()}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions following a successful return from a corresponding
 * {@code await()} in another thread.
 *
 * @since 1.5
 * @author Doug Lea
 *
 * CountDownLatch是并发包中的一个工具类，它的典型应用场景为：一个线程等待几个线程执行，
 * 待这几个线程结束后，该线程再继续执行。
 *
 * 简单起见，可以把它理解为一个倒数的计时器：初始值为线程数，每个线程结束时执行减1操作，
 * 当计数器减到0时等待的线程再继续执行。
 *
 * 场景举例：
 * 场景1：一个线程等待多个线程执行完之后再继续执行
 *
 *  public void test() throws InterruptedException{
 *      int count = 5;
 *      // CountDownLatch 的初始化计数器为 5
 *      // 注意线程数和计数器保持一致
 *      CountDownLatch countDownLatch = new CountDownLatch(count
 *      for （int i = 0; i < count; i++){
 *          int finalI = i;
 *          new Thread(() -> {
 *              try{
 *                  TimeUnit.SECONDS.sleep(finalI);
 *              }catch (InterruptedException e){
 *                  e.printStackTrace();
 *              }
 *              System.out.println(Thread.currentThread().getName() + " is working ...");
 *              // 每个线程执行结束时执行 countDown
 *              countDownLatch.countDown();
 *          }).start();
 *      }
 *      // 主线程进入等待状态（尝试获取资源，成功后才能继续执行）
 *      countDownLatch.await();
 *      System.out.println(Thread.currentThread().getName() + " go on ...");
 *  }
 *
 *  输出结果：
 *      Thread-0 is working ...
 *      Thread-1 is working ...
 *      Thread-2 is working ...
 *      Thread-3 is working ...
 *      Thread-4 is working ...
 *      main go on ...
 *
 *  场景2：一个线程到达指定条件后，通知另一个线程
 *  private static volatile List<Integer> list = new ArrayList<>();
 *
 *  private static void test() {
 *      CountDownLatch countDownLatch = new CountDownLatch(1);
 *
 *      new Thread(() -> {
 *          if (list.size != 5) {
 *              try{
 *                  // list 的大小为 5 时再继续执行，否则等待
 *                  // 等待 state 减到 0
 *                  countDownLatch.await();
 *              }catch (InterruptedException e){
 *                  e.printStackTrace();
 *              }
 *          }
 *          System.out.println(Thread.currentThread().getName() + " start ...");
 *      }).start();
 *
 *      new Thread(() -> {
 *          for(int i = 0; i < 10; i++){
 *              list.add(i);
 *              System.out.println(Thread.currentThread().getName() + " add " + i);
 *              if(list.size() == 5){
 *                  // 满足条件时将 state 减 1
 *                  countDownLatch.countDown();
 *              }
 *              try{
 *                  TimeUnit.SECONDS.sleep(1);
 *              }catch(InterruptedException e){
 *                  e.printStackTrace();
 *              }
 *          }
 *      }).start();
 *  }
 *
 *  输出结果：
 *      Thread-1 add 0
 *      Thread-1 add 1
 *      Thread-1 add 2
 *      Thread-1 add 3
 *      Thread-1 add 4
 *      Thread-0 start ...
 *      Thread-1 add 5
 *      Thread-1 add 6
 *      Thread-1 add 7
 *      Thread-1 add 8
 *      Thread-1 add 9
 *
 */
public class CountDownLatch {
    /**
     * Synchronization control For CountDownLatch.
     * Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        // 构造器，初始化AQS的 state 变量
        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        // 尝试获取资源的操作，只有当 state 变量为0 的时候才能获取成功（返回 1）
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        // 尝试释放资源的操作
        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            // 死循环
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                // 该操作就是尝试把 state 变量减去 1
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    /**
     * Sync 继承了AQS抽象类，根据AQS可知，acquireSharedInterruptibly和tryAcquireSharedNanos方法的实现
     * 都调用了tryAcquireShared。
     *
     * 流程说明：通常先把CountDownLatch的计数器(state)初始化为N，执行await操作就是尝试以共享模式获取资源，
     * 而每次countDown操作就是将 N 减去 1，只有当 N 减到 0 的时候，才能获取成功(tryAcquireShard方法)，然后继续执行。
     */

    private final Sync sync;

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked
     *        before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     *
     * 构造器（该构造器是唯一的）传入一个正整数，且初始化了sync变量，Sync是内部的一个嵌套类，
     * 继承自AQS
     */
    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * 常用方法：
     * 1. await();
     * 2. await(long, TimeUnit);
     * 3. countDown.
     * 其中两个await都是让当前线程进入等待状态（获取资源失败）；
     * 而countDown方法是将计数器减去1，当计数器为0的时候，那些处于
     * 等待状态的线程会继续执行（获取资源成功）
     */

    /**
     * Causes the current thread to wait until the latch has counted down to
     * zero, unless the thread is {@linkplain Thread#interrupt interrupted}.
     *
     * <p>If the current count is zero then this method returns immediately.
     *
     * <p>If the current count is greater than zero then the current
     * thread becomes disabled for thread scheduling purposes and lies
     * dormant until one of two things happen:
     * <ul>
     * <li>The count reaches zero due to invocations of the
     * {@link #countDown} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     *
     * await()调用acquireSharedInterruptibly，它是AQS中【共享模式】的方法，出现异常会响应中断
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * Causes the current thread to wait until the latch has counted down to
     * zero, unless the thread is {@linkplain Thread#interrupt interrupted},
     * or the specified waiting time elapses.
     *
     * <p>If the current count is zero then this method returns immediately
     * with the value {@code true}.
     *
     * <p>If the current count is greater than zero then the current
     * thread becomes disabled for thread scheduling purposes and lies
     * dormant until one of three things happen:
     * <ul>
     * <li>The count reaches zero due to invocations of the
     * {@link #countDown} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     *
     * <p>If the count reaches zero then the method returns with the
     * value {@code true}.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if the count reached zero and {@code false}
     *         if the waiting time elapsed before the count reached zero
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     *
     * await(long timeout, TimeUnit unit)调用tryAcquireSharedNanos，它是AQS中【共享模式】的常用方法，
     * 出现异常会响应中断，并且有超时等待
     */
    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * Decrements the count of the latch, releasing all waiting threads if
     * the count reaches zero.
     *
     * <p>If the current count is greater than zero then it is decremented.
     * If the new count is zero then all waiting threads are re-enabled for
     * thread scheduling purposes.
     *
     * <p>If the current count equals zero then nothing happens.
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * Returns the current count.
     *
     * <p>This method is typically used for debugging and testing purposes.
     *
     * @return the current count
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * Returns a string identifying this latch, as well as its state.
     * The state, in brackets, includes the String {@code "Count ="}
     * followed by the current count.
     *
     * @return a string identifying this latch, as well as its state
     */
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}

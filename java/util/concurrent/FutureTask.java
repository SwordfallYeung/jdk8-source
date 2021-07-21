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
import java.util.concurrent.locks.LockSupport;

/**
 * A cancellable asynchronous computation.  This class provides a base
 * implementation of {@link Future}, with methods to start and cancel
 * a computation, query to see if the computation is complete, and
 * retrieve the result of the computation.  The result can only be
 * retrieved when the computation has completed; the {@code get}
 * methods will block if the computation has not yet completed.  Once
 * the computation has completed, the computation cannot be restarted
 * or cancelled (unless the computation is invoked using
 * {@link #runAndReset}).
 *
 * <p>A {@code FutureTask} can be used to wrap a {@link Callable} or
 * {@link Runnable} object.  Because {@code FutureTask} implements
 * {@code Runnable}, a {@code FutureTask} can be submitted to an
 * {@link Executor} for execution.
 *
 * <p>In addition to serving as a standalone class, this class provides
 * {@code protected} functionality that may be useful when creating
 * customized task classes.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> The result type returned by this FutureTask's {@code get} methods
 *
 * FutureTask是一个可取消的、异步执行任务的类
 *
 * 它实现了RunnableFuture接口，而该接口又继承了Runnable接口和Future接口，
 * 因此FutureTask也具有这两个接口所定义的特征。FutureTask的主要功能：
 * 1. 异步执行任务，并且任务只执行一次；
 * 2. 监控任务是否完成，取消任务；
 * 3. 获取任务执行结果。
 *
 *  FutureTask适合多线程执行一些耗时的操作，然后获取执行结果。下面结合线程池简单分析其用法，示例代码如下：
 *  public class FutureTaskTest{
 *     public static void main(String[] args) throws Exception{
 *           ExecutorService executorService = Executors.newFixedThreadPool(5);
 *           List<FutureTask<Integer>> taskList = new ArrayList<>();
 *           for(int i = 0; i < 10; i++){
 *              int finalI = i;
 *              FutureTask<Integer> futureTask = new FutureTask<>(() -> {
 *                 // 模拟耗时任务
 *                 TimeUnit.SECONDS.sleep(finalI * 2);
 *                 System.out.println(Thread.currentThread().getName() + "计算中......");
 *                 return finalI * finalI;
 *              });
 *              taskList.add(futureTask);
 *              executorService.submit(futureTask); // 提交到线程池
 *           }
 *
 *           System.out.println("任务全部提交，主线程做其他操作");
 *           // 获取执行结果
 *           for(FutureTask<Integer> futureTask: taskList){
 *              Integer result = futureTask.get();
 *              System.out.println("result--->" + result);
 *           }
 *           // 关闭线程池
 *           executorService.shutdown();
 *     }
 *  }
 *
 * 小结：
 *  FutureTask是一个封装任务（Runnable或Callable）的类，可以异步执行任务，并获取执行结果，适用于耗时操作场景。
 */
public class FutureTask<V> implements RunnableFuture<V> {
    /*
     * Revision notes: This differs from previous versions of this
     * class that relied on AbstractQueuedSynchronizer, mainly to
     * avoid surprising users about retaining interrupt status during
     * cancellation races. Sync control in the current design relies
     * on a "state" field updated via CAS to track completion, along
     * with a simple Treiber stack to hold waiting threads.
     *
     * Style note: As usual, we bypass overhead of using
     * AtomicXFieldUpdaters and instead directly use Unsafe intrinsics.
     */

    /**
     * The run state of this task, initially NEW.  The run state
     * transitions to a terminal state only in methods set,
     * setException, and cancel.  During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)). Transitions from these intermediate to final
     * states use cheaper ordered/lazy writes because values are unique
     * and cannot be further modified.
     *
     * Possible state transitions:
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     *
     * 任务的状态
     * 其中state表示任务的状态，总共有7种，它们之间的状态转换可能有以下4种情况：
     * 1. 任务执行正常：NEW -> COMPLETING -> NORMAL
     * 2. 任务执行异常：NEW -> COMPLETING -> EXCEPTIONAL
     * 3. 任务取消：   NEW -> CANCELLED
     * 4. 任务中断：   NEW -> INTERRUPTING -> INTERRUPTED
     */
    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;

    /** The underlying callable; nulled out after running */
    // 提交的任务
    private Callable<V> callable;
    /** The result to return or exception to throw from get() */
    // get() 方法返回的结果（或者异常）
    private Object outcome; // non-volatile, protected by state reads/writes
    /** The thread running the callable; CASed during run() */
    // 执行任务的线程
    private volatile Thread runner;
    /** Treiber stack of waiting threads */
    // 等待线程的 Treiber 栈
    private volatile WaitNode waiters;

    /**
     * Returns result or throws exception for completed task.
     *
     * @param s completed state value
     *
     * 封装返回结果
     *
     * 该方法就是对返回结果的包装，无论是正常结束或是抛出异常。
     */
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        // 输出结果赋值
        Object x = outcome;
        // 正常结束
        if (s == NORMAL)
            return (V)x;
        // 取消
        if (s >= CANCELLED)
            throw new CancellationException();
        // 执行异常
        throw new ExecutionException((Throwable)x);
    }

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Callable}.
     *
     * @param  callable the callable task
     * @throws NullPointerException if the callable is null
     *
     * 创建一个FutureTask对象，在运行时将执行给定的Callable
     */
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable
    }

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Runnable}, and arrange that {@code get} will return the
     * given result on successful completion.
     *
     * @param runnable the runnable task
     * @param result the result to return on successful completion. If
     * you don't need a particular result, consider using
     * constructions of the form:
     * {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException if the runnable is null
     *
     * 创建一个FutureTask，在运行时执行给定的Runnable
     * 并安排get将在成功完成时返回给定的结果
     */
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;       // ensure visibility of callable
    }

    /**
     * 这两个构造器分别传入Callable对象和Runnable对象（适配为Callable对象），然后将其状态初始化NEW
     */

    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    public boolean isDone() {
        return state != NEW;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!(state == NEW &&
              UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                  mayInterruptIfRunning ? INTERRUPTING : CANCELLED)))
            return false;
        try {    // in case call to interrupt throws exception
            if (mayInterruptIfRunning) {
                try {
                    // 若允许中断，则尝试中断线程
                    Thread t = runner;
                    if (t != null)
                        t.interrupt();
                } finally { // final state
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();
        }
        return true;
    }

    /**
     * @throws CancellationException {@inheritDoc}
     *
     * 获取执行结果（阻塞式）
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        // 若任务未执行完，则等待它执行完成
        if (s <= COMPLETING)
            // 任务未完成
            s = awaitDone(false, 0L);
        // 封装返回结果
        return report(s);
    }

    /**
     * @throws CancellationException {@inheritDoc}
     *
     * 获取执行结果（有超时等待）
     */
    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING &&
            (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    /**
     * 这两个方法都是获取任务执行的结果，原理也基本一样，区别在于后者有超时等待（超时会抛出TimeoutException异常）
     */

    /**
     * Protected method invoked when this task transitions to state
     * {@code isDone} (whether normally or via cancellation). The
     * default implementation does nothing.  Subclasses may override
     * this method to invoke completion callbacks or perform
     * bookkeeping. Note that you can query status inside the
     * implementation of this method to determine whether this task
     * has been cancelled.
     */
    protected void done() { }

    /**
     * Sets the result of this future to the given value unless
     * this future has already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon successful completion of the computation.
     *
     * @param v the value
     */
    protected void set(V v) {
        // CAS 将state修改为COMPLETING，该状态是一个中间状态
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            // 输出结果赋值
            outcome = v;
            // 将state更新为NORMAL
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
            finishCompletion();
        }
    }

    /**
     * Causes this future to report an {@link ExecutionException}
     * with the given throwable as its cause, unless this future has
     * already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon failure of the computation.
     *
     * @param t the cause of failure
     */
    protected void setException(Throwable t) {
        // CAS 将 state 修改为COMPLETING，该状态是一个中间状态
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            // 输出结果赋值
            outcome = t;
            // 将state更新为EXCEPTIONAL
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
            finishCompletion();
        }
    }

    public void run() {
        // 使用CAS进行并发控制，防止任务被执行多次
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    // 调用Callable的call方法执行任务
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    // 异常处理
                    result = null;
                    ran = false;
                    setException(ex);
                }
                // 正常处理
                if (ran)
                    set(result);
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            // 线程被中断
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

    /**
     * Executes the computation without setting its result, and then
     * resets this future to initial state, failing to do so if the
     * computation encounters an exception or is cancelled.  This is
     * designed for use with tasks that intrinsically execute more
     * than once.
     *
     * @return {@code true} if successfully run and reset
     */
    protected boolean runAndReset() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    /**
     * Ensures that any interrupt from a possible cancel(true) is only
     * delivered to a task while in run or runAndReset.
     */
    private void handlePossibleCancellationInterrupt(int s) {
        // It is possible for our interrupter to stall before getting a
        // chance to interrupt us.  Let's spin-wait patiently.
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield(); // wait out pending interrupt

        // assert state == INTERRUPTED;

        // We want to clear any interrupt we may have received from
        // cancel(true).  However, it is permissible to use interrupts
        // as an independent mechanism for a task to communicate with
        // its caller, and there is no way to clear only the
        // cancellation interrupt.
        //
        // Thread.interrupted();
    }

    /**
     * Simple linked list nodes to record waiting threads in a Treiber
     * stack.  See other classes such as Phaser and SynchronousQueue
     * for more detailed explanation.
     *
     * 内部嵌套类WaitNode
     *
     * 代码比较简单，就是对Thread的封装，可以理解为单链表的节点
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }

    /**
     * Removes and signals all waiting threads, invokes done(), and
     * nulls out callable.
     *
     * 作用就是唤醒栈中所有等待的线程，并清空栈。其中的done方法实现为空
     */
    private void finishCompletion() {
        // assert state > COMPLETING;
        for (WaitNode q; (q = waiters) != null;) {
            // 将waiters置空
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                for (;;) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        // 唤醒WaitNode封装的线程
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }
        // 子类可以重写该方法实现回调功能
        done();

        callable = null;        // to reduce footprint
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     *
     * @param timed true if use timed waits
     * @param nanos time to wait, if timed
     * @return state upon completion
     *
     * 该方法的主要判断步骤如下：
     * 1. 若线程被中断，则响应中断；
     * 2. 若任务已完成，则返回状态值；
     * 3. 若任务正在执行，则让出CPU时间片
     * 4. 若任务未执行，则将当前线程封装为WaitNode节点；
     * 5. 若WaitNode未入栈，则执行入栈；
     * 6. 若已入栈，则将线程挂起。
     *
     * 以上步骤是循环执行的，其实该方法的主要作用就是：当任务执行完成时，返回状态值；否则将当前线程挂起。
     */
    private int awaitDone(boolean timed, long nanos)
        throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        WaitNode q = null;
        boolean queued = false;
        for (;;) {
            // 响应线程中断
            if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            // s > COMPLETING 表示任务已执行完成（包括正常执行、异常等状态）
            // 则返回对应的状态值
            if (s > COMPLETING) {
                if (q != null)
                    q.thread = null;
                return s;
            }
            // s == COMPLETING 是一个中间状态，表示任务尚未完成
            // 这里让出 CPU 时间片
            else if (s == COMPLETING) // cannot time out yet
                Thread.yield();
            // 执行到这里，表示 s == NEW，将当前线程封装为一个 WaitNode 节点
            else if (q == null)
                q = new WaitNode();
            // 这里表示 q 并未入栈，CAS方式将当 WaitNode 入栈
            else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                     q.next = waiters, q);
            // 有超时的情况
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(q);
                    return state;
                }
                LockSupport.parkNanos(this, nanos);
            }
            // 将当前线程挂起
            else
                LockSupport.park(this);
        }
    }

    /**
     * Tries to unlink a timed-out or interrupted wait node to avoid
     * accumulating garbage.  Internal nodes are simply unspliced
     * without CAS since it is harmless if they are traversed anyway
     * by releasers.  To avoid effects of unsplicing from already
     * removed nodes, the list is retraversed in case of an apparent
     * race.  This is slow when there are a lot of nodes, but we don't
     * expect lists to be long enough to outweigh higher-overhead
     * schemes.
     *
     * 移除栈中的节点
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (;;) {          // restart on removeWaiter race
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) // check for race
                            continue retry;
                    }
                    else if (!UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                          q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}

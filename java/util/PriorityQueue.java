/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

import java.util.function.Consumer;

/**
 * An unbounded priority {@linkplain Queue queue} based on a priority heap.
 * The elements of the priority queue are ordered according to their
 * {@linkplain Comparable natural ordering}, or by a {@link Comparator}
 * provided at queue construction time, depending on which constructor is
 * used.  A priority queue does not permit {@code null} elements.
 * A priority queue relying on natural ordering also does not permit
 * insertion of non-comparable objects (doing so may result in
 * {@code ClassCastException}).
 *
 * <p>The <em>head</em> of this queue is the <em>least</em> element
 * with respect to the specified ordering.  If multiple elements are
 * tied for least value, the head is one of those elements -- ties are
 * broken arbitrarily.  The queue retrieval operations {@code poll},
 * {@code remove}, {@code peek}, and {@code element} access the
 * element at the head of the queue.
 *
 * <p>A priority queue is unbounded, but has an internal
 * <i>capacity</i> governing the size of an array used to store the
 * elements on the queue.  It is always at least as large as the queue
 * size.  As elements are added to a priority queue, its capacity
 * grows automatically.  The details of the growth policy are not
 * specified.
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.  The Iterator provided in method {@link
 * #iterator()} is <em>not</em> guaranteed to traverse the elements of
 * the priority queue in any particular order. If you need ordered
 * traversal, consider using {@code Arrays.sort(pq.toArray())}.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * Multiple threads should not access a {@code PriorityQueue}
 * instance concurrently if any of the threads modifies the queue.
 * Instead, use the thread-safe {@link
 * java.util.concurrent.PriorityBlockingQueue} class.
 *
 * <p>Implementation note: this implementation provides
 * O(log(n)) time for the enqueuing and dequeuing methods
 * ({@code offer}, {@code poll}, {@code remove()} and {@code add});
 * linear time for the {@code remove(Object)} and {@code contains(Object)}
 * methods; and constant time for the retrieval methods
 * ({@code peek}, {@code element}, and {@code size}).
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @since 1.5
 * @author Josh Bloch, Doug Lea
 * @param <E> the type of elements held in this collection
 *
 * PriorityQueue意为优先队列，表示队列中的元素是有优先级的，也就是说元素之间是可比较的。
 * 因此，插入队列的元素要实现Comparable接口或者Comparator接口。
 *
 * PriorityQueue没有实现BlockingQueue接口，并非阻塞队列。它在逻辑上使用【堆】（即完全二叉树）结构实现，
 * 物理上基于【动态数组】存储，如图所示：
 *
 *                  1
 *                /  \
 *      堆结构：   3    2
 *              / \  / \
 *             5  4 6
 *  数组存储结构：
 *             —————————————--
 *             |1|3|2|5|4|6|..
 *             —————————————--
 *
 * 示例代码一：
 *  private static void test(){
 *      // 不指定比较器（默认从小到大排序）
 *      Queue<Integer> queue = new PriorityQueue<>();
 *      for(int i = 0; i< 10; i++){
 *           queue.add(random.nextInt(100));
 *      }
 *      while(!queue.isEmpty()){
 *          System.out.print(queue.poll() + ".");
 *      }
 *  }
 *
 *  输出结果（仅供参考）：
 *      2,13,14,36,39,40,43,55,83,88
 *
 * 示例二：指定比较器（Comparator）
 *  private static void test(){
 *      // 指定比较器（从大到小排序）
 *      Queue<Integer> queue = new PriorityQueue<>(11, (o1, o2) -> o2 - o1);
 *      for(int i = 0; i< 10; i++){
 *           queue.add(random.nextInt(100));
 *      }
 *      while(!queue.isEmpty()){
 *          System.out.print(queue.poll() + ".");
 *      }
 *  }
 *  输出结果（仅供参考）：
 *      76,74,71,69,52,49,41,41,35,1
 *
 * 示例三：求Top N
 *  public class FixedPriorityQueue{
 *      private PriorityQueue<Integer> queue;
 *      private int maxSize;
 *
 *      public FixedPriorityQueue(int maxSize){
 *           this.maxSize = maxSize;
 *           // 初始化优先队列及比较器
 *           // 这里是从大到小（可调整）
 *           this.queue = new PriorityQueue<>(maxSize, (o2, o1) -> o2.compareTo(o1));
 *      }
 *
 *      public void add(Integer i){
 *           // 队列未满时，直接插入
 *           if(queue.size() < maxSize){
 *              queue.add(i);
 *           }else{
 *              // 队列已满，将待插入元素与最小值比较
 *              Integer peek = queue.peek();
 *              if(i.compareTo(peek) > 0){
 *                 // 大于最小值，将最小值移除，该元素插入
 *                queue.poll();
 *                queue.add(i);
 *              }
 *           }
 *      }
 *
 * public static void main(String[] args) {
 *     FixedPriorityQueue fixedQueue = new FixedPriorityQueue(10);
 *     for (int i = 1; i <= 100; i++) {
 *       fixedQueue.add(i);
 *     }
 *
 *     Iterable<Integer> iterable = () -> fixedQueue.queue.iterator();
 *     System.out.println("队列中的元素：");
 *     for (Integer integer : iterable) {
 *       System.out.print(integer + ", ");
 *     }
 *
 *     System.out.println();
 *     System.out.println("最大的 10 个：");
 *     while (!fixedQueue.queue.isEmpty()) {
 *       System.out.print(fixedQueue.queue.poll() + ", ");
 *     }
 *   }
 *  }
 *
 *  输出结果：
 *      队列中的元素：
 *      91, 92, 94, 93, 96, 95, 99, 97, 98, 100,
 *      最大的 10 个：
 *      91, 92, 93, 94, 95, 96, 97, 98, 99, 100,
 *
 * 小结：
 * 1. PriorityQueue为优先队列，实现了Queue接口，但并非阻塞队列；
 * 2. 内部的元素是可比较的（Comparable或Comparator），元素不能为空；
 * 3. 逻辑上使用【堆】（即完全二叉树）结构实现，物理上基于【动态数组】存储；
 * 4. PriorityQueue可用作求解Top N问题。
 */
public class PriorityQueue<E> extends AbstractQueue<E>
    implements java.io.Serializable {

    private static final long serialVersionUID = -7720805057305804111L;

    // 数据的默认初始容量
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    /**
     * Priority queue represented as a balanced binary heap: the two
     * children of queue[n] are queue[2*n+1] and queue[2*(n+1)].  The
     * priority queue is ordered by comparator, or by the elements'
     * natural ordering, if comparator is null: For each node n in the
     * heap and each descendant d of n, n <= d.  The element with the
     * lowest value is in queue[0], assuming the queue is nonempty.
     *
     * 内部数组，用于存储队列中的元素
     */
    transient Object[] queue; // non-private to simplify nested class access

    /**
     * The number of elements in the priority queue.
     *
     * 队列中元素的个数
     */
    private int size = 0;

    /**
     * The comparator, or null if priority queue uses elements'
     * natural ordering.
     *
     * 队列中元素的比较器
     */
    private final Comparator<? super E> comparator;

    /**
     * The number of times this priority queue has been
     * <i>structurally modified</i>.  See AbstractList for gory details.
     *
     * 结构性修改次数
     */
    transient int modCount = 0; // non-private to simplify nested class access

    /**
     * Creates a {@code PriorityQueue} with the default initial
     * capacity (11) that orders its elements according to their
     * {@linkplain Comparable natural ordering}.
     *
     * 构造器1：无参构造器（默认初始容量为11）
     */
    public PriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    /**
     * Creates a {@code PriorityQueue} with the specified initial
     * capacity that orders its elements according to their
     * {@linkplain Comparable natural ordering}.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @throws IllegalArgumentException if {@code initialCapacity} is less
     *         than 1
     *
     * 构造器2：指定容量的构造器
     */
    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * Creates a {@code PriorityQueue} with the default initial capacity and
     * whose elements are ordered according to the specified comparator.
     *
     * @param  comparator the comparator that will be used to order this
     *         priority queue.  If {@code null}, the {@linkplain Comparable
     *         natural ordering} of the elements will be used.
     * @since 1.8
     *
     * 构造器3：指定比较器的构造器
     */
    public PriorityQueue(Comparator<? super E> comparator) {
        this(DEFAULT_INITIAL_CAPACITY, comparator);
    }

    /**
     * Creates a {@code PriorityQueue} with the specified initial capacity
     * that orders its elements according to the specified comparator.
     *
     * @param  initialCapacity the initial capacity for this priority queue
     * @param  comparator the comparator that will be used to order this
     *         priority queue.  If {@code null}, the {@linkplain Comparable
     *         natural ordering} of the elements will be used.
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *         less than 1
     *
     * 构造器3：指定初始容量和比较器的构造器
     */
    public PriorityQueue(int initialCapacity,
                         Comparator<? super E> comparator) {
        // Note: This restriction of at least one is not actually needed,
        // but continues for 1.5 compatibility
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.queue = new Object[initialCapacity];
        this.comparator = comparator;
    }

    /**
     * Creates a {@code PriorityQueue} containing the elements in the
     * specified collection.  If the specified collection is an instance of
     * a {@link SortedSet} or is another {@code PriorityQueue}, this
     * priority queue will be ordered according to the same ordering.
     * Otherwise, this priority queue will be ordered according to the
     * {@linkplain Comparable natural ordering} of its elements.
     *
     * @param  c the collection whose elements are to be placed
     *         into this priority queue
     * @throws ClassCastException if elements of the specified collection
     *         cannot be compared to one another according to the priority
     *         queue's ordering
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     *
     * 构造器4：用给定集合初始化PriorityQueue对象
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(Collection<? extends E> c) {
        // 如果集合是 SortedSet 类型
        if (c instanceof SortedSet<?>) {
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            initElementsFromCollection(ss);
        }
        // 如果集合是 PriorityQueue 类型
        else if (c instanceof PriorityQueue<?>) {
            PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            initFromPriorityQueue(pq);
        }
        else {
            this.comparator = null;
            initFromCollection(c);
        }
    }

    /**
     * Creates a {@code PriorityQueue} containing the elements in the
     * specified priority queue.  This priority queue will be
     * ordered according to the same ordering as the given priority
     * queue.
     *
     * @param  c the priority queue whose elements are to be placed
     *         into this priority queue
     * @throws ClassCastException if elements of {@code c} cannot be
     *         compared to one another according to {@code c}'s
     *         ordering
     * @throws NullPointerException if the specified priority queue or any
     *         of its elements are null
     *
     * 构造器6：用给定的 PriorityQueue 初始化一个PriorityQueue
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(PriorityQueue<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        initFromPriorityQueue(c);
    }

    /**
     * Creates a {@code PriorityQueue} containing the elements in the
     * specified sorted set.   This priority queue will be ordered
     * according to the same ordering as the given sorted set.
     *
     * @param  c the sorted set whose elements are to be placed
     *         into this priority queue
     * @throws ClassCastException if elements of the specified sorted
     *         set cannot be compared to one another according to the
     *         sorted set's ordering
     * @throws NullPointerException if the specified sorted set or any
     *         of its elements are null
     *
     * 构造器7：用给定的 SortedSet 初始化 PriorityQueue
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue(SortedSet<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        initElementsFromCollection(c);
    }

    private void initFromPriorityQueue(PriorityQueue<? extends E> c) {
        // PriorityQueue，则调用initFromCollection方法转换为Object[]
        if (c.getClass() == PriorityQueue.class) {
            // 若给定的时 PriorityQueue，则直接进行初始化
            this.queue = c.toArray();
            this.size = c.size();
        } else {
            initFromCollection(c);
        }
    }

    // 集合是 SortedSet 类型 使用给定集合的元素初始化 PriorityQueue
    private void initElementsFromCollection(Collection<? extends E> c) {
        // 把集合转为数组
        Object[] a = c.toArray();
        // If c.toArray incorrectly doesn't return Object[], copy it.
        // 如果集合c的类型不是Object，则使用Arrays.copyOf转换为Object[]
        if (a.getClass() != Object[].class)
            a = Arrays.copyOf(a, a.length, Object[].class);
        int len = a.length;
        if (len == 1 || this.comparator != null)
            // 确保集合中每个元素不能为空
            for (int i = 0; i < len; i++)
                if (a[i] == null)
                    throw new NullPointerException();
        // 初始化 queue 数组和 size
        this.queue = a;
        this.size = a.length;
    }

    /**
     * Initializes queue array with elements from the given Collection.
     *
     * @param c the collection
     */
    private void initFromCollection(Collection<? extends E> c) {
        // 将集合中的元素转为数组，并赋值给 queue（上面已分析）
        initElementsFromCollection(c);
        // 堆化
        heapify();
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Increases the capacity of the array.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // 原先容量
        int oldCapacity = queue.length;
        // Double size if small; else grow by 50%
        // 原容量较小时，扩大为原先的两倍；否则扩大为原先的1.5倍
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                         (oldCapacity + 2) :
                                         (oldCapacity >> 1));
        // overflow-conscious code
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // 创建一个新的数组
        queue = Arrays.copyOf(queue, newCapacity);
    }

    /**
     * PS: 扩容操作与前文分析的 ArrayList 和 Vector 的扩容操作类似。
     */
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     *
     * 入队操作 实际是调用 offer 方法实现的
     */
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws ClassCastException if the specified element cannot be
     *         compared with elements currently in this priority queue
     *         according to the priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        modCount++;
        int i = size;
        // 如果size大于队列的长度，则扩容
        if (i >= queue.length)
            grow(i + 1);
        // 元素个数加一
        size = i + 1;
        // 原数组为空，即添加第一个元素，直接放到数组首位即可
        if (i == 0)
            queue[0] = e;
        else
            // 向上筛选
            siftUp(i, e);
        return true;
    }

    @SuppressWarnings("unchecked")
    public E peek() {
        return (size == 0) ? null : (E) queue[0];
    }

    // 遍历数组查找指定元素的索引下标
    private int indexOf(Object o) {
        if (o != null) {
            for (int i = 0; i < size; i++)
                if (o.equals(queue[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.  Returns {@code true} if and only if this queue contained
     * the specified element (or equivalently, if this queue changed as a
     * result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     *
     * 删除操作
     */
    public boolean remove(Object o) {
        // 找到需要移除的数组下标索引值
        int i = indexOf(o);
        if (i == -1)
            return false;
        else {
            removeAt(i);
            return true;
        }
    }

    /**
     * Version of remove using reference equality, not equals.
     * Needed by iterator.remove.
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if removed
     */
    boolean removeEq(Object o) {
        for (int i = 0; i < size; i++) {
            if (o == queue[i]) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * Returns an array containing all of the elements in this queue.
     * The elements are in no particular order.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
        return Arrays.copyOf(queue, size);
    }

    /**
     * Returns an array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order.
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If the queue fits in the specified array with room to spare
     * (i.e., the array has more elements than the queue), the element in
     * the array immediately following the end of the collection is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose {@code x} is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of {@code String}:
     *
     *  <pre> {@code String[] y = x.toArray(new String[0]);}</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int size = this.size;
        if (a.length < size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(queue, size, a.getClass());
        System.arraycopy(queue, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    /**
     * Returns an iterator over the elements in this queue. The iterator
     * does not return the elements in any particular order.
     *
     * @return an iterator over the elements in this queue
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<E> {
        /**
         * Index (into queue array) of element to be returned by
         * subsequent call to next.
         */
        private int cursor = 0;

        /**
         * Index of element returned by most recent call to next,
         * unless that element came from the forgetMeNot list.
         * Set to -1 if element is deleted by a call to remove.
         */
        private int lastRet = -1;

        /**
         * A queue of elements that were moved from the unvisited portion of
         * the heap into the visited portion as a result of "unlucky" element
         * removals during the iteration.  (Unlucky element removals are those
         * that require a siftup instead of a siftdown.)  We must visit all of
         * the elements in this list to complete the iteration.  We do this
         * after we've completed the "normal" iteration.
         *
         * We expect that most iterations, even those involving removals,
         * will not need to store elements in this field.
         */
        private ArrayDeque<E> forgetMeNot = null;

        /**
         * Element returned by the most recent call to next iff that
         * element was drawn from the forgetMeNot list.
         */
        private E lastRetElt = null;

        /**
         * The modCount value that the iterator believes that the backing
         * Queue should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        private int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor < size ||
                (forgetMeNot != null && !forgetMeNot.isEmpty());
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (cursor < size)
                return (E) queue[lastRet = cursor++];
            if (forgetMeNot != null) {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if (lastRetElt != null)
                    return lastRetElt;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            if (lastRet != -1) {
                E moved = PriorityQueue.this.removeAt(lastRet);
                lastRet = -1;
                if (moved == null)
                    cursor--;
                else {
                    if (forgetMeNot == null)
                        forgetMeNot = new ArrayDeque<>();
                    forgetMeNot.add(moved);
                }
            } else if (lastRetElt != null) {
                PriorityQueue.this.removeEq(lastRetElt);
                lastRetElt = null;
            } else {
                throw new IllegalStateException();
            }
            expectedModCount = modCount;
        }
    }

    public int size() {
        return size;
    }

    /**
     * Removes all of the elements from this priority queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        modCount++;
        for (int i = 0; i < size; i++)
            queue[i] = null;
        size = 0;
    }

    /**
     *  删除元素2前                 删除元素2后
     *       2               |        4
     *     /   \             |      /   \
     *    6     4            |      6     12
     *   / \   / \           |     / \
     *  10  8 12             |   10  8
     *                       |
     *  ——————————————---    |   ——————————————---
     *  |2|6|4|10|8|12|..|    |   |4|6|12|10|8|..|
     *  ——————————————---    |   ——————————————---
     *
     * 该操作的步骤大概如下：
     * 1. 移除队列的最后一个元素，并将该元素置于首位；
     * 2. 将新的“首位”元素与子节点中较小的一个比较，比较并交换位置（即执行“下沉（siftDown）”操作）。
     *
     * 出队操作：poll
     * @return
     */
    @SuppressWarnings("unchecked")
    public E poll() {
        // 队列为空时，返回null
        if (size == 0)
            return null;
        // 队列大小减一
        int s = --size;
        modCount++;
        // 队列第一个元素
        E result = (E) queue[0];
        // 队列最后一个元素
        E x = (E) queue[s];
        // 把最后一个元素置空
        queue[s] = null;
        if (s != 0)
            // 下沉，这里是把最后一个元素x放置在索引为0的数组位置上
            siftDown(0, x);
        return result;
    }

    /**
     * Removes the ith element from queue.
     *
     * Normally this method leaves the elements at up to i-1,
     * inclusive, untouched.  Under these circumstances, it returns
     * null.  Occasionally, in order to maintain the heap invariant,
     * it must swap a later element of the list with one earlier than
     * i.  Under these circumstances, this method returns the element
     * that was previously at the end of the list and is now at some
     * position before i. This fact is used by iterator.remove so as to
     * avoid missing traversing elements.
     *
     * 大概执行步骤：
     * 1. 若移除末尾元素，直接删除；
     * 2. 若非末尾元素，则将末尾元素删除，并用末尾元素替换待删除的元素；
     * 3. 堆化操作：先执行“下沉（siftDown）”操作，若该元素未“下沉”，则再执行“上浮（siftUp）”操作，
     *    使得数组删除元素后仍满足堆结构。
     */
    @SuppressWarnings("unchecked")
    private E removeAt(int i) {
        // assert i >= 0 && i < size;
        modCount++;
        int s = --size;
        // 移除末尾元素，直接置空
        if (s == i) // removed last element
            queue[i] = null;
        else {
            // 末尾元素
            E moved = (E) queue[s];
            // 删除末尾元素
            queue[s] = null;
            // 操作与poll 方法类似
            siftDown(i, moved);
            // 这里表示该节点未进行“下沉”调整，则执行“上浮”操作
            if (queue[i] == moved) {
                siftUp(i, moved);
                if (queue[i] != moved)
                    return moved;
            }
        }
        return null;
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * promoting x up the tree until it is greater than or equal to
     * its parent, or is the root.
     *
     * To simplify and speed up coercions and comparisons. the
     * Comparable and Comparator versions are separated into different
     * methods that are otherwise identical. (Similarly for siftDown.)
     *
     * @param k the position to fill
     * @param x the item to insert
     *
     * 这里简单比较下siftDown和siftUp这两个方法：
     *  1. siftDown是把指定节点与其子节点中较小的一个比较，父节点较大时“下沉（down）”;
     *  2. siftUp是把指定节点与其父节点比较，若小于父节点，则“上浮（up）”。
     */
    private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x);
        else
            siftUpComparable(k, x);
    }

    /**
     * 该方法逻辑与siftUpUsingComparator一样也是Comparator和Comparable接口的差别
     * @param k
     * @param x
     */
    @SuppressWarnings("unchecked")
    private void siftUpComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        // 不断循环，直到k小于0，循环的过程，k一直(k-1)/2，然后再把该值k=(k-1)/2
        while (k > 0) {
            // 父节点的索引 (k - 1）/ 2
            int parent = (k - 1) >>> 1;
            // 父节点的元素
            Object e = queue[parent];
            // 若该节点元素大于等于父节点，结束循环
            if (key.compareTo((E) e) >= 0)
                break;
            // 该节点元素小于父节点，则和父节点交换位置
            queue[k] = e;
            k = parent;
        }
        // 实际为queue[parent] = x
        queue[k] = key;
    }


    /**
     *       2               |        2                            2                    1
     *     /   \             |      /   \                        /   \                /   \
     *    6     4            |      6     4            ——>      6     1     ——>      6     2
     *   / \   / \           |     / \   / \                   / \   / \            / \   / \
     *  10  8 12  7(插入元素)  |   10  8  12  1（插入元素）       10  8  12  4        10  8  12  4
     *                       |
     *  ——————————————---    |   ——————————————---        ————————————————        ————————————————
     *  |2|6|4|10|8|12|7|    |   |2|6|4|10|8|12|1|    ——> |2|6|1|10|8|12|4|   ——> |1|6|2|10|8|12|4|
     *  ——————————————---    |   ——————————————---        ————————————————        ————————————————
     *
     *  其中分为左右两种情况：
     *  1. 左边插入元素为7，大于父节点4，无需和父节点交换位置，直接插入即可；
     *  2. 右边插入元素为1，小于父节点4，需要和父节点交换位置，并一直往上查找和交换，上图为渐变的数组和对应的树结构
     *
     * @param k 索引
     * @param x 索引下标对应的数组值
     */

    @SuppressWarnings("unchecked")
    private void siftUpUsingComparator(int k, E x) {
        // 不断循环，直到k小于0，循环的过程，k一直(k-1)/2，然后再把该值k=(k-1)/2
        while (k > 0) {
            // 父节点的索引 (k - 1）/ 2
            int parent = (k - 1) >>> 1;
            // 父节点的元素
            Object e = queue[parent];
            // 若该节点元素大于等于父节点，结束循环
            if (comparator.compare(x, (E) e) >= 0)
                break;
            // 该节点元素小于父节点，则和父节点交换位置
            queue[k] = e;
            k = parent;
        }
        // 实际为queue[parent] = x
        queue[k] = x;
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * demoting x down the tree repeatedly until it is less than or
     * equal to its children or is a leaf.
     *
     * @param k the position to fill 要填充的索引值
     * @param x the item to insert   索引值下标对应的数组值
     *
     * 向下筛选？暂未找到恰当的译法，但这不是重点，该方法的作用就是使数组满足堆结构
     * （其思想与冒泡排序有些类似）
     */
    private void siftDown(int k, E x) {
        // 根据 comparator 是否为空采用不同的方法
        if (comparator != null)
            siftDownUsingComparator(k, x);
        else
            siftDownComparable(k, x);
    }

    /**
     * 此方法与siftDownUsingComparator方法实现逻辑完全一样，不同的地方仅
     * 在于该方法是针对Comparable接口，而后者针对Comparator接口
     * @param k
     * @param x
     */
    @SuppressWarnings("unchecked")
    private void siftDownComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>)x;
        // 数组的中间位置
        int half = size >>> 1;        // loop while a non-leaf
        while (k < half) {
            // 获取索引为 k 的节点的左子节点索引child
            int child = (k << 1) + 1; // assume left child is least
            // 获取child的值
            Object c = queue[child];
            // 获取索引为k的节点的右子节点索引
            int right = child + 1;
            // 左子节点的值大于右子节点，则二者换位置
            if (right < size &&
                ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0)
                // 取左右子节点中较小的一个，child的索引变换为right，c也切换成右子节点的值
                c = queue[child = right];
            // 左子节点的值小于右子节点，则不需要换位置
            // c还是左子节点的值，child为左子节点索引
            // 给定的元素 x 与较小的子节点的值比较
            if (key.compareTo((E) c) <= 0)
                break;
            // 将该节点与子节点互换，将较小节点c赋给queue[k]，
            queue[k] = c;
            // 较小节点c赋值给父节点x的索引k所在位置之后，切换k为较小节点c原先的索引
            k = child;
        }
        // 这里实际是queue[child]=x
        queue[k] = key;
    }

    /**
     * 该方法的步骤大概：
     * 1. 找出给定节点（父节点）的子节点中较小的一个，并与之比较大小；
     * 2. 若父节点较大，则交换位置（父节点“下沉”）
     * @param k 索引
     * @param x 索引下标代表的数组值
     */
    @SuppressWarnings("unchecked")
    private void siftDownUsingComparator(int k, E x) {
        // 数组的中间位置
        int half = size >>> 1;
        while (k < half) {
            // 获取索引为 k 的节点的左子节点索引child
            int child = (k << 1) + 1;
            // 获取child的值
            Object c = queue[child];
            // 获取索引为k的节点的右子节点索引
            int right = child + 1;
            // 左子节点的值大于右子节点，则二者换位置
            if (right < size &&
                comparator.compare((E) c, (E) queue[right]) > 0)
                // 取左右子节点中较小的一个，child的索引变换为right，c也切换成右子节点的值
                c = queue[child = right];
            // 左子节点的值小于右子节点，则不需要换位置
            // c还是左子节点的值，child为左子节点索引
            // 给定的元素 x 与较小的子节点的值比较
            if (comparator.compare(x, (E) c) <= 0)
                break;
            // 将该节点与子节点互换，将较小节点c赋给queue[k]，
            queue[k] = c;
            // 较小节点c赋值给父节点x的索引k所在位置之后，切换k为较小节点c原先的索引
            k = child;
        }
        // 这里实际是queue[child]=x
        queue[k] = x;
    }

    /**
     * Establishes the heap invariant (described above) in the entire tree,
     * assuming nothing about the order of the elements prior to the call.
     *
     * 堆化，即将数组元素转为堆的存储结构
     *
     * PS：这里遍历时，从数组的中间位置遍历（根据堆的存储结构，如果某个节点的索引为i，
     * 则其左右子节点的索引分别为2 * i + 1, 2 * i + 2）
     */
    @SuppressWarnings("unchecked")
    private void heapify() {
        // 从数组的中间位置开始for遍历即可
        for (int i = (size >>> 1) - 1; i >= 0; i--)
            siftDown(i, (E) queue[i]);
    }

    /**
     * Returns the comparator used to order the elements in this
     * queue, or {@code null} if this queue is sorted according to
     * the {@linkplain Comparable natural ordering} of its elements.
     *
     * @return the comparator used to order this queue, or
     *         {@code null} if this queue is sorted according to the
     *         natural ordering of its elements
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /**
     * Saves this queue to a stream (that is, serializes it).
     *
     * @serialData The length of the array backing the instance is
     *             emitted (int), followed by all of its elements
     *             (each an {@code Object}) in the proper order.
     * @param s the stream
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out element count, and any hidden stuff
        s.defaultWriteObject();

        // Write out array length, for compatibility with 1.5 version
        s.writeInt(Math.max(2, size + 1));

        // Write out all elements in the "proper order".
        for (int i = 0; i < size; i++)
            s.writeObject(queue[i]);
    }

    /**
     * Reconstitutes the {@code PriorityQueue} instance from a stream
     * (that is, deserializes it).
     *
     * @param s the stream
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in (and discard) array length
        s.readInt();

        queue = new Object[size];

        // Read in all elements.
        for (int i = 0; i < size; i++)
            queue[i] = s.readObject();

        // Elements are guaranteed to be in "proper order", but the
        // spec has never explained what that might be.
        heapify();
    }

    /**
     * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
     * and <em>fail-fast</em> {@link Spliterator} over the elements in this
     * queue.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED},
     * {@link Spliterator#SUBSIZED}, and {@link Spliterator#NONNULL}.
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a {@code Spliterator} over the elements in this queue
     * @since 1.8
     */
    public final Spliterator<E> spliterator() {
        return new PriorityQueueSpliterator<E>(this, 0, -1, 0);
    }

    static final class PriorityQueueSpliterator<E> implements Spliterator<E> {
        /*
         * This is very similar to ArrayList Spliterator, except for
         * extra null checks.
         */
        private final PriorityQueue<E> pq;
        private int index;            // current index, modified on advance/split
        private int fence;            // -1 until first use
        private int expectedModCount; // initialized when fence set

        /** Creates new spliterator covering the given range */
        PriorityQueueSpliterator(PriorityQueue<E> pq, int origin, int fence,
                             int expectedModCount) {
            this.pq = pq;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi;
            if ((hi = fence) < 0) {
                expectedModCount = pq.modCount;
                hi = fence = pq.size;
            }
            return hi;
        }

        public PriorityQueueSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new PriorityQueueSpliterator<E>(pq, lo, index = mid,
                                                expectedModCount);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            PriorityQueue<E> q; Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((q = pq) != null && (a = q.queue) != null) {
                if ((hi = fence) < 0) {
                    mc = q.modCount;
                    hi = q.size;
                }
                else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (E e;; ++i) {
                        if (i < hi) {
                            if ((e = (E) a[i]) == null) // must be CME
                                break;
                            action.accept(e);
                        }
                        else if (q.modCount != mc)
                            break;
                        else
                            return;
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), lo = index;
            if (lo >= 0 && lo < hi) {
                index = lo + 1;
                @SuppressWarnings("unchecked") E e = (E)pq.queue[lo];
                if (e == null)
                    throw new ConcurrentModificationException();
                action.accept(e);
                if (pq.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
        }
    }
}

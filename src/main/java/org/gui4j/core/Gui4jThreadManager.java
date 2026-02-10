package org.gui4j.core;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gui4j.Gui4jCallBase;
import org.gui4j.Gui4jGetValue;
import org.gui4j.exception.ErrorTags;
import org.gui4j.exception.Gui4jUncheckedException;

/**
 * The Thread Manager deals with worker threads used to perform GUI actions. The intention is to
 * take a thread from a pool, use this thread to perform the necessary action and then put the
 * thread back into the pool.
 */
public final class Gui4jThreadManager implements ErrorTags, Serializable {
  private static final long serialVersionUID = 1L;
  protected static Log mLogger = LogFactory.getLog(Gui4jThreadManager.class);

  private transient ConcurrentLinkedQueue<WorkPackage> mWorkPackagesNormal;
  private transient ConcurrentLinkedQueue<WorkPackage> mWorkPackagesHighPriority;
  private transient ReentrantLock mDispatchLock;
  private transient ExecutorService mWorkerExecutor;
  private transient AtomicInteger mCreatedWorkerCount;

  protected final Gui4jInternal mGui4j;
  private final AtomicInteger mWorkPackageCountWaitingNormal = new AtomicInteger();
  private final AtomicInteger mWorkPackageCountWaitingHighPriority = new AtomicInteger();
  private final AtomicInteger mWorkPackageCountRunningNormal = new AtomicInteger();
  private final AtomicInteger mWorkPackageCountRunningHighPriority = new AtomicInteger();
  private int mMaxWorkerId;
  private int mMaxNumberOfWorkerThreads;
  private boolean mUseWorkerThreads;
  private volatile boolean mShutdownThreads;

  /**
   * Constructor for Gui4jThreadManager.
   *
   * @param gui4j
   * @param numberOfWorkerThreads
   */
  private Gui4jThreadManager(Gui4jInternal gui4j, int numberOfWorkerThreads) {
    super();
    mGui4j = gui4j;
    initializeTransientState();
    setNumberOfWorkerThreads(numberOfWorkerThreads);
  }

  /**
   * @param gui4j
   * @param numberOfWorkerThreads
   * @return a new instance of the Thread Manager. This method is used only by the class <code>Gui4j
   *     </code>.
   */
  public static Gui4jThreadManager getNewInstance(Gui4jInternal gui4j, int numberOfWorkerThreads) {
    return new Gui4jThreadManager(gui4j, numberOfWorkerThreads);
  }

  /**
   * Sets the maximum number of worker threads. The value <code>-1</code> represents an unlimited
   * number of threads. Value <code>0</code> implies always using the Swing GUI Thread. Any number
   * greater than <code>0</code> really sets the maximum number of worker threads. If there is work
   * to do and no worker is free, then the work is put into a FIFO queue and handled when worker
   * gets free.
   *
   * @param numberOfWorkerThreads
   */
  public void setNumberOfWorkerThreads(int numberOfWorkerThreads) {
    // it is not allowd to dynamically change the number of worker threads
    assert mCreatedWorkerCount.get() == 0;

    mMaxNumberOfWorkerThreads = numberOfWorkerThreads;
    mUseWorkerThreads = mMaxNumberOfWorkerThreads != 0;
    if (mUseWorkerThreads) {
      mWorkerExecutor = createWorkerExecutor();
    }
  }

  /**
   * Performs the given work. Dependant of the number of maximum worker threads, the work is either
   * performed in the same thread, or by a new worker, or put into a FIFO queue.
   *
   * @param gui4jController
   * @param work
   * @param paramMap
   */
  public void performWork(
      final Gui4jCallBase gui4jController, final Gui4jGetValue[] work, final Map paramMap) {
    performWork(gui4jController, work, paramMap, null);
  }

  /**
   * Performs the given work. Dependant of the number of maximum worker threads, the work is either
   * performed in the same thread, or by a new worker, or put into a FIFO queue.
   *
   * @param gui4jController
   * @param work
   * @param paramMap
   * @param forceExecutionInCurrentThread
   */
  public void performWork(
      final Gui4jCallBase gui4jController,
      final Gui4jGetValue[] work,
      final Map paramMap,
      boolean forceExecutionInCurrentThread) {
    performWork(gui4jController, work, paramMap, null, forceExecutionInCurrentThread);
  }

  public void performWork(
      final Gui4jCallBase gui4jController,
      final Gui4jGetValue[] work,
      final Map paramMap,
      final Gui4jComponentInstance componentInstance) {
    performWork(gui4jController, work, paramMap, componentInstance, false);
  }

  public void performWorkHighPriority(
      final Gui4jCallBase gui4jController,
      final Gui4jGetValue[] work,
      final Map paramMap,
      final Gui4jComponentInstance componentInstance) {
    performWorkHighPriority(gui4jController, work, paramMap, componentInstance, false);
  }

  /**
   * Performs the given work. Dependant of the number of maximum worker threads, the work is either
   * performed in the same thread, or by a new worker, or put into a FIFO queue.
   *
   * @param gui4jController
   * @param work
   * @param paramMap
   * @param componentInstance
   * @param forceExecutionInCurrentThread
   */
  public void performWork(
      final Gui4jCallBase gui4jController,
      final Gui4jGetValue[] work,
      final Map paramMap,
      final Gui4jComponentInstance componentInstance,
      boolean forceExecutionInCurrentThread) {
    performWork(
        gui4jController, work, paramMap, componentInstance, forceExecutionInCurrentThread, false);
  }

  /**
   * Performs the specified work and instructs the worker process to use the special success
   * handling used to implement instant validation, for example.
   *
   * @param gui4jController
   * @param work
   * @param paramMap
   * @param componentInstance
   * @param forceExecutionInCurrentThread
   */
  public void performWorkSpecialSuccessHandling(
      final Gui4jCallBase gui4jController,
      final Gui4jGetValue[] work,
      final Map paramMap,
      final Gui4jComponentInstance componentInstance,
      boolean forceExecutionInCurrentThread) {
    performWork(
        gui4jController,
        work,
        paramMap,
        componentInstance,
        forceExecutionInCurrentThread,
        false,
        true);
  }

  /**
   * Performs the given work. Dependant of the number of maximum worker threads, the work is either
   * performed in the same thread, or by a new worker, or put into a FIFO queue.
   *
   * <p>Note (KKB, 12.7.2005): This method implicitly instructs the worker thread to perform special
   * success handling as in {@link #performWorkSpecialSuccessHandling(Gui4jCallBase,
   * Gui4jGetValue[], Map, Gui4jComponentInstance, boolean)}.
   *
   * @param gui4jController
   * @param work
   * @param paramMap
   * @param componentInstance
   * @param forceExecutionInCurrentThread
   */
  public void performWorkHighPriority(
      final Gui4jCallBase gui4jController,
      final Gui4jGetValue[] work,
      final Map paramMap,
      final Gui4jComponentInstance componentInstance,
      boolean forceExecutionInCurrentThread) {
    performWork(
        gui4jController,
        work,
        paramMap,
        componentInstance,
        forceExecutionInCurrentThread,
        true,
        true);
  }

  private void performWork(
      final Gui4jCallBase gui4jController,
      final Gui4jGetValue[] work,
      final Map paramMap,
      final Gui4jComponentInstance componentInstance,
      final boolean forceExecutionInCurrentThread,
      final boolean isHighPriorityThread) {
    performWork(
        gui4jController,
        work,
        paramMap,
        componentInstance,
        forceExecutionInCurrentThread,
        isHighPriorityThread,
        false);
  }

  /**
   * Perform the given work.
   *
   * @see org.gui4j.core.Gui4jThreadManager#performWork(Gui4jCallBase,Gui4jGetValue[],Map)
   * @param gui4jController
   * @param action
   * @param paramMap
   */
  public void performWork(Gui4jCallBase gui4jController, Gui4jGetValue action, Map paramMap) {
    Gui4jGetValue[] work = {action};
    performWork(gui4jController, work, paramMap);
  }

  /**
   * Perform the given work.
   *
   * @param gui4jController
   * @param action
   * @param paramMap
   * @param componentInstance
   */
  public void performWork(
      Gui4jCallBase gui4jController,
      Gui4jGetValue action,
      Map paramMap,
      Gui4jComponentInstance componentInstance) {
    Gui4jGetValue[] work = {action};
    performWork(gui4jController, work, paramMap, componentInstance);
  }

  /**
   * Performs the given work. Dependant of the number of maximum worker threads, the work is either
   * performed in the same thread, or by a new worker, or put into a FIFO queue.
   *
   * @param gui4jController
   * @param work
   * @param paramMap
   * @param actionHandler
   * @param forceExecutionInCurrentThread
   * @param isHighPriorityThread
   * @param specialSuccessHandling
   */
  private void performWork(
      final Gui4jCallBase gui4jController,
      final Gui4jGetValue[] work,
      final Map paramMap,
      final Gui4jComponentInstance componentInstance,
      final boolean forceExecutionInCurrentThread,
      final boolean isHighPriorityThread,
      final boolean specialSuccessHandling) {
    if (mUseWorkerThreads
        && SwingUtilities.isEventDispatchThread()
        && !forceExecutionInCurrentThread
        && !mShutdownThreads) {
      final InvokerCallStack callStack =
          mGui4j.traceWorkerInvocation()
              ? new InvokerCallStack(Thread.currentThread().getName())
              : null;
      final WorkPackage workPackage =
          new WorkPackage(
              gui4jController,
              work,
              paramMap,
              componentInstance,
              callStack,
              isHighPriorityThread,
              specialSuccessHandling);
      Runnable run =
          new Runnable() {
            public void run() {
              if (isHighPriorityThread) {
                mWorkPackagesHighPriority.add(workPackage);
                mWorkPackageCountWaitingHighPriority.incrementAndGet();
              } else {
                mWorkPackagesNormal.add(workPackage);
                mWorkPackageCountWaitingNormal.incrementAndGet();
              }
              dispatchPendingWork();
            }
          };
      SwingUtilities.invokeLater(run);
    } else {
      for (int i = 0; i < work.length; i++) {
        if (work[i] != null) {
          work[i].getValue(gui4jController, paramMap, null);
        }
      }
    }
  }

  public void shutdown() {
    mShutdownThreads = true;
    ExecutorService executor = mWorkerExecutor;
    if (executor != null) {
      executor.shutdownNow();
    }
  }

  private void dispatchPendingWork() {
    if (!mUseWorkerThreads || mShutdownThreads || mWorkerExecutor == null) {
      return;
    }
    if (!mDispatchLock.tryLock()) {
      return;
    }
    try {
      while (!mShutdownThreads) {
        WorkPackage workPackage = pollNextWorkPackage();
        if (workPackage == null) {
          return;
        }
        submitWorkPackage(workPackage);
      }
    } finally {
      mDispatchLock.unlock();
    }
  }

  private WorkPackage pollNextWorkPackage() {
    if (mWorkPackageCountRunningHighPriority.get() > 0) {
      WorkPackage high = mWorkPackagesHighPriority.poll();
      if (high != null) {
        mWorkPackageCountWaitingHighPriority.decrementAndGet();
        mWorkPackageCountRunningHighPriority.incrementAndGet();
      }
      return high;
    }

    WorkPackage high = mWorkPackagesHighPriority.poll();
    if (high != null) {
      mWorkPackageCountWaitingHighPriority.decrementAndGet();
      mWorkPackageCountRunningHighPriority.incrementAndGet();
      return high;
    }

    WorkPackage normal = mWorkPackagesNormal.poll();
    if (normal != null) {
      mWorkPackageCountWaitingNormal.decrementAndGet();
      mWorkPackageCountRunningNormal.incrementAndGet();
    }
    return normal;
  }

  private void submitWorkPackage(final WorkPackage workPackage) {
    if (mLogger.isDebugEnabled()) {
      mLogger.debug(
          "Submitting work package "
              + (workPackage.mIsHighPriority ? "(high prio)" : "")
              + ". Current work queue: RunningHighPriority: "
              + mWorkPackageCountRunningHighPriority.get()
              + ", WaitingHighPriority: "
              + mWorkPackageCountWaitingHighPriority.get()
              + ", RunningNormal: "
              + mWorkPackageCountRunningNormal.get()
              + ", WaitingNormal: "
              + mWorkPackageCountWaitingNormal.get());
    }

    mWorkerExecutor.execute(
        new Runnable() {
          public void run() {
            WorkerThread workerThread = null;
            Thread t = Thread.currentThread();
            if (t instanceof WorkerThread) {
              workerThread = (WorkerThread) t;
              workerThread.setWorkPackage(workPackage);
            }
            try {
              for (int i = 0; i < workPackage.mWork.length; i++) {
                doWork(workPackage.mWork[i], workPackage, i);
              }
              if (mLogger.isDebugEnabled()) {
                mLogger.debug(Thread.currentThread().getName() + ": work finished.");
              }
            } finally {
              if (workPackage.mIsHighPriority) {
                mWorkPackageCountRunningHighPriority.decrementAndGet();
              } else {
                mWorkPackageCountRunningNormal.decrementAndGet();
              }
              if (workerThread != null) {
                workerThread.setWorkPackage(null);
              }
              dispatchPendingWork();
            }
          }
        });
  }

  private void doWork(Gui4jGetValue work, WorkPackage workPackage, int i) {
    if (work == null) {
      return;
    }
    if (mLogger.isDebugEnabled()) {
      mLogger.trace(
          Thread.currentThread().getName()
              + ": performing work "
              + (workPackage.mIsHighPriority ? "(high prio)" : "")
              + ": "
              + work);
    }
    try {
      work.getValueNoErrorChecking(
          workPackage.mGui4jController, workPackage.mParamMap, workPackage.mComponentInstance);
      // Falls verlangt, rufen wir
      // (nur nach dem ersten Aufruf) die
      // <code>handleSuccess</code>
      // Methode auf.
      // Beim Edit-Feld wird damit im Ok-Fall der
      // Inhalt nochmals
      // angezeigt. Außerdem kann damit Validierung
      // gemacht werden.
      if (i == 0 && workPackage.mSpecialSuccessHandling && workPackage.mComponentInstance != null) {
        workPackage.mComponentInstance.handleSuccess();
      }
    } catch (Throwable t) {
      // Analog zum Ok-Fall, rufen wir im Fehlerfall
      // nach dem ersten
      // Aufruf die <code>handleException</code>
      // Methode auf. Damit
      // kann beispielsweise Validierung gemacht
      // werden.
      if (i == 0 && workPackage.mSpecialSuccessHandling && workPackage.mComponentInstance != null) {
        workPackage.mComponentInstance.handleException(t);
      } else {
        // Falls kein ActionHandler definiert wurde,
        // oder
        // es sich nicht um den ersten Aufruf
        // handelt,
        // erfolgt die normale Fehlerbehandlung.
        mGui4j.handleException(workPackage.mGui4jController, t, null);
      }
    }
  }

  private static class WorkPackage {
    public final Gui4jCallBase mGui4jController;
    public final Gui4jGetValue[] mWork;
    public final Gui4jComponentInstance mComponentInstance;
    public final Map mParamMap;
    public final InvokerCallStack mCallStack;
    public final boolean mSpecialSuccessHandling;
    public final boolean mIsHighPriority;

    public WorkPackage(
        Gui4jCallBase gui4jController,
        Gui4jGetValue[] work,
        Map paramMap,
        Gui4jComponentInstance componentInstance,
        InvokerCallStack callStack,
        boolean isHighPriority,
        boolean specialSuccessHandling) {
      mGui4jController = gui4jController;
      mWork = work;
      mParamMap = paramMap;
      mComponentInstance = componentInstance;
      mCallStack = callStack;
      mIsHighPriority = isHighPriority;
      mSpecialSuccessHandling = specialSuccessHandling;
    }
  }

  public class WorkerThread extends Thread {
    private volatile WorkPackage mWorkPackage;
    private final int mId;

    public WorkerThread(int id, Runnable target) {
      super(target);
      mId = id;

      // This thread will be created by the AWT thread and we have to make
      // sure we don't use the AWT thread's privileged priority.
      setPriority(NORM_PRIORITY);

      setName("Gui4j-Worker " + mId);
      mLogger.debug(this + ": created");
    }

    public Throwable getCallStack() {
      return mWorkPackage != null ? mWorkPackage.mCallStack : null;
    }

    private void setWorkPackage(WorkPackage workPackage) {
      mWorkPackage = workPackage;
    }

    public String toString() {
      return getName();
    }
  }

  private static class InvokerCallStack extends Throwable {
    public InvokerCallStack(String threadName) {
      super(threadName);
    }

    public String toString() {
      return "Thread [" + getMessage() + "]";
    }
  }

  /**
   * If the calling thread is the AWT Event Dispatch Thread (EDT) the specified command is executed
   * and this method returns after completion of the command. If the calling thread is not the EDT,
   * the given <code>Runnable</code> is inserted into the AWT EventQueue via <code>
   * EventQueue.invokeAndWait()</code>. In this case, this call does not return until the EDT has
   * completed the queued task.
   *
   * @param run task to be scheduled in the GUI thread
   */
  public static void executeInSwingThreadAndWait(Runnable run) {
    try {
      if (EventQueue.isDispatchThread()) {
        // if we already are in the EDT we simply execute the command
        run.run();
      } else {
        EventQueue.invokeAndWait(run);
      }
    } catch (InterruptedException ex) {
      mLogger.warn("Interrupted", ex);
    } catch (InvocationTargetException ex) {
      Gui4jReflectionManager.handleInvocationTargetException(ex);
      throw new Gui4jUncheckedException.ProgrammingError(
          PROGRAMMING_ERROR_invocation_target_exception, ex);
    }
  }

  /**
   * The given task is executed in the GUI thread as soon as possible. There are two cases: <br>
   * If this thread is not the GUI thread, the given <code>Runnable</code> is inserted into the task
   * queue of the GUI thread. This call then returns immediately without waiting for the scheduled
   * task to be finished. <br>
   * If this thread is the GUI thread itself, the given task is executed immediately and
   * synchronously, i.e. this call will not return until the task is completed.
   *
   * @param run task to be scheduled in the GUI thread
   */
  public static void executeInSwingThreadAndContinue(final Runnable run) {
    if (EventQueue.isDispatchThread()) {
      run.run();
    } else {
      EventQueue.invokeLater(run);
    }
  }

  private ExecutorService createWorkerExecutor() {
    ThreadFactory workerFactory =
        new ThreadFactory() {
          public Thread newThread(Runnable runnable) {
            mCreatedWorkerCount.incrementAndGet();
            return new WorkerThread(mMaxWorkerId++, runnable);
          }
        };

    if (mMaxNumberOfWorkerThreads == -1) {
      return new ThreadPoolExecutor(
          0,
          Integer.MAX_VALUE,
          60L,
          TimeUnit.SECONDS,
          new SynchronousQueue<Runnable>(),
          workerFactory);
    }

    return new ThreadPoolExecutor(
        mMaxNumberOfWorkerThreads,
        mMaxNumberOfWorkerThreads,
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(),
        workerFactory);
  }

  private void initializeTransientState() {
    mWorkPackagesNormal = new ConcurrentLinkedQueue<WorkPackage>();
    mWorkPackagesHighPriority = new ConcurrentLinkedQueue<WorkPackage>();
    mDispatchLock = new ReentrantLock();
    mCreatedWorkerCount = new AtomicInteger();
    mShutdownThreads = false;
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    initializeTransientState();
    if (mUseWorkerThreads) {
      mWorkerExecutor = createWorkerExecutor();
    }
  }
}

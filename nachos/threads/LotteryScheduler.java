package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
	public static final int priorityMaximum = Integer.MAX_VALUE;


    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	// implement me
	return new LotteryQueue(transferPriority);
    }
	protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new LotteryState(thread);

        return (ThreadState) thread.schedulingState;
    }
    protected class LotteryQueue extends PriorityQueue {

	LotteryQueue(boolean transferPriority) {
		super(transferPriority);
	}

	public ThreadState pickNextThread() {  //returns next theard, chosen by lottery
		if(threadWait.isEmpty()){
			return null;
		}
            int nextPriority = priorityMinimum;
            int total  = getEffectivePriority(); //gets the total number of tickets in the queue
    	    int target = Lib.random(total); //generates random target, between 0 and total

    	    ThreadState next = null;
            for (final ThreadState currThread : this.threadWait) {
		//iterate through threads until target is found
            	if (target < currThread.getEffectivePriority()){ //Target is within threads tickets
            		next = currThread;
           		break;
            }else //get rid of tickets, move to next thread in queue
            	target = target - currThread.getEffectivePriority();
            }
            return next;
	}


    	public int getEffectivePriority() {//for Lotterey, lead node should have sum of all nodes

		/*if (!transferPriority) {
                    return priorityMinimum;
                }else */if (this.priChange) {
                    // Recalculate effective priorities
                    this.effPriority = 0;
                    for (final ThreadState curr : this.threadWait) {
                            this.effPriority += curr.getEffectivePriority();//sum all priorities
                    }
		    this.priChange = false;
            	}                 
                return effPriority;        
	}

    }
    protected class LotteryState extends ThreadState {


	public LotteryState (KThread thread) {
		super(thread);
	}

	public int getEffectivePriority() {//for Lotterey, lead node should have sum of all nodes that want resources it holds

            if (this.currentResources.isEmpty()) {
                return this.getPriority();
            } else if (this.priChange) {
                this.effPriority = this.getPriority();
                for (final PriorityQueue pq : this.currentResources) {
			if(pq.transferPriority){
                    		this.effPriority += pq.getEffectivePriority(); //sum of priorities of threads that depend on resources that this thread holds.
			}
                }
                this.priChange = false;
            }
            return this.effPriority;
	}

    }

}

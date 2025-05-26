package GP2.thread;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class GPExecutor {
    ExecutorService executor ;
    ArrayList<Runnable> tasks ;
    ArrayList<Future<?>> futures ;

    public GPExecutor(int nThreads) {
        executor = Executors.newFixedThreadPool(nThreads);
        tasks = new ArrayList<Runnable>(nThreads);
        futures = new ArrayList<Future<?>>(nThreads);
    }

    public boolean add(Runnable t) {
        try {
            tasks.add(t) ;
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public boolean launch() {
        try {
			for (Runnable t : tasks) {
				futures.add(executor.submit(t)) ;
			}
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public boolean collect() {
        try {
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public boolean shutdown() {
        try {
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            } 
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        return true;
    }
}

package GP2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import GP2.cli.gpcli;
import GP2.utils.Utils;

class GPThread extends Thread {
    private static final Logger logger = LogManager.getLogger(GPThread.class);

    String[] sArgs;
    public GPThread(String[] sA) {
        sArgs = sA;
    }

    // Override the run method
    @Override
    public void run() {
        gpcli gCLI = new gpcli();
        Utils.m_settings = gCLI.parseCommandLine(sArgs);
        gCLI.processCommandLine();
    }
}

public class gp2 {
    private static final Logger logger = LogManager.getLogger(gp2.class);

	// Method to display a rotating spinner with colors
	private static void displaySpinner(int index, long elapsedTime) {
		char[] spinnerChars = {'|', '/', '-', '\\'};
		String[] colors = {
			"\u001B[31m", // Red
			"\u001B[32m", // Green
			"\u001B[33m", // Yellow
			"\u001B[34m"  // Blue
		};
		String resetColor = "\u001B[0m"; // Reset color

		// Select color based on the spinner index
		String color = colors[index % colors.length];
		System.out.print("\r" + color + spinnerChars[index % spinnerChars.length] + resetColor + " " + elapsedTime + " ms");
	}

    // ----------------------------------------------------
    // main
    // ----------------------------------------------------
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;

        GPThread gT = new GPThread(args);
        gT.start();

        int spinnerIndex = 0;
        // Update the spinner while the thread is running
        while (gT.isAlive()) {
            long currentTime = System.currentTimeMillis();
            elapsedTime = currentTime - startTime;
            displaySpinner(spinnerIndex++, elapsedTime);
            Thread.sleep(100); // Update every 100 milliseconds
        }

        // Wait for all threads to finish
        try {
            gT.join();
        } catch (InterruptedException e) {
            logger.error("Error: {}", e.getMessage());
        }

        long d = 1000; // to seconds
        double t = (double) elapsedTime / d;
        System.out.print("\r" + " ".repeat(50));	// blank out spinner
        System.out.printf("\relapsed: %.3fs", t);
    }
}
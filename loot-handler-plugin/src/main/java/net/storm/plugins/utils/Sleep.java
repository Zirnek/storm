package net.storm.plugins.utils;

// import java.util.function.BooleanSupplier;

public class Sleep {
    
    /*
    public static boolean sleepUntil(BooleanSupplier condition, int timeoutMillis, int pollIntervalMillis) {
        final long deadline = System.currentTimeMillis() + timeoutMillis;

        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean())
                return true;
            
            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return condition.getAsBoolean();
    }
    */
}

package sm.peripheral;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PeripheralCommandSetDelegate {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Queue<DataPacket> commandQueue = new LinkedBlockingQueue<>();
    private final Peripheral peripheral;
    private Runnable task;
    private TimeoutCallback timeoutCallback;

    public PeripheralCommandSetDelegate(Peripheral peripheral) {
        this.peripheral = peripheral;
    }

    public void setTimeoutCallback(TimeoutCallback timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
    }

    public void set(DataPacket command) {
        if (commandQueue.contains(command)) return;
        commandQueue.add(command);
        initExecutorService();
    }

    public synchronized void replied(byte type) {
        DataPacket command = commandQueue.peek();
        if (command == null || command.getType() != type) return;
        commandQueue.remove(command);
        Runnable task = this.task;
        if (task == null) return;
        synchronized (task) {
            task.notify();
        }
    }

    private synchronized void initExecutorService() {
        if (task != null) return;
        task = task();
        executorService.execute(task);
    }

    private Runnable task() {
        return new Runnable() {
            @Override
            public void run() {
                while (isRunning()) {
                    DataPacket command = commandQueue.peek();
                    if (command == null) {
                        synchronized (PeripheralCommandSetDelegate.this) {
                            command = commandQueue.peek();
                            if (command == null) {
                                setRunningFalse();
                            }
                        }
                    } else {
                        try {
                            synchronized (this) {
                                if (commandQueue.peek() == command) {
                                    peripheral.set(command);
                                    wait(1000);
                                }
                                if (commandQueue.peek() == command) {
                                    peripheral.set(command);
                                    wait(1000);
                                }
                                if (commandQueue.peek() == command) {
                                    peripheral.set(command);
                                    wait(1000);
                                }
                                if (commandQueue.peek() == command) {
                                    peripheral.set(command);
                                    wait(1000);
                                }
                                if (commandQueue.peek() == command) {
                                    peripheral.set(command);
                                    wait(1000);
                                }
                                if (commandQueue.peek() == command) {
                                    commandQueue.remove(command);
                                    if (timeoutCallback != null) timeoutCallback.onTimeout(command);
                                }
                            }
                        } catch (InterruptedException e) {
                            setRunningFalse();
                            initExecutorService();
                        }
                    }
                }
            }
        };
    }

    private synchronized boolean isRunning() {
        return task != null;
    }

    private synchronized void setRunningFalse() {
        task = null;
    }

    public interface TimeoutCallback {
        void onTimeout(DataPacket command);
    }
}

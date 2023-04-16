import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SavingsAccount {

    private int balance;
    private Lock lock;
    private Condition sufficientFunds;
    private Condition sufficientFundsOrdinary;
    private int preferredWithdrawalsWaiting;

    public SavingsAccount(int initialBalance) {
        balance = initialBalance;
        lock = new ReentrantLock();
        sufficientFunds = lock.newCondition();
        sufficientFundsOrdinary = lock.newCondition();
        preferredWithdrawalsWaiting = 0;
    }

    public void deposit(int amount) {
        lock.lock();
        try {
            balance += amount;
            sufficientFunds.signalAll();
            sufficientFundsOrdinary.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void withdrawPreferred(int amount) throws InterruptedException {
        lock.lock();
        try {
            preferredWithdrawalsWaiting++;
            while (balance < amount || preferredWithdrawalsWaiting > 1) {
                sufficientFunds.await();
            }
            preferredWithdrawalsWaiting--;
            balance -= amount;
        } finally {
            lock.unlock();
        }
    }

    public void withdrawOrdinary(int amount) throws InterruptedException {
        lock.lock();
        try {
            while (balance < amount || preferredWithdrawalsWaiting > 0) {
                sufficientFundsOrdinary.await();
            }
            balance -= amount;
        } finally {
            lock.unlock();
        }
    }

    public void transfer(int k, SavingsAccount reserve) throws InterruptedException {
        lock.lock();
        try {
            while (balance < k) {
                sufficientFunds.await();
            }
            reserve.withdraw(k);
            balance -= k;
        } finally {
            lock.unlock();
        }
    }

}

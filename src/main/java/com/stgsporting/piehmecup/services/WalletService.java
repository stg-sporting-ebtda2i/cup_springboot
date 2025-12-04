package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.enums.TransactionType;
import com.stgsporting.piehmecup.exceptions.InsufficientCoinsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class WalletService {

    private final UserService userService;
    private final TransactionService transactionService;
    private final Map<Long, Lock> userLocks = new ConcurrentHashMap<>();

    public WalletService(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @Transactional
    public void debit(User user, Integer amount) {
        debit(user, amount, null, false);
    }

    @Transactional
    public void debit(User user, Integer amount, String description) {
        debit(user, amount, description, false);
    }

    @Transactional
    public void debit(User user, Integer amount, String description, boolean ignoreCoins) {
        Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null for wallet operations");
        }

        Lock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new IllegalStateException("Unable to acquire lock for user " + userId + ". Another transaction is in progress.");
        }

        try {
            if (!ignoreCoins && user.getCoins() < amount) {
                throw new InsufficientCoinsException("Not enough coins");
            }

            user.setCoins(user.getCoins() - amount);

            userService.save(user);

            transactionService.makeTransaction(user, amount, TransactionType.DEBIT, description);
        } finally {
            lock.unlock();
            userLocks.remove(userId, lock);
        }
    }

    @Transactional
    public void forceDebit(User user, Integer amount, String description) {
        debit(user, amount, description, true);
    }

    @Transactional
    public void credit(User user, Integer amount) {
        credit(user, amount, null);
    }

    @Transactional
    public void credit(User user, Integer amount, String description) {
        Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null for wallet operations");
        }

        Lock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new IllegalStateException("Unable to acquire lock for user " + userId + ". Another transaction is in progress.");
        }

        try {
            user.setCoins(user.getCoins() + amount);

            userService.save(user);

            transactionService.makeTransaction(user, amount, TransactionType.CREDIT, description);
        } finally {
            lock.unlock();
            userLocks.remove(userId, lock);
        }
    }
}

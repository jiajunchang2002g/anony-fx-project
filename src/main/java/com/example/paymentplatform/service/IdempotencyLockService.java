package com.example.paymentplatform.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyLockService {

  private final ConcurrentHashMap<String, ReentrantLock> locks =
      new ConcurrentHashMap<>();

  public <T> T execute(String key, Supplier<T> action) {
    ReentrantLock lock =
        locks.computeIfAbsent(key, ignored -> new ReentrantLock());
    lock.lock();
    try {
      return action.get();
    } finally {
      try {
        lock.unlock();
      } finally {
        if (!lock.isLocked() && !lock.hasQueuedThreads()) {
          locks.remove(key, lock);
        }
      }
    }
  }
}
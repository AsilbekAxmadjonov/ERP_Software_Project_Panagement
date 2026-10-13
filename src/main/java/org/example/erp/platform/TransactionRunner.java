package org.example.erp.platform;

import java.util.function.Supplier;

public interface TransactionRunner {
    <T> T run(Supplier<T> work);

    default void run(Runnable work) {
        run(() -> {
            work.run();
            return null;
        });
    }
}

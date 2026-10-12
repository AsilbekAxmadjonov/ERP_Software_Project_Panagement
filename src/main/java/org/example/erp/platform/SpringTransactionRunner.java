package org.example.erp.platform;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
class SpringTransactionRunner implements TransactionRunner {
    private final TransactionTemplate template;

    SpringTransactionRunner(PlatformTransactionManager manager) {
        this.template = new TransactionTemplate(manager);
    }

    @Override
    public <T> T run(Supplier<T> work) {
        return template.execute(status -> work.get());
    }
}

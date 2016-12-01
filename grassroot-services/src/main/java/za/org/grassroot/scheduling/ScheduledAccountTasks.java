package za.org.grassroot.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import za.org.grassroot.integration.payments.PaymentServiceBroker;
import za.org.grassroot.services.account.AccountBillingBroker;

/**
 * Created by luke on 2016/10/25.
 */
@Component
@ConditionalOnProperty(name = "grassroot.billing.enabled", havingValue = "true")
public class ScheduledAccountTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledAccountTasks.class);

    private final AccountBillingBroker accountBillingBroker;
    private final PaymentServiceBroker paymentServiceBroker;

    @Autowired
    public ScheduledAccountTasks(AccountBillingBroker accountBillingBroker, PaymentServiceBroker paymentServiceBroker) {
        this.accountBillingBroker = accountBillingBroker;
        this.paymentServiceBroker = paymentServiceBroker;
    }

    @Scheduled(cron = "${grassroot.billing.cron.trigger: 0 0 9 * * ?}")
    public void calculateMonthlyBillingAndCosts() {
        logger.info("Calculating billing statements and sending emails and notifications ... ");
        accountBillingBroker.calculateStatementsForDueAccounts(true, true);
    }

    @Scheduled(cron = "${grassroot.payments.cron.trigger: 0 0 20 * * ?}")
    public void processMonthlyBillPayments() {
        logger.info("Charging monthly billing amounts");
        paymentServiceBroker.processAccountPaymentsOutstanding();
    }

}
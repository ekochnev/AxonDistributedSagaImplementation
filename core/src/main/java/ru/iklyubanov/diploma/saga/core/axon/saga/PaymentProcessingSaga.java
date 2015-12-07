package ru.iklyubanov.diploma.saga.core.axon.saga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.saga.annotation.AbstractAnnotatedSaga;
import org.axonframework.saga.annotation.SagaEventHandler;
import org.axonframework.saga.annotation.StartSaga;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.iklyubanov.diploma.saga.core.axon.command.CheckNewPaymentByBankCommand;
import ru.iklyubanov.diploma.saga.core.axon.command.ProcessPaymentByProcessorCommand;
import ru.iklyubanov.diploma.saga.gcore.axon.event.CreatePaymentEvent;
import ru.iklyubanov.diploma.saga.core.axon.event.PaymentExecutionExpiredEvent;
import ru.iklyubanov.diploma.saga.core.axon.util.TransactionId;

import java.util.UUID;

/**
 * Сначала проверяем счет клиента в банке клиента.
 * Если все ок, проверяем счет получателя в банке получателя.
 * Если все ок, передаем процессору средства для платежа.
 * Процессор иницализирует перевод средств от клиента к получателю.
 * Асинхронно происходит списывание со счета клиента в банке клиента и зачисление средств на счет получателя
 * с помощью запущенной системы платежей.
 * Если там и там успешно списалось  - уведомляем клиента.
 * Created by kliubanov on 27.11.2015.
 */
public class PaymentProcessingSaga extends AbstractAnnotatedSaga {
    private static final long serialVersionUID = 5948996680443725871L;
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingSaga.class);

    private TransactionId transactionId;
    private boolean isProcessorValidated = false;
    private boolean isIssuingBankChecked = false;
    private boolean isMerchantBankChecked = false;

    @Autowired
    private transient CommandGateway commandGateway;
    /*todo or 'private transient CommandBus commandBus;'*/

    @Autowired
    private transient EventScheduler eventScheduler;

    @StartSaga/*доработать*/
    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(CreatePaymentEvent event) {
        transactionId = event.getTransactionId();
        logger.info("A new payment '{}' created.", transactionId);

        // ...getting processor..
        ProcessPaymentByProcessorCommand processorCommand = createProcessPaymentByProcessorCommand(event);
        commandGateway.send(processorCommand);

        //SendMoneyByCardNetworkCommand sendMoneyCommand = new SendMoneyByCardNetworkCommand();

        PaymentExecutionExpiredEvent expiredEvent = new PaymentExecutionExpiredEvent(event.getTransactionId());
        eventScheduler.schedule(Duration.standardMinutes(event.getTimeout()), expiredEvent);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(PaymentProcessEvent paymentProcessEvent) {
        //send to card-issuing bank
        CheckNewPaymentByBankCommand checkByBankCommand = new CheckNewPaymentByBankCommand(paymentProcessEvent.getCode(), paymentProcessEvent.getTransactionId());
        commandGateway.send(checkByBankCommand);
    }

    private ProcessPaymentByProcessorCommand createProcessPaymentByProcessorCommand(CreatePaymentEvent event) {
        ProcessPaymentByProcessorCommand processorCommand = new ProcessPaymentByProcessorCommand(event.getTransactionId());
        processorCommand.setAmount(event.getAmount());
        processorCommand.setCurrencyType(event.getCurrencyType());
        processorCommand.setMerchant(event.getMerchant());
        processorCommand.setMerchantBankAccount(event.getMerchantBankAccount());
        processorCommand.setMerchantBankBIK(event.getMerchantBankBIK());
        processorCommand.setMerchantINN(event.getMerchantINN());
        processorCommand.setPaymentType(event.getPaymentType());
        return processorCommand;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }
}

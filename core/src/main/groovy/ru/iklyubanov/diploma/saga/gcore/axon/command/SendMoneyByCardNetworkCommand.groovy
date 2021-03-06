package ru.iklyubanov.diploma.saga.gcore.axon.command

import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier
import ru.iklyubanov.diploma.saga.gcore.annotation.Command

/**
 * Created by kliubanov on 27.11.2015.
 */
@Command
public class SendMoneyByCardNetworkCommand {

    @TargetAggregateIdentifier
    final String paymentId
    final String transactionId
    Long issuingBankId
    Long clientCardId
    Long merchantBankId
    Long merchantCardId

    public SendMoneyByCardNetworkCommand(String paymentId, String transactionId) {
        this.paymentId = paymentId
        this.transactionId = transactionId
    }
}

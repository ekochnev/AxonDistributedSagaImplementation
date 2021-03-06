package ru.iklyubanov.diploma.saga.remote.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import ru.iklyubanov.diploma.saga.core.spring.entity.PaymentProcessor


/**
 * todo и репозитории лучше перенести в core чтоб они были доступны для админки в mvc
 * Created by ivan on 12/6/2015.
 */
interface PaymentProcessorRepository extends CrudRepository<PaymentProcessor, Long> {

    @Query("select distinct pp from PaymentProcessor pp where pp.maxTransactionsCount > pp.currentTransactionsCount")
    PaymentProcessor findFreeProcessor();

    @Query("select distinct pp from PaymentProcessor pp where pp.identifier = :transactionId")
    PaymentProcessor findProcessor(@Param("transactionId") String transactionId);
}

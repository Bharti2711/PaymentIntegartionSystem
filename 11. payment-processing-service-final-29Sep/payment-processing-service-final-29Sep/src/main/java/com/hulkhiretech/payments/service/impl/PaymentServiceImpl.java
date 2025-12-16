package com.hulkhiretech.payments.service.impl;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.TransactionStatusEnum;
import com.hulkhiretech.payments.dao.interfaces.TransactionDao;
import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.entity.TransactionEntity;
import com.hulkhiretech.payments.exception.ProcessingException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.pojo.CreateTxnRequest;
import com.hulkhiretech.payments.pojo.InitiateTxnRequest;
import com.hulkhiretech.payments.pojo.TxnResponse;
import com.hulkhiretech.payments.service.helper.SPCreatePaymentHelper;
import com.hulkhiretech.payments.service.interfaces.PaymentService;
import com.hulkhiretech.payments.service.interfaces.PaymentStatusService;
import com.hulkhiretech.payments.stripeprovider.StripeProviderPaymentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentStatusService paymentStatusService;

	private final ModelMapper modelMapper;

	private final TransactionDao transactionDao;
	
	private final HttpServiceEngine httpServiceEngine;
	
	private final SPCreatePaymentHelper spCreatePaymentHelper;

	@Override
	public TxnResponse createTxn(CreateTxnRequest createTxnRequest) {
		log.info("Creating payment transaction createTxnRequest:{}",
				createTxnRequest);

		TransactionDTO txnDto = modelMapper.map(
				createTxnRequest, TransactionDTO.class);
		log.info("Mapped txnDto: {}", txnDto);

		String txnStatus = TransactionStatusEnum.CREATED.name();
		String txnReference = generateUniqueTxnRef();

		txnDto.setTxnStatus(txnStatus);
		txnDto.setTxnReference(txnReference);

		log.info("Final txnDto to be saved: {}", txnDto);

		TransactionDTO response = paymentStatusService.processStatus(txnDto);
		log.info("Response from PaymentStatusService: {}", response);

		TxnResponse createTxnResponse = new TxnResponse();
		createTxnResponse.setTxnReference(response.getTxnReference());
		createTxnResponse.setTxnStatus(response.getTxnStatus());

		log.info("CreateTxnResponse to be returned: {}", createTxnResponse);

		return createTxnResponse;
	}

	private String generateUniqueTxnRef() {
		return UUID.randomUUID().toString();
	}

	@Override
	public TxnResponse initiateTxn(String txnReference, InitiateTxnRequest initiateTxnRequest) {
		log.info("Initiating payment transaction||id:{}|initiateTxnRequest:{}", 
				txnReference, initiateTxnRequest);

		// update DB as INITIATED
		TransactionEntity txnEntity = transactionDao
				.getTransactionByTxnReference(txnReference);
		TransactionDTO txnDto = modelMapper.map(txnEntity, TransactionDTO.class);
		log.info("Mapped txnDto: {}", txnDto);

		txnDto.setTxnStatus(TransactionStatusEnum.INITIATED.name());
		txnDto = paymentStatusService.processStatus(txnDto);
		log.info("Updated txnDto to INITIATED: {}", txnDto);

		StripeProviderPaymentResponse successResponse = null;
		try {
			// Make Rest Http API call to stripe-provider-service for create-payment api
			HttpRequest httpRequest = spCreatePaymentHelper
					.prepareHttpRequest(initiateTxnRequest);
			log.info("Prepared HttpRequest for stripe-provider-service: {}", httpRequest);
			
			ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
			log.info("Response from stripe-provider-service: {}", response);
			
			successResponse = spCreatePaymentHelper.processResponse(response);
			log.info("Processed StripeProviderPaymentResponse: {}", successResponse);
			
		} catch (ProcessingException e) {
			log.error("Error during initiating transaction: {}", e.getMessage());
			// Handle exception, possibly update txnDto to FAILED status
			txnDto.setTxnStatus(TransactionStatusEnum.FAILED.name());
			txnDto.setErrorCode(e.getErrorCode());
			txnDto.setErrorMessage(e.getMessage());
			txnDto = paymentStatusService.processStatus(txnDto);
			log.info("Updated txnDto to FAILED due to error: {}", txnDto);

			throw e; // re-throw to Global Exception handling 
		}
		
		// Update DB as PENDING, providerReference
		txnDto.setTxnStatus(TransactionStatusEnum.PENDING.name());
		txnDto.setProviderReference(successResponse.getId());
		
		txnDto = paymentStatusService.processStatus(txnDto);
		log.info("Updated txnDto to PENDING: {}", txnDto);

		// return the url back to the invoker.
		TxnResponse txnResponse = new TxnResponse();
		txnResponse.setTxnReference(txnDto.getTxnReference());
		txnResponse.setTxnStatus(txnDto.getTxnStatus());
		txnResponse.setRedirectUrl(successResponse.getUrl());
		log.info("Final TxnResponse to be returned: {}", txnResponse);

		return txnResponse;
	}

}

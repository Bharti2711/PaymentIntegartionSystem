package com.hulkhiretech.payments.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.hulkhiretech.payments.constant.TransactionStatusEnum;
import com.hulkhiretech.payments.dao.interfaces.TransactionDao;
import com.hulkhiretech.payments.dto.TransactionDTO;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.pojo.CreateTxnRequest;
import com.hulkhiretech.payments.pojo.TxnResponse;
import com.hulkhiretech.payments.service.helper.SPCreatePaymentHelper;
import com.hulkhiretech.payments.service.interfaces.PaymentStatusService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

	@Mock
	private PaymentStatusService paymentStatusService;

	@Mock
	private ModelMapper modelMapper;

	@Mock
	private TransactionDao transactionDao;

	@Mock
	private HttpServiceEngine httpServiceEngine;

	@Mock
	private SPCreatePaymentHelper spCreatePaymentHelper;

	@InjectMocks
	private PaymentServiceImpl paymentServiceImpl;

	@Test
	public void testCreateTxn() {
		// "Arrange" Arrange data for calling functional method
		CreateTxnRequest createTxnRequest = new CreateTxnRequest();

		TransactionDTO txnDto = new TransactionDTO();
		
		when(modelMapper.map(createTxnRequest, TransactionDTO.class))
		.thenReturn(txnDto);
		
		when(paymentStatusService.processStatus(txnDto))
		.thenReturn(txnDto);

		// ACT Call Functional method for testing
		TxnResponse txnResponse = paymentServiceImpl.createTxn(createTxnRequest);
		

		// "Assert" Verify
		assertNotNull(txnResponse);
		assertNotNull(txnResponse.getTxnReference());
		assertNotNull(txnResponse.getTxnStatus());
		assertNull(txnResponse.getRedirectUrl());

		assertEquals("CREATED", txnResponse.getTxnStatus());
		assertEquals(36, txnResponse.getTxnReference().length());
		
		assertDoesNotThrow(() -> UUID.fromString(txnResponse.getTxnReference()));
		
		assertTrue(Arrays.stream(TransactionStatusEnum.values())
		        .anyMatch(e -> e.name().equals(txnResponse.getTxnStatus())));
		
		assertEquals("CREATED", txnDto.getTxnStatus());
		assertNotNull(txnDto.getTxnReference());
		
		verify(modelMapper, times(1)).map(createTxnRequest, TransactionDTO.class);

		verify(paymentStatusService, times(1)).processStatus(txnDto);
		
		verifyNoMoreInteractions(modelMapper, paymentStatusService);
		
		log.info("Test method executed");
	}

}

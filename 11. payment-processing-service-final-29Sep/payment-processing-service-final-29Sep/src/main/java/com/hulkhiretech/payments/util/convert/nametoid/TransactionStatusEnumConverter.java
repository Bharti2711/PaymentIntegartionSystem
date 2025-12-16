package com.hulkhiretech.payments.util.convert.nametoid;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payments.constant.TransactionStatusEnum;

public class TransactionStatusEnumConverter extends AbstractConverter<String, Integer> {
    @Override
    protected Integer convert(String source) {
        return TransactionStatusEnum.fromName(source).getId();
    }
}

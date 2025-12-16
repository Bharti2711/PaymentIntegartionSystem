package com.hulkhiretech.payments.util.convert.idtoname;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payments.constant.TransactionStatusEnum;

public class TransactionStatusEnumIdToNameConverter extends AbstractConverter<Integer, String> {
    @Override
    protected String convert(Integer source) {
        return TransactionStatusEnum.fromId(source).getName();
    }
}
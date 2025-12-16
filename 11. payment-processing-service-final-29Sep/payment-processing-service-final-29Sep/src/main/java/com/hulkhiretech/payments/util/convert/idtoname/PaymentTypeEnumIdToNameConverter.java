package com.hulkhiretech.payments.util.convert.idtoname;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payments.constant.PaymentTypeEnum;

public class PaymentTypeEnumIdToNameConverter extends AbstractConverter<Integer, String> {
    @Override
    protected String convert(Integer source) {
        return PaymentTypeEnum.fromId(source).getName();
    }
}

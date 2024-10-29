package me.d1n3x3z7.vndpayment.api;

public interface PaymentClient {

    String create(String username, int id, int count, String coupon) throws Exception;

}

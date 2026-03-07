package br.com.gustavo.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "br.com.gustavo.coupon", 
    "br.com.gustavo.shared"
})
public class CouponApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponApiApplication.class, args);
	}

}

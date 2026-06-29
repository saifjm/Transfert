package com.smi.mstr.transfer;

import com.smi.mstr.transfer.config.MockPaymentBlockingProperties;
import com.smi.mstr.transfer.config.MsTrOperationCodeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		MsTrOperationCodeProperties.class,
		MockPaymentBlockingProperties.class
})
public class MsTrTransferApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsTrTransferApplication.class, args);
	}
}

package com.bms.transaction.config;

import feign.codec.Encoder;
import feign.codec.Decoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectFactory;

@Configuration
public class FeignMultipartConfig {

	@Bean
	public Encoder feignEncoder(ObjectFactory<HttpMessageConverters> converters) {
		return new SpringEncoder(converters);
	}

	@Bean
	public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> converters) {
		return new SpringDecoder(converters);
	}
}


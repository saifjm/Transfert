package com.smi.mstr.transfer.config;

import com.smi.mstr.transfer.application.ref.MsRefClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MsRefClientProperties.class)
public class DownstreamClientConfig {
}
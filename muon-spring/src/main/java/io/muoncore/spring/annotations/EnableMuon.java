package io.muoncore.spring.annotations;

import io.muoncore.spring.MuonConfiguration;
import io.muoncore.spring.AutoConfigurationBeanDefinitionRegistrar;
import io.muoncore.spring.discovery.MuonDiscoveryFactoryBeanRegistrar;
import io.muoncore.spring.transport.MuonTransportFactoryBeanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable spring-muon integration. Will create an instance of Muon
 * @see io.muoncore.spring.annotations.EnableMuonControllers
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MuonTransportFactoryBeanRegistrar.class,  MuonDiscoveryFactoryBeanRegistrar.class,
        MuonConfiguration.class, AutoConfigurationBeanDefinitionRegistrar.class})
public @interface EnableMuon {

    /**
     * Assign a stream to the Muon service instance
     */
    String serviceName();

    /**
     * List of optional service tags of Muon instance
     */
    String[] tags() default {};

    /**
     * AES encryption key, a string of 16/24/32 bytes
     */
    String aesEncryptionKey() default "1234567890abcdef";

}
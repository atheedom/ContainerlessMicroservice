package com.alextheedom;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by atheedom on 21/12/2015.
 */
@ComponentScan
@Configuration
@EnableAutoConfiguration
public class MyApplication implements CommandLineRunner {

//
//    public static void main(String[] args) {
//        SpringApplication.run(MyApplication.class, args);
//    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(MyApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println("Waiting five seconds...");
        Thread.sleep(5000);
        System.out.println("Sending message...");
        rabbitTemplate.convertAndSend(queueName, "Hello from RabbitMQ!");
        receiver().getLatch().await(10000, TimeUnit.MILLISECONDS);
        context.close();
    }


    final static String queueName = "spring-boot";

    @Autowired
    AnnotationConfigApplicationContext context;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("spring-boot-exchange");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(queueName);
    }


//
//
//    @Bean
//    public ConnectionFactory connectionFactory() {
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setHost("rabbitmq");
//        connectionFactory.setPort(5672);
//        connectionFactory.setUsername("remote");
//        connectionFactory.setPassword("remote");
//        connectionFactory.setVirtualHost("/");
//        connectionFactory.setRequestedHeartBeat(200000);
//        connectionFactory.setChannelCacheSize(1);
//        return connectionFactory;
//    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    Receiver receiver() {
        return new Receiver();
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }








    private static final String PACKAGE_TO_SCAN         = "com.alextheedom.domain";
    private static final String MYSQL_DRIVER            = "com.mysql.jdbc.Driver";
    private static final String MYSQL_CONNECTION_STRING = "jdbc:mysql://database:3306/Containerless";
    private static final String MYSQL_USER              = "root";
    private static final String MYSQL_PASSWORD          = "vfr800";

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan(PACKAGE_TO_SCAN);
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(this.additionalProperties());
        return em;
    }

    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(MYSQL_DRIVER);
        dataSource.setUrl(MYSQL_CONNECTION_STRING);
        dataSource.setUsername(MYSQL_USER);
        dataSource.setPassword(MYSQL_PASSWORD);
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

    Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        return properties;
    }
}


package edu.iu.uits.lms.gct.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration("gctDbConfig")
@EnableJpaRepositories(
        entityManagerFactoryRef = "gctEntityMgrFactory",
        transactionManagerRef = "gctTransactionMgr",
        basePackages = {"edu.iu.uits.lms.gct.repository"})
@EnableTransactionManagement
public class PostgresDBConfig {

    @Bean(name = "gctDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "gctEntityMgrFactory")
    public LocalContainerEntityManagerFactoryBean gctEntityMgrFactory(
            final EntityManagerFactoryBuilder builder,
            @Qualifier("gctDataSource") final DataSource dataSource) {
        // dynamically setting up the hibernate properties for each of the datasource.
        final Map<String, String> properties = new HashMap<>();
        return builder
                .dataSource(dataSource)
                .properties(properties)
                .packages("edu.iu.uits.lms.gct.model")
                .build();
    }

    @Bean(name = "gctTransactionMgr")
    public PlatformTransactionManager gctTransactionMgr(
            @Qualifier("gctEntityMgrFactory") final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

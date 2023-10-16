package edu.iu.uits.lms.gct.config;

import lombok.extern.slf4j.Slf4j;
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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration("sqlServerDbConfig")
//@EnableJpaRepositories(
//        entityManagerFactoryRef = "sqlServerDbEntityMgrFactory",
//        transactionManagerRef = "sqlServerDbTransactionMgr",
//        basePackages = {
//                "edu.iu.uits.lms.gct.mailinglist"
//        })
@Slf4j
public class SQLServerDBConfig {

    @Bean(name = "sqlServerDb")
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

//    @Bean(name = "sqlServerDbEntityMgrFactory")
//    public LocalContainerEntityManagerFactoryBean sqlServerDbEntityMgrFactory(
//            final EntityManagerFactoryBuilder builder,
//            @Qualifier("sqlServerDb") final DataSource dataSource) {
//        // dynamically setting up the hibernate properties for each of the datasource.
//        final Map<String, String> properties = new HashMap<>();
//        return builder
//                .dataSource(dataSource)
//                .properties(properties)
//                .packages("edu.iu.uits.lms.gct.mailinglist")
//                .build();
//    }
//
//    @Bean(name = "sqlServerDbTransactionMgr")
//    public PlatformTransactionManager sqlServerDbTransactionMgr(
//            @Qualifier("sqlServerDbEntityMgrFactory") final EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
}

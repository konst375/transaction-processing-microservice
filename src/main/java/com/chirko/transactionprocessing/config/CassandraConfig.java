package com.chirko.transactionprocessing.config;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.cassandra.SessionFactory;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceAttributes;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.cql.keyspace.SpecificationBuilder;
import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator;
import org.springframework.data.cassandra.core.cql.session.init.ResourceKeyspacePopulator;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.List;

@Configuration
@EnableCassandraRepositories("com.chirko.transactionprocessing.repository.cassandra")
public class CassandraConfig extends AbstractCassandraConfiguration implements BeanClassLoaderAware {

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspaceName;

    @Override
    public String getContactPoints() {
        return contactPoints;
    }

    @Override
    public String getKeyspaceName() {
        return keyspaceName;
    }

    @Bean
    public CassandraOperations cassandraTemplate(SessionFactory sessionFactory, CassandraConverter converter) {
        return new CassandraTemplate(sessionFactory, converter);
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = SpecificationBuilder.createKeyspace(keyspaceName).ifNotExists()
                .with(KeyspaceOption.REPLICATION, KeyspaceAttributes.newSimpleReplication())
                .with(KeyspaceOption.DURABLE_WRITES, true);
        return List.of(specification);
    }

    @Override
    protected KeyspacePopulator keyspacePopulator() {
        return new ResourceKeyspacePopulator(
                new ClassPathResource("db/migration/cassandra/init.cql")
        );
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.chirko.transactionprocessing.model.cassandra"};
    }

}

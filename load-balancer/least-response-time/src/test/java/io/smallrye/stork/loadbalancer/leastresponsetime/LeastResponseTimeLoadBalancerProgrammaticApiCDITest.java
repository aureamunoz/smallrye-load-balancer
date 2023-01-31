package io.smallrye.stork.loadbalancer.leastresponsetime;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.inject.Inject;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.stork.Stork;
import io.smallrye.stork.api.NoServiceInstanceFoundException;
import io.smallrye.stork.api.Service;
import io.smallrye.stork.api.ServiceDefinition;
import io.smallrye.stork.api.ServiceInstance;
import io.smallrye.stork.impl.ServiceInstanceWithStatGathering;
import io.smallrye.stork.loadbalancer.leastresponsetime.impl.TestUtils;
import io.smallrye.stork.servicediscovery.staticlist.StaticConfiguration;
import io.smallrye.stork.test.EmptyServicesConfiguration;
import io.smallrye.stork.test.StorkTestUtils;
import io.smallrye.stork.test.TestConfigProviderBean;

@ExtendWith(WeldJunit5Extension.class)
public class LeastResponseTimeLoadBalancerProgrammaticApiCDITest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(TestConfigProviderBean.class,
            LeastResponseTimeLoadBalancerProviderLoader.class);

    @Inject
    TestConfigProviderBean config;

    private static final Logger log = LoggerFactory.getLogger(LeastResponseTimeLoadBalancerProgrammaticApiCDITest.class);

    public static final String FST_SRVC_1 = "localhost:8080";
    public static final String FST_SRVC_2 = "localhost:8081";
    private Stork stork;

    @BeforeEach
    void setUp() {
        config.clear();
        stork = StorkTestUtils.getNewStorkInstance();
        String listOfServices = String.format("%s,%s", FST_SRVC_1, FST_SRVC_2);
        stork
                .defineIfAbsent("first-service", ServiceDefinition.of(
                        new StaticConfiguration().withAddressList(listOfServices),
                        new LeastResponseTimeConfiguration()))
                .defineIfAbsent("first-service-secure-random", ServiceDefinition.of(
                        new StaticConfiguration().withAddressList(listOfServices),
                        new LeastResponseTimeConfiguration().withUseSecureRandom("true")))
                .defineIfAbsent("without-instances", ServiceDefinition.of(
                        new EmptyServicesConfiguration(),
                        new LeastResponseTimeConfiguration()));
    }

    @Test
    void shouldSelectNotSelectedFirst() {
        Service service = stork.getService("first-service");

        ServiceInstance serviceInstance = selectInstance(service);
        assertThat(asString(serviceInstance)).isEqualTo(FST_SRVC_1);
        serviceInstance = selectInstance(service);
        assertThat(asString(serviceInstance)).isEqualTo(FST_SRVC_2);
    }

    @Test
    void testWithSecureRandom() {
        Service service = stork.getService("first-service-secure-random");

        ServiceInstance serviceInstance = selectInstance(service);
        assertThat(asString(serviceInstance)).isEqualTo(FST_SRVC_1);
        serviceInstance.recordStart(true);
        serviceInstance = selectInstance(service);
        assertThat(asString(serviceInstance)).isEqualTo(FST_SRVC_2);
    }

    @Test
    void shouldSelectNonFailing() {
        Service service = stork.getService("first-service");

        // svc1 is not that fast
        ServiceInstance svc1 = selectInstance(service);
        assertThat(asString(svc1)).isEqualTo(FST_SRVC_1);
        int timeInNs = 80;
        mockRecordingTime(svc1, timeInNs);

        // svc2 is faster
        ServiceInstance svc2 = selectInstance(service);
        assertThat(asString(svc2)).isEqualTo(FST_SRVC_2);
        mockRecordingTime(svc2, 50);

        ServiceInstance selected;

        selected = selectInstance(service);
        assertThat(asString(selected)).isEqualTo(FST_SRVC_2);

        // but svc2 sometimes fails

        mockRecordingTime(svc2, 10);
        svc2.recordEnd(new RuntimeException("induced failure"));

        // so we should select svc1 for next calls
        selected = selectInstance(service);
        assertThat(asString(selected)).isEqualTo(FST_SRVC_1);
        selected = selectInstance(service);
        assertThat(asString(selected)).isEqualTo(FST_SRVC_1);
    }

    @SuppressWarnings("deprecation")
    private void mockRecordingTime(ServiceInstance svc1, int timeInNs) {
        ((ServiceInstanceWithStatGathering) svc1).mockRecordingTime(timeInNs);
    }

    @Test
    void shouldSelectFastest() {
        Service service = stork.getService("first-service");

        ServiceInstance svc1 = selectInstance(service);
        assertThat(asString(svc1)).isEqualTo(FST_SRVC_1);
        mockRecordingTime(svc1, 100);

        ServiceInstance svc2 = selectInstance(service);
        assertThat(asString(svc2)).isEqualTo(FST_SRVC_2);
        mockRecordingTime(svc2, 10);

        ServiceInstance selected;

        selected = selectInstance(service);
        assertThat(asString(selected)).isEqualTo(FST_SRVC_2);

        mockRecordingTime(svc2, 10);
        selected = selectInstance(service);
        assertThat(asString(selected)).isEqualTo(FST_SRVC_2);

        mockRecordingTime(svc2, 10);
        selected = selectInstance(service);
        assertThat(asString(selected)).isEqualTo(FST_SRVC_2);

        mockRecordingTime(svc2, 1000);
        selected = selectInstance(service);
        assertThat(asString(selected)).isEqualTo(FST_SRVC_1);

        mockRecordingTime(svc1, 1000);
        selected = selectInstance(service);
        assertThat(asString(selected)).isEqualTo(FST_SRVC_2);
    }

    @Test
    void shouldThrowNoServiceInstanceOnNoInstances() throws ExecutionException, InterruptedException, TimeoutException {
        Service service = stork.getService("first-service");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Callable<ServiceInstance>> callables = asList(
                () -> service.selectInstanceAndRecordStart(true).await().atMost(Duration.ofSeconds(5)),
                () -> service.selectInstanceAndRecordStart(true).await().atMost(Duration.ofSeconds(5)));
        for (int i = 0; i < 20; i++) { // let's test it a few times
            List<Future<ServiceInstance>> futures = executor.invokeAll(callables);

            Set<Long> serviceIds = new HashSet<>();
            for (Future<ServiceInstance> future : futures) {
                ServiceInstance serviceInstance = future.get(5, TimeUnit.SECONDS);
                serviceIds.add(serviceInstance.getId());
            }
            assertThat(serviceIds).hasSize(2); // just make sure different instances are selected

            clearStats((LeastResponseTimeLoadBalancer) service.getLoadBalancer());
        }
    }

    @Test
    void shouldSelectAllAvailableWhenInvokedInParallel() throws ExecutionException, InterruptedException {
        Service service = stork.getService("without-instances");

        CompletableFuture<Throwable> result = new CompletableFuture<>();

        service.selectInstance().subscribe().with(v -> log.error("Unexpected successful result: {}", v),
                result::complete);

        await().atMost(Duration.ofSeconds(10)).until(result::isDone);
        assertThat(result.get()).isInstanceOf(NoServiceInstanceFoundException.class);
    }

    @SuppressWarnings("deprecation")
    private void clearStats(LeastResponseTimeLoadBalancer balancer) {
        TestUtils.clear(balancer.getCallStatistics());
    }

    private ServiceInstance selectInstance(Service service) {
        return service.selectInstanceAndRecordStart(true).await().atMost(Duration.ofSeconds(5));
    }

    private String asString(ServiceInstance serviceInstance) {
        try {
            return String.format("%s:%s", serviceInstance.getHost(), serviceInstance.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

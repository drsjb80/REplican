package edu.msudenver.cs.replican;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReplicationFactoryTest {
    private ReplicationFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ReplicationFactory();
    }

    @Test
    void factoryCanBeInstantiated() {
        assertNotNull(factory);
    }

    @Test
    void factoryCreatesReplicator() {
        REplicanArgs args = new REplicanArgs();
        Replicator replicator = factory.createReplicator(args);
        assertNotNull(replicator);
    }

    @Test
    void createdReplicatorIsUsable() {
        REplicanArgs args = new REplicanArgs();
        Replicator replicator = factory.createReplicator(args);
        replicator.addURL("http://example.com");
        assertEquals(1, replicator.getQueueSize());
    }

    @Test
    void factoryLoadsNetscapeCookies() {
        REplicanArgs args = new REplicanArgs();
        CookieManager cookies = new CookiesAdapter(new Cookies());

        // This should not throw even with null cookie files
        assertDoesNotThrow(() -> factory.loadCookies(args, cookies));
    }
}

package net.syscon.elite.service.impl.keyworker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import net.syscon.elite.api.model.Keyworker;
import org.apache.commons.lang3.RandomUtils;
import org.mockito.ArgumentMatcher;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class KeyworkerTestHelper {
    // Initialises mock logging appender
    public static void initMockLogging(Appender mockAppender) {
        // Set-up mock appender to enable verification of log output
        ch.qos.logback.classic.Logger elite = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("net.syscon.elite");

        when(mockAppender.getName()).thenReturn("MOCK");
        elite.addAppender(mockAppender);
    }

    public static void verifyLog(Appender mockAppender, Level level, String message) {
        verify(mockAppender, atLeastOnce()).doAppend(argThat(new ArgumentMatcher() {

            @Override
            public boolean matches(final Object argument) {
                LoggingEvent loggingEvent = (LoggingEvent) argument;

                // Ignore debug logging
                if (!StringUtils.equals(Level.DEBUG.toString(), loggingEvent.getLevel().toString())) {
                    assertThat(loggingEvent.toString()).isEqualTo(formatLogMessage(level, message));
                }

                return true;
            }
        }));
    }

    public static void verifyException(Throwable thrown, Class<? extends Throwable> expectedException, String expectedMessage) {
        assertThat(thrown).isInstanceOf(expectedException).hasMessage(expectedMessage);
    }

    private static String formatLogMessage(Level level, String message) {
        LoggingEvent loggingEvent = new LoggingEvent();

        loggingEvent.setLevel(level);
        loggingEvent.setMessage(message);

        return loggingEvent.toString();
    }

    // Provides a Key worker with specified staff id and number of allocations
    public static Keyworker getKeyworker(long staffId, int numberOfAllocations) {
        return Keyworker.builder()
                .staffId(staffId)
                .numberAllocated(numberOfAllocations)
                .build();
    }

    // Provides list of Key workers with varying number of allocations (within specified range)
    public static List<Keyworker> getKeyworkers(long total, int minAllocations, int maxAllocations) {
        List<Keyworker> keyworkers = new ArrayList<>();

        for (long i = 1; i <= total; i++) {
            keyworkers.add(Keyworker.builder()
                    .staffId(i)
                    .numberAllocated(RandomUtils.nextInt(minAllocations, maxAllocations + 1))
                    .build());
        }

        return keyworkers;
    }
}

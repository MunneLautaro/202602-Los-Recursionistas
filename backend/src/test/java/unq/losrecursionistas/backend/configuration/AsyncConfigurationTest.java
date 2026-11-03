package unq.losrecursionistas.backend.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigurationTest {

    @Test
    void configuraExecutorDeCotizaciones() {
        ThreadPoolTaskExecutor executor = new AsyncCotizacionConfiguration().cotizacionTaskExecutor();

        assertThat(executor.getCorePoolSize()).isEqualTo(1);
        assertThat(executor.getMaxPoolSize()).isEqualTo(1);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("cotizacion-");
        executor.shutdown();
    }

    @Test
    void configuraExecutorDeIngesta() {
        TaskExecutor executor = new AsyncIngestaConfiguration().ingestaTaskExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) executor;
        assertThat(threadPool.getCorePoolSize()).isEqualTo(1);
        assertThat(threadPool.getThreadNamePrefix()).isEqualTo("ingesta-whoscored-");
        threadPool.shutdown();
    }
}

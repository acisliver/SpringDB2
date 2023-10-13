package study.spring.transaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <p>
 *   트랜잭션 AOP는 기본적으로 프록시 방식의 AOP를 사용한다.
 *   프록시 객체가 트랜잭션을 처리하고, 실제 객체를 호출하는 방식이다.
 *   따라서 트랜잭션을 적용하려면 항상 프록시를 통해서 대상 객체를 호출해야 한다.
 *   만약 대상 객체를 직접 호출하게 되면 트랜잭션이 적용되지 않는 문제가 발생할 수 있다.
 * </p>
 * <p>
 *   AOP를 적용하면 스프링은 대상 객체 대신 프록시를 스프링 빈으로 등록한다.
 *   그래서 실제 객체 대신 프록시 객체가 대신 의존 관계 주입된다.
 *   일반적으로 프록시 객체가 주입되므로 대상 객체를 직접 호출하는 문제는 발생하지 않는다.
 * </p>
 * <p>
 *   하지만 대상 객체 내부에서 메서드 호출이 발생하면 프록시를 거치지 않고 대상 객체를 직접 호출하는 문제가 발생한다.
 * </p>
 * <p>
 *   프록시 객체가 external()을 호출할 때는 @Transactional이 없기 때문에 트랜잭션이 적용되지 않는다.
 *   즉, 트랜잭션이 적용되지 않은 상태에서 대상 객체의 메서드가 호출된다.
 * <p>
 *   external()이 internal()을 호출할 때는 @Transactional이 존재한다.
 *   하지만 external() 메서드는 대상 객체이기 때문에 트랜잭션이 적용되지 않는다.
 * </p>
 * @Transactional self-invocation (in effect, a method within the target object calling another method of the target object) does not lead to an actual transaction at runtime
 *
 * <p>
 *     가장 단순한 해결 방법은 internal() 메서드를 별도의 클래스로 분리하는 것이다.
 * </p>
 */

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void internalCall() {
        callService.internal();
    }

    @Test
    void externalCall() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1Config {
        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    static class CallService {

        public void external() {
            log.info("call external");
            printTxInfo();
            this.internal();
        }

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly={}", readOnly);
        }
    }
}

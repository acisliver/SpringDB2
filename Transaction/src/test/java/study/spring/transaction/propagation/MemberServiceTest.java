package study.spring.transaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired LogRepository logRepository;

    /**
     * memberService     @Transactional: OFF
     * memberRepository  @Transactional: ON
     * logRepository     @Transactional: ON
     */
    @Test
    void outerTxOff_success() {
        String username = "outerTxOff_success";

        memberService.joinV1(username);

        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService     @Transactional: OFF
     * memberRepository  @Transactional: ON
     * logRepository     @Transactional: ON Exception
     */
    @Test
    void outerTxOff_fail() {
        String username = "로그예외_outerTxOff_fail";

        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // log 데이터는 롤백된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService     @Transactional: ON
     * memberRepository  @Transactional: OFF
     * logRepository     @Transactional: OFF
     */
    @Test
    void single_success() {
        String username = "outerTxOff_success";

        memberService.joinV1(username);

        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService     @Transactional: ON
     * memberRepository  @Transactional: ON
     * logRepository     @Transactional: ON
     */
    @Test
    void outerTxOn_success() {
        String username = "outerTxOn_success";

        memberService.joinV1(username);

        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService     @Transactional: ON Log Repository의 Exception 전파됨
     * memberRepository  @Transactional: ON
     * logRepository     @Transactional: ON Exception trasaction rollback-only mark
     */
    @Test
    void outerTxOn_fail() {
        String username = "로그예외_outerTxOn_success";

        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // 모든 데이터(member, log)가 롤백된다.
        // member 저장은 성공했지만 하나의 같은 물리 트랜잭션이기 때문에 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * MemberService에서 LogRepository의 예외 복구
     *
     *
     * memberService     @Transactional: ON Log Repository의 Exception 전파됨
     * memberRepository  @Transactional: ON
     * logRepository     @Transactional: ON Exception trasaction rollback-only mark
     */
    @Test
    void recoverException_fail() {
        String username = "로그예외_recoverException_fail";

        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // 모든 데이터(member, log)가 롤백된다.
        // trasaction rollback-only mark가 되어있어 트랜잭션 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService     @Transactional: ON Log Repository의 Exception 전파됨
     * memberRepository  @Transactional: ON
     * logRepository     @Transactional: ON(REQUIRES_NEW) Exception Suspending current transaction, creating new transaction
     */
    @Test
    void recoverException__success() {
        String username = "로그예외_recoverException__success";

        memberService.joinV2(username);

        // member 저장, log 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }
}

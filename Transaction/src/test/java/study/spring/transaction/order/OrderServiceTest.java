package study.spring.transaction.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @DisplayName("정상")
    @Test
    void complete() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("정상");

        orderService.order(order);

        Order foundOrder = orderRepository.findById(1L).get();
        assertThat(foundOrder.getPayStatus()).isEqualTo("완료");
    }

    @DisplayName("시스템 예외")
    @Test
    void rumtimeException() {
        Order order = new Order();
        order.setUsername("예외");

        assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(RuntimeException.class);
        Optional<Order> foundOrder = orderRepository.findById(1L);
        assertThat(foundOrder.isEmpty()).isTrue();
    }

    @DisplayName("잔고 부족")
    @Test
    void notEnoughMoney() {
        Order order = new Order();
        order.setUsername("잔고부족");
        assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(NotEnoughMoneyException.class);
        Order foundOrder = orderRepository.findById(1L).get();
            assertThat(foundOrder.getPayStatus()).isEqualTo("대기");
    }

}

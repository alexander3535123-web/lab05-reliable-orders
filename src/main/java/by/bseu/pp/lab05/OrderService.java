package by.bseu.pp.lab05;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());
    private final Map<String, Order> orders = new HashMap<>();

    /** Создать и сохранить новый заказ; повторный id запрещён. */
    public Order createOrder(String id, double totalAmount) {
        try {
            OrderValidator.validateId(id);
            OrderValidator.validateAmount(totalAmount);
            if (orders.containsKey(id)) {
                throw new OrderOperationException("Заказ " + id + " уже существует");
            }
            Order order = new Order(id, totalAmount);
            orders.put(id, order);
            LOGGER.info("Создан заказ " + id + ", сумма=" + totalAmount);
            return order;
        } catch (ValidationException | OrderOperationException ex) {
            LOGGER.warning("Операция заказа " + id + " отклонена: " + ex.getMessage());
            throw ex;
        }
    }

    /** Найти существующий заказ; успешный поиск записать на уровне FINE. */
    public Order findOrder(String id) {
        try {
            OrderValidator.validateId(id);
            Order order = getOrderOrThrow(id);
            LOGGER.fine("Найден заказ " + id + ", статус=" + order.getStatus());
            return order;
        } catch (ValidationException | OrderOperationException ex) {
            LOGGER.warning("Операция заказа " + id + " отклонена: " + ex.getMessage());
            throw ex;
        }
    }

    /** Оплатить CREATED-заказ полностью. Проверки выполняются до изменения состояния. */
    public void payOrder(String id, double paymentAmount) {
        try {
            OrderValidator.validateId(id);
            OrderValidator.validateAmount(paymentAmount);
            Order order = getOrderOrThrow(id);
            if (order.getStatus() != OrderStatus.CREATED) {
                throw new OrderOperationException(
                        "Заказ " + id + ": оплата недоступна в статусе " + order.getStatus());
            }
            if (Double.compare(paymentAmount, order.getTotalAmount()) != 0) {
                throw new OrderOperationException(
                        "Заказ " + id + ": сумма оплаты " + paymentAmount
                                + " должна равняться " + order.getTotalAmount());
            }
            order.markPaid();
            LOGGER.info("Оплата заказа " + id + ", сумма=" + paymentAmount);
        } catch (ValidationException | OrderOperationException ex) {
            LOGGER.warning("Операция заказа " + id + " отклонена: " + ex.getMessage());
            throw ex;
        }
    }

    /** Отменить только CREATED-заказ; PAID и CANCELLED запрещены. */
    public void cancelOrder(String id) {
        try {
            OrderValidator.validateId(id);
            Order order = getOrderOrThrow(id);
            if (order.getStatus() != OrderStatus.CREATED) {
                throw new OrderOperationException(
                        "Заказ " + id + ": отмена недоступна в статусе " + order.getStatus());
            }
            order.markCancelled();
            LOGGER.info("Отмена заказа " + id);
        } catch (ValidationException | OrderOperationException ex) {
            LOGGER.warning("Операция заказа " + id + " отклонена: " + ex.getMessage());
            throw ex;
        }
    }

    /** Готовый метод для проверки количества сохранённых заказов. */
    public int getOrderCount() { return orders.size(); }

    /** Готовый поиск без логирования — используйте его внутри операций. */
    private Order getOrderOrThrow(String id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new OrderOperationException("Заказ " + id + " не найден");
        }
        return order;
    }
}

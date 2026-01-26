package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.common.OrderStatus;
import com.szu.mallsystem.dto.order.CreateOrderRequest;
import com.szu.mallsystem.dto.order.OrderItemRequest;
import com.szu.mallsystem.entity.Address;
import com.szu.mallsystem.entity.CartItem;
import com.szu.mallsystem.entity.Order;
import com.szu.mallsystem.entity.OrderItem;
import com.szu.mallsystem.entity.Product;
import com.szu.mallsystem.entity.ProductImage;
import com.szu.mallsystem.mapper.AddressMapper;
import com.szu.mallsystem.mapper.CartItemMapper;
import com.szu.mallsystem.mapper.OrderItemMapper;
import com.szu.mallsystem.mapper.OrderMapper;
import com.szu.mallsystem.mapper.ProductImageMapper;
import com.szu.mallsystem.mapper.ProductMapper;
import com.szu.mallsystem.service.OrderService;
import com.szu.mallsystem.vo.OrderDetailVO;
import com.szu.mallsystem.vo.OrderItemVO;
import com.szu.mallsystem.vo.OrderStatsVO;
import com.szu.mallsystem.vo.OrderSummaryVO;
import com.szu.mallsystem.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    private final AddressMapper addressMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional
    public OrderDetailVO createOrder(Long userId, CreateOrderRequest request) {
        List<OrderItemRequest> items = request.getItems();
        boolean useCart = Boolean.TRUE.equals(request.getUseCart());
        if ((items == null || items.isEmpty()) && useCart) {
            List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId)
                    .eq(CartItem::getChecked, 1));
            if (cartItems == null || cartItems.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "购物车为空，无法下单");
            }
            items = cartItems.stream()
                    .map(ci -> {
                        OrderItemRequest req = new OrderItemRequest();
                        req.setProductId(ci.getProductId());
                        req.setQuantity(ci.getQuantity());
                        return req;
                    })
                    .collect(Collectors.toList());
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Order items are required");
        }

        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Address not found");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<Long> productIds = new ArrayList<>();

        for (OrderItemRequest itemRequest : items) {
            if (itemRequest.getProductId() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "productId is required");
            }
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "quantity must be >= 1");
            }
            Product product = productMapper.selectById(itemRequest.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "Product not found");
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Product is not available");
            }
            if (product.getStock() == null || product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Insufficient stock");
            }

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
            String productImage = getProductImage(product.getId());

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(productImage);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setTotalPrice(lineTotal);
            orderItem.setCreatedAt(now);
            orderItems.add(orderItem);
            productIds.add(product.getId());
        }

        BigDecimal freightAmount = BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.add(freightAmount);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING.getCode());
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(freightAmount);
        order.setPayAmount(payAmount);
        order.setPayMethod(request.getPayMethod());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setProvince(address.getProvince());
        order.setCity(address.getCity());
        order.setDistrict(address.getDistrict());
        order.setDetail(address.getDetail());
        order.setPostalCode(address.getPostalCode());
        order.setRemark(request.getRemark());
        order.setDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        save(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // Update stock after the order is created.
        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            productMapper.updateById(product);
        }

        if (!productIds.isEmpty()) {
            cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId)
                    .in(CartItem::getProductId, productIds));
        }

        return buildOrderDetail(order, orderItems);
    }

    @Override
    public PageResult<OrderSummaryVO> listOrders(Long userId, Integer status, Integer page, Integer pageSize) {
        return listOrdersInternal(userId, status, page, pageSize, false);
    }

    @Override
    public PageResult<OrderSummaryVO> listOrdersByAdmin(Long userId, Integer status, Integer page, Integer pageSize) {
        return listOrdersInternal(userId, status, page, pageSize, true);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long userId, Long orderId, boolean admin) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Order not found");
        }
        if (!admin && !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Order not found");
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        return buildOrderDetail(order, items);
    }

    @Override
    @Transactional
    public void payOrder(Long userId, Long orderId, Integer payMethod) {
        Order order = requireOrderForUser(userId, orderId);
        if (order.getStatus() == null || order.getStatus() != OrderStatus.PENDING.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Order status does not allow payment");
        }
        order.setStatus(OrderStatus.PAID.getCode());
        order.setPayMethod(payMethod != null ? payMethod : 1);
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId, String reason) {
        Order order = requireOrderForUser(userId, orderId);
        if (order.getStatus() == null || order.getStatus() != OrderStatus.PENDING.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Order status does not allow cancel");
        }
        order.setStatus(OrderStatus.CANCELED.getCode());
        order.setCanceledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Order not found");
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.PAID.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Order status does not allow shipment");
        }
        order.setStatus(OrderStatus.SHIPPED.getCode());
        order.setShippedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long userId, Long orderId) {
        Order order = requireOrderForUser(userId, orderId);
        if (order.getStatus() == null || order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Order status does not allow completion");
        }
        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public OrderStatsVO getOrderStats() {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status", "COUNT(*) as cnt", "SUM(pay_amount) as amount")
                .eq("deleted", 0)
                .groupBy("status");
        List<Map<String, Object>> rows = baseMapper.selectMaps(queryWrapper);

        Map<Integer, Long> counts = new HashMap<>();

        long totalOrders = 0;
        BigDecimal totalPayAmount = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            Integer status = ((Number) row.get("status")).intValue();
            Long count = ((Number) row.get("cnt")).longValue();
            BigDecimal amount = row.get("amount") == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(row.get("amount").toString());
            counts.put(status, count);
            totalOrders += count;
            totalPayAmount = totalPayAmount.add(amount);
        }

        return OrderStatsVO.builder()
                .totalOrders(totalOrders)
                .totalPayAmount(totalPayAmount)
                .pendingCount(counts.getOrDefault(OrderStatus.PENDING.getCode(), 0L))
                .paidCount(counts.getOrDefault(OrderStatus.PAID.getCode(), 0L))
                .shippedCount(counts.getOrDefault(OrderStatus.SHIPPED.getCode(), 0L))
                .completedCount(counts.getOrDefault(OrderStatus.COMPLETED.getCode(), 0L))
                .canceledCount(counts.getOrDefault(OrderStatus.CANCELED.getCode(), 0L))
                .build();
    }

    private PageResult<OrderSummaryVO> listOrdersInternal(Long userId, Integer status, Integer page, Integer pageSize, boolean admin) {
        int pageNo = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (!admin) {
            queryWrapper.eq(Order::getUserId, userId);
        } else if (userId != null) {
            queryWrapper.eq(Order::getUserId, userId);
        }
        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }
        queryWrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> pageResult = new Page<>(pageNo, size);
        IPage<Order> orderPage = page(pageResult, queryWrapper);

        List<Order> orders = orderPage.getRecords();
        Map<Long, Integer> itemCountMap = loadItemCounts(orders);

        List<OrderSummaryVO> summaries = orders.stream()
                .map(order -> OrderSummaryVO.builder()
                        .id(order.getId())
                        .orderNo(order.getOrderNo())
                        .status(order.getStatus())
                        .totalAmount(order.getTotalAmount())
                        .payAmount(order.getPayAmount())
                        .itemCount(itemCountMap.getOrDefault(order.getId(), 0))
                        .createdAt(order.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        long totalPages = (orderPage.getTotal() + size - 1) / size;

        return PageResult.<OrderSummaryVO>builder()
                .records(summaries)
                .total(orderPage.getTotal())
                .page(pageNo)
                .pageSize(size)
                .totalPages((int) totalPages)
                .build();
    }

    private Order requireOrderForUser(Long userId, Long orderId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Order not found");
        }
        return order;
    }

    private Map<Long, Integer> loadItemCounts(List<Order> orders) {
        Map<Long, Integer> result = new HashMap<>();
        if (orders.isEmpty()) {
            return result;
        }
        List<Long> orderIds = orders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        for (OrderItem item : items) {
            result.merge(item.getOrderId(), item.getQuantity(), Integer::sum);
        }
        return result;
    }

    private OrderDetailVO buildOrderDetail(Order order, List<OrderItem> items) {
        List<OrderItemVO> itemVOs = items.stream()
                .map(item -> OrderItemVO.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productImage(item.getProductImage())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .freightAmount(order.getFreightAmount())
                .payAmount(order.getPayAmount())
                .payMethod(order.getPayMethod())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .canceledAt(order.getCanceledAt())
                .cancelReason(order.getCancelReason())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .province(order.getProvince())
                .city(order.getCity())
                .district(order.getDistrict())
                .detail(order.getDetail())
                .postalCode(order.getPostalCode())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemVOs)
                .build();
    }

    private String generateOrderNo() {
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        for (int i = 0; i < 5; i++) {
            String candidate = "O" + prefix + randomDigits(6);
            long count = lambdaQuery().eq(Order::getOrderNo, candidate).count();
            if (count == 0) {
                return candidate;
            }
        }
        throw new BusinessException(ErrorCode.SERVER_ERROR, "Failed to generate order number");
    }

    private String randomDigits(int length) {
        int max = (int) Math.pow(10, length);
        int value = ThreadLocalRandom.current().nextInt(max);
        return String.format("%0" + length + "d", value);
    }

    private String getProductImage(Long productId) {
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSort)
                        .last("limit 1")
        );
        if (images.isEmpty()) {
            return null;
        }
        return images.get(0).getUrl();
    }
}

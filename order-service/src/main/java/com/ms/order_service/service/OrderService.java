package com.ms.order_service.service;

import brave.Span;
import brave.Tracer;
import com.ms.order_service.dto.InventoryResponse;
import com.ms.order_service.dto.OrderLineItemsDto;
import com.ms.order_service.dto.OrderRequest;
import com.ms.order_service.event.OrderPlacedEvent;
import com.ms.order_service.model.Order;
import com.ms.order_service.model.OrderLineItems;
import com.ms.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final WebClient.Builder webClientBuilder;

    private final OrderRepository orderRepository;
    private final Tracer tracer;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
      List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtos()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);

     List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();
        //call incventory service

      Span inventoryServiceLookup =  tracer.nextSpan().name("InventoryServiceLookup");
        try(Tracer.SpanInScope spanInScope = tracer.withSpanInScope(inventoryServiceLookup.start())) {

          InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get().uri("http://INVENTORY-SERVICE/api/inventory", uriBuilder ->
                          uriBuilder.queryParam("skuCode", skuCodes).build())

                  .retrieve()
                  .bodyToMono(InventoryResponse[].class)
                  .block();
          boolean allProductsInstock    =  Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);
          if(allProductsInstock){
              orderRepository.save(order);
              kafkaTemplate.send("notificationTopic",new OrderPlacedEvent(order.getOrderNumber()));
              return "order placed sucessfully";
          }

          else {
              throw new IllegalArgumentException("not enough stock please come back later");
          }



      }
      finally{
          inventoryServiceLookup.finish();

          }



    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {

        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }

}

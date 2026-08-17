//package com.TaskFlow.ProjectService.client;
//
//import com.TaskFlow.ProjectService.exception.ApiException;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import java.util.UUID;
//import lombok.Data;
//import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
//import org.springframework.http.HttpStatus;
//
//@FeignClient(name = "user-service", url = "http://localhost:8081")
//public interface UserServiceClient {
//
//    // Defined in docs as GET /internal/users/{id}
//    @GetMapping("/internal/users/{id}")
//    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
//    UserResponse getUser(@PathVariable("id") UUID id);
//
//    default UserResponse getUserFallback(UUID id, Throwable exception) {
//        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "User Service is temporarily unavailable");
//    }
//
//    @Data
//    class UserResponse {
//        private UUID id;
//        private String email;
//        private String fullName;
//    }
//}
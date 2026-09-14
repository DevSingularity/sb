package org.example.createrstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    @NotBlank(message = "Customer name cannot be Empty")
    private String customerName;

    @NotBlank(message = "Customer email cannot be Empty")
    @Email(message = "Enter a valid email")
    private String customerEmail;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> items;
}

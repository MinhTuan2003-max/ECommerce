package fpt.tuanhm43.server.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProductSavedEvent {
    private final UUID productId;
}
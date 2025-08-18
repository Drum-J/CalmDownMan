package chimhaha.chimcard.card.dto;

import java.util.List;

public record SupplyCardRequestDto(Long accountId, List<SupplyCardDto> cards) {

}

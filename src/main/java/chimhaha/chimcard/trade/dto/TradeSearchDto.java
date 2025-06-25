package chimhaha.chimcard.trade.dto;

import chimhaha.chimcard.entity.Grade;
import chimhaha.chimcard.entity.TradeStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;

@Getter @Setter
@NoArgsConstructor
public class TradeSearchDto {
    private int page = 0;
    private int size = 10;
    private String status = "ALL";
    private int grade = 10;

    public PageRequest toPageRequest() {
        return PageRequest.of(page, size);
    }

    public TradeStatus toStatus() {
        return TradeStatus.getEnum(status);
    }

    public Grade toGrade() {
        return Grade.getEnum(grade);
    }
}

package chimhaha.chimcard.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class CardSeason extends TimeStamped {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String seasonName;

    private String imageUrl;

    public CardSeason(String seasonName, String imageUrl) {
        this.seasonName = seasonName;
        this.imageUrl = imageUrl;
    }
}

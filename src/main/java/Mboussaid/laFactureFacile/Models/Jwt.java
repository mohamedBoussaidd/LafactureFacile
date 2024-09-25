package Mboussaid.laFactureFacile.Models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
public class Jwt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String value;
    private boolean isBlackListed;
    private boolean isExpired;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private User user;

    public Jwt() {
    }

    public Jwt(Long id, String value, boolean isBlackListed, boolean isExpired, User user) {
        this.id = id;
        this.value = value;
        this.isBlackListed = isBlackListed;
        this.isExpired = isExpired;
        this.user = user;
    }

}

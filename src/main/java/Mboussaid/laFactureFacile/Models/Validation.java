package Mboussaid.laFactureFacile.Models;

import java.time.ZonedDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "validations")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private ZonedDateTime creation;
    private ZonedDateTime expired;
    private ZonedDateTime activation;
    private String code;
    @OneToOne( cascade = {CascadeType.MERGE, CascadeType.DETACH})
    @JoinColumn(name = "user_id")
    private User user;
    private String uid;

    public Validation() {
    }
    
    public Validation(Integer id, ZonedDateTime creation, ZonedDateTime expired, ZonedDateTime activation, String code, User user, String uid) {
        this.id = id;
        this.creation = creation;
        this.expired = expired;
        this.activation = activation;
        this.code = code;
        this.user = user;
        this.uid = uid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ZonedDateTime getCreation() {
        return creation;
    }

    public void setCreation(ZonedDateTime creation) {
        this.creation = creation;
    }

    public ZonedDateTime getExpired() {
        return expired;
    }

    public void setExpired(ZonedDateTime expired) {
        this.expired = expired;
    }

    public ZonedDateTime getActivation() {
        return activation;
    }

    public void setActivation(ZonedDateTime activation) {
        this.activation = activation;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

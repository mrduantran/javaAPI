package stackjava.com.sbjwt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties
public class UserModel {

    private int id;
    private String username;
    private String password;

}

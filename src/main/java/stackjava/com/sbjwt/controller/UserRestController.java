package stackjava.com.sbjwt.controller;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import stackjava.com.sbjwt.entities.User;
import stackjava.com.sbjwt.model.UserModel;
import stackjava.com.sbjwt.service.JwtService;
import stackjava.com.sbjwt.service.UserService;
import stackjava.com.sbjwt.utils.RSAUtils;


@RestController
@RequestMapping("/rest")
public class UserRestController {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @Value("${RSA_PUBLIC_KEY}")
    private String publicKey;
    @Value("${RSA_PRIVATE_KEY}")
    private String privateKey;

    /* ---------------- GET ALL USER ------------------------ */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<List<User>> getAllUser() {


        return new ResponseEntity<List<User>>(userService.findAll(), HttpStatus.OK);
    }


    /* ----------------encrypt ------------------------ */
    @RequestMapping(value = "/encryptData", method = RequestMethod.POST)
    public ResponseEntity<String> decryptData(HttpServletRequest request, @RequestBody UserModel user) {

        String result = "";
        HttpStatus httpStatus = null;
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(user);
        try {

            byte[] results = RSAUtils.encrypt(json, publicKey);
            result = Base64.getEncoder().encodeToString(results);
            httpStatus = HttpStatus.OK;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<String>(result, httpStatus);
    }


    /* ---------------- GET USER BY ID ------------------------ */
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> getUserById(@PathVariable int id) {
        User user = userService.findById(id);
        if (user != null) {
            return new ResponseEntity<Object>(user, HttpStatus.OK);
        }
        return new ResponseEntity<Object>("Not Found User", HttpStatus.NO_CONTENT);
    }

    /* ---------------- CREATE NEW USER ------------------------ */
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public ResponseEntity<String> createUser(@RequestBody String request) {
        try {
            String res = RSAUtils.decrypt(request, privateKey);

            Gson gson = new GsonBuilder().create();

            if (res != null) {
                User user = gson.fromJson(res, User.class);
                if (userService.add(user)) {
                    return new ResponseEntity<String>("Created!", HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<String>("User Existed!", HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<String>("Data Encrypt Bad", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>("Unknow!", HttpStatus.BAD_REQUEST);
        }
    }

    /* ---------------- DELETE USER ------------------------ */
    @RequestMapping(value = "/users/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteUserById(@PathVariable int id) {
        userService.delete(id);
        return new ResponseEntity<String>("Deleted!", HttpStatus.OK);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<String> login(HttpServletRequest request, @RequestBody User user) {
        String result = "";
        HttpStatus httpStatus = null;
        try {
            if (userService.checkLogin(user)) {
                result = jwtService.generateTokenLogin(user.getUsername());
                httpStatus = HttpStatus.OK;
            } else {
                result = "Wrong userId and password";
                httpStatus = HttpStatus.BAD_REQUEST;
            }
        } catch (Exception ex) {
            result = "Server Error";
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<String>(result, httpStatus);
    }

}

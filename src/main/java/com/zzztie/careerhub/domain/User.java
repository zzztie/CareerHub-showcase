package com.zzztie.careerhub.domain;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzztie.careerhub.enums.UserStatus;
import com.zzztie.careerhub.exception.UserValidationException;

import java.util.Objects;

@TableName("user")
public class User extends BaseEntity implements Describable {
    private String username;
    private String email;
    private UserStatus status;
    private String passwordHash;

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {

        this.passwordHash = passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public User(){ //无参构造器
    }

    public User(Long id,String username,String email){
        super(id);
        this.username=username;
        this.email=email;
    }

    @Override
    public void printInfo(){
        super.printInfo();
        System.out.println("username="+username);
        System.out.println("email="+email);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new UserValidationException("username cannot be blank");
        }
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void updateProfile(String username) {
        this.username = username;
    }

    public void updateProfile(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @Override
    public String getEntityType(){
        return "User";
    }

    @Override
    public String getSummary() {
        return "User: " + getUsername();
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){//看是不是同个对象
            return true;
        }
        if(!(obj instanceof User)){
            return false;
        }
        User user = (User) obj;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(getId());
    }

}

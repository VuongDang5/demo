package vn.vccorp.servicemonitoring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Value("${smtp.host}")
    private String host;

    @Value("${smtp.port}")
    private String port;

    @Value("${smtp.user}")
    private String user;

    @Value("${smtp.password}")
    private String pwd;

    @Value("${smtp.authentication}")
    private String authentication;

    @Value("${smtp.sender.email}")
    private String sender;

    @Value("${smtp.cc.email}")
    private String cc;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    @Override
    public String toString() {
        return new StringBuilder("EmailConfig[host: ").append(host)
                .append(", port: ").append(port)
                .append(", user: ").append(user)
                .append(", pwd: ").append(pwd)
                .append("]")
                .toString();
    }
}

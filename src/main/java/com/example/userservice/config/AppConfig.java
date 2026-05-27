package com.example.userservice.config;

public class AppConfig {
    private String dbUrl;
    private String dbUser;
    private String dbPass;
    private int port;
    private String jwtSecret;
    private String userServiceOrigin;
    private String natsOrigin;
    private String natsUsername;
    private String natsPassword;
    private String natsAuthUserLoginSubject;
    private String natsAuthUserRegisterSubject;
    private static AppConfig config;

    private AppConfig() {
    }

    public static AppConfig GetInstance() {
        if (config == null) {
            config = new AppConfig();
            String dbURL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/authdb");
            config.setDbUrl(dbURL);
            String dbUser = System.getenv().getOrDefault("DB_USER", "root");
            config.setDbUser(dbUser);
            String dbPass = System.getenv().getOrDefault("DB_PASS", "");
            config.setDbPass(dbPass);
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            config.setPort(port);
            String jwtSecret = System.getenv().getOrDefault("JWT_SECRET", "");
            config.setJwtSecret(jwtSecret);
            String userServiceOrigin = System.getenv().getOrDefault("USER_SERVICE_ORIGIN", "");
            config.setUserServiceOrigin(userServiceOrigin);
            String natsOrigin = System.getenv().getOrDefault("NATS_ORIGIN", "");
            config.setNatsOrigin(natsOrigin);
            String natsUsername = System.getenv().getOrDefault("NATS_USERNAME", "");
            config.setNatsUsername(natsUsername);
            String natsPassword = System.getenv().getOrDefault("NATS_PASSWORD", "");
            config.setNatsPassword(natsPassword);
            String natsAuthUserLoginSubject = System.getenv().getOrDefault("NATS_AUTH_USER_LOGIN_SUBJECT", "");
            config.setNatsAuthUserLoginSubject(natsAuthUserLoginSubject);
            String natsAuthUserRegisterSubject = System.getenv().getOrDefault("NATS_AUTH_USER_REGISTER_SUBJECT", "");
            config.setNatsAuthUserRegisterSubject(natsAuthUserRegisterSubject);
        }

        return config;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public void setDbPass(String dbPass) {
        this.dbPass = dbPass;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getUserServiceOrigin() {
        return userServiceOrigin;
    }

    public void setUserServiceOrigin(String userServiceOrigin) {
        this.userServiceOrigin = userServiceOrigin;
    }

    public String getNatsOrigin() {
        return natsOrigin;
    }

    public void setNatsOrigin(String natOrigin) {
        this.natsOrigin = natOrigin;
    }

    public String getNatsUsername() {
        return natsUsername;
    }

    public void setNatsUsername(String natUsername) {
        this.natsUsername = natUsername;
    }

    public String getNatsPassword() {
        return natsPassword;
    }

    public void setNatsPassword(String natPassword) {
        this.natsPassword = natPassword;
    }

    public String getNatsAuthUserLoginSubject() {
        return natsAuthUserLoginSubject;
    }

    public void setNatsAuthUserLoginSubject(String natAuthLoginSubject) {
        this.natsAuthUserLoginSubject = natAuthLoginSubject;
    }

    public String getNatsAuthUserRegisterSubject() {
        return natsAuthUserRegisterSubject;
    }

    public void setNatsAuthUserRegisterSubject(String natAuthRegisterSubject) {
        this.natsAuthUserRegisterSubject = natAuthRegisterSubject;
    }
}

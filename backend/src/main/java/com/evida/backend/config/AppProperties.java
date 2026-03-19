package com.evida.backend.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "evida")
public class AppProperties {

    private final Storage storage = new Storage();
    private final Ai ai = new Ai();
    private final Policy policy = new Policy();
    private final Security security = new Security();

    public Storage getStorage() { return storage; }
    public Ai getAi() { return ai; }
    public Policy getPolicy() { return policy; }
    public Security getSecurity() { return security; }

    public static class Storage {
        private String uploadDir = "uploads";
        public String getUploadDir() { return uploadDir; }
        public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    }

    public static class Ai {
        private String baseUrl = "http://localhost:8000";
        private boolean useLiveService;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public boolean isUseLiveService() { return useLiveService; }
        public void setUseLiveService(boolean useLiveService) { this.useLiveService = useLiveService; }
    }

    public static class Policy {
        private double autoApprovalThreshold = 0.95d;
        private List<String> nondeductibleKeywords = new ArrayList<>();
        public double getAutoApprovalThreshold() { return autoApprovalThreshold; }
        public void setAutoApprovalThreshold(double autoApprovalThreshold) { this.autoApprovalThreshold = autoApprovalThreshold; }
        public List<String> getNondeductibleKeywords() { return nondeductibleKeywords; }
        public void setNondeductibleKeywords(List<String> nondeductibleKeywords) { this.nondeductibleKeywords = nondeductibleKeywords; }
    }

    public static class Security {
        private List<User> users = new ArrayList<>();
        public List<User> getUsers() { return users; }
        public void setUsers(List<User> users) { this.users = users; }
    }

    public static class User {
        private String username;
        private String password;
        private String role;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
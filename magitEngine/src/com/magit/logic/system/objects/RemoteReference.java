package com.magit.logic.system.objects;

public class RemoteReference {

    private String repositoryName;
    private String location;

    public RemoteReference(String repositoryName, String location) {
        this.repositoryName = repositoryName;
        this.location = location;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getLocation() {
        return location;
    }
}

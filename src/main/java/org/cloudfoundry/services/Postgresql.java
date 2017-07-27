
package org.cloudfoundry.services;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Postgresql {

    @SerializedName("credentials")
    @Expose
    private Credentials credentials;
    @SerializedName("syslog_drain_url")
    @Expose
    private Object syslogDrainUrl;
    @SerializedName("volume_mounts")
    @Expose
    private List<Object> volumeMounts = null;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("provider")
    @Expose
    private Object provider;
    @SerializedName("plan")
    @Expose
    private String plan;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("tags")
    @Expose
    private List<String> tags = null;

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public Object getSyslogDrainUrl() {
        return syslogDrainUrl;
    }

    public void setSyslogDrainUrl(Object syslogDrainUrl) {
        this.syslogDrainUrl = syslogDrainUrl;
    }

    public List<Object> getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(List<Object> volumeMounts) {
        this.volumeMounts = volumeMounts;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getProvider() {
        return provider;
    }

    public void setProvider(Object provider) {
        this.provider = provider;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}


package org.cloudfoundry.services;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VcapParams {

    @SerializedName("postgresql")
    @Expose
    private List<Postgresql> postgresql = null;

    public List<Postgresql> getPostgresql() {
        return postgresql;
    }

    public void setPostgresql(List<Postgresql> postgresql) {
        this.postgresql = postgresql;
    }

}

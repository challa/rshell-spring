
package org.cloudfoundry.services;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ports {

    @SerializedName("5432/tcp")
    @Expose
    private String _5432Tcp;

    public String get5432Tcp() {
        return _5432Tcp;
    }

    public void set5432Tcp(String _5432Tcp) {
        this._5432Tcp = _5432Tcp;
    }

}

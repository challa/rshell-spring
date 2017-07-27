package org.cloudfoundry.services;

import com.google.gson.Gson;

public class Jsonparser {
	
	
	public static void main(String[] s)
	{
		
		String vcap_data= "{\"postgresql\":[{ \"credentials\": { \"hostname\": \"10.11.241.0\", \"ports\": { \"5432/tcp\": \"54141\" }, \"port\": \"54141\", \"username\": \"hxBGkjN59-AEMAlY\", \"password\": \"r4zl2R7ZmkovyL5i\", \"dbname\": \"Y23P5OpUsnGVKcdf\", \"uri\": \"postgres://hxBGkjN59-AEMAlY:r4zl2R7ZmkovyL5i@10.11.241.0:54141/Y23P5OpUsnGVKcdf\" }, \"syslog_drain_url\": null, \"volume_mounts\": [ ], \"label\": \"postgresql\", \"provider\": null, \"plan\": \"v9.4-container\", \"name\": \"pgs_28\", \"tags\": [ \"postgresql\", \"relational\" ] }]}";
		
		Gson gson = new Gson();
		VcapParams params = gson.fromJson(vcap_data, VcapParams.class);
		Postgresql postgresData=  params.getPostgresql().get(0);
		String dbName = postgresData.getCredentials().getDbname();
		String user = postgresData.getCredentials().getUsername();
		String password = postgresData.getCredentials().getPassword();
		String host = postgresData.getCredentials().getHostname();
		String port = postgresData.getCredentials().getPort();
		System.out.println(dbName);
		System.out.println(user);
		System.out.println(password);
		System.out.println(host);
		System.out.println(port);
		
		
		
	}

}

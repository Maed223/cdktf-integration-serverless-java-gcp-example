package com.mycompany.app.posts;

import com.hashicorp.cdktf.Resource;
import com.hashicorp.cdktf.providers.google_beta.GoogleServiceNetworkingConnection;
import com.hashicorp.cdktf.providers.google_beta.GoogleSqlDatabase;
import com.hashicorp.cdktf.providers.google_beta.GoogleSqlDatabaseConfig;
import com.hashicorp.cdktf.providers.google_beta.GoogleSqlDatabaseInstance;
import com.hashicorp.cdktf.providers.google_beta.GoogleSqlDatabaseInstanceConfig;
import com.hashicorp.cdktf.providers.google_beta.GoogleSqlUser;
import com.hashicorp.cdktf.providers.google_beta.GoogleSqlUserConfig;
import com.hashicorp.cdktf.providers.google_beta.GoogleSqlDatabaseInstanceSettings;
import com.hashicorp.cdktf.providers.google_beta.GoogleSqlDatabaseInstanceSettingsIpConfiguration;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;


public class Storage extends Resource {

    private String dbHost;
    private String dbName;
    private String dbUserName;
    private String dbUserPassword;

    public Storage(Construct scope, String id, String environment, String user, String project, GoogleServiceNetworkingConnection privateVpcConnection, String vpcId){
        super(scope, id);

        GoogleSqlDatabaseInstance dbInstance = new GoogleSqlDatabaseInstance(this, "db-react-application-instance" + environment + "-" + user, GoogleSqlDatabaseInstanceConfig.builder()
                .name("db-react-application-instance" + environment + "-" + user)
                .project(project)
                .region("us-east1")
                .dependsOn(List.of(privateVpcConnection))
                .settings(GoogleSqlDatabaseInstanceSettings.builder()
                        .tier("db-f1-micro")
                        .availabilityType("REGIONAL")
                        .userLabels(new HashMap<>(){{
                            put("environment", environment);
                        }})
                        .ipConfiguration(GoogleSqlDatabaseInstanceSettingsIpConfiguration.builder()
                                .ipv4Enabled(false)
                                .privateNetwork(vpcId)
                                .build()
                        )
                        .build()
                )
                .databaseVersion("POSTGRES_13")
                .deletionProtection(false)
                .build()
        );

        GoogleSqlDatabase db = new GoogleSqlDatabase(this, "db-react-application-" + environment + "-" + user, GoogleSqlDatabaseConfig.builder()
                .name("db-react-application-" + environment + "-" + user)
                .project(project)
                .instance(dbInstance.getId())
                .build()
        );

        GoogleSqlUser dbUser = new GoogleSqlUser(this, "react-application-db-user-" + environment + "-" + user, GoogleSqlUserConfig.builder()
                .name("react-application-db-user-" + environment + "-" + user)
                .project(project)
                .instance(dbInstance.getId())
                .password("password")
                .build()
        );

        this.dbHost = dbInstance.getPrivateIpAddress()+":5432";
        this.dbName = db.getName();
        this.dbUserName = dbUser.getName();
        this.dbUserPassword = dbUser.getPassword();

    }

    public String getDbHost(){
        return this.dbHost;
    }

    public String getDbName(){
        return this.dbName;
    }

    public String getDbUserName(){
        return this.dbUserName;
    }

    public String getDbUserPassword(){
        return this.dbUserPassword;
    }

}

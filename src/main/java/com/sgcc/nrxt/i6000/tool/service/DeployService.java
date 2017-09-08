package com.sgcc.nrxt.i6000.tool.service;

import com.sgcc.nrxt.i6000.tool.util.HttpUtil;
import com.sgcc.nrxt.i6000.tool.util.OpenShiftTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class DeployService {

    Logger logger = Logger.getLogger(this.getClass().getName());

    @Value("${openshift.url}")
    String openshiftUrl;

    @Value("${openshift.username}")
    String openshiftUsername;

    @Value("${openshift.password}")
    String openshiftPassword;

    public String createService(String name, String project, String ports) {

        String serviceUrl = openshiftUrl + "/api/v1/namespaces/" + project + "/services";
        String body = "{\n" +
                "    \"kind\":\"Service\",\n" +
                "    \"apiVersion\":\"v1\",\n" +
                "    \"metadata\":{\n" +
                "        \"name\":\"" + name + "\",\n" +
                "        \"labels\":{\n" +
                "            \"app\":\"" + name + "\",\n" +
                "            \"deploymentconfig\":\""+ name +"\"\n" +
                "        },\n" +
                "        \"annotations\":{\n" +
                "            \"openshift.io/generated-by\":\"OpenShiftWebConsole\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"spec\":{\n" +
                "        \"selector\":{\n" +
                "            \"deploymentconfig\":\"" + name + "\"\n" +
                "        },\n" +
                "        \"ports\":" + ports + "\n" +
                "    }\n" +
                "}";

//        logger.info("create serviceUrl=" + serviceUrl);
//        logger.info("create service body=" + body);
        return this.httpPost(serviceUrl, body);
    }

    public String createDeployConfig(String name, String project, String imagename, String imagetag, String ports, String envs, String labels) {

        String urlDeployConfig = this.openshiftUrl + "/oapi/v1/namespaces/" + project + "/deploymentconfigs";
        String body = "{\n" +
                "\"kind\": \"DeploymentConfig\",\n" +
                "\"apiVersion\": \"v1\",\n" +
                "\"metadata\": {\n" +
                "\"name\": \"" + name + "\",\n" +
                "    \"labels\":{\n" +
                "    \"app\": \"" + name + "\"\n" +
                "\t},\n" +
                "    \"annotations\": {\"openshift.io/generated-by\": \"OpenShiftWebConsole\"}\n" +
                "},\n" +
                "\"spec\": {\n" +
                "\"strategy\": {\"resources\": {}},\n" +
                "\"triggers\": [{\"type\": \"ConfigChange\"}, {\n" +
                "\"type\": \"ImageChange\",\n" +
                "\t\"imageChangeParams\": {\n" +
                "    \"automatic\": true,\n" +
                "\t    \"containerNames\": [\"" + name + "\"],\n" +
                "    \"from\": {\"kind\": \"ImageStreamTag\", \"name\": \""+ imagetag +"\", \"namespace\": \""+ project +"\"}\n" +
                "}\n" +
                "}],\n" +
                "\"replicas\": 1,\n" +
                "    \"test\": false,\n" +
                "    \"selector\": {\"app\": \"" + name + "\", \"deploymentconfig\": \""+ name +"\"},\n" +
                "\"template\": {\n" +
                "\"metadata\": {\n" +
                "    \"labels\": {\n" +
                "\t    \"app\": \""+ name +"\",\n" +
                "\t    \"deploymentconfig\": \""+ name +"\"\n" +
                "\t\t},\n" +
                "\t    \"annotations\": {\"openshift.io/generated-by\": \"OpenShiftWebConsole\"}\n" +
                "},\n" +
                "\"spec\": {\n" +
                "    \"volumes\": [],\n" +
                "    \"containers\": [{\n" +
                "\t\"name\": \""+ name +"\",\n" +
                "\t\t\"image\": \"" + imagename + "\",\n" +
                "\t\t\"ports\": " + ports + ",\n" +
                "\t\t\"env\": " + envs + ",\n" +
                "\t\t\"volumeMounts\": []\n" +
                "    }],\n" +
                "    \"resources\": {}\n" +
                "}\n" +
                "}\n" +
                "},\n" +
                "\"status\": {}\n" +
                "} ";
//        logger.info("create deploy urlDeployConfig=" + urlDeployConfig);
//        logger.info("create deploy config body=" + body);
        return this.httpPost(urlDeployConfig, body);
    }

    public String createRouter(String name, String project) {

        String routeUrl = openshiftUrl + "/oapi/v1/namespaces/" + project + "/routes/";
        String body = "{\n" +
                "  \"kind\": \"Route\",\n" +
                "  \"apiVersion\": \"v1\",\n" +
                "  \"metadata\": {\n" +
                "    \"name\": \"" + name + "\",\n" +
                "    \"labels\": {\n" +
                "      \"docker-registry\": \"" + project + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"spec\": {\n" +
                "    \"to\": {\n" +
                "      \"kind\": \"Service\",\n" +
                "      \"name\": \"nginx2\"\n" +
                "    },\n" +
                "    \"port\": {\n" +
                "      \"targetPort\": \"80-tcp\"\n" +
                "    },\n" +
                "    \"alternateBackends\": []\n" +
                "  }\n" +
                "}";

//        logger.info("create routeUrl=" + routeUrl);
//        logger.info("create service body=" + body);
        return this.httpPost(routeUrl, body);
    }

    public String httpPost(String url, String param) {

        logger.info("httpPost url=" + url);
        logger.info("httpPost param=" + param.replace("\\n", "").trim());
        String token = "Bearer " + this.getToken();
        String result = null;

        try{
            result = HttpUtil.getInstance().doPost(url, param, token);
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("httpPost result=" + result);
        return result;
    }

    /*{
        "username": "admin",
        "password": "123456"
    }*/
    public String getToken() {

        String url = this.openshiftUrl + "/oauth/authorize?response_type=token&client_id=openshift-challenging-client";
        logger.info("getToken url=" + url);

        String token = OpenShiftTokenUtil.getOpenShiftToken(openshiftUsername, openshiftPassword);
        logger.info("getToken token=" + token);
        return token;
    }

}
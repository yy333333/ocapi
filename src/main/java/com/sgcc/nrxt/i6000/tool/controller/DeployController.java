package com.sgcc.nrxt.i6000.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sgcc.nrxt.i6000.tool.service.DeployService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/deploy")
public class DeployController {
    Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    DeployService deployService;

    /*@GetMapping("/{project}")
    public List list(@PathVariable String project) {
        return deployService.Retrieve(project);
    }*/

    @GetMapping("/tokens")
    public String getToken() {
        return deployService.getToken();
    }

    @PostMapping
    @ResponseBody
    public ObjectNode deploy(@RequestBody String json){
        logger.info("deploy json=" + json);
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        try {
            ObjectNode objectNode = new ObjectMapper().readValue(json, ObjectNode.class);

            String project = objectNode.get("project").asText();
            String name = objectNode.get("name").asText();
            String imagename = objectNode.get("imagename").asText();
            String imagetag = objectNode.get("imagetag").asText();

            String labels = objectNode.path("labels").asText();
            ArrayNode envArr = objectNode.withArray("envs");
            ArrayNode dcPortArr = objectNode.withArray("dcports");
            ArrayNode servicePortArr = objectNode.withArray("serviceports");

            logger.info("deploy labels=" + labels);
            logger.info("deploy envArr=" + envArr);
            logger.info("deploy dcPortArr=" + dcPortArr);
            logger.info("deploy servicePortArr=" + servicePortArr);

            String deployConfig = this.deployService.createDeployConfig(name, project, imagename, imagetag, dcPortArr.toString(), envArr.toString(), labels);
            String service = this.deployService.createService(name, project, servicePortArr.toString());
            String route = this.deployService.createRouter(name, project);

            object.put("deployConfig", deployConfig);
            object.put("service", service);
            object.put("route", route);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    /*@DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id){
        deployService.Delete(Long.valueOf(id));
    }*/
}

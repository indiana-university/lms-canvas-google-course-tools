package edu.iu.uits.lms.gct.controller.rest;

import edu.iu.uits.lms.gct.model.GctProperty;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by chmaurer on 5/15/20.
 */
@RestController
@RequestMapping("/rest/properties")
@Slf4j
@Api(tags = "properties")
public class PropertiesRestController {

    @Autowired
    private GctPropertyRepository gctPropertyRepository = null;

    @GetMapping("/all")
    public Iterable<GctProperty> getAll() {
        return gctPropertyRepository.findAll();
    }

    @GetMapping("/{env}/{key}")
    public GctProperty getProperty(@PathVariable String env, @PathVariable String key) {
        return gctPropertyRepository.findByKeyAndEnv(key, env);
    }

    @PutMapping("/{env}/{key}")
    public GctProperty updateProperty(@PathVariable String env, @PathVariable String key, @RequestBody GctProperty gctProperty) {
        GctProperty prop = gctPropertyRepository.findByKeyAndEnv(key, env);

        if (gctProperty.getValue() != null) {
            prop.setValue(gctProperty.getValue());
        }

        if (gctProperty.getEnv() != null) {
            prop.setEnv(gctProperty.getEnv());
        }
        return gctPropertyRepository.save(prop);
    }

    @PostMapping("/")
    public GctProperty createProperty(@RequestBody GctProperty gctProperty) {
        GctProperty newProp = new GctProperty(gctProperty.getKey(), gctProperty.getValue(), gctProperty.getEnv());
        return gctPropertyRepository.save(newProp);
    }

    @DeleteMapping("/{env}/{key}")
    public String deleteProperty(@PathVariable String env, @PathVariable String key) {
        GctProperty prop = gctPropertyRepository.findByKeyAndEnv(key, env);
        gctPropertyRepository.delete(prop);
        return "Delete success.";
    }

    @DeleteMapping("/{id}")
    public String deletePropertyById(@PathVariable Long id) {
        gctPropertyRepository.deleteById(id);
        return "Delete success.";
    }
}
